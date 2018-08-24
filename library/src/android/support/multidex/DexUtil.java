package android.support.multidex;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

/**
 * Tools to check odex file, remove the bad file
 */
public class DexUtil {

    private static final String DEX_SUFFIX = ".dex";

    /**
     * odex file magic "dey\n036\0"
     */
    private static final String odexFielMagic = "6465790A30333600";

    private static final String CLASSES_DEX = "classes.dex";

    public static boolean verify(ArrayList<? extends File> files, File dexDir, Object[] elements) {
        boolean result = true;
        ZipFile zp = null;
        try {
            for (File zipFile : files) {
                zp = new ZipFile(zipFile);
                File odexFile = new File(optimizedPathFor(zipFile, dexDir));
                if (!odexFile.exists()) {
                    continue;
                }
                long length = odexFile.length();
                if (length == 0) {
                    odexFile.delete();
                    continue;
                }

                MultiDex.log("verify odex file " + odexFile);

                ZipEntry entry = zp.getEntry(CLASSES_DEX);
                long minLen = entry.getSize() + 40;
                if (length <= minLen) {
                    MultiDex.log("odex  " + length + " less than " + minLen + " (uncompress size " + entry.getSize() + " plus header 40 byte)");
                    closeDexFile(zipFile, elements);
                    MultiDex.log("delete file " + odexFile.delete());
                    result = false;
                }

                String headerContent = headerOfDexFile(odexFile);
                if (!headerContent.contains(odexFielMagic)) {
                    MultiDex.log("odex file header content bad:" + headerContent);
                    closeDexFile(zipFile, elements);
                    MultiDex.log("delete file " + odexFile.delete());
                    result = false;
                }

            }
        } catch (IOException e) {
            MultiDex.log("verify error ", e);
            result = false;
        } finally {
            if (zp != null) {
                try {
                    zp.close();
                } catch (IOException e) {
                    MultiDex.log("DexUtil.verify error", e);
                }
            }
        }
        return result;
    }

    public static void closeDexFile(File zipFile, Object... elements) throws IOException {
        for (Object element : elements) {
            Object dexFileValue = null;
            try {
                dexFileValue = MultiDex.getFieldValue(element, "dexFile");
            } catch (Exception e) {
                MultiDex.log("can not get file dexFile from element ", e);
            }
            if (dexFileValue != null && dexFileValue instanceof DexFile) {
                DexFile dexFile = (DexFile) dexFileValue;
                String dexFileName = dexFile.getName();
                if (dexFileName.equals(zipFile.getAbsolutePath())) {
                    dexFile.close();
                }
            }
        }
    }

    /**
     * Converts a dex/jar file path and an output directory to an
     * output file path for an associated optimized dex file.
     */
    private static String optimizedPathFor(File path, File optimizedDirectory) {
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

    public static String headerOfDexFile(File dexFile) throws IOException {
        DataInputStream dis = null;
        try {
            StringBuilder msg = new StringBuilder(40 * 2);
            dis = new DataInputStream(new FileInputStream(dexFile));
            for (int i = 0; i < 40; i++) {
                String value = Integer.toHexString(dis.read());
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
