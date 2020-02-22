package me.ele.dna_compiler;

import com.squareup.javapoet.MethodSpec;
import java.util.List;
import javax.lang.model.element.TypeElement;

public class DnaMethodInfo extends DnaElement {

    private boolean isReturn;

    public DnaMethodInfo(List<ParamInfo> paramterType, TypeElement enclosingElement, String methodName, boolean isReturn) {
        super(paramterType, enclosingElement, methodName);
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
        return null;
    }
}
