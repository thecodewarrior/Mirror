package com.teamwizardry.mirror.coretypes;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;



@SuppressWarnings("WeakerAccess")
public abstract class TypeVisitor {

	private final Map<TypeVariable, AnnotatedTypeVariable> varCache = new IdentityHashMap<>();
	private final Map<AnnotatedCaptureCacheKey, AnnotatedType> captureCache = new HashMap<>();

	protected AnnotatedType visitParameterizedType(AnnotatedParameterizedType type) {
		AnnotatedType[] params = Arrays.stream(type.getAnnotatedActualTypeArguments())
				.map(param -> GenericTypeReflector.transform(param, this))
				.toArray(AnnotatedType[]::new);

		return GenericTypeReflector.replaceParameters(type, params);
	}

	protected AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
		AnnotatedType[] lowerBounds = Arrays.stream(type.getAnnotatedLowerBounds())
				.map(bound -> GenericTypeReflector.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
				.map(bound -> GenericTypeReflector.transform(bound, this))
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
				.map(bound -> GenericTypeReflector.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		variable.init(bounds);
		return variable;
	}

	protected AnnotatedType visitArray(AnnotatedArrayType type) {
		AnnotatedType componentType = GenericTypeReflector.transform(type.getAnnotatedGenericComponentType(), this);
		return new AnnotatedArrayTypeImpl(GenericArrayTypeImpl.createArrayType(componentType.getType()), type.getAnnotations(), componentType);
	}

	protected AnnotatedType visitCaptureType(AnnotatedCaptureType type) {
		AnnotatedCaptureCacheKey key = new AnnotatedCaptureCacheKey(type);
		if (captureCache.containsKey(key)) {
			return captureCache.get(key);
		}
		AnnotatedCaptureType annotatedCapture = new AnnotatedCaptureTypeImpl((CaptureType) type.getType(),
				(AnnotatedWildcardType) GenericTypeReflector.transform(type.getAnnotatedWildcardType(), this),
				(AnnotatedTypeVariable) GenericTypeReflector.transform(type.getAnnotatedTypeVariable(), this),
				null, type.getAnnotations());
		captureCache.put(key, annotatedCapture);
		AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
				.map(bound -> GenericTypeReflector.transform(bound, this))
				.toArray(AnnotatedType[]::new);
		annotatedCapture.setAnnotatedUpperBounds(upperBounds); //complete the type
		return annotatedCapture;
	}

	protected AnnotatedType visitClass(AnnotatedType type) {
		return type;
	}

	protected AnnotatedType visitUnmatched(AnnotatedType type) {
		return type;
	}

	private static class AnnotatedCaptureCacheKey {
		AnnotatedCaptureType capture;
		CaptureType raw;

		AnnotatedCaptureCacheKey(AnnotatedCaptureType capture) {
			this.capture = capture;
			this.raw = (CaptureType) capture.getType();
		}

		@Override
		public int hashCode() {
			return 127 * raw.getWildcardType().hashCode() ^ raw.getTypeVariable().hashCode() ^ GenericTypeReflector.hashCode(Arrays.stream(capture.getAnnotations()));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof AnnotatedCaptureCacheKey)) return false;

			AnnotatedCaptureCacheKey that = ((AnnotatedCaptureCacheKey) obj);
			return this.capture == that.capture ||
					(new GenericTypeReflector.CaptureCacheKey(raw).equals(new GenericTypeReflector.CaptureCacheKey(that.raw))
							&& Arrays.equals(capture.getAnnotations(), that.capture.getAnnotations()));
		}
	}
}
