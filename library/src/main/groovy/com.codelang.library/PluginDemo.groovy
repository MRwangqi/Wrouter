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


        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(this)


        project.afterEvaluate {
            project.logger.error '========= '

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