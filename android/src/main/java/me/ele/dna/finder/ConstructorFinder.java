package me.ele.dna.finder;

import me.ele.dna.model.ParameterInfo;
import me.ele.dna.util.DnaUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class ConstructorFinder {

    Class<?> owner;

    List<String> param;

    public ConstructorFinder(Class<?> owner, List<ParameterInfo> param) {
        this.owner = owner;
        this.param = new ArrayList<>();
        if (!DnaUtils.isEmpty(param)) {
            for (ParameterInfo info : param) {
                this.param.add(info.getType());
            }
        }
    }

    public Constructor<?> getConstructor() {
        if (owner == null) {
            return null;
        }
        Constructor<?>[] cons = owner.getConstructors();
        if (cons == null || cons.length == 0) {
            return null;
        }
        for (Constructor<?> con : cons) {
            if (checkConstructor(con, param)) {
                return con;
            }
        }
        return null;
    }

    private boolean checkConstructor(Constructor<?> con, List<String> param) {
        Class<?>[] parameterTypes = con.getParameterTypes();
        if (parameterTypes.length != param.size()) {
            return false;
        }
        if (parameterTypes.length == 0) {
            return true;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getName() != param.get(i) && param.get(i) != null) {
                return false;
            }
        }
        return true;
    }


}
