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

package net.innig.macker.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Paul Cantrell
 */
public final class MackerRegex {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public MackerRegex(final String patternString) throws MackerRegexSyntaxException {
		this(patternString, true);
	}

	public MackerRegex(final String patternString, final boolean allowParts) throws MackerRegexSyntaxException {
		if (patternString == null) {
			throw new IllegalArgumentException("patternString == null");
		}

		this.patternString = patternString;
		setParts(null);
		setRegex(null);
		setPrevVarValues(new HashMap<String, String>());

		final Pattern pattern;
		if (allowParts) {
			pattern = allowable;
		} else {
			pattern = allowableNoParts;
		}
		
		if (!pattern.matcher(patternString).matches()) {
			throw new MackerRegexSyntaxException(patternString);
		}
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public String getPatternString() {
		return this.patternString;
	}

	private final String patternString;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	public boolean matches(final EvaluationContext context, final String s) throws UndeclaredVariableException,
			MackerRegexSyntaxException {
		return getMatch(context, s) != null;
	}

	public String getMatch(final EvaluationContext context, final String s) throws UndeclaredVariableException,
			MackerRegexSyntaxException {
		parseExpr(context);
		Boolean match = getMatchCache().get(s);
		if (match != null) {
			if (match) {
				return getMatchResultCache().get(s);
			}
			
			return null;
		}

		final Matcher matcher = getRegex().matcher('.' + s);
		match = matcher.matches();
		getMatchCache().put(s, match);
		if (match) {
			final String matchResult = matcher.group(matcher.groupCount());
			getMatchResultCache().put(s, matchResult);
			return matchResult;
		}
		
		return null;
	}

	private void parseExpr(final EvaluationContext context)
			throws UndeclaredVariableException, MackerRegexSyntaxException {
		if (getParts() == null) {
			setParts(new ArrayList<Part>());
			final Matcher varMatcher = var.matcher(getPatternString());
			for (int pos = 0; pos >= 0;) {
				final boolean hasAnotherVar = varMatcher.find(pos);
				final int expEnd;
				if (hasAnotherVar) {
					expEnd = varMatcher.start();
				} else {
					expEnd = getPatternString().length();
				}

				if (pos < expEnd) {
					getParts().add(new ExpPart(parseSubexpr(getPatternString().substring(pos, expEnd))));
				}
				if (hasAnotherVar) {
					getParts().add(new VarPart(varMatcher.group(1)));
				}

				pos = -1;
				if (hasAnotherVar) {
					pos = varMatcher.end();
				}
			}
		}

		// Building the regexp is expensive; there's no point in doing it if we
		// already have one cached, and the relevant variables haven't changed

		boolean changed = getRegex() == null;
		for (Map.Entry<String, String> entry : getPrevVarValues().entrySet()) {
			final String name = entry.getKey();
			final String value = entry.getValue();
			if (!context.getVariableValue(name).equals(value)) {
				changed = true;
				break;
			}
		}

		if (changed) {
			final StringBuffer builtRegexStr = new StringBuffer("^\\.?");
			for (Part part : getParts()) {
				if (part instanceof VarPart) {
					final String varName = ((VarPart) part).getVarName();
					final String varValue = context.getVariableValue(varName);
					getPrevVarValues().put(varName, varValue);
					builtRegexStr.append(parseSubexpr(varValue));
				} else if (part instanceof ExpPart) {
					builtRegexStr.append(((ExpPart) part).getExp());
				}
			}
			builtRegexStr.append('$');

			try {
				setRegex(Pattern.compile(builtRegexStr.toString()));
			} catch (PatternSyntaxException pse) {
				System.out.println("builtRegexStr = " + builtRegexStr);
				throw new MackerRegexSyntaxException(getPatternString(), pse);
			}

			// ! if(???)
			// ! throw new MackerRegexSyntaxException(patternString,
			// "Too many parenthesized expressions");
			setMatchCache(new HashMap<String, Boolean>());
			setMatchResultCache(new HashMap<String, String>());
		}
	}

	private String parseSubexpr(final String exp) {
		return exp.replace(".", "[\\.\\$]").replace("/", "\\.").replace("$", "\\$").replace("*", "\uFFFF").replace(
				"\uFFFF\uFFFF", ".*").replace("\uFFFF", "[^\\.]*");
	}

	private Pattern regex;
	private List<Part> parts;
	private Map<String, String> prevVarValues;
	private Map<String, Boolean> matchCache;
	private Map<String, String> matchResultCache;
	private static Pattern var;
	private static Pattern allowable;
	private static Pattern allowableNoParts;
	static {
		final String varS = "\\$\\{([A-Za-z0-9_\\.\\-]+)\\}";
		final String partS = "(([A-Za-z_]|[\\(\\)]|\\*|" + varS + ")" + "([A-Za-z0-9_]|[\\(\\)]|\\*|" + varS + ")*)";
		var = Pattern.compile(varS);
		allowable = Pattern.compile("^([\\$\\./]?" + partS + ")+$");
		allowableNoParts = Pattern.compile("^" + partS + "$");
	}

	/** Marker interface Part. */
	private static interface Part {
	}

	private static class VarPart implements Part {
		public VarPart(final String varName) {
			this.varName = varName;
		}

		private final String varName;

		public String toString() {
			return "var(" + getVarName() + ")";
		}
		
		public String getVarName() {
			return this.varName;
		}
	}

	private static class ExpPart implements Part {
		public ExpPart(final String exp) {
			this.exp = exp;
		}

		private final String exp;

		public String toString() {
			return "exp(" + getExp() + ")";
		}
		
		public String getExp() {
			return this.exp;
		}
	}

	// --------------------------------------------------------------------------
	// Object
	// --------------------------------------------------------------------------

	public String toString() {
		return '"' + getPatternString() + '"';
	}
	
	private Map<String, Boolean> getMatchCache() {
		return this.matchCache;
	}
	
	private void setMatchCache(final Map<String, Boolean> matchCache) {
		this.matchCache = matchCache;
	}
	
	private Map<String, String> getMatchResultCache() {
		return this.matchResultCache;
	}
	
	private void setMatchResultCache(final Map<String, String> matchResultCache) {
		this.matchResultCache = matchResultCache;
	}
	
	private List<Part> getParts() {
		return this.parts;
	}
	
	private void setParts(final List<Part> parts) {
		this.parts = parts;
	}
	
	private Map<String, String> getPrevVarValues() {
		return this.prevVarValues;
	}
	
	private void setPrevVarValues(final Map<String, String> prevVarValues) {
		this.prevVarValues = prevVarValues;
	}
	
	private Pattern getRegex() {
		return this.regex;
	}
	
	private void setRegex(final Pattern regex) {
		this.regex = regex;
	}
}
