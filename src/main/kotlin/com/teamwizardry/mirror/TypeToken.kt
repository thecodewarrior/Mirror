package com.teamwizardry.mirror

import net.bytebuddy.pool.TypePool
import java.lang.reflect.AnnotatedParameterizedType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeToken<T> {
    fun get(): Type {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

    fun getAnnotated(): AnnotatedType {
//        val annotations = TypeDescription.ForLoadedType(javaClass)
//            .superClass.typeArguments.only.declaredAnnotations
        if(javaClass.superclass != TypeToken::class.java)
            throw IllegalStateException("TypeToken subclasses must directly inherit from TypeToken. ${javaClass.name} doesn't.")
        val description = pool(javaClass.classLoader)
            .describe(javaClass.name)
            .resolve()
        val superClass = description.superClass
        val superArguments = superClass.typeArguments
        val annotations = superArguments.only.declaredAnnotations
        return (javaClass.annotatedSuperclass as AnnotatedParameterizedType).annotatedActualTypeArguments[0]
    }

    private companion object {
        val typeCache = mutableMapOf<Class<*>, AnnotatedType>()
        val poolCache = mutableMapOf<ClassLoader, TypePool>()
        fun getCached() {
        }

        fun pool(loader: ClassLoader): TypePool {
            return poolCache.getOrPut(loader) {
                TypePool.Default.of(loader)
            }
        }
    }
}

inline fun <reified T> typeToken(): Type {
    return (object : TypeToken<T>() {}).get()
}

inline fun <reified T> annotatedTypeToken(): AnnotatedType {
    return (object : TypeToken<T>() {}).getAnnotated()
}
