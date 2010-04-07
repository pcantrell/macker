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

import net.innig.collect.MultiMap;

import java.util.Set;

/**
 * Information about a class's structure.
 * 
 * @author Paul Cantrell
 */
public interface ClassInfo {
	
	ClassManager getClassManager();

	boolean isComplete();

	String getFullClassName();

	String getClassName();

	String getPackageName();

	boolean isInterface();

	boolean isAbstract();

	boolean isFinal();

	AccessModifier getAccessModifier();

	ClassInfo getExtends();

	Set<ClassInfo> getImplements();

	Set<ClassInfo> getDirectSupertypes();

	Set<ClassInfo> getSupertypes();

	MultiMap<ClassInfo, Reference> getReferences();
}
