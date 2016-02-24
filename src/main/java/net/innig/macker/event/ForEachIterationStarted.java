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

package net.innig.macker.event;

import net.innig.macker.rule.ForEach;

/**
 * @author Paul Cantrell
 */
public class ForEachIterationStarted extends ForEachEvent {

	private static final long serialVersionUID = 634479226200429097L;
	
	private final String varValue;

	public ForEachIterationStarted(final ForEach forEach, final String varValue) {
		super(forEach, forEach.getVariableName() + " = " + varValue);
		this.varValue = varValue;
	}

	public String getVariableValue() {
		return this.varValue;
	}
}
