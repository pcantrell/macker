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

package net.innig.macker.util;

import net.innig.macker.rule.RulesException;

/**
 * @author Paul Cantrell
 */
public final class IncludeExcludeLogic {
	
	/**
	 * Private constructor voor utility class.
	 */
	private IncludeExcludeLogic() {
	}

	public static boolean apply(final IncludeExcludeNode node) throws RulesException {
		// include starts with all excluded, and exclude starts with all included
		boolean prevMatches = true;
		if (node.isInclude()) {
			prevMatches = false;
		}
		
		return applyNext(node, prevMatches);
	}

	private static boolean applyNext(final IncludeExcludeNode node, final boolean prevMatches) throws RulesException {
		final IncludeExcludeNode child = node.getChild();
		final IncludeExcludeNode next = node.getNext();
		final boolean curMatches = node.matches();
		boolean matchesSoFar;
		if (node.isInclude()) {
			matchesSoFar = prevMatches || (curMatches && (child == null || apply(child)));
		} else {
			matchesSoFar = prevMatches && (!curMatches || (child != null && apply(child)));
		}
		
		if (next == null) {
			return matchesSoFar;
		}
		
		return applyNext(next, matchesSoFar);
	}
}
