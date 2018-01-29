package com.siongu.sucker.compiler;

import com.google.auto.service.AutoService;
import com.siongu.sucker.annotation.annotations.ListenerClass;
import com.siongu.sucker.annotation.annotations.ListenerMethod;
import com.siongu.sucker.annotation.annotations.SuckClick;
import com.siongu.sucker.annotation.annotations.SuckView;
import com.siongu.sucker.annotation.consts.Consts;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SuckProcessor extends AbstractProcessor {
    private final String ACTIVITY = "android.app.Activity";
    private final String VIEW = "android.view.View";
    Elements elementUtil;
    Types typeUtil;
    Filer filer;
     Map<String, Class<? extends Annotation>> supportedAnnotationsClass = new LinkedHashMap<>();
     Set<String> supportedAnnotations = new LinkedHashSet<>();

     {
        // support annotations
        supportedAnnotations.add(Consts.ANNOTATIONS_SUCK_VIEW);
        supportedAnnotations.add(Consts.ANNOTATIONS_SUCK_CLICK);
        // support annotations class
        supportedAnnotationsClass.put(Consts.ANNOTATIONS_SUCK_VIEW, SuckView.class);
        supportedAnnotationsClass.put(Consts.ANNOTATIONS_SUCK_CLICK, SuckClick.class);

    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtil = processingEnv.getElementUtils();
        typeUtil = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Map<Class<? extends Annotation>, Set<Element>>> rootClasses = new LinkedHashMap<>();
        category(annotations, roundEnv, rootClasses);
        processAnnotationsClass(rootClasses);
        return true;
    }

    private void category(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, Map<String, Map<Class<? extends Annotation>, Set<Element>>> rootClasses) {
        for (TypeElement t : annotations) {
            String key = t.getQualifiedName().toString();
            if (supportedAnnotationsClass.containsKey(key)) {
                Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(supportedAnnotationsClass.get(key));
                for (Element e : rootElements) {
                    Element enclosingElement = e.getEnclosingElement();
                    String name = elementUtil.getBinaryName((TypeElement) enclosingElement).toString();
                    if (!rootClasses.containsKey(name)) {
                        Map<Class<? extends Annotation>, Set<Element>> annotationClasses = new LinkedHashMap<>();
                        Set<Element> set = new LinkedHashSet<>();
                        set.add(e);
                        annotationClasses.put(supportedAnnotationsClass.get(key), set);
                        rootClasses.put(name, annotationClasses);
                    } else {
                        Map<Class<? extends Annotation>, Set<Element>> annotationClasses = rootClasses.get(name);
                        Class<? extends Annotation> classKey = supportedAnnotationsClass.get(key);
                        if (!annotationClasses.containsKey(classKey)) {
                            Set<Element> set = new LinkedHashSet<>();
                            set.add(e);
                            annotationClasses.put(classKey, set);
                        } else {
                            Set<Element> set = annotationClasses.get(classKey);
                            set.add(e);
                        }
                    }
                }
            }
        }
    }

    private void processAnnotationsClass(Map<String, Map<Class<? extends Annotation>, Set<Element>>> rootClasses) {

        for (Map.Entry<String, Map<Class<? extends Annotation>, Set<Element>>> rootClass : rootClasses.entrySet()) {
            String classQualifiedName = rootClass.getKey();
            ClassName annotationsFrom = ClassName.bestGuess(classQualifiedName);
            String packageName = annotationsFrom.packageName();
            String filePrefix = classQualifiedName.substring(packageName.length() + 1).replace(".", "$");
            TypeSpec.Builder fileBuilder = TypeSpec.classBuilder(filePrefix + "$ViewSucking");
            FieldSpec target = FieldSpec.builder(TypeName.OBJECT, "target").build();
            fileBuilder.addField(target);
            MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("suck")
                                                               .addModifiers(Modifier.PUBLIC)
                                                               .addAnnotation(Override.class)
                                                               .returns(TypeName.VOID)
                                                               .addParameter(TypeName.OBJECT, "o");
            injectMethodBuilder.addStatement("target = o");
            // category annotations
            for (Map.Entry<Class<? extends Annotation>, Set<Element>> annotationClass : rootClass.getValue().entrySet()) {
                Class<? extends Annotation> clazz = annotationClass.getKey();
                Set<Element> elements = annotationClass.getValue();
                if (clazz.getTypeName().contains(SuckView.class.getSimpleName())) {// process view
                    processView(injectMethodBuilder, elements);
                } else if (clazz.getTypeName().contains(SuckClick.class.getSimpleName())) {// process listener
                    processListener(fileBuilder, injectMethodBuilder, clazz, elements);
                }
            }

            fileBuilder.addMethod(injectMethodBuilder.build());
            ClassName typeInterface = ClassName.get(Consts.PACKAGE_API, "ISucker");
            TypeSpec file = fileBuilder
                    .addSuperinterface(typeInterface)
                    .addModifiers(Modifier.PUBLIC)
                    .build();
            JavaFile javaFile = JavaFile.builder(packageName, file).build();
            try {
                javaFile.writeTo(filer);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processView(MethodSpec.Builder injectMethodBuilder, Set<Element> elements) {
        for (Element e : elements) {
            SuckView suckView = e.getAnnotation(SuckView.class);
            int id = suckView.value();
            ClassName activity = ClassName.bestGuess(ACTIVITY);
            ClassName view = ClassName.bestGuess(VIEW);
            injectMethodBuilder.beginControlFlow("if((target instanceof $T)||(target instanceof $T))", activity, view);
            injectMethodBuilder.addStatement("(($T)target).$N = (($T)target).findViewById($L)", e.getEnclosingElement()
                    , e.getSimpleName(), e.getEnclosingElement(), id);
            injectMethodBuilder.endControlFlow();
        }
    }

    private void processListener(TypeSpec.Builder fileBuilder, MethodSpec.Builder injectMethodBuilder
            , Class<? extends Annotation> clazz, Set<Element> elements) {
        ListenerClass listenerClass = clazz.getAnnotation(ListenerClass.class);
        ListenerMethod[] methods = listenerClass.method();
        ListenerMethod method = methods[0];
        ClassName listenerType = ClassName.bestGuess(listenerClass.type());
        ClassName parameterType = ClassName.bestGuess(method.parameters()[0]);
        ClassName activity = ClassName.bestGuess(ACTIVITY);
        ClassName view = ClassName.bestGuess(VIEW);
        injectMethodBuilder.beginControlFlow("if((target instanceof $T)||(target instanceof $T))"
                , activity, view);
        for (Element e : elements) {// annotations elements
            SuckClick suckClick = e.getAnnotation(SuckClick.class);
            int[] ids = suckClick.value();
            MethodSpec overideListenerMethod = MethodSpec.methodBuilder(method.name())
                                                         .addParameter(parameterType, "v")
                                                         .addAnnotation(Override.class)
                                                         .returns(TypeName.VOID)
                                                         .addModifiers(Modifier.PUBLIC)
                                                         .addStatement("(($T)target).$N(v)", e.getEnclosingElement(), e.getSimpleName())
                                                         .build();
            TypeSpec anonymousListener = TypeSpec.anonymousClassBuilder("")
                                                 .addSuperinterface(listenerType)
                                                 .addMethod(overideListenerMethod)
                                                 .build();
            Object listener;
            if (ids.length == 1) {
                listener = anonymousListener;
            } else {
                FieldSpec fieldListener = FieldSpec.builder(listenerType, e.getSimpleName().toString() + "Listener")
                                                   .initializer("$L", anonymousListener)
                                                   .build();
                fileBuilder.addField(fieldListener);
                listener = fieldListener.name;
            }

            for (int i = 0; i < ids.length; i++) {
                injectMethodBuilder.addStatement("View v$L = (($T)target).findViewById($L);\nv$L.$N($L)"
                        , ids[i], e.getEnclosingElement(), ids[i], ids[i], listenerClass.setter(), listener);
            }
        }
        injectMethodBuilder.endControlFlow();
    }

}
