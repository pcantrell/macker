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

package net.innig.macker.structure;

import net.innig.util.GraphType;

/**
 * Taxonomy of different kinds of references between classes.
 *
 * @author Paul Cantrell
 * @see Reference
 */
public final class ReferenceType extends GraphType {
	
	private static final long serialVersionUID = 7881805878888320289L;
	public static final ReferenceType SIGNATURE = new ReferenceType("signature");
	public static final ReferenceType MEMBER_SIGNATURE = new ReferenceType("member-signature", SIGNATURE);
	public static final ReferenceType METHOD_SIGNATURE = new ReferenceType("method-signature", MEMBER_SIGNATURE);
	public static final ReferenceType METHOD_PARAM = new ReferenceType("method-param", METHOD_SIGNATURE);
	public static final ReferenceType METHOD_RETURNS = new ReferenceType("method-returns", METHOD_SIGNATURE);
	public static final ReferenceType METHOD_THROWS = new ReferenceType("method-throws", METHOD_SIGNATURE);
	public static final ReferenceType FIELD_SIGNATURE = new ReferenceType("field-signature", MEMBER_SIGNATURE);
	public static final ReferenceType SUPER = new ReferenceType("super");
	public static final ReferenceType EXTENDS = new ReferenceType("extends", SUPER);
	public static final ReferenceType IMPLEMENTS = new ReferenceType("implements", SUPER);
	public static final ReferenceType INTERNAL = new ReferenceType("internal");
	public static final ReferenceType CONSTANT_POOL = new ReferenceType("constant-pool", INTERNAL);

	private ReferenceType(final String name) {
		super(name);
	}

	private ReferenceType(final String name, final ReferenceType parent) {
		super(name, parent);
	}
}
