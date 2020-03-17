package me.ele.dna.finder;

import android.text.TextUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ele.dna.model.MethodInfo;
import me.ele.dna.util.DnaUtils;

public abstract class BaseDnaFinder {
    protected static final int BRIDGE = 0x40;
    protected static final int SYNTHETIC = 0x1000;
    protected static final int MODIFIERS_UN = Modifier.ABSTRACT | BRIDGE | SYNTHETIC;

    protected Class<?> invokeClass;

    protected String methodName;

    protected List<String> paramType;

    protected boolean isConstruct;

    public BaseDnaFinder(Class<?> invokeClass, String methodName, List<String> paramType, boolean isConstruct) {
        this.invokeClass = invokeClass;
        this.methodName = methodName;
        this.paramType = paramType;
        this.isConstruct = isConstruct;
    }

    protected abstract MethodInfo getExactMethod(List<Method> methods);

    public MethodInfo getReflectMethodFromClazz() {
        List<Method> tempMethodList = new ArrayList<>();
        Method[] methods = invokeClass.getMethods();
        if (methods == null) {
            return null;
        }
        for (Method method : methods) {
            int modifier = method.getModifiers();
            if ((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIERS_UN) == 0) {
                if (methodName.equals(method.getName())) {
                    tempMethodList.add(method);
                }
            }
        }
        return getExactMethod(tempMethodList);
    }

    protected MethodInfo createMethod(Method method, String returnType) {
        MethodInfo methodInfo = null;
        if (method != null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            methodInfo = new MethodInfo(method, Arrays.asList(parameterTypes), returnType, this instanceof ProxyFinder, isConstruct);
        }
        return methodInfo;
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
    protected String wrapper(String name) {
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

    protected boolean isEqualType(String dartType, String javaType) {
        if (TextUtils.isEmpty(dartType) && TextUtils.isEmpty(javaType)) {
            return true;
        }

        if (TextUtils.isEmpty(dartType) || TextUtils.isEmpty(javaType)) {
            return false;
        }
        if (javaType.equals(Float.class.getName()) && dartType.equals(Double.class.getName())) {
            return true;
        }
        if (javaType.equals(Long.class.getName()) && dartType.equals(Integer.class.getName())) {
            return true;
        }
        if (javaType.equals(Short.class.getName()) && dartType.equals(Integer.class.getName())) {
            return true;
        }
        return javaType.equals(dartType);
    }

}
