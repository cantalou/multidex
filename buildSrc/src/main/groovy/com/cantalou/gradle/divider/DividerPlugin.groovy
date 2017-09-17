package com.m4399.gradle.divider

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.m4399.gradle.divider.configuration.Config
import com.m4399.gradle.divider.dexcount.dexdeps.DexData
import com.m4399.gradle.divider.extension.DividerConfigExtension
import com.m4399.gradle.divider.transforms.DividerDexTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * 根据上次构建的结果, 平均分配除mainDex以外每个dex文件里面的方法数
 */
public class DividerPlugin implements Plugin<Project> {

    Project project

    @Override
    void apply(Project project) {
        this.project = project
        project.extensions.create("dividerConfig", DividerConfigExtension)

        if (!project.plugins.hasPlugin(AppPlugin)) {
            project.println "Not a android project ignore"
            return
        }

        project.afterEvaluate {

            if(!project.android.defaultConfig.multiDexEnabled){
                project.println "defaultConfig multiDexEnabled is false"
                return
            }

            DividerConfigExtension extension = project.getExtensions().findByName("dividerConfig")
            if (extension == null) {
                project.println "Config extension is not found"
                return
            }

            if (!extension.enable) {
                project.println "Divider plugin disable"
                return
            }

            handle(project, extension)
        }
    }

    public void handle(Project project, DividerConfigExtension extension) {

        project.android.applicationVariants.each { BaseVariant variant ->
            // DexTransform
            def variantName = variant.name.capitalize()

            Task transformDexTask = project.tasks.findByName("transformClassesWithDexFor${variantName}")

            DividerDexTransform dividerDexTransform = new DividerDexTransform(project, transformDexTask.transform)
            def field = TransformTask.class.getDeclaredField("transform")
            field.setAccessible(true)
            //field.set(transformDexTask, dividerDexTransform)

            Thread.start {
                Config config = new Config(project)
                config.dexCount = extension.forceDexCount
                if (extension.forceMethodCount > 0) {
                    config.dexMethodCount = extension.forceMethodCount
                } else {
                    int totalMethodCount = countMethod(variant)
                    if (totalMethodCount == 0) {
                        config.dexMethodCount = extension.dexMethodCount
                    } else if (extension.forceDexCount > 0) {
                        config.dexMethodCount = totalMethodCount / extension.forceDexCount + extension.dexMethodCountPadding
                    } else if (totalMethodCount > extension.dexMethodCount) {
                        int dexCount = 2
                        while ((config.dexMethodCount = totalMethodCount / dexCount + extension.dexMethodCountPadding) > extension.dexMethodCount) {
                            dexCount++
                        }
                    }
                    config.totalMethodCount = totalMethodCount
                }
                config.save()
            }
        }
    }

    private countMethod(BaseVariant variant) {
        int totalMethodCount = 0
        try {
            File dexOutputDir = new File("${project.buildDir}/intermediates/transforms/dex/${variant.dirName}/folders/1000/1f/main")
            if (!dexOutputDir.exists()) {
                project.println "Build dex dir ${dexOutputDir} not exists"
                return totalMethodCount
            }

            dexOutputDir.eachFile {
                if (it.name != "classes.dex") {
                    def dexFile
                    try {
                        dexFile = new RandomAccessFile(it, "r")
                        DexData dexData = new DexData(dexFile);
                        dexData.load();
                        totalMethodCount += dexData.getMethodRefs().length
                    } finally {
                        dexFile.close();
                    }
                }
            }
        } catch (Exception e) {
            totalMethodCount = 0
            project.println "count dex method error , ${e}"
            e.printStackTrace()
        }
        totalMethodCount
    }

}