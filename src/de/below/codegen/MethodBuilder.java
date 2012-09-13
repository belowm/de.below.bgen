package de.below.codegen;

import java.util.ArrayList;
import java.util.List;

import de.below.bgen.codegen.Visibility;
import de.below.codegen.JavaCodeWriter.Tokens;

public class MethodBuilder<T> {

	
	public static interface MethodVisibilityStep<T> {
		public ModifiersStepStep<T> visibility(Visibility visibility);
	}

	public static interface ModifiersStepStep<T> {

		ModifiersStepStep<T> staticMethod();
		ModifiersStepStep<T> finalMethod();
		ArgsAndNameStep<T> returnType(String returnType);
		ArgsAndNameStep<T> voidMethod();
	}
	
	public static interface ArgsAndNameStep<T> {
		ArgsAndNameStep<T> argument(String type, String name);
		InMethodStep<T> name(String methodName);
	}
	
	public static interface InMethodStep<T> {

		InMethodStep<T> addStatement(String... statement);
		T endMethod();
	}	
	
	
	private final static class Steps<T> 
	implements MethodVisibilityStep<T>, ModifiersStepStep<T>, InMethodStep<T>, ArgsAndNameStep<T> {
		
		private final JavaCodeWriter out;
		private final T parent;
		private Visibility visibility;
		private boolean isStatic;
		private boolean isFinal;
		private String returnType;
		private final List<Argument> arguments 
			= new ArrayList<Argument>();

		public Steps(JavaCodeWriter out, T parent) {
			this.out = out;
			this.parent = parent;
		}

		@Override
		public ModifiersStepStep<T> visibility(Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Override
		public ModifiersStepStep<T> staticMethod() {
			this.isStatic = true;
			return this;
		}

		@Override
		public ModifiersStepStep<T> finalMethod() {
			this.isFinal = true;
			return this;
		}

		@Override
		public InMethodStep<T> name(String name) {
			
			out.renderVisibility(visibility);
			
			if (isStatic) {
				out.writeWs(JavaCodeWriter.Tokens.STATIC);
			}
			
			if (isFinal) {
				out.writeWs(JavaCodeWriter.Tokens.FINAL);
			}
			
			if (returnType == null) {
				out.writeWs(JavaCodeWriter.Tokens.VOID);
			}
			else {
				out.writeWs(returnType);
			}
			out.write(name);
			
			out.openParenthesis();
			out.renderArgumentList(arguments);
			out.closeParenthesis();
			
			out.whitespace();
			out.startBlock();
			
			return this;
		}

		@Override
		public InMethodStep<T> addStatement(String... statement) {
			
			for (String part : statement) {
				out.writeWs(part);
			}
			
			if (statement.length > 0) {
				out.write(Tokens.SEMICOLON);
				out.newLine();
			}
			return this;
		}

		@Override
		public T endMethod() {
			out.endBlock();
			return parent;
		}

		@Override
		public ArgsAndNameStep<T> returnType(String returnType) {
			this.returnType = returnType;
			return this;
		}

		@Override
		public ArgsAndNameStep<T> voidMethod() {
			return returnType(null);
		}

		@Override
		public ArgsAndNameStep<T> argument(String type, String name) {
			arguments.add(new Argument(type, name));
			return this;
		}

	}
	
	public static <T> MethodVisibilityStep<T> newMethod(JavaCodeWriter out, T parent) {
		return new Steps<T>(out, parent);
	}
	
	public static MethodVisibilityStep<JavaCodeWriter> newMethod() {
		JavaCodeWriter writer = JavaCodeWriter.create();
		return new Steps<JavaCodeWriter>(writer, writer);
	}
	
}
