package de.below.bgen.generator;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import de.below.bgen.util.StringUtil;

/**
 * Fetches the properties that should be considered by the builder.
 * 
 * @author Martin Below
 */
public class PropertyCollector {

	/**
	 * Fetches the properties from the target type that are relevant for the
	 * builder.
	 * 
	 * Properties originate from two sources:
	 * <ul>
	 * <li>from the arguments of target's class constructor</li>
	 * <li>from a setter method that is declared on the target class</li>
	 * </ul>
	 * 
	 * @param sourceType
	 *            The type for which the the builder is generated
	 * @param constructorMethod
	 *            The constructor method. May be either a constructor, astatic
	 *            factory method, or <code>null</code>, if the implicit default
	 *            constructor of the class should be used.
	 * @return The properties of the source class that should be considered when
	 *         generating the builder, never <code>null</code>.
	 */
	public PropertyList collectProperties(IType sourceType,
			IMethod constructorMethod, List<IMethod> setterMethods)
			throws JavaModelException {

		PropertyList result = new PropertyList();

		for (IMethod setter : setterMethods) {

			String propertyName = StringUtil.getPropertyNameFromSetter(setter
					.getElementName());
			String type = Signature.toString(setter.getParameterTypes()[0]);
			result.add(new Property.SetterProperty(type, propertyName, setter));
		}

		if (constructorMethod != null) {

			for (int i = 0; i < constructorMethod.getParameterNames().length; i++) {

				String propertyName = constructorMethod.getParameterNames()[i];
				String propertyType = Signature.toString(constructorMethod
						.getParameterTypes()[i]);

				result.add(new Property.ConstructorArgument(propertyType,
						propertyName, constructorMethod, i));
			}
		}

		return result;
	}

}
