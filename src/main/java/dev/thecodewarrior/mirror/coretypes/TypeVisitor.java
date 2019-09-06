package dev.thecodewarrior.mirror.coretypes;

import dev.thecodewarrior.mirror.coretypes.CoreTypeUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;



@SuppressWarnings("WeakerAccess")
public abstract class TypeVisitor {

	private final Map<TypeVariable, AnnotatedTypeVariable> varCache = new IdentityHashMap<>();

	protected AnnotatedType visitParameterizedType(AnnotatedParameterizedType type) {
		AnnotatedType[] params = Arrays.stream(type.getAnnotatedActualTypeArguments())
				.map(param -> CoreTypeUtils.transform(param, this))
				.toArray(AnnotatedType[]::new);

		ParameterizedType inner = (ParameterizedType) type.getType();
		ParameterizedType raw = new ParameterizedTypeImpl(CoreTypeUtils.erase(inner),
				Arrays.stream(params).map(AnnotatedType::getType).toArray(Type[]::new), inner.getOwnerType());
		return new AnnotatedParameterizedTypeImpl(raw, type.getAnnotations(), params);
	}

	protected AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
		AnnotatedType[] lowerBounds = Arrays.stream(type.getAnnotatedLowerBounds())
				.map(bound -> CoreTypeUtils.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
				.map(bound -> CoreTypeUtils.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		WildcardType inner = new WildcardTypeImpl(
				Arrays.stream(upperBounds).map(AnnotatedType::getType).toArray(Type[]::new),
				Arrays.stream(lowerBounds).map(AnnotatedType::getType).toArray(Type[]::new));
		return new AnnotatedWildcardTypeImpl(inner, type.getAnnotations(),
				lowerBounds, upperBounds);
	}

	protected AnnotatedType visitVariable(AnnotatedTypeVariable type) {
		TypeVariable var = (TypeVariable) type.getType();
		if (varCache.containsKey(var)) {
			return varCache.get(var);
		}
		AnnotatedTypeVariableImpl variable = new AnnotatedTypeVariableImpl((TypeVariable<?>) var, type.getAnnotations());
		varCache.put(var, variable);
		AnnotatedType[] bounds = Arrays.stream(type.getAnnotatedBounds())
				.map(bound -> CoreTypeUtils.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		variable.init(bounds);
		return variable;
	}

	protected AnnotatedType visitArray(AnnotatedArrayType type) {
		AnnotatedType componentType = CoreTypeUtils.transform(type.getAnnotatedGenericComponentType(), this);
		return new AnnotatedArrayTypeImpl(GenericArrayTypeImpl.createArrayType(componentType.getType()), type.getAnnotations(), componentType);
	}

	protected AnnotatedType visitClass(AnnotatedType type) {
		return type;
	}

	protected AnnotatedType visitUnmatched(AnnotatedType type) {
		return type;
	}
}
