package de.below.bgen.util;


public class StringUtil {

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
	
	public static String getPropertyNameFromSetter(String methodName) {
		return decapitalize(methodName.substring(3));
	}
	

}
