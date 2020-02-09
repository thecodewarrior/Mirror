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

    @ElementHolder("<init>(String)")
    public ExecutableMirrorHolder(String arg) { }

    @ElementHolder("<init>(String...)")
    public ExecutableMirrorHolder(String... args) { }

    public static class InnerConstructor {
        @ElementHolder("InnerConstructor()")
        public InnerConstructor() {}
    }

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

    @ElementHolder("void (String...)")
    public void varargMethod(String... args) { }

    @Annotation1
    @AnnotationArg1(arg = 1)
    public void methodAnnotations() {}
    public void noMethodAnnotations() {}

    @ElementHolder("void (@- @- String)")
    public void parameterAnnotations(@Annotation1 @AnnotationArg1(arg = 1) String s) {}
    @ElementHolder("void (_)")
    public void noParameterAnnotations(String s) {}


    // synthetic methods
    public void privateAccessor() {
        SyntheticHolder nested = new SyntheticHolder();
        String needsAccess = nested.needsAccess;
        String noSyntheticNeeded = nested.noSyntheticNeeded;
    }

    @ElementHolder("SyntheticHolder")
    private static class SyntheticHolder {
        private String needsAccess = "Shhh. Be vewy vewy quiet,";
        public String noSyntheticNeeded = "I'm hunting wabbits";

        public void nonSynthetic() {

        }
    }

    public abstract static class Abstract {
        @ElementHolder("public <init>()")
        public Abstract(byte uniqueSignature) {}
        @ElementHolder("default <init>()")
        Abstract(short uniqueSignature) {}
        @ElementHolder("protected <init>()")
        protected Abstract(int uniqueSignature) {}
        @ElementHolder("private <init>()")
        private Abstract(long uniqueSignature) {}

        @ElementHolder("public void ()")
        public void publicMethod() {}
        @ElementHolder("default void ()")
        void defaultMethod() {}
        @ElementHolder("protected void ()")
        protected void protectedMethod() {}
        @ElementHolder("private void ()")
        private void privateMethod() {}
        @ElementHolder("abstract void ()")
        abstract void abstractMethod();
        @ElementHolder("static void ()")
        static void staticMethod() {}
        @ElementHolder("final void ()")
        final void finalMethod() {}
        @ElementHolder("strictfp void ()")
        strictfp void strictfpMethod() {}
    }

}
