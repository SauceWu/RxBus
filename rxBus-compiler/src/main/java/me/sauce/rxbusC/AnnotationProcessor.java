package me.sauce.rxbusC;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import me.sauce.Subscribe;

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java版本支持
public class AnnotationProcessor extends AbstractProcessor {


    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationsTypes = new LinkedHashSet<>();
        mMessager.printMessage(Diagnostic.Kind.NOTE,"getSupportedAnnotationTypes");
        annotationsTypes.add(Subscribe.class.getCanonicalName());
        return annotationsTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    private Map<String, BusAnnotationProcessor> mBusAnnotatedClassMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mBusAnnotatedClassMap.clear();
        try {
            mMessager.printMessage(Diagnostic.Kind.NOTE,"process");

            processBindBus(roundEnv);
        } catch (IllegalArgumentException e) {
            return true; // stop process
        }

        for (BusAnnotationProcessor annotatedClass : mBusAnnotatedClassMap.values()) {
            try {
                annotatedClass.generateFinder().writeTo(mFiler);
            } catch (IOException e) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Unable to write binding for type %s: %s" + e.getMessage());
            }
        }
        return true;
    }



    private void processBindBus(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(Subscribe.class)) {
            BusAnnotationProcessor annotatedClass = getBusAnnotatedClass(element);
            BindBusField field = new BindBusField(element);
            annotatedClass.addField(field);
        }
    }

    private BusAnnotationProcessor getBusAnnotatedClass(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        BusAnnotationProcessor annotatedClass;
        if (mBusAnnotatedClassMap.containsKey(fullClassName)) {
            annotatedClass = mBusAnnotatedClassMap.get(fullClassName);
        } else {
            annotatedClass = new BusAnnotationProcessor(classElement, mElementUtils);
            mBusAnnotatedClassMap.put(fullClassName, annotatedClass);
        }
        return annotatedClass;
    }


}
