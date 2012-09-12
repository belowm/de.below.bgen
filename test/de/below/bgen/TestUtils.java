package de.below.bgen;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.eclipse.jdt.core.IType;

public class TestUtils {

	public static IType createMockType(String name, boolean replay) {
		IType type = createMock(IType.class);
		expect(type.getElementName()).andReturn(name);
		
		if (replay) {
			replay(type);
		}
		
		return type;
	}

}
