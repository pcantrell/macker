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

import java.util.Set;
import net.innig.collect.MultiMap;

public class IncompleteClassInfo
    extends ClassInfo
    {
    public IncompleteClassInfo(ClassManager classManager, String className)
        {
        super(classManager);
        this.className = className;
        }
    
    public String getClassName()
        { return className; }

    public boolean isInterface()               { throw newIncompleteException("get attributes of"); }
    public boolean isAbstract()                { throw newIncompleteException("get attributes of"); }
    public boolean isFinal()                   { throw newIncompleteException("get attributes of"); }
    public AccessModifier getAccessModifier()  { throw newIncompleteException("determine accessibility of"); }
    public String getExtends()                 { throw newIncompleteException("determine superclass of"); }
    public Set/*<String>*/ getImplements()     { throw newIncompleteException("determine interfaces implemented by"); }
    public MultiMap getReferences()            { throw newIncompleteException("resolve references from"); }
    
    private IncompleteClassInfoException newIncompleteException(String action)
        {
        return new IncompleteClassInfoException(
            "Unable to " + action + " class " + className
            + ", because the class file could not be loaded."
            + " Make sure it is in Macker's classpath.");
        }
    
    public String toString()
        { return getClassName(); }
    
    private String className, classNameUq;
    }


