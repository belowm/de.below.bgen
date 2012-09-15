package de.below.bgen.builder.generator;

import java.util.Collection;
import java.util.List;

import de.below.bgen.codegen.Expression;
import de.below.codegen.JavaCodeWriter;
import de.below.codegen.JavaCodeWriter.Tokens;

/**
 * Misc statements.
 * 
 * @author martin
 * 
 */
public class Expressions {

	public final static Expression NULL_LITERAL = new SimpleExpression("null");
	private static final Type SELF = type("this");
	
	/**
	 * Call of a static method on a class.
	 * 
	 * @param type
	 *            the class on which the method should be called.
	 * @param methodName
	 *            Name of the method to call.
	 * @param args
	 *            Method arguments.
	 */
	public static Expression methodCall(Type type,
			String methodName, Expression... args) {
		return methodCall(type.name, methodName, args);
	}

	/**
	 * Call of a static method on an object.
	 * 
	 * @param object
	 *            the object on which the method should be called.
	 * @param methodName
	 *            Name of the method to call.
	 * @param args
	 *            Method arguments.
	 */
	public static Expression methodCall(ObjectVar object,
			String methodName, Expression... args) {
		return methodCall(object.getName(), methodName, args);
	}
	
	/**
	 * Call of a java method on an object or a class.
	 * 
	 * @param objectOrClass
	 *            Name of object or class to call the method on. Can be
	 *            <code>null</code>, if method is on current object / class.
	 * @param methodName
	 *            Name of the method to call.
	 * @param args
	 *            Method arguments.
	 */
	private static Expression methodCall(final String objectOrClass,
			final String methodName, final Expression... args) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {

				if (objectOrClass != null) {
					out.write(objectOrClass).write(Tokens.DOT);
				}

				out.write(methodName);
				out.openParenthesis();

				for (int i = 0; i < args.length; i++) {

					if (i > 0) {
						out.write(Tokens.COMMA);
					}
					args[i].render(out);
				}

				out.closeParenthesis();

			}
		};
	}

	/**
	 * Call of a static java method on a class.
	 * 
	 * @param type
	 *            Name of the class to call the method on. 
	 * @param methodName
	 *            Name of the method to call.
	 * @param args
	 *            Method arguments.
	 */
	public static Expression methodCall(final Type type,
			final String methodName, final List<Expression> args) {
		
		return methodCall(type, methodName,
				args.toArray(new Expression[args.size()]));
	}

	/**
	 * Call of a java method on an object
	 * 
	 * @param object
	 *            Name of object to call the method on. 
	 * @param methodName
	 *            Name of the method to call.
	 * @param args
	 *            Method arguments.
	 */
	public static Expression methodCall(final ObjectVar object,
			final String methodName, final List<Expression> args) {
		
		return methodCall(object, methodName,
				args.toArray(new Expression[args.size()]));
	}

	/**
	 * A String literal.
	 */
	public static Expression stringLiteral(final String str) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				out.write(Tokens.DOUBLE_QUOTE).write(str)
						.write(Tokens.DOUBLE_QUOTE);
			}

		};
	}

	/**
	 * Creates an expressions from a string.
	 * 
	 * @author martin
	 */
	private final static class SimpleExpression implements Expression {

		private final String expr;

		public SimpleExpression(String expr) {
			this.expr = expr;
		}

		@Override
		public void render(JavaCodeWriter out) {
			out.write(expr);
		}
	}

	/**
	 * Creates an assignment of an expression to a variable on a given
	 * object / class.
	 * 
	 * @param objectOrClass
	 *            The type / object where the variable exists on.
	 * @param var
	 *            The name of the variable.
	 * @param value
	 *            The value to assign
	 */
	public static Expression assignment(final Type objectOrClass,
			final Variable var, final Expression value) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				out.write(objectOrClass.name).write(Tokens.DOT);
				var.render(out);
				out.spc().writeWs(Tokens.EQUAL);
				value.render(out);
			}
		};
	}

	/**
	 * Creates an assignment of an expression to a local variable or field.
	 * 
	 * @param variable
	 *            Name of the variable / field.
	 * @param value
	 *            The value to assign.
	 */
	public static Expression assignment(final Variable variable,
			final Expression value) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				variable.render(out);
				out.spc().writeWs(Tokens.EQUAL);
				value.render(out);
			}
		};
	}

	/**
	 * Creates a variable declaration and assignment of an expression to it.
	 * 
	 * @param type
	 *            The type of the variable to use in the declaration.
	 * @param variable
	 *            The variable.
	 * @param value
	 *            The expression to assign.
	 */
	public static Expression declarationWithAssignment(final Type type,
			final Variable variable, final Expression value) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				out.writeWs(type.name);
				assignment(variable, value).render(out);
			}
		};
	}

	/**
	 * Creates a number literal.
	 * 
	 * @param number
	 *            The number. If <code>null</code>, zero is applied.
	 */
	public static Expression numberLiteral(final Number number) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				out.write(number == null ? "0" : number.toString());
			}
		};
	}

	/**
	 * Creates a return statement.
	 * 
	 * @param value
	 *            The expression to return.
	 */
	public static Expression returnStatement(final Expression value) {
		return new Expression() {

			@Override
			public void render(JavaCodeWriter out) {
				out.writeWs(Tokens.RETURN);
				value.render(out);
			}
		};
	}

	/**
	 * A variable.
	 * 
	 * @param name
	 *            name of the variable.
	 */
	public static Variable variable(final String name) {
		return new Variable() {

			@Override
			public void render(JavaCodeWriter out) {
				out.write(name);
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}
	
	public static Variable field(final Type type, final String name) {
		
		return new Variable() {

			@Override
			public void render(JavaCodeWriter out) {
				out.write(type.name).write(Tokens.DOT).write(name);
			}

			@Override
			public String getName() {
				return type.name + Tokens.DOT + name;
			}
			
		};
		
	}

	/**
	 * A constructor call.
	 * @param string The class to instantiate. 
	 * @param args the arguments.
	 * @return
	 */
	public static Expression instantiation(final Type type,
			final Expression... args) {
		return new Expression() {
			
			@Override
			public void render(JavaCodeWriter out) {
				out.writeWs(Tokens.NEW);
				methodCall((String)null, type.name, args).render(out);
			}
		};
	}

	/**
	 * A constructor call.
	 * @param string The class to instantiate. 
	 * @param args the arguments.
	 * @return
	 */
	public static Expression instantiation(final Type className,
			Collection<Expression> args) {
		return instantiation(className, args.toArray(new Expression[args.size()]));
	}
	
	public static interface Variable extends Expression {
		String getName();
	}
	
	public static interface ObjectVar extends Variable {
		
	}
	
	/**
	 * A type-name.
	 * @author martin
	 */
	public static class Type {
		
		private final String name;

		public Type(String name) {
			this.name = name;
		}
	}
	
	public static Type type(String name) {
		return new Type(name);
	}
	
	public static Type self() {
		return SELF;
	}
	
	/**
	 * An object instance.
	 * @param name The name of the object.
	 */
	public static ObjectVar object(final String name) {
		return new ObjectVar() {
			
			@Override
			public void render(JavaCodeWriter out) {
				out.write(name);
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}
	
	
	

}

