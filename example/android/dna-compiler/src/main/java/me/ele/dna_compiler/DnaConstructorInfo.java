package me.ele.dna_compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import me.ele.dna_annotations.DnaParamFieldList;

public class DnaConstructorInfo extends BaseDnaElement {


    public DnaConstructorInfo(List<ParamInfo> paramterType, TypeElement enclosingElement, String methodName, String returnType) {
        super(paramterType, enclosingElement, methodName, returnType);
    }

    @Override
    public MethodSpec createMethod() {
        MethodSpec.Builder mehthodBuidler;
        mehthodBuidler = MethodSpec.methodBuilder(methodName).
                returns(TypeName.get(enclosingElement.asType())).addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        String parament = "";
        String annotionSpc = "{";
        String[] annotionList = new String[]{};
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
        String stateMement = "return new $N(" + parament;
        Object[] objects = new Object[paramenterSize + 1];
        objects[0] = enclosingElement.getQualifiedName();
        for (int i = 1; i < objects.length; i++) {
            objects[i] = "var" + (i - 1);
        }
        AnnotationSpec spec = AnnotationSpec.builder(DnaParamFieldList.class).addMember("params", annotionSpc, annotionList)
                .addMember("owner", "$S", "")
                .addMember("returnType", "$S", enclosingElement.getQualifiedName().toString())
                .build();
        mehthodBuidler.addStatement(stateMement, objects).addAnnotation(spec);
        return mehthodBuidler.build();
    }
}
