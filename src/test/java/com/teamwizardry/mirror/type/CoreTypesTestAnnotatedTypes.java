package com.teamwizardry.mirror.type;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.testsupport.*;

public class CoreTypesTestAnnotatedTypes extends AnnotatedTypeHolder {
    @TypeHolder("T")
    <T> void type_T(T a) {}
    @TypeHolder("@TypeAnnotation1 T; T")
    <T> void type_TypeAnnotation1_T_T(@TypeAnnotation1 T a, T b) {}
    @TypeHolder("? extends Object1")
    void type_GenericObject1_G_extends_Object1(@Unwrap GenericObject1<? extends Object1> a) {}
    @TypeHolder("@TypeAnnotation1 ? extends Object1")
    void type_GenericObject1_TypeAnnotation1_G_extends_Object1(@Unwrap GenericObject1<@TypeAnnotation1 ? extends Object1> a) {}
    @TypeHolder("? extends @TypeAnnotation1 Object1")
    void type_GenericObject1_G_extends_TypeAnnotation1_Object1(@Unwrap GenericObject1<? extends @TypeAnnotation1 Object1> a) {}

    @TypeHolder("Object1[]")
    void type_Object1_arr(Object1[] a) {}
    @TypeHolder("GenericObject1<Object1>[]")
    void type_GenericObject1_Object1_arr(GenericObject1<Object1>[] a) {}
    @TypeHolder("@TypeAnnotation1 Object1 @TypeAnnotation1 []")
    void type_TypeAnnotation1_Object1_arr(@TypeAnnotation1 Object1 @TypeAnnotation1 [] a) {}
    @TypeHolder("GenericObject1<@TypeAnnotation1 Object1>[]")
    void type_GenericObject1_TypeAnnotation1_Object1_arr(GenericObject1<@TypeAnnotation1 Object1>[] a) {}

    @TypeHolder("GenericObject1")
    void type_Object1(GenericObject1 a) {}
    @TypeHolder("GenericObject1<Object1>")
    void type_GenericObject1_Object1(GenericObject1<Object1> a) {}
    @TypeHolder("OuterGenericClass1.InnerGenericClass")
    void type_OuterGenericClass_InnerGenericClass(OuterGenericClass1.InnerGenericClass a) {}
    @TypeHolder("OuterGenericClass1<Object1>.InnerClass")
    void type_OuterGenericClass_Object1_InnerClass(OuterGenericClass1<Object1>.InnerClass a) {}
    @TypeHolder("OuterGenericClass1<Object1>.InnerGenericClass<Object2>")
    void type_OuterGenericClass_Object1_InnerGenericClass_Object2(OuterGenericClass1<Object1>.InnerGenericClass<Object2> a) {}

    @TypeHolder("@TypeAnnotation1 GenericObject1")
    void type_TypeAnnotation1_Object1(@TypeAnnotation1 GenericObject1 a) {}
    @TypeHolder("@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1>")
    void type_TypeAnnotation1_GenericObject1_TypeAnnotation1_Object1(@TypeAnnotation1 GenericObject1<@TypeAnnotation1 Object1> a) {}
    @TypeHolder("@TypeAnnotation1 OuterGenericClass1.InnerGenericClass")
    void type_TypeAnnotation1_OuterGenericClass_InnerGenericClass(@TypeAnnotation1 OuterGenericClass1.InnerGenericClass a) {}
    @TypeHolder("OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass")
    void type_OuterGenericClass_TypeAnnotation1_Object1_TypeAnnotation1_InnerClass(OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerClass a) {}
    @TypeHolder("OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2>")
    void type_OuterGenericClass_TypeAnnotation1_Object1_TypeAnnotation1_InnerGenericClass_Object2(OuterGenericClass1<@TypeAnnotation1 Object1>.@TypeAnnotation1 InnerGenericClass<Object2> a) {}
}
