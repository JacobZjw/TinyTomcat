package classLoader;

/**
 * @author JwZheng
 * @date 2021/7/23 21:28
 */
public class CommonClassLoader extends ClassLoader{

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}
