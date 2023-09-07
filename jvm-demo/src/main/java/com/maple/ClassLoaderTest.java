package com.maple;

import sun.misc.Launcher;

import java.net.URL;

/**
 * Hello world!
 *
 */
public class ClassLoaderTest
{
    public static void main( String[] args )
    {
        // 获取系统类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(systemClassLoader);

        // 获取上层: 扩展类加载器
        ClassLoader parent = systemClassLoader.getParent();
        System.out.println(parent);

        // 获取上层: 引导类加载器
        ClassLoader boot = parent.getParent();
        System.out.println(boot); // null
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        for (URL urL : urLs) {
            System.out.println(urL); // 引导类加载器加载的路径
        }

        // 对于用户自定义类来说: 默认使用系统类加载器加载
        ClassLoader classLoader = ClassLoaderTest.class.getClassLoader();
        System.out.println(classLoader);

        // String 等核心类库使用引导类进行加载
        ClassLoader loader = String.class.getClassLoader();
        System.out.println(loader); // null
    }
}
