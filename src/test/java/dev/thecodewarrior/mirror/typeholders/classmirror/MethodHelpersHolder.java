package dev.thecodewarrior.mirror.typeholders.classmirror;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;
import dev.thecodewarrior.mirror.testsupport.Object1;
import dev.thecodewarrior.mirror.testsupport.Object1Sub;

@SuppressWarnings("JavaDoc")
public class MethodHelpersHolder extends AnnotatedTypeHolder {
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

    @ElementHolder("MethodsToInherit")
    public static class MethodsToInherit {
        @ElementHolder("int methodToInherit()")
        public int methodToInherit() {
            return 0;
        }

        @ElementHolder("int methodToOverride()")
        public int methodToOverride() {
            return 0;
        }
    }

    @ElementHolder("MethodInheritor")
    public static class MethodInheritor extends MethodsToInherit {
        @ElementHolder("int overriddenMethod()")
        @Override
        public int methodToOverride() {
            return super.methodToOverride();
        }
    }
}
