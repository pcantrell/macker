package net.innig.macker.rule;

public class CompositePattern
    extends Pattern
    {
    public CompositePattern()
        { this(CompositePatternType.INCLUDE, Pattern.ALL); }
    
    public CompositePattern(CompositePatternType type, Pattern head)
        {
        setType(type);
        setHead(head);
        }
    
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
    
    public CompositePattern getChild()
        { return child; }
    
    public void setChild(CompositePattern child)
        { this.child = child; }
    
    public CompositePattern getNext()
        { return next; }
    
    public void setNext(CompositePattern next)
        { this.next = next; }
   
    public String toString()
        {
        return "(" + type + ' ' + head
            + (child == null ? "" : ", " + child) + ')'
            + ( next == null ? "" : ", " +  next);
        }
     
    private CompositePatternType type;
    private Pattern head;
    private CompositePattern child, next;
    }