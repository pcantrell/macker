/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
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
import java.util.*;

public class SubtypeFilter
    implements Filter
    {
    public Pattern createPattern(
            RuleSet ruleSet,
            List/*<Pattern>*/ params,
            Map/*<String,String>*/ options)
        throws RulesException
        {
        if(params.size() != 1)
            throw new FilterSyntaxException(
                this,
                "Filter \"" + options.get("filter") + "\" expects one parameter, but has " + params.size());
        final Pattern supertypePat = (Pattern) params.get(0);
        return new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                throws RulesException
                {
                for(Iterator superI = classInfo.getSupertypes().iterator(); superI.hasNext(); )
                    {
                    ClassInfo supertype = (ClassInfo) superI.next();
                    if(supertypePat.matches(context, supertype))
                        return true;
                    }
                return false;
                }
            };
        }
    }
