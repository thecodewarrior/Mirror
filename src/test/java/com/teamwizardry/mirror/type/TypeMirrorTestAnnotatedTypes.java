package com.teamwizardry.mirror.type;

import com.teamwizardry.mirror.annotations.TypeAnnotation1;
import com.teamwizardry.mirror.annotations.TypeAnnotationArg1;
import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;
import com.teamwizardry.mirror.testsupport.Object1;

public class TypeMirrorTestAnnotatedTypes extends AnnotatedTypeHolder {
    void unannotated(Object1 a) {}
    void annotated(@TypeAnnotation1 Object1 a) {}
    void multiAnnotated(@TypeAnnotation1 @TypeAnnotationArg1(arg = 1) Object1 a) {}
}
