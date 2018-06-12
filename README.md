# Wrouter
 [ ![Download](https://api.bintray.com/packages/codelang-organization/maven/Wrouter-register/images/download.svg?version=1.0.0) ](https://bintray.com/codelang-organization/maven/Wrouter-register/1.0.0/link)
 
 
Wrouter-compile搜索每个module的Route注解，并将搜索到的结果通过javapoet生成路由表类
Wrouter-register是一个自动插入字节码的插件，他将生成的路由表类全路径拿到，然后注入到Wrouter-api的Wrouter类的loadMap方法里面

