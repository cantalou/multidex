package android.support.multidex;

import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import dalvik.system.DexFile;

/**
 * Tools to check odex file and remove if it was bad
 */
public class DexUtil {

    private static final String DEX_SUFFIX = ".dex";

    /**
     * odex file magic "dey\n036\0"
     */
    private static final String odexFileMagic = "6465790a30333600";

    /**
     * verify the header content of optimized dex file.<br/>
     * /system/bin/dexopt rewrite header value ffffffffffffffff -> 6465790a30333600 at last phase, so just check file magic value.
     * In some 4.x device, we found that generated bad odex occasionally which only contains header(40 bytes) and dex file content. Dalvikvm log
     * message like :Unable to extract+optimize DEX from '/data/data/[package]/code_cache/secondary-dexes/[package].apk.classes2.zip', nothing useful message
     * for resolve the issue. So this method verify and delete the cached odex file if was bad for generating new odex file in next time call makeDexElements.
     *
     * @param dexZipFile
     * @param dexDir
     * @param element
     * @return
     */
    public static boolean verify(File dexZipFile, File dexDir, Object element) {
        try {
            File optDexFile = new File(optimizedPathFor(dexZipFile, dexDir));
            if (!optDexFile.exists()) {
                return false;
            }
            MultiDex.log("verify opt dex file " + optDexFile);
            String headerContent = headerOfDexFile(optDexFile);
            if (!TextUtils.isEmpty(headerContent) && headerContent.startsWith(odexFileMagic)) {
                return true;
            }
            MultiDex.log("odex file header content was bad:" + headerContent);
            closeDexFile(dexZipFile, element);
            MultiDex.log("delete file " + optDexFile.delete());
        } catch (IOException e) {
            MultiDex.log("verify error ", e);
        }
        return false;
    }

    public static void closeDexFile(File zipFile, Object element) {
        if (element == null) {
            return;
        }
        try {
            Object dexFileValue = MultiDex.getFieldValue(element, "dexFile");
            if (dexFileValue instanceof DexFile) {
                DexFile dexFile = (DexFile) dexFileValue;
                String dexFileName = dexFile.getName();
                if (dexFileName.equals(zipFile.getAbsolutePath())) {
                    dexFile.close();
                }
            }
        } catch (Exception e) {
            MultiDex.log("can not get file dexFile from element ", e);
        }
    }

    /**
     * Converts a dex/jar file path and an output directory to an
     * output file path for an associated optimized dex file.
     */
    public static String optimizedPathFor(File path, File optimizedDirectory) {
        /*
         * Get the filename component of the path, and replace the
         * suffix with ".dex" if that's not already the suffix.
         *
         * We don't want to use ".odex", because the build system uses
         * that for files that are paired with resource-only jar
         * files. If the VM can assume that there's no classes.dex in
         * the matching jar, it doesn't need to open the jar to check
         * for updated dependencies, providing a slight performance
         * boost at startup. The use of ".dex" here matches the use on
         * files in /data/dalvik-cache.
         */
        String fileName = path.getName();
        if (!fileName.endsWith(DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(DEX_SUFFIX);
                fileName = sb.toString();
            }
        }
        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }

    /**
     * magic(8), dexOffset(4), dexLength(4), depsOffset(4), depsLength(4), optOffset(4), optLength(4), flags(4), checksum(4)
     *
     * @param dexFile
     * @return header content of opt dex file in hex format
     */
    public static String headerOfDexFile(File dexFile) throws IOException {
        DataInputStream dis = null;
        try {
            StringBuilder msg = new StringBuilder(40 * 2);
            dis = new DataInputStream(new FileInputStream(dexFile));
            for (int i = 0; i < 40; i++) {
                int data = dis.read();
                if (data == -1) {
                    break;
                }
                String value = Integer.toHexString(data);
                if (value.length() == 2) {
                    msg.append(value);
                } else {
                    msg.append("0")
                       .append(value);
                }
            }
            return msg.toString();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }
}
