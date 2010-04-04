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

import net.innig.collect.InnigCollections;
import net.innig.collect.MultiMap;
import net.innig.collect.TreeMultiMap;
import net.innig.macker.util.ClassNameTranslator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The global collection of classes in Macker's rule-checking space.
 * 
 * @author Paul Cantrell
 */
public class ClassManager {
	/**
	 * Create a new {@link ClassManager} instance.
	 */
	public ClassManager() {
		// Trees make nice sorted output
		allClasses = new TreeSet<ClassInfo>(ClassInfoNameComparator.INSTANCE);
		primaryClasses = new TreeSet<ClassInfo>(ClassInfoNameComparator.INSTANCE);
		classNameToInfo = new TreeMap<String, ClassInfo>();
		references = new TreeMultiMap<ClassInfo, ClassInfo>(ClassInfoNameComparator.INSTANCE,
				ClassInfoNameComparator.INSTANCE);
		classLoader = Thread.currentThread().getContextClassLoader();

		for (ClassInfo ci : PrimitiveTypeInfo.ALL)
			replaceClass(ci);
	}

	/**
	 * Get the ClassLoader that is used by the manager.
	 * 
	 * @return The used ClassLoader.
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Set the ClassLoader that the manager should use.
	 * 
	 * @param classLoader The ClassLoader to use.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Parse a class file and add it to the manager.
	 * 
	 * @param classFile The {@link File} object referencing the class file.
	 * @return The {@link ClassInfo} containing the parsed information.
	 * @throws ClassParseException When the class couldn't be parsed.
	 * @throws IOException When reading of the class failed.
	 */
	public ClassInfo readClass(File classFile) throws ClassParseException, IOException {
		ClassInfo classInfo = new ParsedClassInfo(this, classFile);
		addClass(classInfo);
		return classInfo;
	}

	/**
	 * Parse a class file and add it to the manager.
	 * 
	 * @param classFile The {@link InputStream} to the class file.
	 * @return The {@link ClassInfo} containing the parsed information.
	 * @throws ClassParseException When the class couldn't be parsed.
	 * @throws IOException When reading of the class failed.
	 */
	public ClassInfo readClass(InputStream classFile) throws ClassParseException, IOException {
		ClassInfo classInfo = new ParsedClassInfo(this, classFile);
		addClass(classInfo);
		return classInfo;
	}

	/**
	 * Add a class to the manager.
	 * 
	 * @param classInfo The {@link ClassInfo} for the class to add.
	 */
	private void addClass(ClassInfo classInfo) {
		ClassInfo existing = findClassInfo(classInfo.getFullName());
		if (existing != null && !(existing instanceof HollowClassInfo))
			throw new IllegalStateException("ClassManager already contains a class named " + classInfo);
		replaceClass(classInfo);
	}

	/**
	 * Replace a class in the manager.
	 * 
	 * @param classInfo The new {@link ClassInfo} for the class to replace.
	 */
	private void replaceClass(ClassInfo classInfo) {
		allClasses.add(classInfo);
		classNameToInfo.put(classInfo.getFullName(), classInfo);
	}

	/**
	 * Add the class as a primary class in the manager.
	 * 
	 * @param classInfo The {@link ClassInfo} for the class to make primary.
	 */
	public void makePrimary(ClassInfo classInfo) {
		if (!classInfo.isComplete())
			throw new IncompleteClassInfoException(classInfo + " cannot be a primary class, because the"
					+ " class file isn't on Macker's classpath");
		if (classInfo instanceof PrimitiveTypeInfo)
			throw new IllegalArgumentException(classInfo + " cannot be a primary class, because it is a primitive type");
		checkOwner(classInfo);
		classInfo = findClassInfo(classInfo.getFullName()); // in case of hollow
		primaryClasses.add(classInfo);
		references.putAll(classInfo, classInfo.getReferences().keySet());
		allClasses.addAll(classInfo.getReferences().keySet());
	}

