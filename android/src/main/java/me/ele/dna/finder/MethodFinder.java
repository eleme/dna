package me.ele.dna.finder;

import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.util.DnaUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class MethodFinder extends BaseDnaFinder {

    public static MethodFinder build(Class<?> invokeClass, String methodName, List<ParameterInfo> paramInfoList, boolean isConstruct) {
        List<String> paramType = new ArrayList<>();
        if (!DnaUtils.isEmpty(paramInfoList)) {
            for (ParameterInfo info : paramInfoList) {
                paramType.add(info.getType());

            }
        }
        return new MethodFinder(invokeClass, methodName, paramType, isConstruct);
    }

    public MethodFinder(Class<?> invokeClass, String methodName, List<String> paramType, boolean isConstruct) {
        super(invokeClass, methodName, paramType, isConstruct);
    }

    @Override
    protected MethodInfo getExactMethod(List<Method> methods) {
        if (DnaUtils.isEmpty(methods)) {
            return null;
        }
        boolean isExactMethod;
        Method curMethod = null;
        for (Method method : methods) {
            Type[] types = method.getGenericParameterTypes();
            if (types == null && paramType == null) {
                curMethod = method;
                break;
            }
            if (types == null || paramType == null || types.length != paramType.size()) {
                continue;
            }
            if (types.length == 0) {
                curMethod = method;
                break;
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
                curMethod = method;
                break;
            }
        }
        return curMethod != null ? createMethod(curMethod, curMethod.getReturnType().getName()) : null;
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
