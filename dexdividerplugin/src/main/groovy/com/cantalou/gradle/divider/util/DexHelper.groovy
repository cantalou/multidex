package com.cantalou.gradle.divider.util

import com.cantalou.gradle.divider.configuration.Config
import com.cantalou.gradle.divider.dexcount.dexdeps.DexData
import com.cantalou.gradle.divider.extension.DividerConfigExtension
import org.gradle.api.Project

/**
 * Calculate the total method count of dex
 */
public class DexHelper {

    public static Project project;

    static void initProject(Project pro) {
        project = pro
    }

    public static void process(String dirName) {

        DividerConfigExtension extension = project.getExtensions().findByName("dividerConfig")
        File dexOutputDir = new File("${project.buildDir}/intermediates/transforms/dex/${dirName}/folders/1000/1f/main")

        Config config = Config.getInstance(project)
        config.dexCount = extension.forceDexCount
        config.totalMethodCount = countMethod(dexOutputDir)
        config.dexMethodCount = evaluateDexMethodCount(project, config.totalMethodCount, extension)
        config.save()
    }

    public static int evaluateDexMethodCount(int totalMethodCount, DividerConfigExtension extension) {
        if (extension.forceMethodCount > 0) {
            return extension.forceMethodCount
        } else if (totalMethodCount == 0) {
            return extension.desiredDexMethodCount
        } else if (extension.forceDexCount > 0) {
            return totalMethodCount / extension.forceDexCount + extension.dexMethodCountPadding
        } else {
            int dexCount = 2
            int dexMethodCount
            while ((dexMethodCount = totalMethodCount / dexCount + extension.dexMethodCountPadding) > extension.desiredDexMethodCount) {
                project.println "dexCount ${dexCount}, dexMethodCount ${dexMethodCount} > extension.desiredDexMethodCount ${extension.desiredDexMethodCount}"
                dexCount++
                if (dexCount > extension.desiredMaxDexCount) {
                    dexMethodCount = 0
                    break
                }
            }
            return dexMethodCount
        }
    }

    public static countMethod(File file, Closure closure) {
        int totalMethodCount = 0
        try {
            if (!file.exists()) {
                project.println "Build dex dir ${file} not exists"
                return totalMethodCount
            }

            if (file.isDirectory()) {
                file.eachFile {
                    def dexMethodCount = countMethod(it, closure)
                    project.println "Dex file ${it.name} method count ${dexMethodCount}"
                    totalMethodCount += dexMethodCount
                }
            } else {
                if (closure == null || closure.call(file)) {
                    DexData dexData = loadDex(file)
                    totalMethodCount = dexData.getMethodRefs().length
                }
            }

        } catch (Exception e) {
            totalMethodCount = 0
            project.println "count dex method error , ${e}"
        }
        totalMethodCount
    }

    public static DexData loadDex(File file) {
        def dexFile
        try {
            dexFile = new RandomAccessFile(file, "r")
            DexData dexData = new DexData(dexFile);
            dexData.load();
            return dexData
        } finally {
            dexFile.close();
        }
    }

}
