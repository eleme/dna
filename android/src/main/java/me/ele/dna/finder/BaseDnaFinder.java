package me.ele.dna.finder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ele.dna.model.MethodInfo;

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

}
