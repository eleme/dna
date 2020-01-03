package com.example.dna.model;

import com.example.dna.util.GsonUtils;
import com.example.dna.exception.ArgsException;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * FileName: MethodTacker
 * Description:
 */

public class MethodTacker {
    MethodInfo info;

    public MethodTacker(MethodInfo info) {
        this.info = info;
    }

    public List<Object> getArgs(List<ParameterInfo> args) throws ArgsException {
        if (info == null) {
            return null;
        }
        List<Class<?>> reflectArgs = info.getArgs();
        if (reflectArgs == null || args == null) {
            return null;
        }
        if (reflectArgs.size() != args.size()) {
            throw new ArgsException("Args size error");
        }
        List<Object> argsElements = new ArrayList<>();
        for (int i = 0; i < reflectArgs.size(); i++) {
            argsElements.add(isPrimitiveClass(reflectArgs.get(i)) ? args.get(i).getContent() : GsonUtils.fromJson(args.get(i).getContent(), reflectArgs.get(i)));
        }

        return argsElements;
    }

    // todo check
    private static boolean isPrimitiveClass(Class clz) {
        if (clz.getName() == String.class.getName()) {
            return true;
        }
        return clz.isPrimitive() || isWrapClass(clz);
    }

    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
