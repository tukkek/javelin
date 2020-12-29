package javelin.model.world.location.dungeon.branch.temple;

import java.util.List;

import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Spirit;

/**
 * Found resting in the {@link Plains}. Evil enemies never found here. 1-3 good
 * spirits can tell show you the location of an undiscovered {@link Feature}.
 *
 * @see Temple
 * @see Monster#morals
 * @author alex
 */
public class GoodTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new GoodBranch();

	private static final String FLUFF="The bizarre and tall complex seems to be carved entirely out of ivory and white stones.\n"
			+"Despite being in no place of particular importance, the common animals in the area seem to avoid it.\n"
			+"In fact, the eerie silence around the entire place makes you wonder for a moment whether this is all only a fleeting dream.\n"
			+"You approach the holy ground, daring say nothing as you breath deeply in anticipation of the vistas inside.";

	static class GoodBranch extends Branch{
		protected GoodBranch(){
			super("Holy","of good","floortemplegood","walldungeon");
			features.add(Spirit.class);
			terrains.add(Terrain.PLAIN);
		}

		@Override
		public boolean validate(List<Combatant> foes){
			for(var foe:foes){
				var m=foe.source;
				if(m.alignment.isevil()||MonsterType.UNDEAD.equals(m.type))
					return false;
			}
			return true;
		}

	}

	/** Constructor. */
	public GoodTemple(){
		super(Realm.GOOD,new GoodBranch(),FLUFF);
	}
}
