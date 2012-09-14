package de.below.bgen.codegen;

import de.below.codegen.JavaCodeWriter;

public interface Expression {
	public void render(JavaCodeWriter out);
}
