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

import net.innig.util.GraphType;

public class ReferenceType
    extends GraphType
    {
    public static final ReferenceType
        API                        = new ReferenceType("api"),
            MEMBER_API             = new ReferenceType("member-api", API),
                METHOD_API         = new ReferenceType("method-api", MEMBER_API),
                    METHOD_PARAM   = new ReferenceType("method-param", METHOD_API),
                    METHOD_RETURNS = new ReferenceType("method-returns", METHOD_API),
                    METHOD_THROWS  = new ReferenceType("method-throws", METHOD_API),
                FIELD_API          = new ReferenceType("field-api", MEMBER_API),
            SUPER                  = new ReferenceType("super"),
                EXTENDS            = new ReferenceType("extends", SUPER),
                IMPLEMENTS         = new ReferenceType("implements", SUPER),
        INTERNAL                   = new ReferenceType("internal"),
            CONSTANT_POOL          = new ReferenceType("constant-pool", INTERNAL);
    
    private ReferenceType(String name) { super(name); }
    private ReferenceType(String name, ReferenceType parent) { super(name, parent); }
    }
