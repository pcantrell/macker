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
 
package net.innig.macker.rule.filter;

import net.innig.macker.rule.*;
import net.innig.macker.structure.ClassInfo;
import java.util.Map;

public class InterfaceFilter
    implements Filter
    {
    public Pattern createPattern(
            RuleSet ruleSet,
            Map/*<String,String>*/ options)
        throws RulesException
        { return INTERFACE_PATTERN; } 
    
    public Pattern createPattern(
            RuleSet ruleSet,
            Pattern childPattern,
            Map/*<String,String>*/ options)
        throws RulesException
        { throw new FilterSyntaxException(options.get("filter") + " filter does not take a nested pattern"); }

    private final Pattern INTERFACE_PATTERN =
        new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                throws RulesException
                { return classInfo.isInterface(); }
            };
    }
