package de.below.codegen;

/**
 * A single argument consisting of name and type.
 * @author martin below
 */
public class Argument {

	private final String type;
	private final String name;

	public Argument(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
}
