package dev.thecodewarrior.mirror.typeholders;

import dev.thecodewarrior.mirror.annotations.TypeAnnotation1;
import dev.thecodewarrior.mirror.annotations.TypeAnnotationArg1;
import dev.thecodewarrior.mirror.testsupport.*;

import java.util.List;

@SuppressWarnings("JavaDoc")
public class TypeMirrorHolder extends AnnotatedTypeHolder {
    @TypeHolder("Object1")
    void type_Object1(Object1 a) {}
    @TypeHolder("@TypeAnnotation1 Object1")
    void type_TypeAnnotation1_Object1(@TypeAnnotation1 Object1 a) {}
    //@TypeHolder("@KotlinTypeAnnotation1 Object1") // kotlin type annotations don't work in Java
    //void type_KotlinTypeAnnotation1_Object1(Unwrap<@KotlinTypeAnnotation1 Object1> a) {}
    @TypeHolder("@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1")
    void type_TypeAnnotation1_TypeAnnotationArg1_arg_1_Object1(@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1 a) {}
    @TypeHolder("GenericObject1<@TypeAnnotation1 Object1>")
    void type_GenericObject1_TypeAnnotation1_Object1(GenericObject1<@TypeAnnotation1 Object1> a) {}
    @TypeHolder("@TypeAnnotation1 Object[]")
    void type_TypeAnnotation1_Object_arr(@TypeAnnotation1 Object[] a) {}
    @TypeHolder("Object @TypeAnnotation1[]")
    void type_Object_TypeAnnotation1_arr(Object @TypeAnnotation1[] a) {}
    @TypeHolder("List<@TypeAnnotation1 ? extends Object1>")
    void type_List_TypeAnnotation1_Q_extends_Object1(List<@TypeAnnotation1 ? extends Object1> a) {}
    @TypeHolder("@TypeAnnotation1 GenericObject1<Object1>[]")
    void type_TypeAnnotation1_GenericObject1_Object1_arr(@TypeAnnotation1 GenericObject1<Object1>[] a) {}

    @TypeHolder("Object1Sub")
    void type_Object1Sub(Object1Sub a) {}
    @TypeHolder("? extends Object1Sub")
    void type_Q_extends_Object1Sub(Unwrap<? extends Object1Sub> a) {}
    @TypeHolder("? super Object1Sub")
    void type_Q_super_Object1Sub(Unwrap<? super Object1Sub> a) {}
    @TypeHolder("? extends @TypeAnnotation1 Object1")
    void type_Q_extends_TypeAnnotation1_Object1(Unwrap<? extends @TypeAnnotation1 Object1> a) {}
    @TypeHolder("? super @TypeAnnotation1 Object1")
    void type_Q_super_TypeAnnotation1_Object1(Unwrap<? super @TypeAnnotation1 Object1> a) {}

    @TypeHolder("T")
    <T> void type_T(T a) {}
    @TypeHolder("T[]; T")
    <T> void type_T_arr(T[] a, T b) {}
    @TypeHolder("T extends Object1")
    <T extends Object1> void type_T_extends_Object1(T a) {}
    @TypeHolder("T extends Interface2 & Interface1")
    <T extends Interface2 & Interface1> void type_T_extends_Interface2_amp_Interface1(T a) {}
    @TypeHolder("T extends @TypeAnnotation1 Object1")
    <T extends @TypeAnnotation1 Object1> void type_T_extends_TypeAnnotation1_Object1(T a) {}

    // for directly reflecting and reflecting through class equality check
    public static class DirectInClassEquality {
        @ElementHolder("void method()")
        void method() { }
        @ElementHolder("int field")
        int field = 0;
    }
}
