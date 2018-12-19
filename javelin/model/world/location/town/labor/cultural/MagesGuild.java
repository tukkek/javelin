package javelin.model.world.location.town.labor.cultural;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseWisdom;
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
	/** All mage guild data. */
	public static final ArrayList<MageGuildData> GUILDS=new ArrayList<>();

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
		protected Academy generateacademy(){
			return RPG.pick(GUILDS).generate();
		}
	}

	/** Representation of a type of Mage Guild. */
	public static class MageGuildData{
		String name;
		HashSet<Upgrade> upgrades;
		RaiseAbility ability;

		MageGuildData(String name,HashSet<Upgrade> upgrades,RaiseAbility ability){
			this.name=name;
			this.upgrades=upgrades;
			this.ability=ability;
		}

		/** @return An actual instance from this representation. */
		public MagesGuild generate(){
			return new MagesGuild(name,upgrades,ability);
		}
	}

	static{
		UpgradeHandler uh=UpgradeHandler.singleton;
		uh.gather();

		RaiseAbility cha=RaiseCharisma.SINGLETON;
		GUILDS.add(new MageGuildData("Compulsion guild",uh.schoolcompulsion,cha));
		GUILDS.add(new MageGuildData("Conjuration guild",uh.schoolconjuration,cha));
		GUILDS.add(new MageGuildData("Abjuration guild",uh.schoolabjuration,cha));

		RaiseAbility wisdom=RaiseWisdom.SINGLETON;
		GUILDS.add(new MageGuildData("Healing guild",uh.schoolrestoration,wisdom));
		GUILDS.add(new MageGuildData("Totem guild",uh.schooltotem,wisdom));
		GUILDS
				.add(new MageGuildData("Restoration guild",uh.schoolhealwounds,wisdom));
		GUILDS
				.add(new MageGuildData("Divination guild",uh.schooldivination,wisdom));

		RaiseAbility i=RaiseIntelligence.SINGLETON;
		GUILDS.add(new MageGuildData("Necromancy guild",uh.schoolnecromancy,i));
		GUILDS.add(new MageGuildData("Wounding guild",uh.schoolwounding,i));
		GUILDS.add(new MageGuildData("Evocation guild",uh.schoolevocation,i));
		GUILDS
				.add(new MageGuildData("Transmutation guild",uh.schooltransmutation,i));
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raiseWisdom
	 */
	public MagesGuild(String knownnamep,HashSet<Upgrade> spells,
			RaiseAbility raise){
		super(knownnamep,"Mages guild",0,0,spells,raise,Aristocrat.SINGLETON);
		ArrayList<Spell> ascending=new ArrayList<>(spells.size());
		for(Upgrade u:spells)
			if(u instanceof Spell) ascending.add((Spell)u);
		ascending.sort((o1,o2)->o1.casterlevel-o2.casterlevel);
		minlevel=ascending.get(0).casterlevel;
		maxlevel=ascending.get(ascending.size()-1).casterlevel;
		if(maxlevel>10) maxlevel=10;
	}
}
