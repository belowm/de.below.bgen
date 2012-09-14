package de.below.codegen;

import de.below.bgen.codegen.Visibility;
import de.below.codegen.JavaCodeWriter.Tokens;

/**
 * Facility for building java field declarations.
 * @author martin below
 */
public class FieldBuilder {

	public static interface FieldTypeStep<T> {
		
		/**
		 * Sets the type of the field.
		 * @param typeName The field to set. Must not be <code>null</code>.
		 * @return The next step
		 */
		FieldNameStep<T> type(String typeName);
	}
	
	public static interface FieldNameStep<T> {
		
		/**
		 * Sets the name of the field.
		 * @param name The name to set. Must not be <code>null</code>.
		 * @return The next step.
		 */
		BuildStep<T> name(String name);
	}
	
	public static interface BuildStep<T> {
	
		/**
		 * Configures the builder to create a final field.
		 * @return this step
		 */
		BuildStep<T> finalField(boolean isFinal);
		
		/**
		 * Configures the builder to create a static field.
		 * @return this step
		 */
		BuildStep<T> staticField(boolean staticField);

		/**
		 * Sets the fields visibility
		 * @param visibility The visibility to set.
		 * @return The next step
		 */
		BuildStep<T> visibility(Visibility visibility);

		/**
		 * Renders the type.
		 * 
		 * @return The parent object given on construction of the class.
		 */
		T buildField();
	}
	
	private final static class Steps<T> implements FieldTypeStep<T>, FieldNameStep<T>, BuildStep<T> {

		private boolean finalField;
		private Visibility visibility;
		private String name;
		private String typeName;
		private final JavaCodeWriter out;
		private boolean staticField;
		private final T parent;

		Steps(JavaCodeWriter out, T parent) {
			this.out = out;
			this.parent = parent;
		}
		
		@Override
		public BuildStep<T> finalField(boolean isFinal) {
			this.finalField = isFinal;
			return this;
		}
		
		@Override
		public BuildStep<T> staticField(boolean staticField) {
			this.staticField = staticField;
			return this;
		}
		

		@Override
		public BuildStep<T> visibility(Visibility visibility) {
			this.visibility = visibility;
			return this;
		}
		
		@Override
		public BuildStep<T> name(String name) {
			this.name = name;
			return this;
		}
		
		@Override
		public FieldNameStep<T> type(String typeName) {
			this.typeName = typeName;
			return this;
		}

		@Override
		public T buildField() {
			
			out.renderVisibility(visibility);
			
			if (finalField) {
				out.writeWs(Tokens.FINAL);
			}
			
			if (staticField) {
				out.writeWs(Tokens.STATIC);
			}
			
			out.writeWs(typeName)
				.write(name)
				.write(';')
				.newLine()
				;
			
			return parent;
		}

	}
	
	public static <T> FieldTypeStep<T> newField(JavaCodeWriter out, T parent) {
		return new Steps<T>(out, parent);
	}
	
	public static FieldTypeStep<JavaCodeWriter> newField() {
		JavaCodeWriter writer = JavaCodeWriter.create();
		return newField(writer, writer);
	}
	
}
