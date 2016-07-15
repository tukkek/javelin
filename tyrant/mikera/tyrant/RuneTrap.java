package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class RuneTrap extends Trap {

	public static Thing create(int level) {
		Thing t=Lib.create("rune trap");
	
		t.set("Image", 160 + RPG.r(3));
		t.set("TrapSpellName",Spell.randomOffensiveSpell(Skill.TRUEMAGIC,level).name());
		return t;
	}

	public static Thing create(Thing owner, Thing thespell) {
		Thing t=create();
		t.set("Actor", owner);
		Game.assertTrue(thespell.getFlag("IsSpell"));
		t.set("TrapSpellName",thespell.getName(Game.hero()));
		return t;
	}


	public static void init() {
		Thing t=Lib.extend("rune trap","base trap");
		t.set("IsInvisible",0);
		t.set("Image",160);
		t.set("ImageSource","Scenery");
		t.set("TrapSpell","Fireball"); // default spell
		t.set("OnEnterTrigger",new SpellTrapTrigger());
		Lib.add(t);
		
	}
}