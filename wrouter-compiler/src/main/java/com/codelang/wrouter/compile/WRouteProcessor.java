package com.codelang.wrouter.compile;

import com.codelang.wrouter.annotation.WRoute;
import com.codelang.wrouter.compile.utils.Constant;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
public class WRouteProcessor extends AbstractProcessor {


    private Messager mMessager;
    private Elements mElementUtils;
    private Filer mFiler;
    private String moduleName;
    private Elements elements;
    private Map<String, Element> routeMap = new TreeMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mFiler = processingEnv.getFiler();
        // Get class meta.
        elements = processingEnv.getElementUtils();


        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Constant.MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            //module-java替换成modulejava
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            mMessager.printMessage(Diagnostic.Kind.NOTE, "The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "These no module name, at 'build.gradle', like :\n" +
                    "apt {\n" +
                    "    arguments {\n" +
                    "        moduleName project.getName();\n" +
                    "    }\n" +
                    "}\n");
            throw new RuntimeException("ARouter::Compiler >>> No module name, for more information, look at gradle log.");
        }

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(WRoute.class.getName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        routeMap.clear();


        //得到所有注解了WRoute的对象
        Set<? extends Element> elms = roundEnvironment.getElementsAnnotatedWith(WRoute.class);
           /*

              ```Map<String, Class>```
             */
        ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Class.class)
        );
          /*
            ```Map<String, Class>  routes```
             */
        ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routes").build();


          /* @Override
                 public  void loadMap(Map<String, Class> routes);
             */
        MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder("loadMap")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(rootParamSpec);


        for (Element element : elms) {
            TypeMirror tm = element.asType();
            WRoute route = element.getAnnotation(WRoute.class);

            TypeElement te = (TypeElement) element;
            ClassName cn = ClassName.get(te);

            mMessager.printMessage(Diagnostic.Kind.NOTE, route.path() + "---" + route.desc() + ">>> Found activity Wroute: " + cn + " <<<");
//            Map<String, Integer> paramsType = new HashMap<>(2);
//            //获取当前元素的所有field
//            for (Element field : element.getEnclosedElements()) {
//                if (field.getKind().isField() && field.getAnnotation(Autowired.class) != null) {
//                    Autowired paramConfig = field.getAnnotation(Autowired.class);
////                    paramsType.put(StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeExchange(field));
//
//                    mMessager.printMessage(Diagnostic.Kind.NOTE,
//                            (StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName().toString() : paramConfig.name())
//                                    + "---" + field.asType().toString());
//                }
//            }


            loadIntoMethodOfRootBuilder.addStatement("routes.put($S,$T.class)", route.path(), cn);
        }

        // 拼接 Arouter$$Root$$<moduleName>类
        String rootFileName = Constant.NAME_OF_GROUP + moduleName;


        //生成类
        TypeSpec routClass = TypeSpec.classBuilder(rootFileName)
                .addJavadoc(Constant.WARNING_TIPS)
                .addSuperinterface(ClassName.get(elements.getTypeElement(Constant.IROUTE_GROUP)))
                .addModifiers(PUBLIC)
                .addMethod(loadIntoMethodOfRootBuilder.build())
                .build();

        try {
            //将类写到文件
            JavaFile.builder(Constant.PACKAGE_OF_GENERATE_FILE, routClass).build().writeTo(mFiler);
        } catch (Exception e) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, Constant.PACKAGE_OF_GENERATE_FILE);
            mMessager.printMessage(Diagnostic.Kind.NOTE, routClass.name);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "写文件失败");
        }
        return false;
    }



}
