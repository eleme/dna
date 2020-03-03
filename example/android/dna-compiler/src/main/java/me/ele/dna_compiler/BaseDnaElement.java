package me.ele.dna_compiler;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.TypeElement;

public abstract class BaseDnaElement {
    protected List<ParamInfo> paramterType;
    protected TypeElement enclosingElement;
    protected String methodName;
    protected String returnType;

    public BaseDnaElement(List<ParamInfo> paramterType, TypeElement enclosingElement, String methodName, String returnType) {
        this.paramterType = paramterType;
        this.enclosingElement = enclosingElement;
        this.methodName = methodName;
        this.returnType = returnType;
    }

    public List<ParamInfo> getParamterType() {
        return paramterType;
    }

    public TypeElement getEnclosingElement() {
        return enclosingElement;
    }

    public String getMethodName() {
        return methodName;
    }

    public abstract MethodSpec createMethod();



}
