package me.ele.dna_example;


import me.ele.dna_compiler.DnaMethod;

public class DnaVersion {

    public DnaVersion() {
    }

    @DnaMethod
    public String getVersion(DnaTest test) {
        return android.os.Build.VERSION.RELEASE;
    }

    public String getVersion2() {
        return android.os.Build.VERSION.RELEASE;
    }


}
