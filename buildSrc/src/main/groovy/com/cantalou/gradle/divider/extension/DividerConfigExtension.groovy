package com.cantalou.gradle.divider.extension

/**
 * Config info for Divider plugin
 */
public class DividerConfigExtension {

    /**
     * Path of config file.
     * Auto add project dir prefix if it was not full path
     */
    String configFile = "divider-config.properties"

    boolean enable = true

    /**
     * Force method count , ignore other attribute
     */
    int forceMethodCount = 0

    /**
     * Divider will ignore <b>dexMethodCount</b> value if forceDexCount number lg 0
     */
    int forceDexCount = 0

    /**
     * The desired number of per dex file
     */
    int dexMethodCount = 35000

    /**
     * To avoid generating small dex file
     */
    int dexMethodCountPadding = 2000

    /**
     * ICS compatible
     */
    int maxDexMethodCount = 52000
}
