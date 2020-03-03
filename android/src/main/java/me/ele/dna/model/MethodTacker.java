package me.ele.dna.model;

import me.ele.dna.util.GsonUtils;
import me.ele.dna.exception.ArgsException;

import java.util.ArrayList;
import java.util.List;

import me.ele.dna.exception.ArgsException;


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

    public List<Object> getArgs(List<ParameterInfo> args, Object owner) throws ArgsException {
        if (info == null) {
            return null;
        }
        List<Class<?>> reflectArgs = info.getArgs();
        if (reflectArgs == null || args == null) {
            return null;
        }
        if (info.isProxy() && !info.isConstruct() && reflectArgs.size() != args.size() + 1) {
            throw new ArgsException("proxy class Args size error");

        }
        if (!info.isProxy() && reflectArgs.size() != args.size()) {
            throw new ArgsException("Args size error");
        }
        List<Object> argsElements = new ArrayList<>();
        if (info.isProxy() && !info.isConstruct()) {
            argsElements.add(0, owner);
            for (int i = 1; i < reflectArgs.size(); i++) {
                argsElements.add(isString(reflectArgs.get(i)) ? args.get(i - 1).getContent() : GsonUtils.fromJson(args.get(i - 1).getContent(), reflectArgs.get(i)));
            }
        } else {
            for (int i = 0; i < reflectArgs.size(); i++) {
                argsElements.add(isString(reflectArgs.get(i)) ? args.get(i).getContent() : GsonUtils.fromJson(args.get(i).getContent(), reflectArgs.get(i)));
            }
        }

        return argsElements;
    }

    public static boolean isString(Class clz) {
        if (clz.getName().equals(String.class.getName())) {
            return true;
        }
        return false;
    }

}
