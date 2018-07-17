# Wrouter
 [ ![Download](https://api.bintray.com/packages/codelang-organization/maven/Wrouter-register/images/download.svg?version=1.0.0) ](https://bintray.com/codelang-organization/maven/Wrouter-register/1.0.0/link)
 
 
Wrouter-compile搜索每个module的Route注解，并将搜索到的结果通过javapoet生成路由表类
Wrouter-register是一个自动插入字节码的插件，他将生成的路由表类全路径拿到，然后注入到Wrouter-api的Wrouter类的loadMap方法里面



# 依赖

> 项目目录下的build.gradle

```
 dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath 'com.codelang.plugin:wrouter-register:1.0.0'
    }
```

>每个组件的build.gradle

```
android {
    ...
     javaCompileOptions {
            annotationProcessorOptions {
                arguments = [moduleName: project.getName()]
            }
        }
   }
    
dependencies {   
     annotationProcessor project(':wrouter-compiler')
   }
```

>每个组件的apply application/library 都改成
```
apply plugin: 'codelang.plugin'
```
控制每个组件是否单独运行,在每个组件下面新建```gradle.properties```
```
isRunAlone=false //添加参数设置是否单独模块运行
```
组件单独模块运行的时候，会制定debug目录下的资源，具体参考```readcomponent```


>application的集成

每个组件都可以有application，都是通过主模块的初始化来分发到每个组件上面，组件的application是根据包名来确定的，必须放在包名```com.codelang.applike```下，且该类实现AppLike接口，具体可以参看```readcomponent```的[ReadApplication](https://github.com/MRwangqi/Wrouter/blob/master/readcomponent/src/main/java/com/codelang/applike/ReadApplication.java)

### 知识点
- apt
- plugin
- transfrom
- asm/javassit

### 小节
模块之间可以通过git submodule设置几个不同仓库的组件，分发给团队成员开发

该项目参考了Arouter路由和得到的组件化，路由部分精简了Arouter，路由表的收集参考了auto-regsiter，目前该项目还有值得更改的地方，但作为入门参考是一个非常不错的选择。

在老项目中，使用路由这种注解方式还是有点太耦合，装载和卸载各个路由特别麻烦，目前在参考CC的组件化方案，以渐进式的方式过度老项目，采用总线的方式替代路由的方式，总线唯一的好处就是，不对老项目进行改造，只需要添砖加瓦，对外暴露componentName，每个组件的可以通过actionName再进行细分。






