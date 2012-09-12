package de.below.bgen.generator.components;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;


public class InstantiationStrategyTest {

	@Test
	public void renderImplicitDefaultConstructorCall() throws JavaModelException {
		
		IType type = createMockType();

		replay(type);

		String instantiation = InstantiationStrategy.implicitDefaultConstructorCall(type).renderInstantiation();
		
		assertEquals("constructor call", "new Person()", instantiation);
		
	}

	
	@Test
	public void renderConstructorCallWithNoArgument() throws JavaModelException {
		
		IType type = createMockType();
		IMethod method = createMock(IMethod.class);
		
		expect(method.getParameterNames()).andStubReturn(new String[] { } );
		expect(method.getParameterTypes()).andStubReturn(new String[] { } );
		expect(method.getDeclaringType()).andStubReturn(type);
		
		replay(type, method);

		String instantiation = InstantiationStrategy.constructorCall(method).renderInstantiation();
		
		assertEquals("constructor call", "new Person()", instantiation);
		
	}
	
	
	@Test
	public void renderConstructorCallWithOneArgument() throws JavaModelException {
		
		IType type = createMockType();
		IMethod method = createMock(IMethod.class);
		
		expect(method.getParameterNames()).andStubReturn(new String[] { "foo" } );
		expect(method.getParameterTypes()).andStubReturn(new String[] { "QString;" } );
		expect(method.getDeclaringType()).andStubReturn(type);
		
		replay(type, method);

		String instantiation = InstantiationStrategy.constructorCall(method).renderInstantiation();
		
		assertEquals("constructor call", "new Person(foo)", instantiation);
		
	}

	@Test
	public void renderConstructorCallWithTwoArguments() throws JavaModelException {
		
		IType type = createMockType();
		
		IMethod method = createMock(IMethod.class);
		
		expect(method.getParameterNames()).andStubReturn(new String[] { "foo", "bar" } );
		expect(method.getParameterTypes()).andStubReturn(new String[] { "QString;", "QString;" } );
		expect(method.getDeclaringType()).andStubReturn(type);

		replay(type, method);

		String instantiation = InstantiationStrategy.constructorCall(method).renderInstantiation();
		
		assertEquals("constructor call", "new Person(foo, bar)", instantiation);
		
	}
	
	@Test
	public void renderFactoryMethodCallWithNoArgument() throws JavaModelException {
		
		IType type = createMockType();
		IMethod method = createMock(IMethod.class);
		
		expect(method.getElementName()).andStubReturn("newInstance");
		expect(method.getParameterNames()).andStubReturn(new String[] { } );
		expect(method.getParameterTypes()).andStubReturn(new String[] { } );
		expect(method.getDeclaringType()).andStubReturn(type);
		
		replay(type, method);

		String instantiation = InstantiationStrategy.factoryMethodCall(method).renderInstantiation();
		
		assertEquals("factory-method call", "Person.newInstance()", instantiation);
		
	}
	
	@Test
	public void renderFactoryMethodCallWithTwoArguments() throws JavaModelException {
		
		IType type = createMockType();
		IMethod method = createMock(IMethod.class);
		
		expect(method.getElementName()).andStubReturn("newInstance");
		expect(method.getParameterNames()).andStubReturn(new String[] { "hans", "peter" } );
		expect(method.getParameterTypes()).andStubReturn(new String[] { "QString;", "QString;" } );
		expect(method.getDeclaringType()).andStubReturn(type);
		
		replay(type, method);

		String instantiation = InstantiationStrategy.factoryMethodCall(method).renderInstantiation();
		
		assertEquals("factory-method call", "Person.newInstance(hans, peter)", instantiation);
		
	}

	private IType createMockType() {
		IType type = createMock(IType.class);
		expect(type.getElementName()).andReturn("Person");
		return type;
	}
	
	
	
}
