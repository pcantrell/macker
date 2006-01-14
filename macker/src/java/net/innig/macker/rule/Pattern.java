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
 
package net.innig.macker.rule;

import net.innig.macker.structure.ClassInfo;

public interface Pattern
    {
    public static final Pattern ALL =
        new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                { return true; }
            public String toString() { return "<all>"; }
            };
    
    public static final Pattern NONE =
        new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                { return false; }
            public String toString() { return "<none>"; }
            };
    
    public boolean matches(EvaluationContext context, ClassInfo classInfo)
        throws RulesException;
    }
