package dev.thecodewarrior.mirror.joor;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.Map.Entry;

// Licensed under Apache-2.0
// Source: https://github.com/jOOQ/jOOR
// Modifications:
// - Added `CACHED_LOOKUP_CONSTRUCTOR` from org.joor.Reflect
// - Added support for multiple files

/**
 * A utility that simplifies in-memory compilation of new classes.
 *
 * @author Lukas Eder
 */
public class Compile {
    public static Class<?> compile(String className, String content, CompileOptions compileOptions) {
        Lookup lookup = MethodHandles.lookup();
        ClassLoader cl = lookup.lookupClass().getClassLoader();

        try {
            return cl.loadClass(className);
        }
        catch (ClassNotFoundException ignore) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            try {
                ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

                List<CharSequenceJavaFileObject> files = new ArrayList<>();
                files.add(new CharSequenceJavaFileObject(className, content));
                StringWriter out = new StringWriter();

                List<String> options = new ArrayList<>(compileOptions.options);
                if (!options.contains("-classpath")) {
                    StringBuilder classpath = new StringBuilder();
                    String separator = System.getProperty("path.separator");
                    String prop = System.getProperty("java.class.path");

                    if (prop != null && !"".equals(prop))
                        classpath.append(prop);

                    if (cl instanceof URLClassLoader) {
                        for (URL url : ((URLClassLoader) cl).getURLs()) {
                            if (classpath.length() > 0)
                                classpath.append(separator);

                            if ("file".equals(url.getProtocol()))
                                classpath.append(new File(url.toURI()));
                        }
                    }

                    options.addAll(Arrays.asList("-classpath", classpath.toString()));
                }

                CompilationTask task = compiler.getTask(out, fileManager, null, options, null, files);

                if (!compileOptions.processors.isEmpty())
                    task.setProcessors(compileOptions.processors);

                task.call();

                if (fileManager.isEmpty())
                    throw new ReflectException("Compilation error: " + out);

                Class<?> result = null;

                // This works if we have private-access to the interfaces in the class hierarchy
                if (CACHED_LOOKUP_CONSTRUCTOR != null && CACHED_DEFINECLASS_METHOD != null) {
                    result = fileManager.loadAndReturnMainClass(className,
                        (name, bytes) -> (Class<?>) CACHED_DEFINECLASS_METHOD.invoke(cl, name, bytes, 0, bytes.length));
                }

                return result;
            }
            catch (ReflectException e) {
                throw e;
            }
            catch (Exception e) {
                throw new ReflectException("Error while compiling " + className, e);
            }
        }
    }

    static final class JavaFileObject extends SimpleJavaFileObject {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        JavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        byte[] getBytes() {
            return os.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() {
            return os;
        }
    }

    static final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, JavaFileObject> fileObjectMap;
        private Map<String, byte[]> classes;

        ClassFileManager(StandardJavaFileManager standardManager) {
            super(standardManager);

            fileObjectMap = new HashMap<>();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling
        ) {
            JavaFileObject result = new JavaFileObject(className, kind);
            fileObjectMap.put(className, result);
            return result;
        }

        boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }

        Map<String, byte[]> classes() {
            if (classes == null) {
                classes = new HashMap<>();

                for (Entry<String, JavaFileObject> entry : fileObjectMap.entrySet())
                    classes.put(entry.getKey(), entry.getValue().getBytes());
            }

            return classes;
        }

        Class<?> loadAndReturnMainClass(String mainClassName, ThrowingBiFunction<String, byte[], Class<?>> definer) throws Exception {
            Class<?> result = null;

            for (Entry<String, byte[]> entry : classes().entrySet()) {
                Class<?> c = definer.apply(entry.getKey(), entry.getValue());
                if (mainClassName.equals(entry.getKey()))
                    result = c;
            }

            return result;
        }

        Map<String, Class<?>> loadAndReturnClasses(ThrowingBiFunction<String, byte[], Class<?>> definer) throws Exception {
            Map<String, Class<?>> result = new HashMap<>();

            for (Entry<String, byte[]> entry : classes().entrySet()) {
                Class<?> c = definer.apply(entry.getKey(), entry.getValue());
                result.put(entry.getKey(), c);
            }

            return result;
        }
    }

    @FunctionalInterface
    interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }

    static final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }

    // from org.joor.Reflect
    static final Constructor<Lookup> CACHED_LOOKUP_CONSTRUCTOR;
    static final Method CACHED_DEFINECLASS_METHOD;

    static {
        Constructor<MethodHandles.Lookup> result;

        try {
            result = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);

            if (!result.isAccessible())
                result.setAccessible(true);
        }
        // Can no longer access the above in JDK 9
        catch (Throwable ignore) {
            result = null;
        }

        CACHED_LOOKUP_CONSTRUCTOR = result;
    }

    static {
        Method result;

        try {
            result = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

            if (!result.isAccessible())
                result.setAccessible(true);
        }
        catch (Throwable ignore) {
            result = null;
        }

        CACHED_DEFINECLASS_METHOD = result;
    }
}

