package me.sauce.rxbusC;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Created by sauce on 2017/3/8.
 * Version 1.0.0
 */
public class BusAnnotationProcessor {

    public TypeElement mClassElement;
    public List<BindBusField> mFields;
    public Elements mElementUtils;
    public static final ClassName RXBUS_TYPE = ClassName.get("me.sauce.rxBus", "RxBus");
    public static final ClassName UNBIND_TYPE = ClassName.get("me.sauce.rxBus", "UnSubscribe");

    public static final ClassName DISPOSABLES_TYPE = ClassName.get("io.reactivex.disposables", "CompositeDisposable");

    public BusAnnotationProcessor(TypeElement classElement, Elements mElementUtils) {
        this.mClassElement = classElement;
        this.mFields = new ArrayList<>();
        this.mElementUtils = mElementUtils;

    }

    public void addField(BindBusField field) {
        mFields.add(field);
    }

    public JavaFile generateFinder() {
        TypeMirror typeMirror = mClassElement.asType();
        TypeName typeName = TypeName.get(typeMirror);
        FieldSpec compositeDisposable = FieldSpec.builder(DISPOSABLES_TYPE, "mCompositeDisposable", Modifier.PUBLIC).initializer("new CompositeDisposable()").build();
        MethodSpec.Builder injectMethodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName instanceof ParameterizedTypeName ? ((ParameterizedTypeName) typeName).rawType : ClassName.get(typeMirror), "target");

        injectMethodBuilder.addStatement("$T rxBus =$T.getInstance()", RXBUS_TYPE, RXBUS_TYPE);
        for (BindBusField field : mFields) {
            // find views
            injectMethodBuilder.addCode("mCompositeDisposable.add(");
            injectMethodBuilder.addCode("rxBus.toObservable($L)", field.getTag());
            injectMethodBuilder.addCode(".observeOn($L)", field.getFieldThread());
            if (field.getParameters().size() == 0)
                injectMethodBuilder.addCode(".subscribe(o ->target.$N()));\n", field.getFieldName());
            else
                injectMethodBuilder.addCode(".subscribe(o ->target.$N(($T)$L)));\n", field.getFieldName(), field.getParameters().get(0).asType(), "o");
        }
        MethodSpec.Builder unBind = MethodSpec.methodBuilder("unSubscribe")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get(Override.class))
                .addCode("if(!mCompositeDisposable.isDisposed())")
                .addStatement("mCompositeDisposable.dispose()");

        TypeSpec finderClass = TypeSpec.classBuilder(mClassElement.getSimpleName() + "_BusManager")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(injectMethodBuilder.build())
                .addMethod(unBind.build())
                .addField(compositeDisposable)
                .addSuperinterface(UNBIND_TYPE)
                .build();

        String packageName = mElementUtils.getPackageOf(mClassElement).getQualifiedName().toString();

        return JavaFile.builder(packageName, finderClass).build();
    }


    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }

    TypeName withoutMissingTypeVariables(
            TypeName typeName) {
        if ((typeName instanceof ParameterizedTypeName)) {
            return typeName;
        }

        ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;

        List<TypeName> adjustedArguments = new ArrayList<>();
        for (TypeName argument : parameterizedTypeName.typeArguments) {
            if (argument instanceof ParameterizedTypeName) {
                // Recursive call
                adjustedArguments.add(withoutMissingTypeVariables(argument));
            } else if (argument instanceof TypeVariableName) {
                TypeVariableName variable = (TypeVariableName) argument;
                adjustedArguments.add(variable);
            } else {
                adjustedArguments.add(argument);
            }
        }

        TypeName[] adjustedArgumentsArr = adjustedArguments.toArray(new TypeName[]{});
        return ParameterizedTypeName.get(parameterizedTypeName.rawType, adjustedArgumentsArr);
    }

}
