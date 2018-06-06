package com.codelang.library

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * https://www.jianshu.com/p/37df81365edf
 */
public class PluginDemo extends Transform implements Plugin<Project> {
    static Project project

    static File fileContainsInitClass

    static List<String> clazzList = new ArrayList<>()

    static List<String> pathList = new ArrayList<>()


    @Override
    void apply(Project project) {
        this.project = project

        System.out.println("------------------开始----------------------");
        System.out.println("这是我们的自定义插件!");
        System.out.println("------------------结束----------------------->");

        project.extensions.create("demoBuild", DemoExtension)

        //拿到android
        def android = project.extensions.getByType(AppExtension)
        //给android注册transform
        android.registerTransform(this)
        //所有注册transform都会被添加到transformManager进行管理，执行的时候，也是按添加的顺序遍历执行
        //如果想看gradle task的执行流程的话，可以看下TaskManager的createAndroidTestVariantTasks方法，
        // 整个Task的执行流程都可以看到，先判断是否是javac编译还是jack编译，
        // 然后看下createPostCompilationTasks方法，transform、ProGuard、multiDex、DexTransform，
        //层层都是dependOn依赖每个task，task依赖方面的知识可以看下教父的文章
        //链接是:https://blog.csdn.net/lzyzsd/article/details/46935405


    }

    public void transform(
            @NonNull Context context,
            @NonNull Collection<TransformInput> inputs,
            @NonNull Collection<TransformInput> referencedInputs,
            @Nullable TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {

        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        //输出app gradle设置的 extension
        def desc = project.extensions.demoBuild.desc
        def isAuto = project.extensions.demoBuild.isAuto


        println "==== extension === " + desc + "---" + isAuto

        boolean leftSlash = File.separator == '/'


        inputs.each { TransformInput input ->
            // scan class files
            input.directoryInputs.each { DirectoryInput directoryInput ->

                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                String root = directoryInput.file.absolutePath

//                MyInjects.inject(root,project)

                if (!root.endsWith(File.separator))
                    root += File.separator
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '')
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }

                    if (file.isFile() && MyInjects.shouldProcessClass(path)) {
                        MyInjects.showClass(path)

                        pathList.add(root)
                        println "class path=" + root
                    }
                }
                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)


            }
            // scan all jars
            input.jarInputs.each { JarInput jarInput ->

                String destName = jarInput.name
                def hexName = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                //原来input的文件
                File src = jarInput.file
                //新生成output的文件
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //排除掉m2repository和support路径的jar包
                if (MyInjects.shouldProcessPreDexJar(src.absolutePath)) {
                    MyInjects.scanJar(src, dest)
                    pathList.add(src.absolutePath)
                    println "jar path=" + src.absolutePath
                }
                FileUtils.copyFile(src, dest)
            }
        }

        //扫描结束，判断fileContainsInitClass是否为空，
        //如果不为空，说明我们需要插入的字节码的类在jar包中
        //这时候，我们需要对jar做一些处理
        if (fileContainsInitClass) {
            Register2Jar.insertInitCodeTo()

        }

    }

    //这个名字会在app-build-intermediates-getName  生成该名字的文件夹
    @Override
    String getName() {
        return "codelang"
    }

//要处理的数据类型  有class文件和resource资源文件
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }
//范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

}