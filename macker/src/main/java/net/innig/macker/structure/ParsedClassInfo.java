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

import net.innig.collect.CompositeMultiMap;
import net.innig.collect.InnigCollections;
import net.innig.collect.MultiMap;
import net.innig.macker.util.ClassNameTranslator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * Class info retrieved from a class file.
 * 
 * @author Paul Cantrell
 */
public class ParsedClassInfo extends AbstractClassInfo {
	
	private String fullClassName;
	private boolean isInterface;
	private boolean isAbstract;
	private boolean isFinal;
	private AccessModifier accessModifier;
	private ClassInfo extendsClass;
	private Set<ClassInfo> implementsClasses;
	private MultiMap<ClassInfo, Reference> references;

	ParsedClassInfo(final ClassManager classManager, final File classFile) throws IOException, ClassParseException {
		super(classManager);
		try {
			parse(new ClassParser(classFile.getPath()).parse());
		} catch (ClassFormatError cfe) {
			throw new ClassParseException(cfe);
		}
	}

	ParsedClassInfo(final ClassManager classManager, final InputStream classFileStream)
			throws IOException, ClassParseException {
		super(classManager);
		try {
			parse(new ClassParser(classFileStream, null).parse());
		} catch (ClassFormatError cfe) {
			throw new ClassParseException(cfe);
		}
	}

	private void parse(final JavaClass classFile) throws ClassParseException {
		parseClassName(classFile);
		parseFlags(classFile);
		parseAccess(classFile);
		parseExtends(classFile);
		parseImplements(classFile);
		parseReferences(classFile);
	}

	private void parseClassName(final JavaClass classFile) {
		this.fullClassName = classFile.getClassName();
	}

	public String getFullClassName() {
		return this.fullClassName;
	}

	public boolean isComplete() {
		return true;
	}

	private void parseFlags(final JavaClass classFile) throws ClassParseException {
		this.isInterface = classFile.isInterface();
		this.isAbstract = classFile.isAbstract();
		this.isFinal = classFile.isFinal();
	}

	private void parseAccess(final JavaClass classFile) throws ClassParseException {
		if (getFullClassName().indexOf('$') == -1) {
			// classes!
			setAccessModifier(translateAccess(classFile));
		} else {
			final Attribute[] attributes = classFile.getAttributes();
			final String classNameRaw = classFile.getClassName().replace('.', '/');
			for (int a = 0; a < attributes.length; a++) {
				if (attributes[a] instanceof InnerClasses) {
					final InnerClass[] inners = ((InnerClasses) attributes[a]).getInnerClasses();
					for (int i = 0; i < inners.length; i++) {
						final String innerClassNameRaw = classFile.getConstantPool().getConstantString(
								inners[i].getInnerClassIndex(), org.apache.bcel.Constants.CONSTANT_Class);
						if (innerClassNameRaw.equals(classNameRaw)) {
							if (getAccessModifier() != null) {
								throw new ClassParseException("Found multiple inner class attributes for " + this,
										classFile);
							}
							setAccessModifier(translateAccess(new AccessFlags(inners[i].getInnerAccessFlags()) {
								private static final long serialVersionUID = -2899679656234174128L;
							}));
						}
					}
				}
			}
			if (getAccessModifier() == null) {
				throw new ClassParseException("Could not find any class attributes for " + this, classFile);
			}
		}
	}

