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

import net.innig.macker.structure.ClassManager;

import junit.framework.TestCase;

public final class MackerRegexTest extends TestCase {
	public MackerRegexTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		context = new EvaluationContext(new ClassManager(), new RuleSet(RuleSet.getMackerDefaults()));
	}

	protected void tearDown() throws Exception {
	}

	private EvaluationContext context;

	private void testMatches(MackerRegex re, String[] positive, String[] negative) throws Exception {
		for (int n = 0; n < positive.length; n++)
			assertTrue("\"" + re + "\" should match \"" + positive[n] + "\"", re.matches(context, positive[n]));

		for (int n = 0; n < negative.length; n++)
			assertTrue("\"" + re + "\" shouldn't match \"" + negative[n] + "\"", !re.matches(context, negative[n]));
	}

	public void test_basic() throws Exception {
		testMatches(new MackerRegex("java.lang.*"), new String[] { "java.lang.String" }, new String[] {
				"java.lang.reflect.Method", "java.language.MadeUpClass" });

		testMatches(new MackerRegex("java.lang.**"), new String[] { "java.lang.String", "java.lang.reflect.Method" },
				new String[] { "java.language.MadeUpClass" });

		testMatches(new MackerRegex("java.lang**"), new String[] { "java.lang.String", "java.lang.reflect.Method",
				"java.language.MadeUpClass" }, new String[0]);

		testMatches(new MackerRegex("String"), new String[] { "String" }, new String[] { "java.lang.String" });

		testMatches(new MackerRegex("java.lang.String"), new String[] { "java.lang.String" }, new String[] { "String" });

		testMatches(new MackerRegex("String"), new String[] { "String" }, new String[] { "java.lang.String",
				"java.lang.StringBuffer", "java.text.AttributedString" });
		testMatches(new MackerRegex("**String*"), new String[] { "String", "java.lang.String",
				"java.lang.StringBuffer", "java.text.AttributedString" }, new String[0]);

		testMatches(new MackerRegex("**String"), new String[] { "String", "java.lang.String",
				"java.text.AttributedString" }, new String[] { "java.lang.StringBuffer" });

		testMatches(new MackerRegex("**.String*"), new String[] { "String", "java.lang.String",
				"java.lang.StringBuffer" }, new String[] { "java.text.AttributedString" });

		testMatches(new MackerRegex("a.b"), new String[] { "a.b", "a$b" }, new String[] { "" });
	}

	public void test_innerClass() throws Exception {
		testMatches(new MackerRegex("a.b"), new String[] { "a$b", "a.b" }, new String[0]);

		testMatches(new MackerRegex("a$b"), new String[] { "a$b" }, new String[] { "a.b" });

		testMatches(new MackerRegex("a/b"), new String[] { "a.b" }, new String[] { "a$b" });

		testMatches(new MackerRegex("**a*b**"), new String[] { "x.axb", "x$axb", "a$b" }, // !
				// is
				// this
				// really
				// right?
				new String[] { "a.b" });
	}

	public void test_variable() throws Exception {
		MackerRegex re = new MackerRegex("x${var}y");

		try {
			re.matches(context, "");
			fail("expected exception");
		} catch (UndeclaredVariableException uve) { /* correct */
		}

		context.setVariableValue("var", "");
		testMatches(re, new String[] { "xy" }, new String[] { "xay" });

		context.setVariableValue("var", ".a");
		testMatches(re, new String[] { "x.ay" }, new String[] { "xy", "x.y", "ax.y" });

		context.setVariableValue("var", ".*");
		testMatches(re, new String[] { "x.y", "x.ay" }, new String[] { "x.a.y" });

		context.setVariableValue("rav", "**");
		context.setVariableValue("var", ".${rav}");
		testMatches(re, new String[] { "x.y", "x.ay", "x.a.y" }, new String[] { "xy" });

		context.setVariableValue("rav", "*A*");
		testMatches(new MackerRegex("*${rav}*"), new String[] { "Apple" }, new String[] { "granny.smith.Apple",
				"Apple.computer" });
	}

	public void test_illegal() throws Exception {
		String[] illegal = new String[] { "#", "@", "^", "|", "!",
		// ! "()()", "(())", "(a)(b)", "((a))"
				"\n", "\r", "a\nb", "a\rb", "a.", ".", "" // and ".a" ... except
		// we need that to
		// grab top-level
		// parts, as in
		// .(*).**
		};
		for (int n = 0; n < illegal.length; n++)
			try {
				new MackerRegex(illegal[n]).matches(context, "");
				fail("expected exception: \"" + illegal[n] + "\" shouldn't be a legal regex");
			} catch (MackerRegexSyntaxException mrse) { /* good */
			}
	}

	public void test_noParts() throws Exception {
		String[] illegal = new String[] { "a.b", "a$b", "a/b", ".a", "a.", ".", "" };
		for (int n = 0; n < illegal.length; n++)
			try {
				new MackerRegex(illegal[n], false).matches(context, "");
				fail("expected exception: \"" + illegal[n] + "\" shouldn't be a legal regex in no-parts mode");
			} catch (MackerRegexSyntaxException mrse) { /* good */
			}

		assertTrue(new MackerRegex("a*z").matches(context, "abblefrabazz"));
	}
}
