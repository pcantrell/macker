package net.innig.macker.rule;

public class AccessRule
    extends Rule
    {
    public AccessRule()
        {
        type = AccessRuleType.DENY;
        from = to = Pattern.ALL;
        }
    
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
    
    public Rule getNext()
        { return next; }
    
    public void setNext(Rule next)
        { this.next = next; }
    
    private AccessRuleType type;
    private Pattern from, to;
    private String fromMessage, toMessage;
    private boolean bound;
    private AccessRule child;
    private Rule next;
    }



