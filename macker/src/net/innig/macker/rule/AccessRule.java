package net.innig.macker.rule;

import net.innig.macker.structure.ClassManager;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.util.IncludeExcludeLogic;
import net.innig.macker.util.IncludeExcludeNode;
import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.MackerIsMadException;

import java.util.*;
import net.innig.collect.MultiMap;

public class AccessRule
    extends Rule
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AccessRule()
        {
        type = AccessRuleType.DENY;
        from = to = Pattern.ALL;
        }
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    public AccessRuleType getType()
        { return type; }
    
    public void setType(AccessRuleType type)
        {
        if(type == null)
            throw new NullPointerException("type parameter cannot be null");
        this.type = type;
        }
    
    public Pattern getFrom()
        { return from; }
    
    public void setFrom(Pattern from)
        { this.from = from; }
    
    public String getFromMessage()
        { return fromMessage; }
    
    public void setFromMessage(String fromMessage)
        { this.fromMessage = fromMessage; }
    
    public String getToMessage()
        { return toMessage; }
    
    public void setToMessage(String toMessage)
        { this.toMessage = toMessage; }
    
    public Pattern getTo()
        { return to; }
    
    public void setTo(Pattern to)
        { this.to = to; }
    
    public AccessRule getChild()
        { return child; }
    
    public void setChild(AccessRule child)
        { this.child = child; }
    
    public AccessRule getNext()
        { return next; }
    
    public void setNext(AccessRule next)
        { this.next = next; }
    
    private AccessRuleType type;
    private Pattern from, to;
    private String fromMessage, toMessage;
    private boolean bound;
    private AccessRule child, next;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public void check(EvaluationContext context, ClassManager classes)
        throws RulesException, MackerIsMadException
        {
        for(Iterator refIter = classes.getReferences().entrySet().iterator(); refIter.hasNext(); )
            {
            MultiMap.Entry entry = (MultiMap.Entry) refIter.next();
            ClassInfo from = classes.getClassInfo((String) entry.getKey());
            ClassInfo to   = classes.getClassInfo((String) entry.getValue());

            if(!checkAccess(context, from, to))
                context.broadcastEvent(
                    new AccessRuleViolation(this, from, to, Collections.EMPTY_LIST));
            }
        }
    
    public boolean checkAccess(EvaluationContext context, ClassInfo fromClass, ClassInfo toClass)
        throws RulesException
        { return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, fromClass, toClass)); }
    
    private static IncludeExcludeNode makeIncludeExcludeNode(
            final AccessRule rule,
            final EvaluationContext context,
            final ClassInfo fromClass,
            final ClassInfo toClass)
        {
        return (rule == null)
            ? null
            : new IncludeExcludeNode()
                {
                public boolean isInclude()
                    { return rule.getType() == AccessRuleType.ALLOW; }
        
                public boolean matches()
                    throws RulesException
                    {
                    return rule.getFrom().matches(context, fromClass)
                        && rule.  getTo().matches(context, toClass);
                    }
                
                public IncludeExcludeNode getChild()
                    { return makeIncludeExcludeNode(rule.getChild(), context, fromClass, toClass); }
                
                public IncludeExcludeNode getNext()
                    { return makeIncludeExcludeNode(rule.getNext(), context, fromClass, toClass); }
                };
        }
    }



