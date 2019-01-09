package com.teamwizardry.mirror.type;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1;
import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;
import com.teamwizardry.mirror.testsupport.GenericObject1;
import com.teamwizardry.mirror.testsupport.Object1;

public class TypeMirrorTestAnnotatedTypes extends AnnotatedTypeHolder {
    void getAnnotation_ofUnannotatedType_shouldReturnEmptyList_1(Object1 a) {}
    void getAnnotation_ofAnnotatedType_shouldReturnAnnotation_1(@TypeAnnotation1 Object1 a) {}
    void getAnnotation_ofMultiAnnotatedType_shouldReturnAnnotations_1(@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1 a) {}
    void getAnnotation_ofAnnotatedTypeParameter_shouldReturnAnnotations_1(GenericObject1<@TypeAnnotation1 Object1> a) {}
    void getAnnotation_ofAnnotatedArrayComponent_shouldReturnAnnotations_1(@TypeAnnotation1 Object[] a) {}
    void getAnnotation_ofArrayWithAnnotatedComponent_shouldReturnAnnotations_1(@TypeAnnotation1 Object[] a) {}
    void getAnnotation_ofAnnotatedArrayWithUnannotatedComponent_shouldReturnAnnotations_1(Object @TypeAnnotation1[] a) {}
    void getAnnotation_ofUnannotatedComponentOfAnnotatedArray_shouldReturnAnnotations_1(Object @TypeAnnotation1[] a) {}
}
