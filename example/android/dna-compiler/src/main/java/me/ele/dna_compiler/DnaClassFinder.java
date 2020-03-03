package me.ele.dna_compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;


import static javax.lang.model.element.Modifier.PUBLIC;
import static me.ele.dna_annotations.DnaConstants.PROXYCLASS;

public class DnaClassFinder {
    private List<BaseDnaElement> methodInfos = new ArrayList<>();

    private String packageName;

    public DnaClassFinder(String packageName) {
        this.packageName = packageName;
    }

    public void addMethodInfo(BaseDnaElement info) {
        if (methodInfos == null) {
            methodInfos = new ArrayList<>();
        }
        methodInfos.add(info);
    }

    public String getPackageName() {
        return packageName;
    }

    public JavaFile createJavaFile() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(PROXYCLASS).addModifiers(PUBLIC);
        for (BaseDnaElement info : methodInfos) {
            classBuilder.addMethod(info.createMethod());
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .addFileComment("Generated code from DNA Do not modify!")
                .build();
    }
}
