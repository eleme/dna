package me.ele.dna.model;

/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class ParameterInfo {
    String content;

    String type;

    public ParameterInfo(String content, String type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
