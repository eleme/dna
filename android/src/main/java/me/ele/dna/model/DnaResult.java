package me.ele.dna.model;

public class DnaResult {

    Object content;

    String varId;

    public DnaResult(Object content, String varId) {
        this.content = content;
        this.varId = varId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }
}
