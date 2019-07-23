package com.teamwizardry.mirror.typeholders;

import com.teamwizardry.mirror.annotations.Annotation1;
import com.teamwizardry.mirror.annotations.AnnotationArg1;
import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;

public class AnnotatedDeclarationHolder extends AnnotatedTypeHolder {

    class NonAnnotatedClass {}
    @Annotation1
    @AnnotationArg1(arg = 1)
    class AnnotatedClass {}

    public AnnotatedDeclarationHolder() { }
    @Annotation1
    @AnnotationArg1(arg = 1)
    public AnnotatedDeclarationHolder(int arg) { }

    public void nonAnnotatedMethod() {}
    @Annotation1
    @AnnotationArg1(arg = 1)
    public void annotatedMethod() {}

    public int nonAnnotatedField;
    @Annotation1
    @AnnotationArg1(arg = 1)
    public int annotatedField;

    public void parameterHolder(int nonAnnotatedParameter, @Annotation1 @AnnotationArg1(arg = 1) int annotatedParameter) {}
}
