package me.ele.dna_compiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class DnaMethodInfo {

    List<TypeName> paramterType;
    String methodName;
    TypeElement enclosingElement;
    boolean isReturn;

    public DnaMethodInfo(List<TypeName> paramterType, String methodName, TypeElement enclosingElement, boolean isReturn) {
        this.paramterType = paramterType;
        this.methodName = methodName;
        this.enclosingElement = enclosingElement;
        this.isReturn = isReturn;
    }

    public List<TypeName> getParamterType() {
        return paramterType;
    }

    public String getMethodName() {
        return methodName;
    }

    public TypeElement getEnclosingElement() {
        return enclosingElement;
    }

    public String getClassName() {
        String qualifiedName = null;
        if (enclosingElement != null) {
            qualifiedName = enclosingElement.getQualifiedName().toString();
        }
        if (qualifiedName != null) {
            return qualifiedName.replace(".", "_");
        }
        return "";
    }

    public boolean isReturn() {
        return isReturn;
    }
}
