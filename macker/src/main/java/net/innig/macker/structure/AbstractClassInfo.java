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

import net.innig.collect.GraphWalker;
import net.innig.collect.Graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Paul Cantrell
 */
public abstract class AbstractClassInfo implements ClassInfo {

	private ClassManager classManager;
	private Set<ClassInfo> cachedAllSuper;
	private Set<ClassInfo> cachedAllDirectSuper;
	
	public AbstractClassInfo(final ClassManager classManager) {
		this.classManager = classManager;
	}

	public String getClassName() {
		final String className = getFullName();
		return className.substring(className.lastIndexOf('.') + 1);
	}

	public String getPackageName() {
		final String className = getFullName();
		final int lastDotPos = className.lastIndexOf('.');
		if (lastDotPos <= 0) {
			return "";
		}
		
		return className.substring(0, lastDotPos);
	}

	public Set<ClassInfo> getDirectSupertypes() {
		if (this.cachedAllDirectSuper == null) {
			final Set<ClassInfo> newAllDirectSuper = new HashSet<ClassInfo>(getImplements());
			newAllDirectSuper.add(getExtends());
			// failure atomicity
			this.cachedAllDirectSuper = newAllDirectSuper;
		}
		return this.cachedAllDirectSuper;
	}

	public Set<ClassInfo> getSupertypes() {
		if (this.cachedAllSuper == null) {
			this.cachedAllSuper = Graphs.reachableNodes(this, new GraphWalker<ClassInfo>() {
				public Collection<ClassInfo> getEdgesFrom(final ClassInfo node) {
					return node.getDirectSupertypes();
				}
			});
		}
		return this.cachedAllSuper;
	}

	public final ClassManager getClassManager() {
		return this.classManager;
	}

	public final boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (!(that instanceof ClassInfo)) {
			return false;
		}
		return getFullName().equals(((ClassInfo) that).getFullName());
	}

	public final int hashCode() {
		return getFullName().hashCode();
	}

	public String toString() {
		return getFullName();
	}
}
