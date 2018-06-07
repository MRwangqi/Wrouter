package com.codelang.library

import jdk.internal.org.objectweb.asm.ClassReader

import java.util.jar.JarEntry
import java.util.jar.JarFile


class ScanClass {

    /**
     * 扫描jar包里面的类
     * @param jarFile
     * @param destFile
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()

                //扫描类
                showClass(entryName)

                if (entryName == Cons.INJECT_CLASS) {
                    //这个地方的判断就是找到我们要插入字节的类，
                    //如果他是一个jar包里面的类，我们就把他记录下来
                    //然后拷贝一个新的jar包，对这个jar包的WRouter进行注册
                    //注册好后，删除原来的jar，将这个拷贝的jar重新命名为删除的jar包
                    RouteTransform.fileContainsInitClass = destFile
                }
            }
            file.close()
        }
    }

    /**
     * 扫描class类，将符合标准的注册表类插入到类池中
     * @param className
     */
    static void showClass(String className) {

        if (className.contains("com/codelang/applike")) {
            //将找到的application存储起来
            //com/codelang/applike/ReadApplication.class
            //com.codelang.applike.ReadApplication
            RouteTransform.applicationList.add(getClassName(className))
            println "---------" + className
        }
        if (shouldProcessClass(className)) {
            //将找到的注册表类暂时添加到一个集合里面
            RouteTransform.clazzList.add(getClassName(className))
            println "---------" + className
        }
    }


    static String getClassName(String className) {
        className = className.replace("/", ".")
        className = className.substring(0, className.lastIndexOf("."))
        return className
    }


    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    /**
     * 主要判断该class是否是存放路由表下面的包路径
     * @param entryName
     * @return
     */
    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith("com/codelang/wrouter/routes")
    }
}