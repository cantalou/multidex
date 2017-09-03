package com.cantalou.gradle.divider.configuration

import com.cantalou.gradle.divider.extension.DividerExtension
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

    /**
     * divider-plugin.properties
     */
    private static final String DEFAULT_CONFIG_FILE = "divider-plugin.properties";

    public int totalMethodCount;

    public int dexMethodCount;

    public int dexCount;

    private Project project;

    private Properties prop;

    public Config(Project project) {
        this.project = project
        parse()
    }

    public void parse() {

        def configFileName
        DividerExtension ext = project.extensions.dividerConfig
        if (ext == null) {
            configFileName = Config.DEFAULT_CONFIG_FILE
        } else {
            configFileName = ext.configFile
        }

        prop = new Properties();
        def configFile = new File(project.getRootDir(), configFileName)
        configFile.withInputStream("UTF-8") {
            prop.load(it)
        }

        totalMethodCount = prop.getProperty("totalMethodCount") as int
        dexMethodCount = prop.getProperty("dexMethodCount") as int
        dexCount = prop.getProperty("dexCount") as int
    }

    public void save() {
        prop.setProperty("totalMethodCount", totalMethodCount)
        prop.setProperty("dexMethodCount", dexMethodCount)
        prop.setProperty("dexCount", dexCount)
        new File(project.getRootDir(), configFileName).withOutputStream {
            prop.store(it, "UTF-8")
        }
    }
}