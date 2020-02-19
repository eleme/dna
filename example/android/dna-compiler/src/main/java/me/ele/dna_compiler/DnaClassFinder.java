package me.ele.dna_compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.PUBLIC;
import static me.ele.dna_compiler.DnaConstants.PROXYCLASS;

public class DnaClassFinder {
    private List<DnaMethodInfo> methodInfos = new ArrayList<>();
    private String packageName;

    public DnaClassFinder(String packageName) {
        this.packageName = packageName;
    }

    public void addMethodInfo(DnaMethodInfo info) {
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
        MethodSpec.Builder mehthodBuidler;
        List<TypeName> paramterType;
        String[] annotionList = null;

        for (DnaMethodInfo info : methodInfos) {
            mehthodBuidler = MethodSpec.methodBuilder("Dna_" + info.methodName + "_Proxy").
                    returns(info.isReturn() ? Object.class : Void.class);
            paramterType = info.getParamterType();
            String parament = "";
            String annotionSpc = "{";
            try {
                String methodPackage = info.getEnclosingElement().getQualifiedName().toString();
                mehthodBuidler.addParameter(ClassName.get(methodPackage.substring(0, methodPackage.lastIndexOf(".")), info.getEnclosingElement().getSimpleName().toString()), "owner")
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            int paramenterSize = 0;
            if (paramterType != null && paramterType.size() > 0) {
                annotionList = new String[paramterType.size()];
                paramenterSize = paramterType.size();
                for (int i = 0; i < paramterType.size(); i++) {
                    mehthodBuidler.addParameter(paramterType.get(i), "var" + i);
                    annotionList[i] = ;
                    if (i == paramterType.size() - 1) {
                        parament += "$N";
                        annotionSpc += "$N";
                    } else {
                        parament += "$N,";
                        annotionSpc += "$N,";

                    }
                }
            }
            parament += ")";
            annotionSpc += "}";
            String stateMement;
            if (info.isReturn()) {
                stateMement = "return $N.$N(" + parament;
            } else {
                stateMement = "$N.$N(" + parament;
            }
            Object[] objects = new Object[paramenterSize + 2];
            objects[0] = "owner";
            objects[1] = info.getMethodName();
            for (int i = 2; i < objects.length; i++) {
                objects[i] = "var" + (i - 2);
            }
            AnnotationSpec spec = AnnotationSpec.builder(DnaParamFieldList.class).addMember("params", annotionSpc, annotionList).build();
            mehthodBuidler.addStatement(stateMement, objects).addAnnotation(spec);
            classBuilder.addMethod(mehthodBuidler.build());
        }
        return JavaFile.builder(packageName, classBuilder.build())
                .addFileComment("Generated code from DNA Do not modify!")
                .build();

    }
}
