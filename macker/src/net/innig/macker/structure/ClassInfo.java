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

public abstract class ClassInfo
    implements Comparable
    {
    public abstract String getClassName();
    
    public String getClassNameShort()
        {
        String className = getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
        }
    
    public int compareTo(Object that)
        { return getClassName().compareTo(((ClassInfo) that).getClassName()); }
    
    public abstract Set/*<String>*/ getReferences();
    }



