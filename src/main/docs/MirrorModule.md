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


