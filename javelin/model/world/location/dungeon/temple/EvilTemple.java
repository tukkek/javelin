package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Marsh;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Skull;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.old.RPG;

/**
 * Found drowning in the {@link Marsh}. Good creatures are never found in the
 * temple. An evil force can bring you back to the stairs up at any point (or
 * stairs down if you have the {@link Skull}).
 *
 * @see Temple
 * @see Monster#morals
 * @author alex
 */
public class EvilTemple extends Temple{
	private static final String FLUFF="You have heard of this fort once before, upon a dark stormy night.\n"
			+"You recognize the looming towers from that tale. It was related to you as the Fortress of Regrets.\n"
			+"It is said that a once powerful king oversaw his domain from his throne here but bad tidings befell him.\n"
			+"The once great castle became a prison, torture chamber and hall of twisted pleasures as the kingdom's honor slowly faded into oblivion.";

	/** Constructor. */
	public EvilTemple(Integer level){
		super(Realm.EVIL,level,new Skull(level),FLUFF);
		terrain=Terrain.MARSH;
		floor="dungeonfloortempleevil";
		wall="dungeonwalltempleevil";
		doorbackground=false;
	}

	@Override
	public boolean validate(List<Monster> foes){
		for(Monster m:foes)
			if(m.alignment.isgood()) return false;
		return true;
	}

	@Override
	public boolean hazard(Dungeon d){
		if(!RPG.chancein(d.stepsperencounter*10)) return false;
		Class<? extends Feature> targettype;
		if(Squad.active.equipment.get(Skull.class)==null)
			targettype=StairsUp.class;
		else if(d.features.has(Altar.class))
			targettype=Altar.class;
		else
			targettype=StairsDown.class;
		var target=d.features.stream().filter(f->targettype.isInstance(f)).findAny()
				.orElse(null);
		if(target==null) return false;
		d.squadlocation=target.getlocation();
		Javelin.message("A macabre force draws upon you...",true);
		return true;
	}
}
