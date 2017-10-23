package com.cantalou.gradle.divider

import com.android.build.gradle.internal.pipeline.TransformTask
import com.cantalou.gradle.divider.extension.DividerConfigExtension
import com.cantalou.gradle.divider.transforms.DividerDexTransform2
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Plugin used to hack "transformClassesWithDexFor" task and add a task to collect method info
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

                DividerDexTransform2 dividerDexTransform = new DividerDexTransform2(project, transformDexTask.transform)
                def field = TransformTask.class.getDeclaredField("transform")
                field.setAccessible(true)
                field.set(transformDexTask, dividerDexTransform)
                project.println "Change transformClassesWithDexFor${variantName}'s transform to DividerDexTransform2"

//                Config config = Config.getInstance(project)
//                if(config.dexMethodCount > 0){
//                    DividerDexTransform2 dividerDexTransform = new DividerDexTransform2(project, transformDexTask.transform)
//                    def field = TransformTask.class.getDeclaredField("transform")
//                    field.setAccessible(true)
//                    field.set(transformDexTask, dividerDexTransform)
//                    project.println "Change transformClassesWithDexFor${variantName}'s transform to DividerDexTransform"
//                }
//
//                String taskName = "countMethodFor${variantName}"
//                Task countTask = project.tasks.create(taskName) {
//                    doLast {
//                        DexHelper.process(project, variant.dirName)
//                    }
//                }
//                countTask.dependsOn transformDexTask
//                project.tasks.findByName("assemble${variantName}").dependsOn countTask.getPath()
            }
        }
    }

}