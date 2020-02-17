package dev.thecodewarrior.mirror.typeholders.classmirror;

import dev.thecodewarrior.mirror.testsupport.AnnotatedTypeHolder;

@SuppressWarnings("JavaDoc")
public class MethodsHolder extends AnnotatedTypeHolder {
    public static class inheritedMethods_withSuperclassMethods_shouldIncludeCorrectSuperclassMethods {
        public static class X {
            public void publicMethod() {}
            protected void protectedMethod() {}
            void packagePrivateMethod() {}
            private void privateMethod() {}
        }
    }
}
