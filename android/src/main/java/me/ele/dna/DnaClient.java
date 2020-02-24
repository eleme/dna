package me.ele.dna;

import android.util.Log;

import me.ele.dna.exception.AbnormalConstructorException;
import me.ele.dna.exception.AbnormalMethodException;
import me.ele.dna.exception.ArgsException;
import me.ele.dna.finder.ConstructorFinder;
import me.ele.dna.finder.DnaFinder;
import me.ele.dna.finder.MethodFinder;
import me.ele.dna.finder.ProxyFinder;
import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.MethodTacker;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.model.ResultInfo;
import me.ele.dna.util.DnaUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ele.dna_compiler.DnaConstants;

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
     * @throws IllegalAccessException
     */
    public ResultInfo invokeConstructorMethod(String className, List<ParameterInfo> param)
            throws ClassNotFoundException, ArgsException, AbnormalMethodException {
        return invokeReleaseConstructorMethod(className, param);
    }

    public ResultInfo invokeReleaseConstructorMethod(String className, List<ParameterInfo> param) throws ArgsException, ClassNotFoundException, AbnormalMethodException {
        String methodName = DnaConstants.PROXYCONSTRUCTOR.concat(className.substring(className.lastIndexOf(".") + 1));
        return invokeConstructMethod(className, methodName, param);
    }

    public Object invokeCommonConstructorMethod(String className, List<ParameterInfo> param)
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
    public ResultInfo invokeMethod(String className, Object owner, String methodName, List<ParameterInfo> param) throws ArgsException, ClassNotFoundException, AbnormalMethodException {
        Map<String, MethodInfo> methods = methodCache.get(className);
        MethodInfo methodObj = null;
        if (methods == null) {
            methods = new HashMap<>();
        }
        if (!methods.isEmpty()) {
            methodObj = methods.get(methodName);
        }
        if (methodObj == null || !methodObj.checkParam(param)) {
            methodObj = getMethod(false, className, methodName, param);
            methods.put(methodName, methodObj);
        }
        if (methodObj == null) {
            throw new AbnormalMethodException("method exception");
        }
        MethodTacker methodTacker = new MethodTacker(methodObj);
        List<Object> args = isRelease() ? methodTacker.getReleaseArgs(param, owner, false) :
                methodTacker.getArgs(param);
        Object returnResult = reflectMethod(methodObj.getMethod(), isRelease() ? null : owner, args != null ? args.toArray() : null);
        return new ResultInfo(returnResult, methodObj.getReturnType());
    }

    public ResultInfo invokeConstructMethod(String className, String methodName, List<ParameterInfo> param) throws ArgsException, ClassNotFoundException, AbnormalMethodException {
        Map<String, MethodInfo> methods = methodCache.get(className);
        MethodInfo methodObj = null;
        if (methods == null) {
            methods = new HashMap<>();
        }
        if (!methods.isEmpty()) {
            methodObj = methods.get(methodName);
        }
        if (methodObj == null || !methodObj.checkParam(param)) {
            methodObj = getMethod(true, className, methodName, param);
            methods.put(methodName, methodObj);
        }
        if (methodObj == null) {
            throw new AbnormalMethodException("method exception");
        }
        MethodTacker methodTacker = new MethodTacker(methodObj);
        List<Object> args = methodTacker.getArgs(param);
        Object returnResult = reflectMethod(methodObj.getMethod(), null, args != null ? args.toArray() : null);
        return new ResultInfo(returnResult, methodObj.getReturnType());
    }

    private boolean isRelease() {
        return true;
    }

    private MethodInfo getMethod(boolean isConstruct, String className, String methodName, List<ParameterInfo> param) throws ClassNotFoundException {
        return isRelease() ? getReleaseReflectMethod(isConstruct, className, methodName, param) : getReflectMethod(className, methodName, param);
    }

    private MethodInfo getReflectMethod(String className, String methodName, List<ParameterInfo> param) throws ClassNotFoundException {
        Class<?> invokeClass = classCahe.get(className);
        if (invokeClass == null) {
            invokeClass = Class.forName(className);
            classCahe.put(className, invokeClass);
        }
        MethodFinder finder = MethodFinder.build(invokeClass, methodName, param);
        return finder.getReflectMethodFromClazz();
    }

    private MethodInfo getReleaseReflectMethod(boolean isConstruct, String className, String methodName, List<ParameterInfo> param) throws ClassNotFoundException {
        Log.i("ceshi", "getReleaseReflectMethod:" + className);
        methodName = isConstruct ? methodName : className.substring(className.lastIndexOf(".") + 1) + "_" + methodName;
        className = className.substring(0, className.lastIndexOf(".") + 1) + DnaConstants.PROXYCLASS;
        Class<?> invokeClass = classCahe.get(className);
        if (invokeClass == null) {
            invokeClass = Class.forName(className);
            classCahe.put(className, invokeClass);
        }
        DnaFinder finder = ProxyFinder.build(invokeClass, methodName, param, isConstruct);
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
            return method.invoke(owner, (args == null || args.length == 0) ? null : args);
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
