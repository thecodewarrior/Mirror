package dev.thecodewarrior.mirror.typeholders;

import dev.thecodewarrior.mirror.annotations.Annotation1;
import dev.thecodewarrior.mirror.annotations.AnnotationArg1;
import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

@SuppressWarnings("JavaDoc")
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
