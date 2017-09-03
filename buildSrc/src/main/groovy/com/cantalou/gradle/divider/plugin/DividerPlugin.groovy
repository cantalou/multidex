package com.cantalou.gradle.divider.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.transforms.JarMergingTransform
import com.android.build.gradle.internal.transforms.MultiDexTransform
import com.cantalou.gradle.divider.configuration.Config
import com.cantalou.gradle.divider.extension.DividerExtension
import com.cantalou.gradle.divider.transforms.DividerJarMergingTransform
import com.cantalou.gradle.divider.transforms.DividerProGuardTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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
 * @date 2017-09-02 18:43
 */

public class DividerPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        project.extensions.create("dividerConfig", DividerExtension)

        if (!project.plugins.hasPlugin(AppPlugin)) {
            project.println "Not a android project ignore"
            return
        }

        if (!project.android.defaultConfig.multiDexEnabled) {
            project.println "multiDexEnabled disable ignore"
            return
        }

        Config config = new Config(project)

        project.afterEvaluate {
            project.android.applicationVariants.each { BaseVariant variant ->

                // debug combined.jar, JarMergingTransform
                def variantName = variant.name.capitalize()
                Task jarMergeTask = appProject.tasks.findByName("transformClassesWithJarMergingFor${variantName}")
                DividerJarMergingTransform dividerJarMergingTransform = new DividerJarMergingTransform(project, jarMergeTask.transform)
                set(jarMergeTask, "transform", dividerJarMergingTransform)

                // release combined.jar, mapping.txt, ProGuardTransform
                Task proguardTask = appProject.tasks.findByName("transformClassesAndResourcesWithProguardFor${variantName}")
                DividerProGuardTransform dividerProGuardTransform = new DividerProGuardTransform(project, proguardTask.transform)
                set(jarMergeTask, "transform", dividerProGuardTransform)

                // MultiDexTransform 生成 maindexlist.txt
                Task transformMultiDexListTask = appProject.tasks.findByName("transformClassesWithMultidexlistFor${variantName}")
                transformMultiDexListTask.doLast {
                }

                // DexTransform
                Task transformDexTask = appProject.tasks.findByName("transformClassesWithDexFor${variantName}")
            }
        }
    }

    private void set(Object instance, def fieldName, def value) {
        def field = instance.getClass().getField(fieldName)
        field.setAccessible(true)
        field.set(instance, value)
    }
}