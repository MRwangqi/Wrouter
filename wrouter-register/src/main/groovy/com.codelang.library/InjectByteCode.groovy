package com.codelang.library

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import sun.rmi.runtime.Log

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class InjectByteCode {

    static void insertInitCodeTo() {
        File file = RouteTransform.fileContainsInitClass
        InjectByteCode register2Jar = new InjectByteCode()
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
                if (Cons.INJECT_CLASS == entryName) {
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
            //是不是loadMap方法
            if (name == Cons.INJECT_METHOD) {
                boolean _static = (access & Opcodes.ACC_STATIC) > 0
                println("static------" + _static)
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

                //插入路由表操作
                RouteTransform.clazzList.each { name ->
                    mv.visitLdcInsn(name)
                    // 给WRouter.loadMap()方法插入字符串  register("$name");
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , Cons.INJECT_CLASS_NAME
                            , Cons.GENERATE_STRING//register
                            , "(Ljava/lang/String;)V"
                            , false)
                }

                //插入application操作
                RouteTransform.applicationList.each { name ->
                    mv.visitLdcInsn(name)

                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , Cons.INJECT_CLASS_NAME
                            , Cons.APPLICATION_STRING//initApplication
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

}