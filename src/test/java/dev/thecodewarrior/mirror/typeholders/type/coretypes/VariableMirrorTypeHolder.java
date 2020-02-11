package dev.thecodewarrior.mirror.typeholders.type.coretypes;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@SuppressWarnings("JavaDoc")
public class VariableMirrorTypeHolder extends AnnotatedTypeHolder {
    @Target(ElementType.TYPE_USE)
    @interface A {}

    <T> void generics(
            @TypeHolder("T") T a,
            @TypeHolder("@A T") @A T b
    ) {}
}
