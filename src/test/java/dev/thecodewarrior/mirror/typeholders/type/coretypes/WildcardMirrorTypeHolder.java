package dev.thecodewarrior.mirror.typeholders.type.coretypes;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@SuppressWarnings("JavaDoc")
public class WildcardMirrorTypeHolder extends AnnotatedTypeHolder {
    @Target(ElementType.TYPE_USE)
    @interface A {}
    static class X {}

    void types(
            @TypeHolder("? extends X") Unwrap<? extends X> a,
            @TypeHolder("? super X") Unwrap<? super X> b,
            @TypeHolder("@A ? extends X") Unwrap<@A ? extends X> c,
            @TypeHolder("? extends @A X") Unwrap<? extends @A X> d,
            @TypeHolder("? extends X") Unwrap<? extends X> e,
            @TypeHolder("? extends @A X") Unwrap<? extends @A X> f
    ) {}
}
