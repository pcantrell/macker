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

import net.innig.util.EnumeratedType;

/**
 * @author Paul Cantrell
 */
public final class AccessRuleType extends EnumeratedType {
	
	private static final long serialVersionUID = -2283793245002944532L;

	public static final AccessRuleType ALLOW = new AccessRuleType("allow");
	public static final AccessRuleType DENY = new AccessRuleType("deny");

	private AccessRuleType(final String name) {
		super(name);
	}
}
