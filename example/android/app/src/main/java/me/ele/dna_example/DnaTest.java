package me.ele.dna_example;


import android.util.Log;

import me.ele.dna_annotations.DnaMethod;


public class DnaTest {

    @DnaMethod
    public DnaTest() {
    }

    @DnaMethod
    public DnaVersion getDnaVersion() {
        return new DnaVersion();
    }

    public void logString() {
        Log.i("ceshi", "logString");
    }
}
