package de.i3mainz.utils;

import info.labeling.v1.utils.PropertiesLocal;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertiesLocalTest {

	@Test
	public void testGetPropertyParam() throws Exception {
		assertNotNull(PropertiesLocal.getPropertyParam(PropertiesLocal.getHOST()));
		assertNotNull(PropertiesLocal.getPropertyParam(PropertiesLocal.getLSDETAIL()));
		assertNotNull(PropertiesLocal.getPropertyParam(PropertiesLocal.getREPOSITORY()));
		assertNotNull(PropertiesLocal.getPropertyParam(PropertiesLocal.getSESAMESERVER()));
	}
	
	@Test
	public void givenValidFileShouldReturnTrue() throws Exception {
		assertTrue(PropertiesLocal.loadpropertyFile(PropertiesLocal.getFileName()));
	}

}
