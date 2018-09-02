# Module Mirror

Mirror is designed to provide a simpler means to access to the data provided by the Java and Kotlin reflection APIs 
and make generic types no longer the bane of your existence. It does this by providing separate internal representations
of the reflection data that can be freely manipulated to provide a powerful API. Although there are numerous ease of use
and quality of life changes, the focus of Mirror is on generics and making their use in reflection feasible. It does 
this by utilizing the independent nature of mirrors to "specialize" type parameters, substituting the generic type
parameters with concrete types.

```
typeMirror = Mirror.reflectClass(HashMap.class).specialize(Mirror.reflect(String.class), Mirror.reflect(File.class))
methodMirror = typeMirror.getMethod("get", [api tbd...]);
assert methodMirror.getReturnType() == Mirror.reflect(File.class);
methodMirror.invoke(someMap, "foo")
```

# Note for contributors

This entire library hinges upon types being lazily evaluated. This means that mirror classes cannot construct any 
other mirror classes during their own construction. This is crucial because it prevents loops from occurring, as each 
mirror will be cached before any other mirrors (which may contain circular references) are created.


# Vocabulary

| name | meaning |
|------|---------|
| Mirror | An object that "mirrors" a JVM reflection object, providing easier or more flexible access to said object. |
| Specialized Mirror | A mirror that stores more information than the JVM reflection object itself. Currently all the extra information is in the form of generic type parameters |
| Specialization | The process of taking a mirror and creating a specialized mirror given some additional information |
