package classLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JwZheng
 * @date 2021/7/23 20:33
 */
public class WebappClassLoader extends URLClassLoader {
    private static final String CLASS_FILE_SUFFIX = ".class";

    private final Map<String, Class<?>> classMap;

    private final ClassLoader j2seClassLoader;

    private final ClassLoader parent;

    public WebappClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.classMap = new ConcurrentHashMap<>();
        this.j2seClassLoader = getSystemClassLoader();
        this.parent = parent;
    }


    /**
     * 查找类并加载
     *
     * @param name 类名
     * @return Class
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = binaryNameToPath(name);
        URL[] urLs = super.getURLs();
        File classFile = null;
        for (URL url : urLs) {
            File base = null;
            try {
                base = new File(url.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            classFile = new File(base, path);
            if (classFile.exists()) {
                break;
            }
            classFile = null;
        }
        if (classFile == null) {
            throw new ClassNotFoundException(name);
        }
        byte[] bytes = loadClassBytes(classFile);
        return this.defineClass(name, bytes, 0, bytes.length);
    }

    /**
     * 将 class 文件读取到字节流
     *
     * @param classFile class 文件
     * @return 字节流
     */
    private byte[] loadClassBytes(File classFile) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(classFile)) {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * 查找当前是否已加载该类
     *
     * @param name 类名
     * @return Class or NULL
     */
    protected Class<?> findLoadedClass0(String name) {
        return classMap.get(name);
    }

    /**
     * 将类名转换成 .class 文件路径
     *
     * @param binaryName 类名
     * @return .class 文件路径
     */
    private String binaryNameToPath(String binaryName) {
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        path.append('/');
        path.append(binaryName.replace('.', '/'));
        path.append(CLASS_FILE_SUFFIX);
        return path.toString();
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            //1、检查该类是否已被当前 WebappClassLoader 加载
            Class<?> clazz = findLoadedClass0(name);
            if (clazz != null) {
                return clazz;
            }

            //2、检查JVM缓存是否已加载该类
            clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }

            //3、尝试通过系统类加载器 AppClassLoader 加载类，防止 WebappClassLoader 重写 JDK 中的类
            try {
                clazz = j2seClassLoader.loadClass(name);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException ignored) {

            }

            //4、使用 WebappClassLoader 加载
            clazz = findClass(name);
            if (clazz != null) {
                classMap.put(name, clazz);
                return clazz;
            }

            //5、如果 WebappClassLoader 没有加载到，则无条件委托给父类加载
            try {
                clazz = Class.forName(name, false, parent);
                return clazz;
            } catch (Exception ignored) {

            }
        }
        throw new ClassNotFoundException(name);
    }

}
