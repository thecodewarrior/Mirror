package com.teamwizardry.mirror.typeholders;

import com.teamwizardry.mirror.testsupport.AnnotatedTypeHolder;
import com.teamwizardry.mirror.testsupport.Object1;
import com.teamwizardry.mirror.testsupport.Object1Sub;

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

    // these have to be @ElementHolders because they are for modifier testing and so they can't all have the `public` modifier
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

    public interface EmptyInterface {}
    public interface NonEmptyInterface {
        Object1 getValue();
    }
    public interface NonEmptyInterfaceOverride extends NonEmptyInterface {
        Object1 getValue();
    }
    public interface NonEmptyInterfaceShadow extends NonEmptyInterface {
        Object1Sub getValue();
    }

    public static abstract class NonEmptyInterfaceOverrideImpl implements NonEmptyInterfaceOverride {}
    public static abstract class NonEmptyInterfaceImplSuperOverrideImpl extends NonEmptyInterfaceOverrideImpl implements NonEmptyInterface {}

    public interface InterfaceWithStatics {
        int staticInterfaceField = 0;
        static void staticInterfaceMethod() {}
    }

    public static class ClassWithStatics {
        static int staticField = 0;
        static void staticMethod() {}
    }

    public static class ClassWithStaticsInSupertypes extends ClassWithStatics implements InterfaceWithStatics {

    }
}
