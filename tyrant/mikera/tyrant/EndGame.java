/*
 * Created on 08-Dec-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EndGame {
	public static BattleMap getFinalMap() {
		BattleMap m=new BattleMap(21,41);
		m.setTheme("metal");
		m.set("Level",30);
		m.set("Description","The Tyrant's Throne Room");
		m.set("EnterMessage","\"Prepare to DIE, puny mortal!!\"");
		
		m.fillArea(0,0,20,40,m.wall());
		m.fillArea(1,1,19,19,Tile.LAVA);
		m.fillOval(5,7,15,13,m.floor());
		m.fillArea(9,10,11,39,m.floor());
		
		// the tyrant!!!
		m.addThing(Lib.create("The Tyrant"),10,10);
		
		// base room
		m.fillArea(1,21,19,39,m.floor());
		for (int i=22; i<=39; i+=2) {
			m.addThing("demon vortex",7,i);
			m.addThing("demon vortex",13,i);
		}
		
		// the entrance
		m.addEntrance("stairs down");
		
		return m;
	}
	
	public static void init() {
		Thing t=Lib.extendNamed("The Tyrant","human");
		Monster.stats(t,300,200,250,300,100,300,100,150);
		t.set("ARM",400);
		Monster.strengthen(t,0.6); // low power right now
		t.set("IsHostile",1);
		t.set("MoveSpeed",200);
		t.set("AttackSpeed",200);
		t.set("Image",503);
		t.set(Skill.UNARMED,2);
		t.set(Skill.ATTACK,3);
		t.set(Skill.DEFENCE,4);
		t.set(Skill.CASTING,4);
		t.set(Skill.TRUEMAGIC,5);
		t.set(Skill.MAGICRESISTANCE,4);
		t.set(Skill.BRAVERY,5);
		t.set("RES:fire",1000);
		t.set("IsDisplaceable",0);
		t.set("DefaultThings","The Tyrant's Mace,The Tyrant's Armour,[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell]");
		t.set("LevelMin",30);
		t.set("Luck",100);
		t.set("ASCII","T");
		t.addHandler("OnDeath",new Script() {
			private static final long serialVersionUID = 1L;

            public boolean handle(Thing t,Event e) {
				Game.over=true;
				return false;
			}
		});
		Lib.add(t);
		
		t=Lib.extendNamed("Bel-Gorimoth","master voidling");
		Monster.stats(t,2000,2000,2000,3000,1500,3000,1000,3000);
		t.set("Armour",2000);
		t.set(Skill.UNARMED,5);
		t.set(Skill.ATTACK,5);
		t.set(Skill.DEFENCE,5);
		t.set(Skill.CASTING,20);
		t.set(Skill.FOCUS,10);
		t.set(Skill.TRUEMAGIC,5);
		t.set(Skill.MAGICRESISTANCE,6);
		t.set(Skill.BRAVERY,10);
		t.set("Luck",200);
		t.set("Speed",200);
		t.set("AttackSpeed",300);
		t.set("MoveSpeed",300);
		t.set("UnarmedWeapon",Lib.create("disintegrate attack"));
		Lib.add(t);
	}
}