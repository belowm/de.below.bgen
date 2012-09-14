package de.below.codegen;

import java.util.Iterator;
import java.util.List;

import de.below.bgen.codegen.Visibility;
import de.below.bgen.util.CodeGenUtils;

/**
 * Low-Level-API for writing java source code.
 * 
 * @author martin
 * 
 */
public class JavaCodeWriter {

	public static class Tokens {
		public static final String VOID = "void";
		public static final String CLASS = "class";
		public static final String STATIC = "static";
		public static final char NEW_LINE = '\n';
		public static final char WHITESPACE = ' ';
		public static final String FINAL = "final";
		public static final char SEMICOLON = ';';
		public static final String RETURN = "return";
		public static final char DOT = '.';
		public static final String THROWS = "throws";
		public static final char COMMA = ',';
		public static final char EQUAL = '=';
		public static final char DOUBLE_QUOTE = '"';
		public static final String NEW = "new";
		public static final char TAB = '\t';
	}

	private final StringBuilder sink;
	private int indention = 0;
	private boolean newLine;

	public static JavaCodeWriter create() {
		return create(new StringBuilder());
	}

	public static JavaCodeWriter create(StringBuilder strBuilder) {
		return new JavaCodeWriter(strBuilder);
	}

	private JavaCodeWriter(StringBuilder sink) {
		this.sink = sink;
	}

	public JavaCodeWriter startBlock() {
		return writeLine("{").increaseIndention();
	}

	public JavaCodeWriter endBlock() {
		return decreaseIndention().writeLine("}");
	}

	/**
	 * Writes a space character.
	 * 
	 * @return this
	 */
	public JavaCodeWriter spc() {
		return write(Tokens.WHITESPACE);
	}

	public JavaCodeWriter writeLine(String line) {
		return write(line).newLine();
	}

	public JavaCodeWriter write(String string) {
		indent();
		sink.append(string);
		return this;
	}

	public JavaCodeWriter write(char character) {
		indent();
		sink.append(character);
		return this;
	}

	private void indent() {
		if (newLine) {
			for (int i = 0; i < indention; i++) {
				sink.append(Tokens.TAB );
			}
			newLine = false;
		}
	}

	public JavaCodeWriter newLine() {
		sink.append(Tokens.NEW_LINE);
		this.newLine = true;
		return this;
	}

	public JavaCodeWriter increaseIndention() {
		indention++;
		return this;
	}

	public JavaCodeWriter decreaseIndention() {
		if (indention <= 0) {
			throw new IllegalArgumentException("not in a block");
		}
		indention--;
		return this;
	}

	public JavaCodeWriter renderVisibility(Visibility visibility) {

		if (visibility != null && visibility != Visibility.PACKAGE_DEFAULT) {
			writeWs(visibility.name().toLowerCase());
		}

		return this;

	}

	@Override
	public String toString() {
		return render();
	}

	public String render() {
		return sink.toString();
	}

	public JavaCodeWriter openParenthesis() {
		return write("(");
	}

	public JavaCodeWriter closeParenthesis() {
		return write(")");
	}

	/**
	 * Writes a string appending a whitespace.
	 * 
	 * @param str
	 *            The String to write.
	 * @return
	 */
	public JavaCodeWriter writeWs(String str) {
		return write(str).spc();
	}

	public void writeClassDeclaration(String className, Visibility visibility,
			boolean isStatic, boolean isFinal) {

		renderVisibility(visibility);

		if (isStatic) {
			writeWs(Tokens.STATIC);
		}

		if (isFinal) {
			writeWs(Tokens.FINAL);
		}

		writeWs(Tokens.CLASS);
		writeWs(className);

	}

	public JavaCodeWriter renderArgumentList(
			List<? extends Argument> arguments, boolean includeTypes) {

		for (Iterator<? extends Argument> it = arguments.iterator(); it
				.hasNext();) {

			Argument arg = it.next();

			if (includeTypes) {
				writeWs(arg.getType());
			}

			write(arg.getName());

			if (it.hasNext()) {
				write(", ");
			}

		}

		return this;

	}

	public JavaCodeWriter terminateStatement() {
		return write(Tokens.SEMICOLON);
	}

	/**
	 * Creates a representation of the given string where the first letter is
	 * upper-case, and writes it.
	 * 
	 * @param str
	 *            The String to capitalize and write.
	 * @return this
	 */
	public JavaCodeWriter writeCapitalized(String str) {
		return write(CodeGenUtils.capitalize(str));
	}

	public JavaCodeWriter writeWs(char character) {
		return write(character).spc();
	}

}
