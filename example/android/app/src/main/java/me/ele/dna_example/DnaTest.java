package me.ele.dna_example;


import me.ele.dna_compiler.DnaMethod;

public class DnaTest {

    public DnaTest() {
    }

    @DnaMethod
    public DnaVersion getDnaVersion() {
        return new DnaVersion();
    }
}
