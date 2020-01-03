package com.example.dna.model;

import java.util.List;

/**
 * Author: Zhiqing.Zhang
 * FileName: DnaNode
 * Description:
 */
public class DnaNode {
    String returnId;

    List<Object> para;

    String methodName;


    public DnaNode(String returnId, List<Object> para, String methodName) {
        this.returnId = returnId;
        this.para = para;
        this.methodName = methodName;
    }

    public String getReturnId() {
        return returnId;
    }

    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }

    public List<Object> getPara() {
        return para;
    }

    public void setPara(List<Object> para) {
        this.para = para;
    }

    public String getMethodName() {
        return methodName;
    }

}
