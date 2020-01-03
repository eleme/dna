package com.example.dna.model;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * FileName: MethodInfo
 * Description:
 */

public class MethodInfo {


    Method method;

    List<Class<?>> args;


    public MethodInfo(Method method, List<Class<?>> args) {
        this.method = method;
        this.args = args;
    }


    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<Class<?>> getArgs() {
        return args;
    }

    public void setArgs(List<Class<?>> args) {
        this.args = args;
    }
}
