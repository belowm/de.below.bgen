package de.below.bgen.wizard;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import de.below.rcp.widgets.SimpleLabelProvider;

public class LabelProviders {

	protected static final SimpleLabelProvider<IMethod> METHOD = new SimpleLabelProvider<IMethod>(
			IMethod.class) {

		@Override
		protected String getTextFor(IMethod item) {
			
			StringBuilder result = new StringBuilder(item.getElementName());
			
			result.append("(");
			
			try {
				for (int i = 0; i < item.getParameterNames().length; i++) {
					
					if (i > 0) {
						result.append(", ");
					}
					
					result.append(Signature.toString(item.getParameterTypes()[i]))
						.append(" ")
						.append(item.getParameterNames()[i]);
					
				}
			} 
			catch (JavaModelException e) {
				e.printStackTrace();
			}
			result.append(")");
			return result.toString();
		}

		@Override
		protected Image getImageFor(IMethod item) {
			return null;
		}

	};
	
	protected static final ILabelProvider SOURCE_FOLDER = new SimpleLabelProvider<IPackageFragmentRoot>(IPackageFragmentRoot.class) {

		@Override
		protected String getTextFor(IPackageFragmentRoot item) {
			return item.getElementName();
		}

		@Override
		protected Image getImageFor(IPackageFragmentRoot item) {
			return null;
		}
	};

}
