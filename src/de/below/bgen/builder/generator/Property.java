package de.below.bgen.builder.generator;

import org.eclipse.jdt.core.IMethod;

import de.below.bgen.util.CodeGenUtils;
import de.below.codegen.Argument;

/**
 * @author martin
 */
public abstract class Property extends Argument {

	Property(String type, String name) {
		super(type, name);
	}

	/**
	 * A property that is passed to the target class as an constructor argument.
	 * 
	 * @author martin
	 */
	public static class ConstructorArgument extends Property {
		private final IMethod constructor;
		private final int index;

		/**
		 * @param type
		 *            The type of the argument
		 * @param name
		 *            The name of the argument
		 * @param constructor
		 *            The constructor this argument belongs to
		 * @param index
		 *            The index of the constructor argument, starting at 0.
		 */
		public ConstructorArgument(String type, String name,
				IMethod constructor, int index) {
			super(type, name);
			this.constructor = constructor;
			this.index = index;
		}

		public IMethod getConstructor() {
			return constructor;
		}

		public int getIndex() {
			return index;
		}

	}

	/**
	 * A property that is accessable via a "setter" method.
	 * 
	 * @author martin
	 * 
	 */
	public static class SetterProperty extends Property {

		private final IMethod setter;

		SetterProperty(String type, String name, IMethod setter) {
			super(type, name);
			this.setter = setter;
		}

		public IMethod getSetter() {
			return setter;
		}

	}

	@Override
	public String toString() {
		return "Property [type=" + getType() + ", name=" + getName() + "]";
	}

	/**
	 * Returns the name of the getter method for this property.
	 * 
	 * @return The getter method name, never <code>null</code>.
	 */
	public String renderGetterName() {
		return "get" + CodeGenUtils.capitalize(getName());
	}

	/**
	 * Returns the name of the getter method for this property.
	 * 
	 * @return The getter method name, never <code>null</code>.
	 */
	public String renderSetterName() {
		return "set" + CodeGenUtils.capitalize(getName());
	}
	
}
