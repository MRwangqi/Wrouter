package com.codelang.library;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author wangqi
 * @since 2018/6/7 10:14
 *
 *
 */

class RegisterPlugin implements Plugin<Project> {

    /**
     * 设置主模块的module为app
     */
    String defaultModule = "app"
    /**
     * 编译的模块
     */
    String compileModule
    /**
     * 是否单独运行
     */
    boolean isRunAlone

    @Override
    void apply(Project project) {
        System.out.println("------------------插件开始----------------------")
        project.extensions.create("demoBuild", DemoExtension)

        //app  readcomponent
        String module = project.path.replace(":", "")

        System.out.println("------------------" + module + "----------------------")

        AssembleTask assembleTask = getAssembleInfo(project)


        if (assembleTask.isAssemble) {//如果是assemble操作
            setCompileModule(assembleTask.module)
        }

        //设置模块是否单独运行
        runAlone(project, assembleTask, module)

        //模块的依赖
        moduleApply(module, assembleTask, project)


    }

    /**
     * 设置模块的依赖
     * @param assembleTask
     * @param project
     */
    void moduleApply(String module, AssembleTask assembleTask, Project project) {
        if (isRunAlone) {
            project.apply plugin: 'com.android.application'
            //如果当前的模块的不是app模块
            if (!module.equals(defaultModule)) {
                project.android.sourceSets {
                    main {
                        manifest.srcFile 'src/main/debug/AndroidManifest.xml'
                        java.srcDirs = ['src/main/java', 'src/main/debug/java']
                        res.srcDirs = ['src/main/res', 'src/main/debug/res']
                    }
                }
            }
            //如果当前是app模块
            if (assembleTask.isAssemble && module.equals(defaultModule)) {
                //自动依赖组件
                compileComponents(assembleTask, project)
                //拿到android
                def android = project.extensions.getByType(AppExtension)
                //给android注册transform
                android.registerTransform(new RouteTransform(project))
            }
        } else {
            //只有module为library的时候，才会输出aar文件，并将aar文件移动到componentRelease下面
            project.apply plugin: 'com.android.library'

            project.afterEvaluate {
                Task assembleReleaseTask = project.tasks.findByPath("assembleRelease")
                if (assembleReleaseTask != null) {
                    assembleReleaseTask.doLast {
                        //如果当前readComponent模块，则将生成的 readComponent-release.aar
                        File infile = project.file("build/outputs/aar/$module-release.aar")
                        //设置文件的输出目录，在library的上一级目录
                        File outfile = project.file("../componentRelease")
                        //
                        File desFile = project.file("$module-release.aar")
                        project.copy {
                            from infile
                            into outfile
                            rename {
                                String fileName -> desFile.name
                            }
                        }
                        System.out.println("$module-release.aar copy success ");
                    }
                }
            }
        }
    }

    void compileComponents(AssembleTask assembleTask, Project project) {
        String components

        if (assembleTask.isDebug) {
            components = (String) project.properties.get("debugComponent")
        } else {
            components = (String) project.properties.get("compileComponent")
        }

        if (components == null || components.length() == 0) {
            System.out.println("app的gradle.properties没有设置依赖")
            return
        }

        String[] compileComponents = components.split(",")

        for (String str : compileComponents) {

            println "--------------" + compileComponents

            //如果是aar文件依赖的话，如 compile project(':FacebookPhotoPicker-release')
            if (str.contains(":")) {
                File file = project.file("../componentRelease/" + str.split(":")[1] + "-debug.aar")
                if (file.exists()) {
                    project.dependencies.add("implementation", str + "-debug@aar")

                    System.out.println("add dependencies aar : " + str + "-debug@aar")

                } else {
                    throw new RuntimeException(str + " not found ! maybe you should generate a new one ")
                }
            } else {
                //如果是直接compile项目的话
                project.dependencies.add("implementation", project.project(':' + str))

                System.out.println("------------------add dependencies project : " + str)
            }
        }

    }

    /**
     * 判断该模快是否单独运行
     * 如果运行的模块是app，则其他的模块都必须是library状态
     * 当然，运行的模块是app的时候，isRunAlone肯定为true
     */
    void runAlone(Project project, AssembleTask assembleTask, String module) {
        isRunAlone = Boolean.parseBoolean((project.properties.get("isRunAlone")))
        if (compileModule.equals(defaultModule)) {
            isRunAlone = true
        }
        if (isRunAlone && assembleTask.isAssemble) {
            //如果当前运行的模块就是编译的模块
            if (module.equals(compileModule)) {
                isRunAlone = true
            } else {
                isRunAlone = false
            }
        }
        println "-----isRunAlone-------" + isRunAlone + "---" + compileModule

        project.setProperty("isRunAlone", isRunAlone)
    }

    /**
     * 设置编译模块
     */
    void setCompileModule(String module) {
        if (module.length() > 0) {
            compileModule = module
        } else {
            compileModule = defaultModule
        }
        println("------compileModule------" + compileModule)
    }
    /**
     在app模块run时候输出
     task---->:app:assembleDebug
     task---->:readcomponent:assembleDebug

     assembleRelease的时候
     task---->assembleRelease
     */
    AssembleTask getAssembleInfo(Project project) {
        AssembleTask assembleTask = new AssembleTask()
        for (String task : project.gradle.startParameter.taskNames) {
            println("task---->" + task)
            if (task.toUpperCase().contains("ASSEMBLE")) {
                assembleTask.isAssemble = true
                String[] arr = task.split(":")
                if (arr.length == 3) {
                    assembleTask.module = arr[1]//app or readcomponent
                }
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true
                }
                break
            }
        }
        return assembleTask
    }

    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false
        String module
    }

}
