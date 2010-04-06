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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Paul Cantrell
 */
public class MackerIsMadException extends Exception {
	
	private static final long serialVersionUID = -6422294499450829625L;

	private static final String BASE_MESSAGE = "Macker rules checking failed";
	
	private final List<MackerEvent> events;
	
	public MackerIsMadException() {
		super();
		this.events = null;
	}

	public MackerIsMadException(final MackerEvent event) {
		this(Collections.singletonList(event));
	}

	public MackerIsMadException(final List<MackerEvent> events) {
		super(BASE_MESSAGE);
		if (events.isEmpty()) {
			throw new IllegalArgumentException("Macker needs a non-empty list of things to be mad about.");
		}
		this.events = Collections.unmodifiableList(new ArrayList<MackerEvent>(events));
	}

	public List<MackerEvent> getEvents() {
		return this.events;
	}
}
