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

package net.innig.macker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paul Cantrell
 */
public final class ClassNameTranslator {
	
	/**
	 * Private constructor for utility class.
	 */
	private ClassNameTranslator() {
	}
	
	public static boolean isJavaIdentifier(final String className) {
		return legalJavaIdentRE.matcher(className).matches();
	}

	public static List<String> signatureToClassNames(final String signature) {
		final List<String> names = new ArrayList<String>();
		for (int pos = 0; pos < signature.length();) {
			final String remaining = signature.substring(pos);
			final Matcher sigMatcher = sigExtractorRE.matcher(remaining);
			if (!sigMatcher.find()) {
				throw new IllegalArgumentException("Unable to extract type info from: " + remaining);
			}
			if (sigMatcher.group(2) != null) {
				names.add(primitiveTypeMap.get(sigMatcher.group(2)));
			}
			if (sigMatcher.group(3) != null) {
				names.add(resourceToClassName(sigMatcher.group(3)));
			}
			pos += sigMatcher.end();
		}
		return names;
	}

	public static String typeConstantToClassName(final String typeName) {
		final Matcher arrayMatcher = arrayExtractorRE.matcher(typeName);
		if (arrayMatcher.matches()) {
			if (arrayMatcher.group(2) != null) {
				return primitiveTypeMap.get(arrayMatcher.group(2));
			}
			if (arrayMatcher.group(3) != null) {
				return resourceToClassName(arrayMatcher.group(3));
			}
		}
		return resourceToClassName(typeName);
	}

	public static String resourceToClassName(final String className) {
		return classSuffixRE.matcher(className).replaceAll("").replace('/', '.').intern();
	}

	public static String classToResourceName(final String resourceName) {
		return (resourceName.replace('.', '/') + ".class").intern();
	}

	private static Pattern classSuffixRE;
	private static Pattern arrayExtractorRE;
	private static Pattern sigExtractorRE;
	private static Pattern legalJavaIdentRE;
	private static Map<String, String> primitiveTypeMap;
	static {
		classSuffixRE = Pattern.compile("\\.class$");
		arrayExtractorRE = Pattern.compile("^(\\[+([BSIJCFDZV])|\\[+L([^;]*);)$");
		sigExtractorRE = Pattern.compile("^\\(?\\)?(\\[*([BSIJCFDZV])|\\[*L([^;]*);)");
		final String javaIdent = "[\\p{Alpha}$_][\\p{Alnum}$_]*";
		legalJavaIdentRE = Pattern.compile("^(" + javaIdent + ")(\\.(" + javaIdent + "))*$");

		primitiveTypeMap = new HashMap<String, String>();
		primitiveTypeMap.put("B", "byte");
		primitiveTypeMap.put("S", "short");
		primitiveTypeMap.put("I", "int");
		primitiveTypeMap.put("J", "long");
		primitiveTypeMap.put("C", "char");
		primitiveTypeMap.put("F", "float");
		primitiveTypeMap.put("D", "double");
		primitiveTypeMap.put("Z", "boolean");
		primitiveTypeMap.put("V", "void");
	}
}
