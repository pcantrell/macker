package net.innig.macker.rule;

import net.innig.util.EnumeratedType;

public final class CompositePatternType
    extends EnumeratedType
    {
    public static final CompositePatternType
        INCLUDE = new CompositePatternType("include"),
        EXCLUDE = new CompositePatternType("exclude");
    
    private CompositePatternType(String name) { super(name); }
    }