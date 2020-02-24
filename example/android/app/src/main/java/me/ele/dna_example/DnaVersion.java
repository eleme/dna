package me.ele.dna_example;


import me.ele.dna_compiler.DnaMethod;

public class DnaVersion {

    @DnaMethod
    public DnaVersion() {
    }

    @DnaMethod
    public String getVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

}
