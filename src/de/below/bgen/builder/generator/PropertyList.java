package de.below.bgen.builder.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.below.bgen.builder.generator.Property.ConstructorArgument;
import de.below.bgen.builder.generator.Property.SetterProperty;
import de.below.bgen.util.Handler;

/**
 * A mutable collection of {@link Property Properties}
 * 
 * @author martin
 */
public class PropertyList {

	private final List<ConstructorArgument> constructorArgs = new ArrayList<Property.ConstructorArgument>();
	private final List<SetterProperty> setterProperties = new ArrayList<Property.SetterProperty>();
	private List<Property> all;

	public PropertyList() {
	}

	public void add(Property property) {

		if (property instanceof ConstructorArgument) {
			constructorArgs.add((ConstructorArgument) property);
		} else if (property instanceof SetterProperty) {
			setterProperties.add((SetterProperty) property);
		} else {
			throw new IllegalArgumentException("unexpected property type "
					+ property);
		}

	}

	public List<ConstructorArgument> getConstructorArgs() {
		return Collections.unmodifiableList(constructorArgs);
	}

	public List<SetterProperty> getSetterProperties() {
		return Collections.unmodifiableList(setterProperties);
	}

	/**
	 * Visits all properties (constructor arguments and setter methods) present
	 * in this {@link PropertyList}.
	 * 
	 * @param handler
	 *            The visitor
	 */
	public void visitAll(Handler<Property> handler) {
		visitConstructorArgs(handler);
		visitSetterMethods(handler);
	}

	/**
	 * Visits the setter methods present in this {@link PropertyList}.
	 * 
	 * @param handler
	 *            The visitor
	 */
	public void visitSetterMethods(Handler<? super SetterProperty> handler) {

		for (SetterProperty setterProperty : setterProperties) {
			handler.handle(setterProperty);
		}

	}

	/**
	 * Visits the constructor arguments present in this {@link PropertyList}.
	 * 
	 * @param handler
	 *            The visitor
	 */
	public void visitConstructorArgs(
			Handler<? super ConstructorArgument> handler) {

		for (ConstructorArgument constructorArg : constructorArgs) {
			handler.handle(constructorArg);
		}

	}

	/**
	 * FIXME clean up
	 * @return
	 */
	public List<Property >getAll() {
		
		if (all == null) {
			List<Property> result = new ArrayList<Property>();
			result.addAll(constructorArgs);
			result.addAll(setterProperties);
			all = result;
		}
		return all;
	}

}
