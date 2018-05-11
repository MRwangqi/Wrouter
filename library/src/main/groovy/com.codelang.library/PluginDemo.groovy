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
    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        System.out.println("------------------开始----------------------");
        System.out.println("这是我们的自定义插件!");
        System.out.println("------------------结束----------------------->");

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

}

public void transform(
        @NonNull Context context,
        @NonNull Collection<TransformInput> inputs,
        @NonNull Collection<TransformInput> referencedInputs,
        @Nullable TransformOutputProvider outputProvider,
        boolean isIncremental) throws IOException, TransformException, InterruptedException {

    super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

    inputs.each { TransformInput input ->
        input.directoryInputs.each { DirectoryInput directoryInput ->

            //注入代码操作
            MyInjects.inject(directoryInput.file.absolutePath, project)

            if (directoryInput.file.isDirectory()) {
                println "==== directoryInput.file === " + directoryInput.file
                directoryInput.file.eachFileRecurse { File file ->
                    // ...对目录进行插入字节码
                }
            }
            //处理完输入文件之后，要把输出给下一个任务
            def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
            FileUtils.copyDirectory(directoryInput.file, dest)
        }

        input.jarInputs.each { JarInput jarInput ->
            println "------=== jarInput.file === " + jarInput.file.getAbsolutePath()
            File tempFile = null
            if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                // ...对jar进行插入字节码
            }
            /**
             * 重名输出文件,因为可能同名,会覆盖
             */
            def jarName = jarInput.name

            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            //处理jar进行字节码注入处理
            def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(jarInput.file, dest)

        }
    }

}


@Override
String getName() {
    return PluginDemo.getName()
}

//要处理的数据类型  有class文件和resource资源文件
@Override
Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
}

@Override
Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT
}

@Override
boolean isIncremental() {
    return false
}

}