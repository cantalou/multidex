import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.nio.charset.Charset

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

        if(!project.plugins.hasPlugin(AppPlugin)){
            project.println "Not a android project ignore"
            return
        }


        project.afterEvaluate {
            project.android.applicationVariants.each{ BaseVariant variant ->
                // debug combined.jar, JarMergingTransform
                def variantName = variant.name.capitalize()
                Task jarMergeTask = appProject.tasks.findByName("transformClassesWithJarMergingFor${ variantName}")

                // Task CreateManifestKeepList 生成 manifest_keep.txt 文件
                Task collectMultiDexComponentsTask = appProject.tasks.findByName("collect${variantName}MultiDexComponents")

                // MultiDexTransform 生成 maindexlist.txt
                Task transformMultiDexListTask = appProject.tasks.findByName("transformClassesWithMultidexlistFor${variantName}")

                // mapping.txt
                Task proguardTask = appProject.tasks.findByName("transformClassesAndResourcesWithProguardFor${variantName}")

                // DexTransform
                Task transformDexTask = appProject.tasks.findByName("transformClassesWithDexFor${variantName}")
            }
        }

    }
}