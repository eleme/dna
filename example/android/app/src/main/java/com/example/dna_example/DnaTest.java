package com.example.dna_example;

public class DnaTest {
    String version;

    public DnaTest(String version) {
        this.version = version;
    }

    public DnaVersion getDnaVersion() {
        return new DnaVersion(version);
    }

    public String getString() {
        return "dna test";
    }
}
