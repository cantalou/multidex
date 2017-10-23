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
import com.cantalou.gradle.divider.extension.DividerConfigExtension
import com.cantalou.gradle.divider.util.DexHelper
import org.gradle.api.Project

import static com.android.utils.FileUtils.deleteIfExists

/**
 *
 */
public class DividerDexTransform2 extends DexTransform {

    Project project

    DexTransform dexTransform

    File dividerBuildDir

    public DividerDexTransform2(Project project, DexTransform dexTransform) {
        super(dexTransform.dexOptions, dexTransform.debugMode, dexTransform.multiDex, dexTransform.mainDexListFile, dexTransform.intermediateFolder,
                dexTransform.androidBuilder, dexTransform.logger.logger, dexTransform.instantRunBuildContext, dexTransform.buildCache);
        this.project = project
        this.dexTransform = dexTransform

        dividerBuildDir = new File("${project.buildDir}/intermediates/divider")
        dividerBuildDir.mkdirs()
        DexHelper.initProject(project)
    }

    @Override
    public void transform(final TransformInvocation invocation) throws TransformException, IOException, InterruptedException {

        super.transform(invocation)

        def mainDexClass = new HashSet()
        def mainOutputDir = invocation.getOutputProvider().getContentLocation("main", getOutputTypes(), getScopes(), Format.DIRECTORY);

        new File(dividerBuildDir, "mainDexClass.txt").withWriter("UTF-8") { Writer writer ->
            DexHelper.loadDex(new File(mainOutputDir, "classes.dex")).getAllClassName().each {
                mainDexClass << it
                writer.println it
            }
        }

        //now only one file named combined.jar
        File combinedJar
        for (TransformInput input : invocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                combinedJar = jarInput.getFile()
            }
        }

        def secondaryJar = createSecondaryCombineJar(combinedJar, mainDexClass, dividerBuildDir)

        try {
            def secondaryOutputDir = new File("${dividerBuildDir}/dex");
            secondaryOutputDir.mkdirs()
            secondaryOutputDir.eachFile { it.delete() }

            DexOptions tempDexOptions = DefaultDexOptions.copyOf(dexTransform.dexOptions)
            def additionalParameters = new ArrayList<>(tempDexOptions.additionalParameters)
            additionalParameters.remove("--minimal-main-dex")

            //Add set-max-idx-number
            DividerConfigExtension extension = project.getExtensions().findByName("dividerConfig")
            def totalMethodCount = DexHelper.countMethod(mainOutputDir) {
                it.name != "classes.dex"
            }
            project.println "Total method count ${totalMethodCount} exclude main dex"

            def perDexMethodCount = DexHelper.evaluateDexMethodCount(totalMethodCount, extension)
            if (perDexMethodCount > 0) {
                additionalParameters.removeAll { it.contains("set-max-idx-number") }
                additionalParameters.add("--set-max-idx-number=" + perDexMethodCount)
                println "Add DexOptions set-max-idx-number value ${perDexMethodCount} for spliting secondary dex"
            }
            tempDexOptions.additionalParameters = additionalParameters

            def ab = dexTransform.androidBuilder
            ProcessOutputHandler outputHandler = new ParsingProcessOutputHandler(new ToolOutputParser(new DexParser(), Message.Kind.ERROR, dexTransform.logger),
                    new ToolOutputParser(new DexParser(), dexTransform.logger), ab.getErrorReporter());
            def dexByteCodeConverter = new DexByteCodeConverter(ab.getLogger(), ab.mTargetInfo, ab.mJavaProcessExecutor, ab.mVerboseExec);
            dexByteCodeConverter.convertByteCode([secondaryJar], secondaryOutputDir, dexTransform.multiDex, null, tempDexOptions, dexTransform.getOptimize(), outputHandler);

            int classesIndex = secondaryOutputDir.listFiles().length + 1
            new File(secondaryOutputDir, "classes.dex").renameTo(new File(secondaryOutputDir, "classes" + classesIndex + ".dex"))

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

    /**
     * Create second combined.jar file by removing class in mainDexListFile
     *
     * @param combinedJar
     * @param mainDexClass line class info in mainDexListFile
     */
    private File createSecondaryCombineJar(File combinedJar, HashSet<String> mainDexClass, File outputDir) {

        File secondaryJar = new File(outputDir, "combined-secondary.jar")
        deleteIfExists(secondaryJar);

        ZipEntryFilter filter = new ZipEntryFilter() {
            @Override
            public boolean checkEntry(String archivePath) {
                project.println "${archivePath}, ${!mainDexClass.contains(archivePath)}"
                return archivePath.endsWith(SdkConstants.DOT_CLASS) && !mainDexClass.contains(archivePath)
            }
        }
        merge(combinedJar, secondaryJar, filter)
        println "CreateSecondDexCombineJar success ${secondaryJar}"
        return secondaryJar
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
