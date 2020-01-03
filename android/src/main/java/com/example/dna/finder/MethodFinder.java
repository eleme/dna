package com.example.dna.finder;

import com.example.dna.model.MethodInfo;
import com.example.dna.model.ParameterInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class MethodFinder {
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_UN = Modifier.ABSTRACT | BRIDGE | SYNTHETIC;

    Class<?> invokeClass;

    String methodName;

    List<String> paramType;

    public MethodFinder(Class<?> invokeClass, String methodName, List<ParameterInfo> paramInfoList) {
        this.invokeClass = invokeClass;
        this.methodName = methodName;
        paramType = new ArrayList<>();
        if (paramInfoList != null && !paramInfoList.isEmpty()) {
            for (ParameterInfo info : paramInfoList) {
                paramType.add(info.getType());

            }
        }
    }

    /**
     * 不支持ParameterizedType参数
     */
    public MethodInfo getReflectMethodFromClazz() {
        List<Method> tempMethodList = new ArrayList<>();
        Method[] methods = invokeClass.getDeclaredMethods();
        MethodInfo methodInfo = null;
        for (Method method : methods) {
            int modifier = method.getModifiers();
            if ((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIERS_UN) == 0) {
                if (methodName.equals(method.getName())) {
                    tempMethodList.add(method);
                }
            }
        }
        Method method = getExactMethod(tempMethodList);
        if (method != null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            methodInfo = new MethodInfo(method, Arrays.asList(parameterTypes));
        }
        return methodInfo;
    }


    private Method getExactMethod(List<Method> methods) {
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        boolean isExcatMethod;
        for (Method method : methods) {
            Type[] types = method.getGenericParameterTypes();
            if (types.length != paramType.size()) {
                continue;
            }
            if (types.length == 0) {
                return method;
            }
            isExcatMethod = true;
            for (int i = 0; i < types.length; i++) {
                if (!(types[i] instanceof Class)) {
                    return null;
                }
                if (paramType.get(i) != null && !(paramType.get(i).equals(((Class) types[i]).getName()))) {
                    isExcatMethod = false;
                    break;
                }

            }
            if (isExcatMethod) {
                return method;
            }
        }
        return null;
    }

}
