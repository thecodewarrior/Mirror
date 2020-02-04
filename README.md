<h1 align="center">
  <br>
    <img src="https://raw.github.com/TeamWizardry/Mirror/master/logo/logo_500x500.png" title="Mirror logo by Tris 
    Astral" width="200" height="200">
  <br>
  Mirror
  <br>
</h1>

<h4 align="center">A library that makes advanced Java reflection features viable for the non-expert to use</h4>

The central feature of Mirror is the family of “Mirrors,” which wrap Java Core Reflection objects and make many advanced
reflection features simple and easy to use. Mirrors can contain information about type arguments and annotations,
meaning, among other things, the superclass of `ArrayList<@NotNull String>` is `AbstractList<@NotNull String>`, not the 
raw `AbstractList` or generic `AbstractList<T>`.

### Major features so far
* Type specialization
  - Elevates generics and type annotations to be fully fledged members of the type system
* Near-native speed reflective member access using
[MethodHandles](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandle.html)
* All-around easier access to information
  - Out with the `Modifier.isStatic(someClass.modifiers)`, in with the `classMirror.isStatic()`

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

### Minor features

#### Generic-aware `isAssignableFrom`
```java
ClassMirror integerLinkedList = Mirror.reflect(new TypeToken<LinkedList<Integer>>() {});
ClassMirror stringList = Mirror.reflect(new TypeToken<List<String>>() {});
ClassMirror numberList = Mirror.reflect(new TypeToken<List<Number>>() {});

assert !stringList.isAssignableFrom(integerLinkedList);
assert numberList.isAssignableFrom(integerLinkedList);
```

#### Type specificity comparison
```java
List<TypeMirror> mirrors = Arrays.asList(
    Mirror.reflect(new TypeToken<List<Number>>() {}),
    Mirror.reflect(new TypeToken<ArrayList<Integer>>() {}),
    Mirror.reflect(new TypeToken<List<Integer>>() {}),
    Mirror.reflect(new TypeToken<List<Object>>() {})
);
mirrors.sort(Comparator.comparing(TypeMirror::getSpecificity));
// mirrors = 
//    List<Object>,
//    List<Number>,
//    List<Integer>,
//    ArrayList<Integer>
```

#### Creating annotation instances

```java
Contract contractAnnotation = Mirror.newAnnotation(Contract.class,
    new Pair<>("value", "_, null -> null"),
    new Pair<>("pure", true)
);

// builder style has not been implemented yet. Pairs are convenient in Kotlin but not so much in Java.
Contract contractAnnotation = Mirror.newAnnotation(Contract.class)
    .set("value", "_, null -> null")
    .set("pure", true)
    .build();
```

… readme wip …
