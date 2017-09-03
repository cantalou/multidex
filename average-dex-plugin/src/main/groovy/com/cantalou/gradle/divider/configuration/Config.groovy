import org.gradle.api.Project
import org.gradle.api.internal.tasks.PublicTaskSpecification

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
public class Config {

    private static final String DEFAULT_CONFIG_FILE = "divider-plugin.properties";

    public int totalMethodCount;

    public int dexMethodCount;

    public int dexCount;

    private Project project;

    public Config(Project project) {
        this.project = project
    }

    public void parse(){

        def configFile = new File(project.getRootDir(), DEFAULT_CONFIG_FILE)
        def averageDexMethodCount = 0
        if(configFile.exists()){
            def content = configFile.getText("UTF-8")
            if(content){
                averageDexMethodCount = content as int
            }
        }
    }

    public void save(){

    }
}