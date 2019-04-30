<h1 align="center">
  <br>
    <img src="https://raw.github.com/TeamWizardry/Mirror/master/logo.svg?sanitize=true" title="Mirror logo by Tris 
    Astral" width="200" height="200">
  <br>
  Mirror
  <br>
</h1>

<h4 align="center">A library designed to simplify advanced Java reflection</h4>

The central feature of Mirror is the family of “Mirrors,” which wrap Java Core Reflection objects and make many advanced 
reflection features simple and easy to use. Mirrors can contain information about type arguments and annotations, 
meaning, among other things, the superclass of `ArrayList<@NotNull String>` is `AbstractList<@NotNull String>`, not the 
raw `AbstractList`.

* Type specialization
  - Elevates generics and type annotations to be fully fledged members of the type system
* Near-native speed reflective member access using 
[MethodHandles](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandle.html)

```java
// create a mirror of LinkedList<String>
ClassMirror linkedList = Mirror.reflectClass(new TypeToken<LinkedList<String>>() {});
// get the `removeFirst` method, which is defined in Deque
MethodMirror removeFirst = linkedList.getMethod("removeFirst");

// the type argument `String` has propagated down and specialized this method's return type
assert removeFirst.getReturnType().equals(Mirror.reflect(String.class));
// this mirror of the method is actually located in the `Deque<String>` class
assert removeFirst.getDeclaringClass().equals(Mirror.reflect(new TypeToken<Deque<String>>(){}));

// after the initial overhead of creating the MethodHandle, this will run at near-native speed
String value = removeFirst.call(linkedListInstance);
```
