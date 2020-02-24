package me.ele.dna.model;

public class ResultInfo {
    Object object;

    String returnType;

    public ResultInfo(Object object, String returnType) {
        this.object = object;
        this.returnType = returnType;
    }

    public Object getObject() {
        return object;
    }

    public String getReturnType() {
        return returnType;
    }
}
