package com.cantalou.gradle.divider.transforms

import com.android.SdkConstants
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.incremental.InstantRunBuildContext;
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMerger;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.DexOptions;
import com.android.builder.internal.utils.FileCache
import com.android.builder.packaging.ZipEntryFilter
import com.android.utils.FileUtils
import com.cantalou.gradle.divider.configuration.Config
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream

import static com.android.utils.FileUtils.deleteIfExists;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author cantalou
 * @date 2017-09-02 22:30
 */

public class DividerDexTransform extends DexTransform {

    private Project project

    public DividerDexTransform(Project project, DexTransform dexTransform) {
        super(dexTransform.dexOptions, dexTransform.debugMode, dexTransform.multiDex, dexTransform.mainDexListFile, dexTransform.intermediateFolder,
                dexTransform.androidBuilder, dexTransform.logger, dexTransform.instantRunBuildContext, dexTransform.buildCache);
        this.project = project
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {

        def mainDexClass = new HashSet()
        mainDexListFile.getText("UTF-8").eachLine {
            mainDexClass << it
        }

        //now only one jar combined.jar
        File combinedJar
        for (TransformInput input : invocation.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                combinedJar = jarInput.getFile()
            }
        }
        //rename combined.jar to combined-bak.jar
        File combinedBakJar = combinedJar.renameTo(new File(combinedJar.absolutePath.replace(combinedJar.name, "bak-" + combinedJar.name)))

        ExecutorService service = Executors.newFixedThreadPool(2)
        service.submit(new Runnable() {
            @Override
            void run() {
                createMainDexCombineJar(combinedBakJar, mainDexClass)
            }
        })
        service.submit(new Runnable() {
            @Override
            void run() {
                createSecondDexCombineJar(combinedBakJar, mainDexClass)
            }
        })
        service.shutdown()
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)

        super.transform(transformInvocation);

        Config config = new Config(project)
    }

    private void createMainDexCombineJar(File combinedBakJar, HashSet<String> mainDexClass) {
        File combinedJar = new File(combinedBakJar.getParentFile(), "combined.jar")
        deleteIfExists(combinedJar);
        ZipEntryFilter filter = new ZipEntryFilter() {
            @Override
            public boolean checkEntry(String archivePath) {
                return archivePath.endsWith(SdkConstants.DOT_CLASS) && mainDexClass.contains(archivePath)
            }
        }
        merge(combinedBakJar, combinedJar, filter)
    }

    private void createSecondDexCombineJar(File combinedBakJar, HashSet<String> mainDexClass) {

        File secondaryJar = new File(combinedBakJar.getParentFile(), "secondary.jar")
        deleteIfExists(secondaryJar);

        ZipEntryFilter filter = new ZipEntryFilter() {
            @Override
            public boolean checkEntry(String archivePath) {
                return archivePath.endsWith(SdkConstants.DOT_CLASS) && !mainDexClass.contains(archivePath)
            }
        }
        merge(combinedJar, secondaryJar, filter)

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
