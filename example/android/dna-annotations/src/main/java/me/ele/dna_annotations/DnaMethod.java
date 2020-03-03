package me.ele.dna_annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({METHOD, CONSTRUCTOR})
@Retention(CLASS)
public @interface DnaMethod {
}
