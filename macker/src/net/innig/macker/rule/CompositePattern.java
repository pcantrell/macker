package net.innig.macker.rule;

import net.innig.macker.structure.ClassInfo;

public class CompositePattern
    extends Pattern
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public CompositePattern()
        { this(CompositePatternType.INCLUDE, Pattern.ALL); }
    
    public CompositePattern(CompositePatternType type, Pattern head)
        {
        setType(type);
        setHead(head);
        }
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------
    
    public CompositePatternType getType()
        { return type; }

    public void setType(CompositePatternType type)
        {
        if(type == null)
            throw new NullPointerException("type parameter cannot be null");
        this.type = type;
        }
    
    public Pattern getHead()
        { return head; }

    public void setHead(Pattern head)
        { this.head = head; }
    
    public Pattern getChild()
        { return child; }
    
    public void setChild(Pattern child)
        { this.child = child; }
    
    public CompositePattern getNext()
        { return next; }
    
    public void setNext(CompositePattern next)
        { this.next = next; }
    
    private CompositePatternType type;
    private Pattern head;
    private Pattern child;
    private CompositePattern next;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public boolean matches(EvaluationContext context, ClassInfo classInfo)
        {
        return matchesAsNext(
            context,
            classInfo,
            (type == CompositePatternType.INCLUDE)
                ? false  // include starts with all excluded, and
                : true); // exclude starts with all included
        }

    protected boolean matchesAsNext(
            EvaluationContext context,
            ClassInfo classInfo,
            boolean prevMatches)
        {
        return
            getNext().matchesAsNext(
                context,
                classInfo,
                ((type == CompositePatternType.INCLUDE) == prevMatches)
                || selfMatches(context, classInfo));
        }

    protected boolean selfMatches(EvaluationContext context, ClassInfo classInfo)
        {
        return getHead().matches(context, classInfo)
               && (getChild() == null || getChild().matches(context, classInfo));
        }
    
    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------

    public String toString()
        {
        return "(" + type + ' ' + head
            + (child == null ? "" : ", " + child) + ')'
            + ( next == null ? "" : ", " +  next);
        }
    }





