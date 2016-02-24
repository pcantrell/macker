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

import net.innig.macker.structure.ClassInfo;
import net.innig.macker.util.IncludeExcludeLogic;
import net.innig.macker.util.IncludeExcludeNode;

/**
 * @author Paul Cantrell
 */
public final class CompositePattern implements Pattern {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public static Pattern create(final CompositePatternType type,
			final Pattern head, final Pattern child, final Pattern next) {
		if (type == null) {
			throw new IllegalArgumentException("type parameter cannot be null");
		}

		if (head == null && child == null && next == null) {
			if (type == CompositePatternType.INCLUDE) {
				return Pattern.ALL;
			}
			
			return Pattern.NONE;
		}
		if (head == null && child == null) {
			return create(type, next, null, null);
		}
		if (head == null) {
			return create(type, child, null, next);
		}
		if (type == CompositePatternType.INCLUDE && child == null && next == null) {
			return head;
		}

		return new CompositePattern(type, head, child, next);
	}

	private CompositePattern(final CompositePatternType type,
			final Pattern head, final Pattern child, final Pattern next) {
		this.type = type;
		this.head = head;
		this.child = child;
		this.next = next;
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public CompositePatternType getType() {
		return this.type;
	}

	public Pattern getHead() {
		return this.head;
	}

	public Pattern getChild() {
		return this.child;
	}

	public Pattern getNext() {
		return this.next;
	}

	private final CompositePatternType type;
	private final Pattern head;
	private final Pattern child;
	private final Pattern next;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	public boolean matches(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
		return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, classInfo));
	}

	private static IncludeExcludeNode makeIncludeExcludeNode(final Pattern pat, final EvaluationContext context,
			final ClassInfo classInfo) {
		if (pat == null) {
			return null;
		}

		final boolean include;
		final Pattern head;
		final Pattern child;
		final Pattern next;

		if (pat instanceof CompositePattern) {
			final CompositePattern compositePat = (CompositePattern) pat;
			include = compositePat.getType() == CompositePatternType.INCLUDE;
			head = compositePat.getHead();
			child = compositePat.getChild();
			next = compositePat.getNext();
		} else {
			include = true;
			head = pat;
			child = null;
			next = null;
		}

		return new IncludeExcludeNode() {
			public boolean isInclude() {
				return include;
			}

			public boolean matches() throws RulesException {
				return head.matches(context, classInfo);
			}

			public IncludeExcludeNode getChild() {
				return makeIncludeExcludeNode(child, context, classInfo);
			}

			public IncludeExcludeNode getNext() {
				return makeIncludeExcludeNode(next, context, classInfo);
			}
		};
	}

	// --------------------------------------------------------------------------
	// Object
	// --------------------------------------------------------------------------

	public String toString() {
		return "(" + getType() + ' ' + getHead()
				+ (getChild() == null ? "" : " + " + getChild()) + ')'
				+ (getNext() == null ? "" : ", " + getNext());
	}
}
