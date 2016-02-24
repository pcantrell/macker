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

import org.apache.bcel.classfile.JavaClass;

/**
 * A class file was found, but was unparsable.
 * 
 * @author Paul Cantrell
 */
public class ClassParseException extends Exception {

	private static final long serialVersionUID = -5569141764651140271L;

	public ClassParseException(final String message) {
		super(message);
	}

	public ClassParseException(final String message, final JavaClass classFile) {
		super(message + "\n" + classFile);
	}

	public ClassParseException(final ClassFormatError cfe) {
		super(cfe);
	}
}
