package me.ele.dna_compiler;

import com.squareup.javapoet.TypeName;

public class ParamInfo {
    String className;
    TypeName typeName;

    public ParamInfo(String className, TypeName typeName) {
        this.className = className;
        this.typeName = typeName;
    }

    public String getClassName() {
        return className;
    }

    public TypeName getTypeName() {
        return typeName;
    }
}
