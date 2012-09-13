package de.below.bgen.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class CodeGenUtils {

	/**
	 * Returns a representation of the input string where the first letter
	 * starts with a lower case character. The rest of the string remains as it
	 * is.
	 * 
	 * @param inpu
	 *            The string to transform.
	 */
	public static String decapitalize(String input) {

		if (input == null) {
			return null;
		}

		if (input.length() == 1) {
			return input.toLowerCase();
		}

		return Character.toLowerCase(input.charAt(0)) + input.substring(1);
	}

	/**
	 * Returns a representation of the input string where the first letter
	 * starts with a lower case character. The rest of the string remains as it
	 * is.
	 * 
	 * @param inpu
	 *            The string to transform.
	 */
	public static String capitalize(String input) {

		if (input == null) {
			return null;
		}

		if (input.length() == 1) {
			return input.toUpperCase();
		}

		return Character.toUpperCase(input.charAt(0)) + input.substring(1);

	}

	/**
	 * Extracts the property name from a getter- or setter-method name.
	 * 
	 * @param methodName Then name of the accessor method.
	 * @return The property name. Must not be <code>null</code>.
	 */
	public static String getPropertyNameFromAccessorMethod(String methodName) {

		if (methodName.length() > 3 && (methodName.startsWith("set") || methodName.startsWith("get"))) {
			return decapitalize(methodName.substring(3));
		}

		throw new IllegalArgumentException("not an accessor method: "
				+ methodName);

	}

	public static void renderArgumentList(final IMethod method,
			StringBuilder target) throws JavaModelException {
		target.append("(");
		for (int i = 0; i < method.getParameterNames().length; i++) {

			String paramName = method.getParameterNames()[i];

			if (i > 0) {
				target.append(", ");
			}

			target.append(paramName);

		}
		target.append(")");
	}

	public static boolean isGetter(IMethod method) throws JavaModelException {
		String elementName = method.getElementName();
		return elementName.startsWith("get") && elementName.length() > 3
				&& method.getParameterNames().length == 0 && method.getReturnType() != null;
	}
}
