package dev.thecodewarrior.mirror.joor;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.Map.Entry;

// Licensed under Apache-2.0
// Source: https://github.com/jOOQ/jOOR
// Modifications:
// - Added support for multiple files by creating a classloader for each call

/**
 * A utility that simplifies in-memory compilation of new classes.
 *
 * @author Lukas Eder
 */
public class Compile {
    public static RuntimeClassLoader compile(Map<String, String> code, CompileOptions compileOptions) {
        Lookup lookup = MethodHandles.lookup();
        ClassLoader cl = lookup.lookupClass().getClassLoader();

        if(code.isEmpty()) {
            return new RuntimeClassLoader(cl, new HashMap<>());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        try {
            ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

            List<CharSequenceJavaFileObject> files = new ArrayList<>();
            for(Entry<String, String> entry : code.entrySet()) {
                files.add(new CharSequenceJavaFileObject(entry.getKey(), entry.getValue()));
            }
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

            if (!out.toString().equals(""))
                throw new CompileException("Compilation error:\n" + out);

            return fileManager.createClassLoader(cl);
        }
        catch (Exception e) {
            throw new CompileException("Error while compiling classes", e);
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

        RuntimeClassLoader createClassLoader(ClassLoader parent) {
            return new RuntimeClassLoader(parent, classes());
        }
    }

    public static final class RuntimeClassLoader extends ClassLoader {
        private final Map<String, byte[]> classes;
        public final Set<String> classNames;

        public RuntimeClassLoader(ClassLoader parent, Map<String, byte[]> classes) {
            super(parent);
            this.classes = classes;
            this.classNames = Collections.unmodifiableSet(classes.keySet());
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] data = classes.get(name);
            if(data != null) {
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    String pkgname = name.substring(0, i);
                    // Check if package already loaded.
                    if(getPackage(pkgname) == null) {
                        definePackage(pkgname, null, null, null, null, null, null, null);
                    }
                }
                return defineClass(name, data, 0, data.length);
            }
            throw new ClassNotFoundException(name);
        }
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
}

