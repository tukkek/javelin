package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

/**
 * Generator class for temples
 * 
 * @author Mike Anderson
 *
 */
public class Temple {
	
	
	private static void initGuards() {
		Thing t;
		
		t=Lib.extend("blood guard","human");
		Monster.stats(t,60,90,50,70,50,120,70,60);
		t.set("Image",40);
		t.set("DefaultThings","[IsSword],[IsArmour],[IsArmour],[IsItem],[IsCoin]");
		t.set(Skill.ATTACK,4);
		t.set(Skill.DEFENCE,5);
		t.set(Skill.FEROCITY,3);
		t.set(Skill.ATHLETICS,3);
		t.set("LevelMin",17);
		t.set("Religion",Gods.YANTHRALL);
		Lib.add(t);
	}
	
	public static void init() {
		initGuards();
		
	}
}
