package com.cantalou.gradle.divider

import com.android.build.gradle.internal.pipeline.TransformTask
import com.cantalou.gradle.divider.extension.DividerConfigExtension
import com.cantalou.gradle.divider.tasks.CountMethodTask
import com.cantalou.gradle.divider.transforms.DividerDexTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * 根据上次构建的结果, 平均分配除mainDex以外每个dex文件里面的方法数
 */
public class DividerPlugin implements Plugin<Project> {

    Project project

    DividerConfigExtension extension

    @Override
    void apply(Project pro) {

        project = pro
        project.ext {
            handle = this.&handle
            countMethod = this.&countMethod
        }

        project.extensions.create("dividerConfig", DividerConfigExtension)

        project.afterEvaluate {

            if (!project.android.defaultConfig.multiDexEnabled) {
                project.println "defaultConfig multiDexEnabled is false"
                return
            }

            extension = project.getExtensions().findByName("dividerConfig")
            if (extension == null) {
                project.println "Config extension is not found"
                return
            }

            if (!extension.enable) {
                project.println "Divider plugin disable"
                return
            }

            project.android.applicationVariants.each { def variant ->

                // DexTransform
                def variantName = variant.name.capitalize()

                Task transformDexTask = project.tasks.findByName("transformClassesWithDexFor${variantName}")

                DividerDexTransform dividerDexTransform = new DividerDexTransform(project, transformDexTask.transform)
                def field = TransformTask.class.getDeclaredField("transform")
                field.setAccessible(true)
                field.set(transformDexTask, dividerDexTransform)

                CountMethodTask countMethodTask = project.tasks.create("countMethodFor${variantName}", CountMethodTask)
                countMethodTask.setVariant(variant)
                countMethodTask.dependsOn transformDexTask.getPath()

                project.tasks.findByName("assemble${variantName}").dependsOn countMethodTask.getPath()

            }
        }
    }

}