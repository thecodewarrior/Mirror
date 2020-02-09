package dev.thecodewarrior.mirror.typeholders.member;

import dev.thecodewarrior.mirror.annotations.Annotation1;
import dev.thecodewarrior.mirror.annotations.AnnotationArg1;
import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;
import dev.thecodewarrior.mirror.testsupport.Exception1;
import dev.thecodewarrior.mirror.testsupport.Exception2;

@SuppressWarnings("JavaDoc")
public class ExecutableMirrorHolder extends AnnotatedTypeHolder {

    @ElementHolder("<init>()")
    public ExecutableMirrorHolder() { }

    @ElementHolder("void name()")
    public void name() {}

    @ElementHolder("void ()")
    public void noParameters() {}

    @ElementHolder("String ()")
    public String stringReturn() { throw e; }

    @ElementHolder("void (String, int)")
    public void stringReturn(@ElementHolder("> s") String s, @ElementHolder("> i") int i) {}

    @ElementHolder("void () throws")
    public void exceptions() throws Exception1, Exception2 { }

    @ElementHolder("<T> void (T)")
    public <T> void generic(@TypeHolder("> T") T t) {}

    @Annotation1
    @AnnotationArg1(arg = 1)
    public void methodAnnotations() {}
    public void noMethodAnnotations() {}

    @ElementHolder("void (@- @- String)")
    public void parameterAnnotations(@Annotation1 @AnnotationArg1(arg = 1) String s) {}
    @ElementHolder("void (_)")
    public void noParameterAnnotations(String s) {}
}
