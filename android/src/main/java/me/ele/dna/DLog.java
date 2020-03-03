package me.ele.dna;

import android.util.Log;

/**
 * Author: Zhiqing.Zhang
 * FileName: DLog
 * Description: dna log utls
 */

public class DLog {

    public static final String DNA = "dna";

    public static void e(String msg) {
        Log.e(DNA, msg);
    }

    public static void i(String msg) {
        Log.i(DNA, msg);
    }

}
