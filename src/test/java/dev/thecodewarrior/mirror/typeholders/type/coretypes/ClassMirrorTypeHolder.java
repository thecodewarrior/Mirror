package dev.thecodewarrior.mirror.typeholders.type.coretypes;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@SuppressWarnings("ALL")
public class ClassMirrorTypeHolder extends AnnotatedTypeHolder {
    @Target(ElementType.TYPE_USE)
    @interface A {}
    static class X {}
    static class Y {}
    static class Generic<A> {
        class Inner {}
        class InnerGeneric<B> {}
    }

    void types(
            @TypeHolder("Generic") Generic a,
            @TypeHolder("Generic<X>") Generic<X> b,
            @TypeHolder("Generic.InnerGeneric") Generic.InnerGeneric c,
            @TypeHolder("Generic<X>.Inner") Generic<X>.Inner d,
            @TypeHolder("Generic<X>.InnerGeneric<Y>") Generic<X>.InnerGeneric<Y> e,
            @TypeHolder("@A Generic") @A Generic f,
            @TypeHolder("@A Generic<@A X>") @A Generic<@A X> g,
            @TypeHolder("@A Generic.InnerGeneric") @A Generic.InnerGeneric h,
            @TypeHolder("Generic<@A X>.@A Inner") Generic<@A X>.@A Inner i,
            @TypeHolder("Generic<@A X>.@A InnerGeneric<Y>") Generic<@A X>.@A InnerGeneric<Y> j
    ) {}
}
