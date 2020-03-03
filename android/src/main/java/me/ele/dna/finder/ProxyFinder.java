package me.ele.dna.finder;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.util.DnaUtils;
import me.ele.dna_annotations.DnaParamFieldList;

public class ProxyFinder extends BaseDnaFinder {

    public static ProxyFinder build(Class<?> invokeClass, String methodName, List<ParameterInfo> paramInfoList, boolean isConstruct) {
        List<String> paramType = new ArrayList<>();
        if (!DnaUtils.isEmpty(paramInfoList)) {
            for (ParameterInfo info : paramInfoList) {
                paramType.add(info.getType());

            }
        }
        return new ProxyFinder(invokeClass, methodName, paramType, isConstruct);
    }

    public ProxyFinder(Class<?> invokeClass, String methodName, List<String> paramType, boolean isConstruct) {
        super(invokeClass, methodName, paramType, isConstruct);
    }

    @Override
    protected MethodInfo getExactMethod(List<Method> methods) {
        if (DnaUtils.isEmpty(methods)) {
            return null;
        }
        boolean isExactMethod;
        String[] params;
        String returnType;
        for (Method method : methods) {
            DnaParamFieldList fieldList = method.getAnnotation(DnaParamFieldList.class);
            if (fieldList == null) {
                continue;
            }
            params = fieldList.params();
            returnType = fieldList.returnType();
            if (paramType == null || params.length != paramType.size()) {
                continue;
            }
            if (params.length == 0) {
                return createMethod(method, returnType);
            }
            isExactMethod = true;
            for (int i = 0; i < params.length; i++) {
                if (paramType.get(i) != null) {
                    if (paramType.get(i).equals(params[i])) {
                        isExactMethod = false;
                        break;
                    }
                }
            }
            if (isExactMethod) {
                return createMethod(method, returnType);
            }
        }
        return null;
    }


}
