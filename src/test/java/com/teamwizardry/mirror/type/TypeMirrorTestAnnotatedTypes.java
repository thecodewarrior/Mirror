package com.teamwizardry.mirror.type;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1;
import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;
import com.teamwizardry.mirror.testsupport.GenericObject1;
import com.teamwizardry.mirror.testsupport.Object1;

import java.util.List;

public class TypeMirrorTestAnnotatedTypes extends AnnotatedTypeHolder {
    public TypeMirrorTestAnnotatedTypes() {
        methods.put("Object1", "type_Object1");
        methods.put("@TypeAnnotation1 Object1", "type_TypeAnnotation1_Object1");
        methods.put("@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1", "type_TypeAnnotation1_TypeAnnotationArg1_arg_1_Object1");
        methods.put("GenericObject1<@TypeAnnotation1 Object1>", "type_GenericObject1_TypeAnnotation1_Object1");
        methods.put("@TypeAnnotation1 Object[]", "type_TypeAnnotation1_Object_arr");
        methods.put("Object @TypeAnnotation1[]", "type_Object_TypeAnnotation1_arr");
        methods.put("List<@TypeAnnotation1 ? extends Object1>", "type_List_TypeAnnotation1_q_extends_Object1");
        methods.put("@TypeAnnotation1 GenericObject1<Object1>[]", "type_TypeAnnotation1_GenericObject1_Object1_arr");
    }

    void type_Object1(Object1 a) {}
    void type_TypeAnnotation1_Object1(@TypeAnnotation1 Object1 a) {}
    void type_TypeAnnotation1_TypeAnnotationArg1_arg_1_Object1(@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1 a) {}
    void type_GenericObject1_TypeAnnotation1_Object1(GenericObject1<@TypeAnnotation1 Object1> a) {}
    void type_TypeAnnotation1_Object_arr(@TypeAnnotation1 Object[] a) {}
    void type_Object_TypeAnnotation1_arr(Object @TypeAnnotation1[] a) {}
    void type_List_TypeAnnotation1_q_extends_Object1(List<@TypeAnnotation1 ? extends Object1> a) {}
    void type_TypeAnnotation1_GenericObject1_Object1_arr(@TypeAnnotation1 GenericObject1<Object1>[] a) {}
}
