package me.ele.dna.finder;

import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.util.DnaUtils;
import me.ele.dna_compiler.DnaParamFieldList;

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

    Object ownerObject;

    public MethodFinder(Object owner, Class<?> invokeClass, String methodName, List<ParameterInfo> paramInfoList) {
        this.ownerObject = owner;
        this.invokeClass = invokeClass;
        this.methodName = methodName;
        paramType = new ArrayList<>();
        if (!DnaUtils.isEmpty(paramInfoList)) {
            for (ParameterInfo info : paramInfoList) {
                paramType.add(info.getType());

            }
        }
    }


    public MethodInfo getReflectMethodFromClazz() {
        List<Method> tempMethodList = new ArrayList<>();
        Method[] methods = invokeClass.getMethods();
        if (methods == null) {
            return null;
        }
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

    public MethodInfo getReleaseReflectMethodFromClazz() {
        List<Method> tempMethodList = new ArrayList<>();
        Method[] methods = invokeClass.getMethods();
        if (methods == null) {
            return null;
        }
        MethodInfo methodInfo = null;
        for (Method method : methods) {
            int modifier = method.getModifiers();
            if ((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIERS_UN) == 0) {
                if (methodName.equals(method.getName())) {
                    tempMethodList.add(method);
                }
            }
        }
        Method method = getExactReleaseMethod(tempMethodList);
        if (method != null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            List<Class<?>> params = new ArrayList<>();
            params.add(ownerObject.getClass());
            params.addAll(Arrays.asList(parameterTypes));
            methodInfo = new MethodInfo(method, params);
        }
        return methodInfo;
    }

    private Method getExactReleaseMethod(List<Method> methods) {
        if (DnaUtils.isEmpty(methods)) {
            return null;
        }
        boolean isExactMethod;
        for (Method method : methods) {
            DnaParamFieldList fieldList = method.getAnnotation(DnaParamFieldList.class);
            if (fieldList == null) {
                continue;
            }
            String[] params = fieldList.params();
            String owner = fieldList.owner();
            if (!owner.equals(ownerObject.getClass().getName())) {
                continue;
            }
            if (paramType == null || params.length != paramType.size()) {
                continue;
            }
            if (params.length == 0) {
                return method;
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
                return method;
            }
        }
        return null;
    }


    private Method getExactMethod(List<Method> methods) {
        if (DnaUtils.isEmpty(methods)) {
            return null;
        }
        boolean isExactMethod;
        for (Method method : methods) {
            Type[] types = method.getGenericParameterTypes();
            if (types == null && paramType == null) {
                return method;
            }
            if (types == null || paramType == null || types.length != paramType.size()) {
                continue;
            }
            if (types.length == 0) {
                return method;
            }
            isExactMethod = true;
            for (int i = 0; i < types.length; i++) {
                if (!(types[i] instanceof Class)) {
                    return null;
                }
                if (paramType.get(i) != null) {
                    String typeName = ((Class) types[i]).isPrimitive()
                            ? wrapper(((Class) types[i]).getName())
                            : ((Class) types[i]).getName();
                    if (!(paramType.get(i).equals(typeName))) {
                        isExactMethod = false;
                        break;
                    }
                }
            }
            if (isExactMethod) {
                return method;
            }
        }
        return null;
    }

    /**
     * java.lang.Boolean#TYPE
     * java.lang.Character#TYPE
     * java.lang.Byte#TYPE
     * java.lang.Short#TYPE
     * java.lang.Integer#TYPE
     * java.lang.Long#TYPE
     * java.lang.Float#TYPE
     * java.lang.Double#TYPE
     * java.lang.Void#TYPE
     *
     * @param name
     * @return
     */
    private String wrapper(String name) {
        switch (name) {
            case "boolean":
                return Boolean.class.getName();
            case "char":
                return Character.class.getName();
            case "byte":
                return Byte.class.getName();
            case "short":
                return Short.class.getName();
            case "int":
                return Integer.class.getName();
            case "long":
                return Long.class.getName();
            case "float":
                return Float.class.getName();
            case "double":
                return Double.class.getName();
            case "void":
                return Void.class.getName();
            default:
                return null;
        }
    }

}
