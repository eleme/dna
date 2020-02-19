package me.ele.dna.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Author: Zhiqing.Zhang
 * FileName: GsonUtils
 * Description:
 */

public class GsonUtils {
    private static final Gson GSON = new Gson();

    private GsonUtils() {
    }

    public static String toJson(Object obj) {
        return null == obj ? "" : GSON.toJson(obj);
    }

    public static Object fromJson(String json, Type classType) {
        return null == json ? null : GSON.fromJson(json, classType);
    }

}
