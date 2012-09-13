package de.below.bgen.bean.generator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.below.bgen.util.CodeGenUtils;

public class MethodAnalyzer {

	private final List<IMethod> getters;
	private final List<IMethod> otherMethods;

	private MethodAnalyzer(List<IMethod> getters, List<IMethod> otherMethods) {
		this.getters = getters;
		this.otherMethods = otherMethods;
	}

	public List<IMethod> getGetters() {
		return getters;
	}

	public List<IMethod> getOtherMethods() {
		return otherMethods;
	}

	public static MethodAnalyzer analyze(IType type) throws JavaModelException {
		
		List<IMethod> getters = new ArrayList<IMethod>();
		List<IMethod> otherMethods = new ArrayList<IMethod>();
		
		for (IMethod method : type.getMethods()) {
			
			if (CodeGenUtils.isGetter(method)) {
				getters.add(method);
			}
			else {
				otherMethods.add(method);
			}
			
		}
		
		
		return new MethodAnalyzer(getters, otherMethods);
	}

	
}
