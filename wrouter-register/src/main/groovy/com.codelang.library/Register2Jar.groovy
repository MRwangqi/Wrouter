package com.codelang.library

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class Register2Jar {

    static void insertInitCodeTo() {
        File file = PluginDemo.fileContainsInitClass
        Register2Jar register2Jar = new Register2Jar()
        if (file.getName().endsWith('.jar')) {
            println "appendClassPath-----" + file.absolutePath
            register2Jar.insertInitCodeIntoJarFile(file)
        }

    }

    /**
     * generate code into jar file
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return
     */
    private File insertInitCodeIntoJarFile(File jarFile) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if ("com/codelang/api/core/WRouter.class" == entryName) {

                    println('Insert init code to class >> ' + entryName)

                    def bytes = referHackWhenInit(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

    private byte[] referHackWhenInit(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc,
                                  String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            //是不是loadRouterMap方法
            if (name == "loadMap") {
                boolean _static = (access & Opcodes.ACC_STATIC) > 0

                println("static------"+_static)
                mv = new RouteMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    class RouteMethodVisitor extends MethodVisitor {

        RouteMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            //generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                PluginDemo.clazzList.each { name ->
                    name = name.replaceAll("/", ".")
                    println("asm-----" + name)
                    mv.visitLdcInsn(name)//存储group分组的module类名
                    // generate invoke register method into LogisticsCenter.loadRouterMap()
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , "com/codelang/api/core/WRouter"//com/alibaba/android/arouter/core/LogisticsCenter
                            , "register"//register
                            , "(Ljava/lang/String;)V"
                            , false)
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }

//    private static final ClassPool pool = ClassPool.getDefault()
//    /**
//     *
//     * @param path 类的路径
//     */
//    static void injectJar(String jarPath) {
//        println("jarPath ---- " + jarPath)
//        //将拥有WRouter类的jar包插入到类路径里面
//        pool.appendClassPath(jarPath)
//        pool.appendClassPath(PluginDemo.project.android.bootClasspath[0].toString())
//        pool.importPackage("android.util.Log")
//
//        //去jar包里面找WRouter类，如果找到的话，返回这个类实例
//        CtClass ctClass = pool.getCtClass("com.codelang.api.core.WRouter")
//        println("ctClass ---- " + ctClass)
//        //解冻
//        if (ctClass.isFrozen())
//            ctClass.defrost()
//        //找到loadMap方法
//        CtMethod ctMethod = ctClass.getDeclaredMethod("loadMap")
//        println("方法名 = " + ctMethod)
//
//        String insetBeforeStr = ""
//
//        for (String clazz : PluginDemo.clazzList) {
//            insetBeforeStr += "register(" +"\""+ clazz +"\""+ ");"
//        }
//        println("-----insetBeforeStr----" + insetBeforeStr)
//
//        //在方法开头插入代码
//        ctMethod.insertBefore(insetBeforeStr)
//        ctClass.detach()//释放
//    }
}