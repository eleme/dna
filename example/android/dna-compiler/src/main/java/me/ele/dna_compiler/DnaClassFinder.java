package me.ele.dna_compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;


import static javax.lang.model.element.Modifier.PUBLIC;
import static me.ele.dna_compiler.DnaConstants.PROXYCLASS;

public class DnaClassFinder {
    private List<DnaElement> methodInfos = new ArrayList<>();

    private String packageName;

    public DnaClassFinder(String packageName) {
        this.packageName = packageName;
    }

    public void addMethodInfo(DnaElement info) {
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
        List<ParamInfo> paramterType;
        String[] annotionList = new String[]{};
        for (DnaMethodInfo info : methodInfos) {
            mehthodBuidler = MethodSpec.methodBuilder(info.getClassName() + "_" + info.getMethodName()).
                    returns(info.isReturn() ? Object.class : Void.class);
            paramterType = info.getParamterType();
            String parament = "";
            String annotionSpc = "{";
            try {
                mehthodBuidler.addParameter(
                        TypeName.get(info.getEnclosingElement().asType()), "owner");
            } catch (Exception e) {
                e.printStackTrace();
            }
            int paramenterSize = 0;
            if (paramterType != null && paramterType.size() > 0) {
                annotionList = new String[paramterType.size()];
                paramenterSize = paramterType.size();
                for (int i = 0; i < paramterType.size(); i++) {
                    mehthodBuidler.addParameter(paramterType.get(i).getTypeName(), "var" + i);
                    annotionList[i] = paramterType.get(i).getClassName();
                    if (i == paramterType.size() - 1) {
                        parament += "$N";
                        annotionSpc += "$S";
                    } else {
                        parament += "$N,";
                        annotionSpc += "$S,";

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
            AnnotationSpec spec = AnnotationSpec.builder(DnaParamFieldList.class).addMember("params", annotionSpc, annotionList)
                    .addMember("owner", "$S", info.getEnclosingElement().getQualifiedName().toString())
                    .build();
            mehthodBuidler.addStatement(stateMement, objects).addAnnotation(spec);
            classBuilder.addMethod(mehthodBuidler.build());
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .addFileComment("Generated code from DNA Do not modify!")
                .build();

    }
}
