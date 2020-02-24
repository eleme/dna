package me.ele.dna.finder;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.util.DnaUtils;
import me.ele.dna_compiler.DnaParamFieldList;

public class ProxyFinder extends DnaFinder {
    boolean isConstruct;

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
        super(invokeClass, methodName, paramType);
        this.isConstruct = isConstruct;
    }

    @Override
    protected MethodWithReturnType getExactMethod(List<Method> methods) {
        if (DnaUtils.isEmpty(methods)) {
            return null;
        }
        boolean isExactMethod;
        String[] params;
        String returnType;
        String owner;
        for (Method method : methods) {
            DnaParamFieldList fieldList = method.getAnnotation(DnaParamFieldList.class);
            if (fieldList == null) {
                continue;
            }
            params = fieldList.params();
            owner = fieldList.owner();
            returnType = fieldList.returnType();
           /* if (!isConstruct && !owner.equals(invokeClass.getClass().getName())) {
                continue;
            }*/
            if (paramType == null || params.length != paramType.size()) {
                continue;
            }
            if (params.length == 0) {
                return new MethodWithReturnType(method, returnType);
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
                return new MethodWithReturnType(method, returnType);
            }
        }
        return null;
    }


}
