package me.ele.dna.model;

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

    public boolean checkParam(List<ParameterInfo> parameterInfos) {
        if (args == null && parameterInfos == null) {
            return true;
        }
        if (args == null
                || parameterInfos == null
                || args.size() != parameterInfos.size()) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).getName() != parameterInfos.get(i).getType()) {
                return false;
            }
        }
        return true;
    }
}
