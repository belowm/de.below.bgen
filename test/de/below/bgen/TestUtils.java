package de.below.bgen;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class TestUtils {

	private static final String WHITESPACE = " ";
	private final static Pattern NORMALIZE_SOURCE_PATTERN = Pattern.compile("[ ]{2,}|[\\n\\r]");
	
	/**
	 * Asserts that a method with the given name and arguments exists on the
	 * given type.
	 * 
	 * @param type
	 *            The type where the method is expected to exist
	 * @param methodName
	 *            The name of the method to look for
	 * @param arguments
	 *            The arguments of the method.
	 */
	public static void assertMethodExists(IType type, String methodName,
			String... arguments) {

		IMethod method = type.getMethod(methodName, arguments);
		assertTrue("did not create method " + methodName + "(" + Arrays.toString(arguments) + ")", method.exists());
	}

	/**
	 * Asserts that a constructor with the given arguments exists on the given
	 * type.
	 * 
	 * @param type
	 *            The type where the method is expected to exist
	 * @param arguments
	 *            The arguments of the method.
	 */
	public static void assertConstructorExists(IType type, String... arguments)
			throws JavaModelException {

		IMethod method = type.getMethod(type.getElementName(), arguments);
		assertTrue(
				"did not create constructor with arguments "
						+ Arrays.toString(arguments) + " - existing methods: " + Arrays.toString(type.getMethods()),
				method.exists() && method.isConstructor());
	}

	/**
	 * Asserts that the return type of a method if of a given type. given type.
	 * 
	 * @param method
	 *            The method whose return type should be asserted
	 * @param expectedReturnTypeName
	 *            The expected return type of the method.
	 */
	public static void assertReturnType(IMethod method,
			String expectedReturnTypeName) throws JavaModelException {

		assertEquals("return-type of method" + method,
				Signature.toString(expectedReturnTypeName),
				Signature.toString(method.getReturnType()));
	}

	/**
	 * Asserts that a field with the given name is pesent on the given type.
	 * 
	 * @param type
	 *            The type where the field is expected to exists.
	 * @param fieldName
	 *            The name of the field to look for.
	 */
	public static void assertFieldExists(IType type, String fieldName) {
		assertTrue("did not create field for " + fieldName,
				type.getField(fieldName).exists());
	}

	/**
	 * Creates a mock type with the given name.
	 * <p>
	 * <b>Note</b> that the result is an easy mock object that only returns its
	 * name! all other methods return <code>null</code>. Therefore, the result
	 * does not conform to the {@link IType} API!
	 * </p>
	 * 
	 * FIXME: replace with a proper mock
	 * 
	 * @param name
	 *            The name of the type.
	 * @param replay
	 *            If <code>true</code>, the easy mock object is put into replay
	 *            state.
	 * @return The mock object, never <code>null</code>
	 */
	public static IType createMockType(String name, boolean replay) {
		IType type = createMock(IType.class);
		expect(type.getElementName()).andReturn(name);

		if (replay) {
			replay(type);
		}

		return type;
	}

	/**
	 * Asserts that two source code strings are equal. This method removed
	 * linebreaks and normalizes whitespaces before the comparison.
	 * 
	 * @param message
	 * @param expected
	 * @param actual
	 */
	public static void assertCodeEquals(String message, String expected,
			String actual) {
		assertEquals(message, normalize(expected), normalize(actual));
	}

	private static String normalize(String input) {
		
		if (input == null)
			return null;
		
		return NORMALIZE_SOURCE_PATTERN.matcher(input.trim()).replaceAll(WHITESPACE);
	}

}
