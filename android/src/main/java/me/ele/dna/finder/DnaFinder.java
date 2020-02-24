package me.ele.dna.finder;

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ele.dna.model.MethodInfo;

public abstract class DnaFinder {
    protected static final int BRIDGE = 0x40;
    protected static final int SYNTHETIC = 0x1000;
    protected static final int MODIFIERS_UN = Modifier.ABSTRACT | BRIDGE | SYNTHETIC;

    protected Class<?> invokeClass;

    protected String methodName;

    protected List<String> paramType;

    public DnaFinder(Class<?> invokeClass, String methodName, List<String> paramType) {
        this.invokeClass = invokeClass;
        this.methodName = methodName;
        this.paramType = paramType;
    }

    protected abstract ProxyFinder.MethodWithReturnType getExactMethod(List<Method> methods);

    public MethodInfo getReflectMethodFromClazz() {
        Log.i("ceshi", "methodName:" + methodName);
        List<Method> tempMethodList = new ArrayList<>();
        Method[] methods = invokeClass.getMethods();
        if (methods == null) {
            return null;
        }
        MethodInfo methodInfo = null;
        for (Method method : methods) {
            int modifier = method.getModifiers();
            if ((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIERS_UN) == 0) {
                String name = method.getName();
                Log.i("ceshi", "methodName:" + name);
                if (methodName.equals(method.getName())) {
                    tempMethodList.add(method);
                }
            }
        }
        Log.i("ceshi", "tempMethodList:" + tempMethodList.size());
        MethodWithReturnType method = getExactMethod(tempMethodList);
        if (method != null) {
            methodInfo = createMethod(method.getMethod(), method.getReturnType());
        }
        return methodInfo;
    }

    protected MethodInfo createMethod(Method method, String returnType) {
        MethodInfo methodInfo = null;
        if (method != null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            methodInfo = new MethodInfo(method, Arrays.asList(parameterTypes), returnType);
        }
        return methodInfo;
    }

    class MethodWithReturnType {
        Method method;
        String returnType;

        public MethodWithReturnType(Method method, String returnType) {
            this.method = method;
            this.returnType = returnType;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }
    }
}
