package me.ele.dna_compiler;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class DnaConstructorInfo extends DnaElement {

    public DnaConstructorInfo(List<ParamInfo> paramterType, TypeElement enclosingElement, String methodName) {
        super(paramterType, enclosingElement, methodName);
    }

    @Override
    public MethodSpec createMethod() {
        return null;
    }
}
