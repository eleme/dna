package me.ele.dna_example;


import me.ele.dna_compiler.DnaMethod;

public class DnaTest {

    @DnaMethod
    public DnaTest(DnaVersion a) {
    }

    @DnaMethod
    public DnaVersion getDnaVersion() {
        return new DnaVersion();
    }
}
