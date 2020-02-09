package dev.thecodewarrior.mirror.typeholders.member;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

@SuppressWarnings("JavaDoc")
public class MethodMirrorHolder extends AnnotatedTypeHolder {

    @ElementHolder("void ()")
    public void voidMethod() { }

    // bridge methods
    public static abstract class GenericClass<T> {
        public abstract void genericMethod(T arg);
    }

    @ElementHolder("GenericInheritor")
    public static class GenericInheritor extends GenericClass<String> {
        @Override
        public void genericMethod(String arg) {

        }

        public void nonBridge(String arg) {

        }
    }

    // defaults
    public interface InterfaceWithDefaults {
        @ElementHolder("default int ()")
        default int defaultInterfaceMethod() {
            return 10;
        }

        @ElementHolder("non-default int ()")
        int nonDefaultInterfaceMethod();
    }

    public static class DefaultOverride implements InterfaceWithDefaults {
        @Override
        @ElementHolder("overridden default int ()")
        public int defaultInterfaceMethod() {
            return 0;
        }

        @Override
        public int nonDefaultInterfaceMethod() {
            return 0;
        }
    }

    // default values

    @ElementHolder("@DefaultedAnnotation")
    public @interface DefaultedAnnotation {
        String stringDefaulted() default "default value";
        int intDefaulted() default 10;
    }
}