	public boolean isInterface() {
		return this.isInterface;
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public boolean isFinal() {
		return this.isFinal;
	}

	public AccessModifier getAccessModifier() {
		return this.accessModifier;
	}

	private void parseExtends(final JavaClass classFile) throws ClassParseException {
		this.extendsClass = getSafeClassInfo(classFile.getSuperclassName());
	}

	public ClassInfo getExtends() {
		return this.extendsClass;
	}

	private void parseImplements(final JavaClass classFile) throws ClassParseException {
		this.implementsClasses = new TreeSet<ClassInfo>(new ClassInfoNameComparator());
		final String[] names = classFile.getInterfaceNames();
		for (int n = 0; n < names.length; n++) {
			getImplements().add(getSafeClassInfo(names[n]));
		}
		this.implementsClasses = Collections.unmodifiableSet(getImplements());
	}

	public Set<ClassInfo> getImplements() {
		return this.implementsClasses;
	}

	private void parseReferences(final JavaClass classFile) throws ClassParseException {
		this.references = new CompositeMultiMap<ClassInfo, Reference>(new TreeMap<ClassInfo, Set<Reference>>(
				new ClassInfoNameComparator()), HashSet.class);
		parseConstantPoolReferences(classFile);
		parseMethodReferences(classFile);
		parseFieldReferences(classFile);
		this.references = InnigCollections.unmodifiableMultiMap(getReferences());
	}

	private void parseConstantPoolReferences(final JavaClass classFile) throws ClassParseException {
		// Add accessed classes from constant pool entries
		final ConstantPool constantPool = classFile.getConstantPool();
		final Constant[] constants = constantPool.getConstantPool();
		for (int a = 1; a < constants.length; a++) {
			if (constants[a] instanceof ConstantClass) {
				addReference(new Reference(this, getSafeClassInfo(constantPool.constantToString(constants[a])),
						ReferenceType.CONSTANT_POOL, null, null));
			}
		}
	}

	private void parseMethodReferences(final JavaClass classFile) throws ClassParseException {
		// Add yet more accessed classes from method & field signatures
		final Method[] methods = classFile.getMethods();
		for (int m = 0; m < methods.length; m++) {
			final Method method = methods[m];
			final AccessModifier methodAccess = translateAccess(method);

			final List<String> paramsAndReturn = ClassNameTranslator.signatureToClassNames(method.getSignature());
			if (paramsAndReturn.isEmpty()) {
				throw new ClassParseException("unable to read types for method " + getFullClassName() + '.'
						+ method.getName(), classFile);
			}

			for (final Iterator<String> i = paramsAndReturn.iterator(); i.hasNext();) {
				final String refTo = i.next();
				final ClassInfo safeClassInfo = getSafeClassInfo(refTo, method.getSignature());
				final ReferenceType referenceType;
				if (i.hasNext()) {
					referenceType = ReferenceType.METHOD_PARAM;
				} else {
					referenceType = ReferenceType.METHOD_RETURNS;
				}
				
				addReference(new Reference(this, safeClassInfo, referenceType, method.getName(), methodAccess));
			}

			if (method.getExceptionTable() != null) {
				final String[] exceptionNames = method.getExceptionTable().getExceptionNames();
				for (int e = 0; e < exceptionNames.length; e++) {
					addReference(new Reference(this, getSafeClassInfo(exceptionNames[e]), ReferenceType.METHOD_THROWS,
							method.getName(), methodAccess));
				}
			}
		}
	}

	private void parseFieldReferences(final JavaClass classFile) throws ClassParseException {
		final Field[] fields = classFile.getFields();
		for (int a = 0; a < fields.length; a++) {
			final Field field = fields[a];
			final List<String> types = ClassNameTranslator.signatureToClassNames(field.getSignature());
			if (types.size() != 1) {
				throw new ClassParseException("expected one type for field " + getFullClassName() + '.'
					+ field.getName() + "; got: " + types + " (signature is \"" + field.getSignature()
					+ "\")", classFile);
			}

			addReference(new Reference(this, getSafeClassInfo(types.get(0), field.getSignature()),
					ReferenceType.FIELD_SIGNATURE, field.getName(), translateAccess(field)));
		}
	}

	private AccessModifier translateAccess(final AccessFlags accessFlags) throws ClassParseException {
		if (accessFlags.isPublic()) {
			return AccessModifier.PUBLIC;
		}
		
		if (accessFlags.isProtected()) {
			return AccessModifier.PROTECTED;
		}
		
		if (accessFlags.isPrivate()) {
			return AccessModifier.PRIVATE;
		}
		
		return AccessModifier.PACKAGE;
	}

	private ClassInfo getSafeClassInfo(final String className) throws ClassParseException {
		return getSafeClassInfo(ClassNameTranslator.typeConstantToClassName(className), className);
	}

	private ClassInfo getSafeClassInfo(final String className, final String unparsedClassName)
			throws ClassParseException {
		if (!ClassNameTranslator.isJavaIdentifier(className)) {
			throw new ClassParseException("unable to parse class name / signature: \"" + unparsedClassName
					+ "\" (got \"" + className + "\")");
		}
		return getClassManager().getClassInfo(className);
	}

	private void addReference(final Reference ref) {
		getReferences().put(ref.getTo(), ref);
	}

	public MultiMap<ClassInfo, Reference> getReferences() {
		return this.references;
	}
	
	private void setAccessModifier(final AccessModifier accessModifier) {
		this.accessModifier = accessModifier;
	}
}
