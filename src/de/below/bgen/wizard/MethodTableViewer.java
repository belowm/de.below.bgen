package de.below.bgen.wizard;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.widgets.Composite;

import de.below.rcp.widgets.SimpleTableViewer;

public class MethodTableViewer extends SimpleTableViewer<IMethod> {

	public static enum Columns implements ColumnProvider<IMethod> {
		
		SIGNATURE(new TextColumn<IMethod>() {
			
			@Override
			public String getLabel(IMethod entity) {
				return LabelProviders.METHOD.getText(entity);
			}

			@Override
			public String getColumnHeader() {
				return "Setter";
			}
		});
		
		;

		private final Column<IMethod, ?> column;
		
		private Columns(Column<IMethod, ?> column) {
			this.column = column;
		}
		
		@Override
		public SimpleTableViewer.Column<IMethod, ?> getColumn() {
			return column;
		}
		
	}
	
	public MethodTableViewer(
			Composite parent,
			int style,
			int viewerStyle,
			Columns... visibleColumns) {
		
		super(parent, style, viewerStyle, IMethod.class, visibleColumns);
		
	}

}
