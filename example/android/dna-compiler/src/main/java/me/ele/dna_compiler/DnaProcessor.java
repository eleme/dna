package me.ele.dna_compiler;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import me.ele.dna_annotations.DnaConstants;
import me.ele.dna_annotations.DnaMethod;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;

public class DnaProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;

    /**
     * `
     * 初始化操作
     *
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(DnaMethod.class.getCanonicalName());
        return set;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<DnaClassFinder> classFinders = getMethodInfos(roundEnv);
        if (classFinders != null && !classFinders.isEmpty()) {
            for (DnaClassFinder finder : classFinders) {
                try {
                    finder.createJavaFile().writeTo(filer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private List<DnaClassFinder> getMethodInfos(RoundEnvironment roundEnv) {
        DnaPackageFinder finder = new DnaPackageFinder();
        String packageName;
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DnaMethod.class);
        BaseDnaElement dnaElement;
        for (Element element : annotatedElements) {
            if (!(element instanceof ExecutableElement) || (element.getKind() != METHOD && element.getKind() != CONSTRUCTOR)) {
                throw new IllegalStateException("DnaMethod annotation must be on a method.");
            }
            boolean isReturn;
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String methodName;
            if (!isAccessible(element)) {
                throw new IllegalStateException(" annotated method can't access.");
            }
            List<? extends VariableElement> parameters = executableElement.getParameters();
            TypeMirror methodParameterType;
            packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            List<ParamInfo> paramterType = new ArrayList<>();
            if (parameters != null && parameters.size() != 0) {
                for (VariableElement variableElement : parameters) {
                    methodParameterType = variableElement.asType();
                    if (methodParameterType instanceof TypeVariable) {
                        TypeVariable typeVariable = (TypeVariable) methodParameterType;
                        methodParameterType = typeVariable.getUpperBound();
                    }
                    paramterType.add(new ParamInfo(methodParameterType.toString(), TypeName.get(methodParameterType)));

                }
            }
            TypeMirror returnType = executableElement.getReturnType();
            String reutrnName = returnType.toString();
            if (element.getKind() == CONSTRUCTOR) {
                methodName = enclosingElement.getSimpleName().toString();
                dnaElement = new DnaConstructorInfo(paramterType, enclosingElement, DnaConstants.PROXYCONSTRUCTOR.concat(methodName), reutrnName);
            } else {
                methodName = executableElement.getSimpleName().toString();
                isReturn = returnType != null && returnType.getKind() != TypeKind.VOID;
                dnaElement = new DnaMethodInfo(paramterType, enclosingElement, methodName, isReturn, reutrnName);
            }

            finder.addMethodInfo(packageName, dnaElement);
        }
        return finder.getInfoList();

    }

    private boolean isAccessible(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        if (modifiers.contains(PRIVATE)) {
            return false;
        }

        if (enclosingElement.getKind() != CLASS || enclosingElement.getModifiers().contains(PRIVATE)) {
            return false;
        }

        return true;
    }

}
