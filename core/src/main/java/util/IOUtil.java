package util;

import cn.hutool.core.io.IoUtil;
import com.sun.jdi.Bootstrap;

import java.io.*;
import java.util.Objects;

/**
 * @author JwZheng
 * @date 2021/7/22 13:32
 */
public class IOUtil {
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        int size = 1024;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[size];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }
    public static byte[] getBytesFromFile(String fileName) throws IOException {
        String path = getAbsolutePath(fileName);
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(path);
        }
        return getBytesFromStream(new FileInputStream(file));
    }

    public static String getAbsolutePath(String path) {
        String absolutePath = Objects.requireNonNull(IOUtil.class.getResource("/")).getPath();
        return absolutePath.replaceAll("\\\\", "/") + path;
    }
}
