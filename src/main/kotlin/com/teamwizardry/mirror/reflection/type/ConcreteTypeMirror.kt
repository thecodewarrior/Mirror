package com.teamwizardry.mirror.reflection.type

abstract class ConcreteTypeMirror: TypeMirror() {
    abstract override val rawType: Class<*>
}

