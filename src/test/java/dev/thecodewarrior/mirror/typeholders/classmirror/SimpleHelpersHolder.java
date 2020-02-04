package dev.thecodewarrior.mirror.typeholders.classmirror;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;
import dev.thecodewarrior.mirror.testsupport.Object1;

public class SimpleHelpersHolder extends AnnotatedTypeHolder {
    @TypeHolder("Object1")
    void type_Object1(Object1 a) {}

    private Object1 innerAnonymous = new Object1() {
        public boolean hi = true;
    };

    public Object1 getInnerAnonymous() {
        return innerAnonymous;
    }

    public Object1 getAnonymous() {
        return new Object1() {
            public boolean hi = true;
        };
    }

    public Class getLocal() {
        class LocalClass {}
        return LocalClass.class;
    }

    public Runnable getLambda() {
        return () -> {};
    }

    // these have to be @ElementHolders because they are for modifier testing and can't all have the `public` modifier
    @ElementHolder("public static class") public class PublicStaticClass {}
    @ElementHolder("public class") public class PublicClass {}
    @ElementHolder("default class") class DefaultClass {}
    @ElementHolder("protected class") protected class ProtectedClass {}
    @ElementHolder("private class") private class PrivateClass {}
    @ElementHolder("abstract class") abstract class AbstractClass {}
    @ElementHolder("static class") static class StaticClass {}
    @ElementHolder("final class") final class FinalClass {}
    @ElementHolder("strictfp class") strictfp class StrictClass {}
    @ElementHolder("annotation class") @interface AnnotationInterface {}
    @ElementHolder("interface") interface Interface {}
}
