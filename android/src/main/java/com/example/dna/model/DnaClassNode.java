package com.example.dna.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: ZhiQing.Zhang
 **/
public class DnaClassNode {
    String className;
    String classNodeId;

    List<String> methodIdList = new ArrayList<>();

    public DnaClassNode(String classNodeId, String className) {
        this.className = className;
        this.classNodeId = classNodeId;
    }

    public void addMethodId(String methodId) {
        if (methodIdList.contains(methodId)) {
            return;
        }
        methodIdList.add(methodId);
    }

    public boolean hasMethod(String methodId) {
        return methodIdList.contains(methodId);
    }

    public String getClassNodeId() {
        return classNodeId;
    }

    public String getClassName() {
        return className;
    }
}