	/**
	 * Get all registered classes.
	 * 
	 * @return Unmodifiable set containing all loaded classes.
	 */
	public Set<ClassInfo> getAllClasses() {
		return Collections.unmodifiableSet(allClasses);
	}

	/**
	 * Get all primary classes.
	 * 
	 * @return Unmodifiable set containing all primary classes.
	 */
	public Set<ClassInfo> getPrimaryClasses() {
		return Collections.unmodifiableSet(primaryClasses);
	}

	/**
	 * Get all registered references between classes.
	 * 
	 * @return {@link MultiMap} containing all references.
	 */
	public MultiMap<ClassInfo, ClassInfo> getReferences() {
		return InnigCollections.unmodifiableMultiMap(references);
	}

	/**
	 * Get the {@link ClassInfo} for the given class name, updating the class if it was already registered.
	 * 
	 * @param className The fully qualified class name.
	 * @return The {@link ClassInfo} for the class.
	 */
	public ClassInfo getClassInfo(String className) {
		ClassInfo classInfo = findClassInfo(className);
		if (classInfo != null)
			return classInfo;
		else {
			classInfo = new HollowClassInfo(this, className);
			replaceClass(classInfo);
			return classInfo;
		}
	}

	/**
	 * Load the {@link CassInfo}
	 * 
	 * @param className
	 * @return
	 */
	ClassInfo loadClassInfo(String className) {
		ClassInfo classInfo = findClassInfo(className);
		if (classInfo == null || classInfo instanceof HollowClassInfo) {
			classInfo = null; // don't use hollow!
			String resourceName = ClassNameTranslator.classToResourceName(className);
			InputStream classStream = classLoader.getResourceAsStream(resourceName);

			if (classStream == null) {
				showIncompleteWarning();
				System.out.println("WARNING: Unable to find class " + className + " in the classpath");
			} else
				try {
					classInfo = new ParsedClassInfo(this, classStream);
				} catch (Exception e) {
					if (e instanceof RuntimeException)
						throw (RuntimeException) e;
					showIncompleteWarning();
					System.out.println("WARNING: Unable to load class " + className + ": " + e);
				} finally {
					try {
						classStream.close();
					} catch (IOException ioe) {
					} // nothing we can do
				}

			if (classInfo == null)
				classInfo = new IncompleteClassInfo(this, className);

			replaceClass(classInfo);
		}

		return classInfo;
	}

	private ClassInfo findClassInfo(String className) {
		return classNameToInfo.get(className);
	}

	/**
	 * Check if the current {@link ClassManager} is managing the class.
	 * 
	 * @param classInfo the {@link ClassInfo} of the class to check.
	 * @throws IllegalStateException When the {@link ClassManager} is not managing the class.
	 */
	private void checkOwner(ClassInfo classInfo) throws IllegalStateException {
		if (classInfo.getClassManager() != this)
			throw new IllegalStateException("classInfo argument (" + classInfo
					+ ") is not managed by this ClassManager");
	}

	/**
	 * Show a warning on {@link System.out} that not all referenced classes can be found.
	 */
	private void showIncompleteWarning() {
		if (!incompleteClassWarning) {
			incompleteClassWarning = true;
			System.out.println("WARNING: Macker is unable to load some of the external classes"
					+ " used by the primary classes (see warnings below).  Rules which"
					+ " depend on attributes of these missing classes other than their" + " names will fail.");
		}
	}

	/** Flag indicating if all referenced, external, classes could be resolved. */
	private boolean incompleteClassWarning;
	/** The ClassLoader to use. */
	private ClassLoader classLoader;
	/** Set holding all classes. */
	private Set<ClassInfo> allClasses;
	/** Set holding all primary classes. */
	private Set<ClassInfo> primaryClasses;
	/**
	 * Map holding all {@link ClassInfo} instances, indexed by their fully qualified name.
	 */
	private Map<String, ClassInfo> classNameToInfo;
	/** MultiMap holding all references between classes. */
	private MultiMap<ClassInfo, ClassInfo> references;
}
