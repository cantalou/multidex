package com.cantalou.gradle.divider.util

import com.cantalou.gradle.divider.configuration.Config
import com.cantalou.gradle.divider.dexcount.dexdeps.DexData
import com.cantalou.gradle.divider.extension.DividerConfigExtension
import org.gradle.api.Project

/**
 * Calculate the total method count of dex
 */
public class CountMethodUtil {

    public static void process(Project project, String dirName) {

        DividerConfigExtension extension = project.getExtensions().findByName("dividerConfig")

        Config config = Config.getInstance(project)
        config.dexCount = extension.forceDexCount
        if (extension.forceMethodCount > 0) {
            config.dexMethodCount = extension.forceMethodCount
        } else {
            int totalMethodCount = countMethod(project, dirName)
            if (totalMethodCount == 0) {
                config.dexMethodCount = extension.desiredDexMethodCount
            } else if (extension.forceDexCount > 0) {
                config.dexMethodCount = totalMethodCount / extension.forceDexCount + extension.dexMethodCountPadding
            } else {
                int dexCount = 2
                while ((config.dexMethodCount = totalMethodCount / dexCount + extension.dexMethodCountPadding) > extension.desiredDexMethodCount) {
                    dexCount++
                    if (dexCount > extension.desiredMaxDexCount) {
                        config.dexMethodCount = extension.maxDexMethodCount
                        break
                    }
                }
            }
            config.totalMethodCount = totalMethodCount
        }
        config.save()
    }

    private static countMethod(Project project, String dirName) {
        int totalMethodCount = 0
        try {
            File dexOutputDir = new File("${project.buildDir}/intermediates/transforms/dex/${dirName}/folders/1000/1f/main")
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
        }
        totalMethodCount
    }

}
