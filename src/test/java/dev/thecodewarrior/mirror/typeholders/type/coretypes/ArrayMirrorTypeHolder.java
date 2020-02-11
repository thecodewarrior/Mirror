package dev.thecodewarrior.mirror.typeholders.type.coretypes;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@SuppressWarnings("JavaDoc")
public class ArrayMirrorTypeHolder extends AnnotatedTypeHolder {
    @Target(ElementType.TYPE_USE)
    @interface A {}
    static class X {}
    static class Generic<T> {}

    void nonGenerics(
            @TypeHolder("X[]") X[] a,
            @TypeHolder("Generic<X>[]") Generic<X>[] b,
            @TypeHolder("@A X @A []") @A X @A [] c,
            @TypeHolder("Generic<@A X>[]") Generic<@A X>[] d
    ) {}
}
