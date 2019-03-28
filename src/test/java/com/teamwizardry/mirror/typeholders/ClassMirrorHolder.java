package com.teamwizardry.mirror.typeholders;

import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;
import com.teamwizardry.mirror.testsupport.Object1;

public class ClassMirrorHolder extends AnnotatedTypeHolder {
    @TypeHolder("Object1")
    void type_Object1(Object1 a) {}

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
