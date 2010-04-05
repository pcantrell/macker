package net.innig.macker.structure;

import java.io.Serializable;
import java.util.Comparator;

public class ClassInfoNameComparator implements Comparator<ClassInfo>, Serializable {

	public int compare(final ClassInfo a, final ClassInfo b) {
		return a.getFullName().compareTo(b.getFullName());
	}
}
