package com.codelang.library

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class MyInjects {
    //初始化类池
//    private final static ClassPool pool = ClassPool.getDefault()
//
//    public static void inject(String path, Project project) {
//        //将当前路径加入类池,不然找不到这个类
//        pool.appendClassPath(path);
//        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
//        pool.appendClassPath(project.android.bootClasspath[0].toString());
//        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
//        pool.importPackage("android.os.Bundle")
//        pool.importPackage("com.codelang.wrouter.routes")
//
//        File dir = new File(path);
//        if (dir.isDirectory()) {
//            //遍历文件夹
//            dir.eachFileRecurse { File file ->
//                String filePath = file.absolutePath
//                println("filePath = " + filePath)
//                if (file.getName().equals("MainActivity.class")) {
//                    //获取MainActivity.class
//                    CtClass ctClass = pool.getCtClass("com.codelang.transform.MainActivity");
//                    println("ctClass = " + ctClass)
//                    //解冻
//                    if (ctClass.isFrozen())
//                        ctClass.defrost()
//                    //获取到OnCreate方法
//                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")
//                    println("方法名 = " + ctMethod)
//
//                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_SHORT).show(); """
//
//                    //在方法开头插入代码
//                    ctMethod.insertBefore(insetBeforeStr);
//                    ctClass.writeFile(path)
//                    ctClass.detach()//释放
//                }
//            }
//        }
//
//    }

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

                if (shouldProcessClass(entryName)) {
                    //拿到对应class类  这个class类的路径是用斜杠标识的，我们需要转换成点号
                    showClass(entryName)
                } else if (entryName == "com/codelang/api/core/WRouter.class") {
                    //这个地方的判断就是找到我们要插入字节的类，
                    //如果他是一个jar包里面的类，我们就把他记录下来
                    //然后拷贝一个新的jar包，对这个jar包的WRouter进行注册
                    //注册好后，删除原来的jar，将这个拷贝的jar重新命名为删除的jar包
                    PluginDemo.fileContainsInitClass = destFile
                }
            }
            file.close()
        }
    }

    /**
     * 扫描类，将符合标准的注册表类插入到类池中
     * @param className
     */
    static void showClass(String className) {
//        className = className.replace("/", ".");
//        className = className.substring(0, className.lastIndexOf("."));
//        String prefix = "com.codelang.wrouter.routes"
        if (shouldProcessClass(className)) {
            // className= com/codelang/wrouter/routes/Wrouter$$Group$$app.class
            //replace后=com.codelang.wrouter.routes.Wrouter$$Group$$app.class
            className = className.replace("/", ".")
            //substring后=com.codelang.wrouter.routes.Wrouter$$Group$$app
            className = className.substring(0, className.lastIndexOf("."))
            //将找到的类暂时添加到一个集合里面
            PluginDemo.clazzList.add(className)
            println "---------" + className
        }
    }


    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith("com/codelang/wrouter/routes")
    }
}