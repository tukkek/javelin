package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Ankh;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
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
	private static final String FLUFF="The bizarre and tall complex seems to be carved entirely out of ivory and white stones.\n"
			+"Despite being in no place of particular importance, the common animals seem to avoid it.\n"
			+"In fact, the eerie silence around the entire place makes you wonder if this is truly happening or only a fleeting dream.\n"
			+"You enter the holy ground, daring say nothing as you breath deeply in anticipation of the vistas inside.";

	/** Constructor. */
	public GoodTemple(Integer level){
		super(Realm.GOOD,level,new Ankh(level),FLUFF);
		terrain=Terrain.PLAIN;
		floor="terrainarena";
		wall="terraindungeonwall";
		feature=Spirit.class;
	}

	@Override
	public boolean validate(List<Monster> foes){
		for(var foe:foes)
			if(foe.alignment.isevil()||MonsterType.UNDEAD.equals(foe.type))
				return false;
		return true;
	}
}
