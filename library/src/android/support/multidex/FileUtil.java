package android.support.multidex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 */
public class FileUtil {

    /**
     * copy file to dest(File or Directory)
     *
     * @param srcFile
     * @param dest
     */
    public static boolean copy(File srcFile, File dest) {

        if (dest.isDirectory()) {
            dest = new File(dest, srcFile.getName());
        }

        dest.delete();

        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(dest);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            fis = new FileInputStream(srcFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bos.flush();
            fos.getFD()
               .sync();
            return true;
        } catch (IOException e) {
            MultiDex.log("copy file error ", e);
            return false;
        } finally {
            close(fis, fos);
        }
    }

    public static void close(Object... obj) {
        for (Object o : obj) {
            if (o instanceof Closeable) {
                try {
                    ((Closeable) o).close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }
}
