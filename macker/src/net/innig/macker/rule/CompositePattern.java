/*______________________________________________________________________________
 *
 * Current distribution and futher info:  http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2.1, as published
 * by the Free Software Foundation. See the file LICENSE.html for more info.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker.rule;

import net.innig.macker.structure.ClassInfo;
import net.innig.macker.util.IncludeExcludeLogic;
import net.innig.macker.util.IncludeExcludeNode;

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
    
    public CompositePattern(CompositePatternType type, Pattern head, CompositePattern child)
        {
        setType(type);
        setHead(head);
        setChild(child);
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
    
    public CompositePattern getChild()
        { return child; }
    
    public void setChild(CompositePattern child)
        { this.child = child; }
    
    public CompositePattern getNext()
        { return next; }
    
    public void setNext(CompositePattern next)
        { this.next = next; }
    
    private CompositePatternType type;
    private Pattern head;
    private CompositePattern child, next;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public boolean matches(EvaluationContext context, ClassInfo classInfo)
        throws RulesException
        { return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, classInfo)); }
    
    private static IncludeExcludeNode makeIncludeExcludeNode(
            final CompositePattern pat,
            final EvaluationContext context,
            final ClassInfo classInfo)
        {
        return (pat == null)
            ? null
            : new IncludeExcludeNode()
                {
                public boolean isInclude()
                    { return pat.getType() == CompositePatternType.INCLUDE; }
        
                public boolean matches()
                    throws RulesException
                    { return pat.getHead().matches(context, classInfo); }
                
                public IncludeExcludeNode getChild()
                    { return makeIncludeExcludeNode(pat.getChild(), context, classInfo); }
                
                public IncludeExcludeNode getNext()
                    { return makeIncludeExcludeNode(pat.getNext(), context, classInfo); }
                };
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





