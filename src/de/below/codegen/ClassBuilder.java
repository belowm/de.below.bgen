package de.below.codegen;

import de.below.bgen.codegen.Visibility;
import de.below.codegen.ConstructorBuilder.ConstructorVisibilityStep;
import de.below.codegen.FieldBuilder.FieldTypeStep;
import de.below.codegen.MethodBuilder.MethodVisibilityStep;

public class ClassBuilder {

	public static interface VisibilityStep<T> {

		/**
		 * Sets the visibility of the class to be created.
		 * 
		 * @param visibility
		 *            The visibility to set
		 */
		ClassNameStep<T> visibility(Visibility visibility);

	}

	public static interface ClassNameStep<T> {

		/**
		 * Creates a static class.
		 */
		ClassNameStep<T> staticClass();

		/**
		 * Creates a final class
		 */
		ClassNameStep<T> finalClass();

		/**
		 * Sets the name of the class.
		 * 
		 * @param string
		 *            The class name
		 */
		InClassStep<T> name(String string);

	}

	public static interface InClassStep<T> {

		/**
		 * Adds a new Method to the class.
		 */
		MethodVisibilityStep<InClassStep<T>> beginMethod();

		/**
		 * Adds a new Constructor to the class.
		 */
		ConstructorVisibilityStep<InClassStep<T>> beginConstructor();

		/**
		 * Adds a nested class.
		 */
		VisibilityStep<InClassStep<T>> beginNestedClass();
		
		/**
		 * Adds a new field to the class.
		 */
		FieldTypeStep<InClassStep<T>> beginField();

		/**
		 * Finishes creation of the class.
		 * 
		 * @return The parent type given upon construction of the builder, or a
		 *         {@link JavaCodeWriter} if no parent was given.
		 */
		T endClass();

	}

	private final static class Steps<T> implements VisibilityStep<T>,
			ClassNameStep<T>, InClassStep<T> {

		private Visibility visibility;
		private boolean isStatic;
		private boolean isFinal;

		private final JavaCodeWriter out;
		private final T parent;
		private String className;

		public Steps(JavaCodeWriter writer, T parent) {
			this.out = writer;
			this.parent = parent;
		}

		@Override
		public ClassNameStep<T> visibility(Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Override
		public ClassNameStep<T> staticClass() {
			this.isStatic = true;
			return this;
		}

		@Override
		public ClassNameStep<T> finalClass() {
			this.isFinal = true;
			return this;
		}

		@Override
		public InClassStep<T> name(String className) {

			this.className = className;
			out.writeClassDeclaration(className, visibility, isStatic, isFinal);
			out.startBlock();
			return this;
		}

		@Override
		public T endClass() {
			out.endBlock();
			return parent;
		}

		@Override
		public MethodVisibilityStep<InClassStep<T>> beginMethod() {
			return MethodBuilder.<InClassStep<T>>newMethod(out, this);
		}

		@Override
		public VisibilityStep<InClassStep<T>> beginNestedClass() {
			return ClassBuilder.<InClassStep<T>>newClass(out, this);
		}

		@Override
		public FieldTypeStep<InClassStep<T>> beginField() {
			return FieldBuilder.<InClassStep<T>>newField(out, this);
		}

		@Override
		public ConstructorVisibilityStep<InClassStep<T>> beginConstructor() {
			return ConstructorBuilder.<InClassStep<T>>newConstructor(out, this, className);
		}

	}

	public static <T> VisibilityStep<T> newClass(JavaCodeWriter writer,
			T parent) {
		return new Steps<T>(writer, parent);

	}

	public static VisibilityStep<JavaCodeWriter> newClass() {
		JavaCodeWriter writer = JavaCodeWriter.create();
		return new Steps<JavaCodeWriter>(writer, writer);
	}

}
