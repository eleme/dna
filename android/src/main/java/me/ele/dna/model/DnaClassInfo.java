package me.ele.dna.model;

import java.util.List;

/**
 * Author: ZhiQing.Zhang
 **/
public class DnaClassInfo {
    String className;

    boolean isConstrcut;

    List<Object> args;

    public DnaClassInfo(String className, boolean isConstrcut) {
        this.className = className;
        this.isConstrcut = isConstrcut;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isConstrcut() {
        return isConstrcut;
    }

    public void setConstrcut(boolean constrcut) {
        isConstrcut = constrcut;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }
}
