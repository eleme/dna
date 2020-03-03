package me.ele.dna;

import android.text.TextUtils;

import me.ele.dna.exception.AbnormalConstructorException;
import me.ele.dna.exception.AbnormalMethodException;
import me.ele.dna.exception.ArgsException;
import me.ele.dna.finder.ConstructorFinder;
import me.ele.dna.finder.BaseDnaFinder;
import me.ele.dna.finder.MethodFinder;
import me.ele.dna.finder.ProxyFinder;
import me.ele.dna.model.MethodInfo;
import me.ele.dna.model.MethodTacker;
import me.ele.dna.model.ParameterInfo;
import me.ele.dna.model.ResultInfo;
import me.ele.dna.util.DnaUtils;
import me.ele.dna_annotations.DnaConstants;

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
    private IResultCallBack iResultCallBack;

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

    public void setiResultCallBack(IResultCallBack iResultCallBack) {
        this.iResultCallBack = iResultCallBack;
    }

    public IResultCallBack getiResultCallBack() {
        return iResultCallBack;
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
            throws ClassNotFoundException, ArgsException, AbnormalMethodException, IllegalAccessException, InstantiationException, AbnormalConstructorException, InvocationTargetException {
        boolean isProxy = false;
        if (!TextUtils.isEmpty(className) && (className.startsWith("android.") || className.startsWith("java."))) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                isProxy = true;
            }
        } else {
            isProxy = true;
        }
        if (isProxy) {
            String methodName = DnaConstants.PROXYCONSTRUCTOR.concat(className.substring(className.lastIndexOf(".") + 1));
            return invokeMethod(true, className, null, methodName, param);
        }
        return invokeRawConstructorMethod(className, param);
    }

    public ResultInfo invokeRawConstructorMethod(String className, List<ParameterInfo> param)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, AbnormalConstructorException, InvocationTargetException {
        Class<?> constructorClass = classConstructiorCahe.get(className);
        if (constructorClass == null) {
            constructorClass = Class.forName(className);
            classConstructiorCahe.put(className, constructorClass);
        }

        if (DnaUtils.isEmpty(param)) {
            return new ResultInfo(constructorClass.newInstance(), className);
        }

        return new ResultInfo(constructorOwner(constructorClass, param), className);
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
    public ResultInfo invokeMethod(boolean isConstruct, String className, Object owner, String methodName, List<ParameterInfo> param) throws ArgsException, ClassNotFoundException, AbnormalMethodException {
        Map<String, MethodInfo> methods = methodCache.get(className);
        MethodInfo methodImp = null;
        if (methods == null) {
            methods = new HashMap<>();
        }
        if (!methods.isEmpty()) {
            methodImp = methods.get(methodName);
        }
        if (methodImp == null || !methodImp.checkParam(param)) {
            methodImp = getReflectMethod(isConstruct, className, methodName, param);
            methods.put(methodName, methodImp);
        }
        if (methodImp == null) {
            throw new AbnormalMethodException("method exception");
        }
        MethodTacker methodTacker = new MethodTacker(methodImp);
        List<Object> args = methodTacker.getArgs(param, owner);
        Object returnResult = reflectMethod(methodImp.getMethod(), methodImp.isProxy() ? null : owner, args != null ? args.toArray() : null);
        return new ResultInfo(returnResult, methodImp.getReturnType());
    }

    private MethodInfo getReflectMethod(boolean isConstruct, String className, String methodName, List<ParameterInfo> param) throws ClassNotFoundException {
        boolean isProxy = false;
        Class<?> invokeClass = classCahe.get(className);
        if (invokeClass == null) {
            if (!TextUtils.isEmpty(className) && (className.startsWith("android.") || className.startsWith("java."))) {
                try {
                    invokeClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    isProxy = true;
                }
            } else {
                isProxy = true;
            }
            if (isProxy) {
                methodName = isConstruct ? methodName : className.substring(className.lastIndexOf(".") + 1) + "_" + methodName;
                className = className.substring(0, className.lastIndexOf(".") + 1) + DnaConstants.PROXYCLASS;
                invokeClass = Class.forName(className);
            }
            classCahe.put(className, invokeClass);
        }
        BaseDnaFinder finder = isProxy ? ProxyFinder.build(invokeClass, methodName, param, isConstruct) : MethodFinder.build(invokeClass, methodName, param, isConstruct);
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
