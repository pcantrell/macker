package net.innig.macker.event;

import net.innig.macker.rule.AccessRule;
import net.innig.macker.structure.ClassInfo;

import java.util.List;

public class AccessRuleViolation
    extends MackerIsMadEvent
    {
    public AccessRuleViolation(AccessRule accessRule, ClassInfo from, ClassInfo to, List messages)
        {
        super(accessRule, "Illegal reference\n  from " + from + "\n    to " + to, messages);
        this.accessRule = accessRule;
        this.from = from;
        this.to = to;
        }
    
    public final AccessRule getAccessRule()
        { return accessRule; }
    
    public final ClassInfo getFrom()
        { return from; }
        
    public final ClassInfo getTo()
        { return to; }
    
    private final AccessRule accessRule;
    private final ClassInfo from, to;
    }