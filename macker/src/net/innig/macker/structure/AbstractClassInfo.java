/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */

package net.innig.macker.structure;

import java.util.*;
import net.innig.collect.*;

public abstract class AbstractClassInfo
    implements ClassInfo
    {
    public AbstractClassInfo(ClassManager classManager)
        { this.classManager = classManager; }
    
    public String getClassNameShort()
        {
        String className = getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
        }
        
    public String getPackageName()
        {
        String className = getClassName();
        return className.substring(0, className.lastIndexOf('.'));
        }
    
    public Set/*<ClassInfo>*/ getDirectSupertypes()
        {
        if(cachedAllDirectSuper == null)
            {
            Set newAllDirectSuper = new HashSet(getImplements());
            newAllDirectSuper.add(getExtends());
            cachedAllDirectSuper = newAllDirectSuper; // failure atomicity
            }
        return cachedAllDirectSuper;
        }
    
    public Set/*<ClassInfo>*/ getSupertypes()
        {
        if(cachedAllSuper == null)
            cachedAllSuper = Graphs.reachableNodes(
                this,
                new GraphWalker()
                    {
                    public Collection getEdgesFrom(Object node)
                        { return ((ClassInfo) node).getDirectSupertypes(); }
                    } );
        return cachedAllSuper;
        }
    
    public final ClassManager getClassManager()
        { return classManager; }
    
    public abstract boolean isComplete();
    public abstract String getClassName();
    public abstract boolean isInterface();
    public abstract boolean isAbstract();
    public abstract boolean isFinal();
    public abstract AccessModifier getAccessModifier();
    public abstract ClassInfo getExtends();
    public abstract Set/*<ClassInfo>*/ getImplements();
    public abstract MultiMap/*<ClassInfo,Reference>*/ getReferences();
    
    public int compareTo(Object that)
        { return getClassName().compareTo(((ClassInfo) that).getClassName()); }
    
    public boolean equals(Object that)
        {
        if(this == that)
            return true;
        if(that == null)
            return false;
        if(this.getClass() != that.getClass())
            return false;
        return getClassName().equals(((ClassInfo) that).getClassName());
        }
    
    public int hashCode()
        { return getClassName().hashCode(); }
    
    public String toString()
        { return getClassName(); }
    
    private ClassManager classManager;
    private Set cachedAllSuper, cachedAllDirectSuper;
    }

