package com.example.dna.util;

import com.example.dna.model.ParameterInfo;

import java.util.ArrayList;
import java.util.List;
/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class DnaUtils {

    public static List<String> getParamContent(List<ParameterInfo> parameterInfos) {
        if (parameterInfos == null || parameterInfos.isEmpty()) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (ParameterInfo info : parameterInfos) {
            list.add(info.getContent());
        }
        return list;
    }
}
