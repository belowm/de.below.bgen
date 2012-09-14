package de.below.bgen.util;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Static utility-methods for dealing with the Eclipse JDT (Java Development
 * Tools) API.
 * 
 * @author martin
 */
public class JdtUtils {

	/**
	 * @return <code>true</code>, if the given method is a public method.
	 */
	public static boolean isPublic(IMethod method) throws JavaModelException {
		int flag = Flags.AccPublic;
		return hasFlag(method, flag);
	}

	/**
	 * @return <code>true</code>, if the method is flagged as static.
	 */
	public static boolean isStatic(IMethod method) throws JavaModelException {
		return hasFlag(method, Flags.AccStatic);
	}
	
	/**
	 * @return <code>true</code> if the given method has the given flag
	 * @see Flags
	 */
	private static boolean hasFlag(IMethod method, int flag)
			throws JavaModelException {
		return (method.getFlags() & flag) > 0;
	}

}
