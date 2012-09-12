package de.below.bgen.generator;

import org.eclipse.jdt.core.IMethod;

/**
 * @author martin
 */
public abstract class Property {

	private final String type;
	private final String name;

	Property(String type, String name) {
		this.type = type;
		this.name = name;
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

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Property [type=" + type + ", name=" + name + "]";
	}

}
