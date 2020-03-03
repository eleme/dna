package me.ele.dna_compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import me.ele.dna_annotations.DnaParamFieldList;

public class DnaMethodInfo extends BaseDnaElement {

    private boolean isReturn;

    public DnaMethodInfo(List<ParamInfo> paramterType, TypeElement enclosingElement, String methodName, boolean isReturn, String returnType) {
        super(paramterType, enclosingElement, methodName, returnType);
        this.isReturn = isReturn;
    }

    public String getClassName() {
        if (enclosingElement != null) {
            return enclosingElement.getSimpleName().toString();

        }
        return "";
    }

    public boolean isReturn() {
        return isReturn;
    }

    @Override
    public MethodSpec createMethod() {
        MethodSpec.Builder mehthodBuidler;
        mehthodBuidler = MethodSpec.methodBuilder(getClassName() + "_" + methodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                returns(isReturn ? Object.class : Void.class);
        String parament = "";
        String annotionSpc = "{";
        String[] annotionList = new String[]{};
        mehthodBuidler.addParameter(
                TypeName.get(enclosingElement.asType()), "owner");
        int paramenterSize = 0;
        if (paramterType != null && paramterType.size() > 0) {
            paramenterSize = paramterType.size();
            annotionList = new String[paramenterSize];
            for (int i = 0; i < paramenterSize; i++) {
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
        if (isReturn) {
            stateMement = "return $N.$N(" + parament;
        } else {
            stateMement = "$N.$N(" + parament;
        }
        Object[] objects = new Object[paramenterSize + 2];
        objects[0] = "owner";
        objects[1] = methodName;
        for (int i = 2; i < objects.length; i++) {
            objects[i] = "var" + (i - 2);
        }
        AnnotationSpec spec = AnnotationSpec.builder(DnaParamFieldList.class).addMember("params", annotionSpc, annotionList)
                .addMember("owner", "$S", enclosingElement.getQualifiedName().toString())
                .addMember("returnType", "$S", returnType)
                .build();
        mehthodBuidler.addStatement(stateMement, objects).addAnnotation(spec);
        return mehthodBuidler.build();
    }
}
