package javelin.model.world.location.town.labor.cultural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.controller.kit.Kit;
import javelin.controller.kit.wizard.Wizard;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * Teaches spells from a certain school of magic or specialization.
 *
 * @author alex
 */
public class MagesGuild extends Academy{
	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildMagesGuild extends BuildAcademies{
		/** Constructor. */
		public BuildMagesGuild(){
			super(Rank.VILLAGE);
		}

		@Override
		public Academy generateacademy(){
			var guilds=Kit.KITS.stream().filter(k->k instanceof Wizard)
					.collect(Collectors.toList());
			return new MagesGuild((Wizard)RPG.pick(guilds));
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raiseWisdom
	 */
	public MagesGuild(Wizard kit){
		super(kit.name+"s guild","Mages guild",0,0,kit.getupgrades(),kit.ability,
				Aristocrat.SINGLETON);
		while(upgrades.size()>20){
			var u=RPG.pick(upgrades);
			if(u instanceof Spell) upgrades.remove(u);
		}
		ArrayList<Spell> ascending=new ArrayList<>(upgrades.size());
		for(Upgrade u:upgrades)
			if(u instanceof Spell) ascending.add((Spell)u);
		ascending.sort((o1,o2)->o1.casterlevel-o2.casterlevel);
		minlevel=ascending.get(0).casterlevel;
		maxlevel=ascending.get(ascending.size()-1).casterlevel;
		if(maxlevel>10) maxlevel=10;
	}
}
