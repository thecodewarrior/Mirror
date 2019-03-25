/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package com.teamwizardry.mirror.coretypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

class AnnotatedCaptureTypeImpl extends AnnotatedTypeImpl implements AnnotatedCaptureType {

    private final AnnotatedWildcardType wildcard;
    private final AnnotatedTypeVariable variable;
    private final AnnotatedType[] lowerBounds;
    private AnnotatedType[] upperBounds;
    private final CaptureType type;
    private final Annotation[] declaredAnnotations;

    AnnotatedCaptureTypeImpl(AnnotatedWildcardType wildcard, AnnotatedTypeVariable variable) {
        this(new CaptureTypeImpl((WildcardType) wildcard.getType(), (TypeVariable) variable.getType()), wildcard, variable);
    }

    AnnotatedCaptureTypeImpl(CaptureType type, AnnotatedWildcardType wildcard, AnnotatedTypeVariable variable) {
        this(type, wildcard, variable, null, null);
    }

    AnnotatedCaptureTypeImpl(CaptureType type, AnnotatedWildcardType wildcard, AnnotatedTypeVariable variable, AnnotatedType[] upperBounds, Annotation[] annotations) {
        super(type, annotations != null ? annotations : Stream.concat(stream(wildcard.getAnnotations()), stream(variable.getAnnotations())).toArray(Annotation[]::new));
        this.type = type;
        this.wildcard = wildcard;
        this.variable = variable;
        this.lowerBounds = wildcard.getAnnotatedLowerBounds();
        this.upperBounds = upperBounds;
        this.declaredAnnotations = Stream.concat(
                Arrays.stream(wildcard.getDeclaredAnnotations()),
                Arrays.stream(variable.getDeclaredAnnotations())
        ).toArray(Annotation[]::new);
    }

    /**
     * Initialize this CaptureTypeImpl. This is needed for type variable bounds referring to each
     * other: we need the capture of the argument.
     */
    void init(VarMap varMap) {
        ArrayList<AnnotatedType> upperBoundsList = new ArrayList<>(Arrays.asList(varMap.map(variable.getAnnotatedBounds())));

        List<AnnotatedType> wildcardUpperBounds = Arrays.asList(wildcard.getAnnotatedUpperBounds());
        if (wildcardUpperBounds.size() > 0 && wildcardUpperBounds.get(0).getType() == Object.class) {
            // skip the Object bound, we already have a first upper bound from 'variable'
            upperBoundsList.addAll(wildcardUpperBounds.subList(1, wildcardUpperBounds.size()));
        } else {
            upperBoundsList.addAll(wildcardUpperBounds);
        }
        upperBounds = new AnnotatedType[upperBoundsList.size()];
        upperBoundsList.toArray(upperBounds);

        ((CaptureTypeImpl) type).init(varMap);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return declaredAnnotations;
    }

    /**
     * Returns an array of <tt>Type</tt> objects representing the upper bound(s) of this capture.
     * This includes both the upper bound of a <tt>? extends</tt> wildcard, and the bounds declared
     * with the type variable. References to other (or the same) type variables in bounds coming
     * from the type variable are replaced by their matching capture.
     */
    @Override
    public AnnotatedType[] getAnnotatedUpperBounds() {
        assert upperBounds != null;
        return upperBounds.clone();
    }

    @Override
    public void setAnnotatedUpperBounds(AnnotatedType[] upperBounds) {
        this.upperBounds = upperBounds;
        this.type.setUpperBounds(Arrays.stream(upperBounds).map(AnnotatedType::getType).toArray(Type[]::new));
    }

    /**
     * Returns an array of <tt>Type</tt> objects representing the lower bound(s) of this type
     * variable. This is the bound of a <tt>? super</tt> wildcard. This normally contains only one
     * or no types; it is an array for consistency with {@link WildcardType#getLowerBounds()}.
     */
    @Override
    public AnnotatedType[] getAnnotatedLowerBounds() {
        return lowerBounds.clone();
    }

    @Override
    public AnnotatedTypeVariable getAnnotatedTypeVariable() {
        return variable;
    }

    @Override
    public AnnotatedWildcardType getAnnotatedWildcardType() {
        return wildcard;
    }
}
