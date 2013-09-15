package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;

public class Artifact_TC extends TyrantTestCase {
	public void testUniqueCreation() {
		Thing a=Lib.create("Yanthrall's Sword");
		Thing b=Lib.create("Yanthrall's Sword");
		assertNotNull(a);
		assertEquals(a,b);
		
		
	}
}
