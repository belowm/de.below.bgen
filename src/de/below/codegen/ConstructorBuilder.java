package de.below.codegen;

import java.util.ArrayList;
import java.util.List;

import de.below.bgen.codegen.Visibility;
import de.below.codegen.JavaCodeWriter.Tokens;

public class ConstructorBuilder {
	
	public static interface ConstructorVisibilityStep<T> {
		public HasArgsStep<T> visibility(Visibility visibility);
	}
	
	public static interface HasArgsStep<T> {
		public InConstructorStep<T> noArguments();
		public ArgsStep<T> withArguments();
	}

	public static interface ArgsStep<T> {
		
		ArgsStep<T> argument(String type, String name);
		InConstructorStep<T> endArguments();
		
	}
	
	public static interface InConstructorStep<T> {
		InConstructorStep<T> addStatement(String... statement);
		T endConstructor();
	}	
	
	
	private final static class Steps<T> 
	implements ConstructorVisibilityStep<T>, InConstructorStep<T>, ArgsStep<T>, HasArgsStep<T> {
		
		private final JavaCodeWriter out;
		private final T parent;
		private Visibility visibility;
		private final List<Argument> arguments 
			= new ArrayList<Argument>();
		private final String typeName;

		public Steps(JavaCodeWriter out, T parent, String typeName) {
			this.out = out;
			this.parent = parent;
			this.typeName = typeName;
		}

		@Override
		public HasArgsStep<T> visibility(Visibility visibility) {
			this.visibility = visibility;
			return this;
		}
		
		@Override
		public InConstructorStep<T> addStatement(String... statement) {
			
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
		public T endConstructor() {
			out.endBlock();
			return parent;
		}

		@Override
		public ArgsStep<T> argument(String type, String name) {
			arguments.add(new Argument(type, name));
			return this;
		}

		@Override
		public InConstructorStep<T> endArguments() {
			
			out.renderVisibility(visibility);
			
			out.write(typeName);
			
			out.openParenthesis();
			out.renderArgumentList(arguments);
			out.closeParenthesis();
			
			out.whitespace();
			out.startBlock();
			
			return this;
		}

		@Override
		public InConstructorStep<T> noArguments() {
			return endArguments();
		}

		@Override
		public ArgsStep<T> withArguments() {
			return this;
		}


	}
	
	public static <T> ConstructorVisibilityStep<T> newConstructor(JavaCodeWriter out, T parent, String typeName) {
		return new Steps<T>(out, parent, typeName);
	}
	
	public static ConstructorVisibilityStep<JavaCodeWriter> newConstructor(String typeName) {
		JavaCodeWriter writer = JavaCodeWriter.create();
		return new Steps<JavaCodeWriter>(writer, writer, typeName);
	}
	
}
