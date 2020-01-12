package com.example.dna_example;


public class DnaVersion {
    String version;

    public DnaVersion(String verssion) {
        this.version = verssion;
    }

    public String getVersion() {
        return version + android.os.Build.VERSION.RELEASE;
    }

}
