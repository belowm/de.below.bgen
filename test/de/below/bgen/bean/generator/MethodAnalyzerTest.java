package de.below.bgen.bean.generator;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;


public class MethodAnalyzerTest {

	@Test
	public void testWithGetterMethod() throws JavaModelException {
		
		IType type = createMock(IType.class);
		
		IMethod getter = createMock(IMethod.class);
		expect(getter.getElementName()).andStubReturn("getFoo");
		expect(getter.getReturnType()).andStubReturn("String");
		expect(getter.getParameterNames()).andReturn(new String[] {});
		expect(getter.getParameterTypes()).andReturn(new String[] {});
		
		expect(type.getMethods()).andStubReturn(new IMethod[] {
			getter
		});
		
		replay(type, getter);
		
		MethodAnalyzer analysis = MethodAnalyzer.analyze(type);
		
		assertEquals("number of getters", 1, analysis.getGetters().size());
		
	}
	
	@Test
	public void testGetterWithArgument() throws JavaModelException {
		
		IType type = createMock(IType.class);
		
		IMethod getter = createMock(IMethod.class);
		expect(getter.getElementName()).andStubReturn("getFoo");
		expect(getter.getReturnType()).andStubReturn("QString;");
		expect(getter.getParameterNames()).andReturn(new String[] { "arg" });
		expect(getter.getParameterTypes()).andReturn(new String[] { "QString;" });
		
		expect(type.getMethods()).andStubReturn(new IMethod[] {
			getter
		});
		
		replay(type, getter);
		
		MethodAnalyzer analysis = MethodAnalyzer.analyze(type);
		
		assertEquals("number of getters", 0, analysis.getGetters().size());
		assertEquals("number of other methods", 1, analysis.getOtherMethods().size());
		
	}
	
	@Test
	public void testGetterWithNoReturnType() throws JavaModelException {
		
		IType type = createMock(IType.class);
		
		IMethod getter = createMock(IMethod.class);
		expect(getter.getElementName()).andStubReturn("getFoo");
		expect(getter.getReturnType()).andStubReturn(null);
		expect(getter.getParameterNames()).andReturn(new String[] { });
		expect(getter.getParameterTypes()).andReturn(new String[] { });
		
		expect(type.getMethods()).andStubReturn(new IMethod[] {
			getter
		});
		
		replay(type, getter);
		
		MethodAnalyzer analysis = MethodAnalyzer.analyze(type);
		
		assertEquals("number of getters", 0, analysis.getGetters().size());
		assertEquals("number of other methods", 1, analysis.getOtherMethods().size());

		
	}	

	
	@Test
	public void testWithOtherMethod() throws JavaModelException {
		
		IType type = createMock(IType.class);
		
		IMethod method = createMock(IMethod.class);
		expect(method.getElementName()).andStubReturn("foo");
		expect(method.getReturnType()).andStubReturn("String");
		expect(method.getParameterNames()).andReturn(new String[] {});
		expect(method.getParameterTypes()).andReturn(new String[] {});
		
		expect(type.getMethods()).andStubReturn(new IMethod[] {
			method
		});
		
		replay(type, method);
		
		MethodAnalyzer analysis = MethodAnalyzer.analyze(type);
		
		assertEquals("number of getters", 0, analysis.getGetters().size());
		assertEquals("number of other methods", 1, analysis.getOtherMethods().size());
		
		
		
	}
	
}
