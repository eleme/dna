package me.ele.dna.util;


import java.util.Collection;
/**
 * Author: Zhiqing.Zhang
 * Description:
 */
public class DnaUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }


    public static boolean isPrimitiveClass(Class clz) {
        if (clz.getName() == String.class.getName()) {
            return true;
        }
        return clz.isPrimitive() || isWrapClass(clz);
    }

    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
