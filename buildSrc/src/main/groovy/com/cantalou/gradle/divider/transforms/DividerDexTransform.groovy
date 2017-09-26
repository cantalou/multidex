package com.cantalou.gradle.divider.transforms

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMerger
import com.android.builder.core.DefaultDexOptions
import com.android.builder.core.DexByteCodeConverter
import com.android.builder.core.DexOptions
import com.android.builder.packaging.ZipEntryFilter
import com.android.ide.common.blame.Message
import com.android.ide.common.blame.ParsingProcessOutputHandler
import com.android.ide.common.blame.parser.DexParser
import com.android.ide.common.blame.parser.ToolOutputParser
import com.android.ide.common.process.ProcessOutputHandler
import com.cantalou.gradle.divider.configuration.Config
import org.gradle.api.Project

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import static com.android.utils.FileUtils.deleteIfExists

/**
 *
 */
public class DividerDexTransform extends DexTransform {

    Project project

    DexTransform dexTransform

    File dividerBuildDir

    Config config

    public DividerDexTransform(Project project, DexTransform dexTransform) {
        super(dexTransform.dexOptions, dexTransform.debugMode, dexTransform.multiDex, dexTransform.mainDexListFile, dexTransform.intermediateFolder,
                dexTransform.androidBuilder, dexTransform.logger.logger, dexTransform.instantRunBuildContext, dexTransform.buildCache);
        this.project = project
        this.dexTransform = dexTransform

        dividerBuildDir = new File("${project.buildDir}/intermediates/divider")
        dividerBuildDir.mkdirs()
    }

    @Override
    public void transform(final TransformInvocation invocation) throws TransformException, IOException, InterruptedException {

        def mainDexClass = new HashSet()
        println dexTransform.mainDexListFile
        dexTransform.mainDexListFile.getText("UTF-8").eachLine {
            mainDexClass << it
        }

        //now only one file combined.jar
        File combinedJar
        for (TransformInput input : invocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                combinedJar = jarInput.getFile()
            }
        }

        //secondary jar dir
        File secondaryJarDir = new File("${dividerBuildDir}/jar")
        secondaryJarDir.mkdirs()
        secondaryJarDir.eachFile { it.delete() }
        project.copy {
            from combinedJar
            into secondaryJarDir
        }

        ExecutorService service = Executors.newCachedThreadPool()
        //main dex
        service.submit(new Runnable() {
            @Override
            void run() {
                createMainDexCombineJar(combinedJar, mainDexClass)
                def additionalParameters = dexTransform.dexOptions.additionalParameters
                if (!additionalParameters.contains("--minimal-main-dex")) {
                    additionalParameters.add("--minimal-main-dex")
                }
                dexTransform.transform(invocation);
            }
        })

        //secondary dex
        service.submit(new Runnable() {
            @Override
            void run() {
                try {
                    File secondaryCombinedJar = new File(secondaryJarDir, combinedJar.name)
                    createSecondDexCombineJar(secondaryCombinedJar, mainDexClass)
                    transformSecondary(invocation, secondaryCombinedJar)
                } catch (Exception e) {
                    println e.printStackTrace()
                }
            }
        })
        service.shutdown()
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    public void transformSecondary(TransformInvocation transformInvocation, File secondaryJar) throws TransformException, IOException, InterruptedException {

        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        def androidBuilder = dexTransform.androidBuilder
        ProcessOutputHandler outputHandler = new ParsingProcessOutputHandler(
                new ToolOutputParser(new DexParser(), Message.Kind.ERROR, dexTransform.logger),
                new ToolOutputParser(new DexParser(), dexTransform.logger),
                androidBuilder.getErrorReporter());

        try {
            def secondaryOutputDir = new File("${dividerBuildDir}/dex");
            secondaryOutputDir.mkdirs()
            secondaryOutputDir.eachFile { it.delete() }

            DexOptions tempDexOptions = DefaultDexOptions.copyOf(dexTransform.dexOptions)
            def additionalParameters = new ArrayList<>(tempDexOptions.additionalParameters)
            additionalParameters.remove("--minimal-main-dex")

            config = Config.getInstance(project)
            if (config.dexMethodCount > 0) {
                additionalParameters.removeAll { it.contains("set-max-idx-number") }
                additionalParameters.add("--set-max-idx-number=" + config.dexMethodCount)
                println "Add DexOptions set-max-idx-number value ${config.dexMethodCount}"
            }
            tempDexOptions.additionalParameters = additionalParameters

            def dexByteCodeConverter = new DexByteCodeConverter(androidBuilder.getLogger(), androidBuilder.mTargetInfo, androidBuilder.mJavaProcessExecutor, androidBuilder.mVerboseExec);
            dexByteCodeConverter.convertByteCode([secondaryJar], secondaryOutputDir, dexTransform.multiDex, null, tempDexOptions, dexTransform.getOptimize(), outputHandler);

            int classesIndex = secondaryOutputDir.listFiles().length + 1
            new File(secondaryOutputDir, "classes.dex").renameTo(new File(secondaryOutputDir, "classes" + classesIndex + ".dex"))

            def mainOutputDir = outputProvider.getContentLocation("main", dexTransform.getOutputTypes(), dexTransform.getScopes(), Format.DIRECTORY);
            secondaryOutputDir.eachFile { def secondaryDex ->
                project.copy {
                    from secondaryDex
                    into mainOutputDir
                }
            }

        } catch (Exception e) {
            project.println e
            throw new TransformException(e);
        }
    }

    private void createMainDexCombineJar(File combinedJar, HashSet<String> mainDexClass) {
        File combinedTmpJar = new File(combinedJar.getParentFile(), "combined-tmp.jar")
        deleteIfExists(combinedTmpJar);
        ZipEntryFilter filter = new ZipEntryFilter() {
            @Override
            public boolean checkEntry(String archivePath) {
                return archivePath.endsWith(SdkConstants.DOT_CLASS) && mainDexClass.contains(archivePath)
            }
        }
        merge(combinedJar, combinedTmpJar, filter)
        combinedJar.delete()
        combinedTmpJar.renameTo(combinedJar)
        println "CreateMainDexCombineJar success ${combinedJar}"
    }

    /**
     * Create second combined.jar file by removing class in mainDexListFile
     *
     * @param combinedJar
     * @param mainDexClass line class info in mainDexListFile
     */
    private void createSecondDexCombineJar(File combinedJar, HashSet<String> mainDexClass) {

        File secondaryJar = new File(combinedJar.getParentFile(), "combined-tmp.jar")
        deleteIfExists(secondaryJar);

        ZipEntryFilter filter = new ZipEntryFilter() {
            @Override
            public boolean checkEntry(String archivePath) {
                return archivePath.endsWith(SdkConstants.DOT_CLASS) && !mainDexClass.contains(archivePath)
            }
        }
        merge(combinedJar, secondaryJar, filter)
        combinedJar.delete()
        secondaryJar.renameTo(combinedJar)
        println "CreateSecondDexCombineJar success ${combinedJar}"
    }

    private void merge(File inputJar, File outputJar, ZipEntryFilter filter) {
        JarMerger jarMerger = new JarMerger(outputJar);
        try {
            jarMerger.setFilter(filter);
            jarMerger.addJar(inputJar);
        } catch (FileNotFoundException e) {
            throw new TransformException(e);
        } catch (IOException e) {
            throw new TransformException(e);
        } finally {
            jarMerger.close();
        }
    }
}
