package com.example.dna;

import com.example.dna.exception.AbnormalConstructorException;
import com.example.dna.exception.AbnormalMethodException;
import com.example.dna.exception.ArgsException;
import com.example.dna.finder.ConstructorFinder;
import com.example.dna.finder.MethodFinder;
import com.example.dna.model.MethodInfo;
import com.example.dna.model.MethodTacker;
import com.example.dna.model.ParameterInfo;
import com.example.dna.util.DnaUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Zhiqing.Zhang
 * FileName: DnaClient
 * Description:
 */

public class DnaClient {
    private static Map<String, Class<?>> classCahe = new HashMap<>();

    private static Map<String, Class<?>> classConstructiorCahe = new HashMap<>();

    private static Map<String, Map<String, MethodInfo>> methodCache = new HashMap<>();

    private static DnaClient mInstance;

    public static DnaClient getClient() {
        if (mInstance == null) {
            synchronized (DnaClient.class) {
                if (mInstance == null) {
                    mInstance = new DnaClient();
                }
            }
        }
        return mInstance;
    }

    public DnaClient() {

    }

    /**
     * 调用构造方法
     *
     * @param className
     * @param param
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invokeConstructorMethod(String className, List<ParameterInfo> param)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, AbnormalConstructorException, InvocationTargetException {
        Class<?> constructorClass = classConstructiorCahe.get(className);
        if (constructorClass == null) {
            constructorClass = Class.forName(className);
            classConstructiorCahe.put(className, constructorClass);
        }

        if (DnaUtils.isEmpty(param)) {
            return constructorClass.newInstance();
        }

        return constructorOwner(constructorClass, param);

    }

    /**
     * 调用方法
     *
     * @param className
     * @param owner
     * @param methodName
     * @param param
     * @return
     * @throws ArgsException
     * @throws ClassNotFoundException
     * @throws AbnormalMethodException
     */
    public Object invokeMethod(String className, Object owner, String methodName, List<ParameterInfo> param) throws ArgsException, ClassNotFoundException, AbnormalMethodException {
        Map<String, MethodInfo> methods = methodCache.get(className);
        MethodInfo methodObj = null;
        if (methods == null) {
            methods = new HashMap<>();
        }
        if (!methods.isEmpty()) {
            methodObj = methods.get(methodName);
        }
        if (methodObj == null || !methodObj.checkParam(param)) {
            methodObj = getReflectMethod(className, methodName, param);
            methods.put(methodName, methodObj);
        }
        if (methodObj == null) {
            throw new AbnormalMethodException("method exception");
        }
        List<Object> args = new MethodTacker(methodObj).getArgs(param);
        return reflectMethod(methodObj.getMethod(), owner, args != null ? args.toArray() : null);
    }

    private MethodInfo getReflectMethod(String className, String methodName, List<ParameterInfo> param) throws ClassNotFoundException {
        Class<?> invokeClass = classCahe.get(className);
        if (invokeClass == null) {
            invokeClass = Class.forName(className);
            classCahe.put(className, invokeClass);
        }
        MethodFinder finder = new MethodFinder(invokeClass, methodName, param);
        return finder.getReflectMethodFromClazz();
    }


    /**
     * 构造函数
     *
     * @param param
     */
    private Object constructorOwner(Class<?> owner, List<ParameterInfo> param) throws InstantiationException, IllegalAccessException, AbnormalConstructorException,
            IllegalArgumentException, InvocationTargetException {
        Constructor<?>[] cons = owner.getConstructors();
        if (cons == null || cons.length == 0) {
            return null;
        }
        Object ownerInstance;
        ConstructorFinder constructorFinder = new ConstructorFinder(owner, param);
        Constructor<?> con = constructorFinder.getConstructor();
        if (con == null) {
            throw new AbnormalConstructorException("invalid constructor");
        } else {
            ownerInstance = con.newInstance(getParamContent(param).toArray());
        }
        return ownerInstance;

    }

    private Object reflectMethod(Method method, Object owner, Object... args) {
        try {
            return method.invoke(owner, args);
        } catch (InvocationTargetException e) {
            DLog.i(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
        return null;
    }

    private List<String> getParamContent(List<ParameterInfo> parameterInfos) {
        if (DnaUtils.isEmpty(parameterInfos)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (ParameterInfo info : parameterInfos) {
            list.add(info.getContent());
        }
        return list;
    }

}
