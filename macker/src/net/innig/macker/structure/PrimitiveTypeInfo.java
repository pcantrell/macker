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

public class PrimitiveTypeInfo
    extends ClassInfo
    {
    static public PrimitiveTypeInfo getPrimitiveTypeInfo(String typeName)
        { return (PrimitiveTypeInfo) nameToTypeMap.get(typeName); }

    static private Map nameToTypeMap;
    static
        {
        nameToTypeMap = new HashMap();
        nameToTypeMap.put("byte",    new PrimitiveTypeInfo("byte"));
        nameToTypeMap.put("short",   new PrimitiveTypeInfo("short"));
        nameToTypeMap.put("int",     new PrimitiveTypeInfo("int"));
        nameToTypeMap.put("long",    new PrimitiveTypeInfo("long"));
        nameToTypeMap.put("char",    new PrimitiveTypeInfo("char"));
        nameToTypeMap.put("boolean", new PrimitiveTypeInfo("boolean"));
        nameToTypeMap.put("float",   new PrimitiveTypeInfo("float"));
        nameToTypeMap.put("double",  new PrimitiveTypeInfo("double"));
        nameToTypeMap.put("void",    new PrimitiveTypeInfo("void"));
        }
    
    private PrimitiveTypeInfo(String className)
        { this.className = className; }
    
    public String getClassName()
        { return className; }
    
    public Set/*<String>*/ getReferences()
        { return Collections.EMPTY_SET; }
    
    public String toString()
        { return getClassName(); }
    
    public String className;
    }



