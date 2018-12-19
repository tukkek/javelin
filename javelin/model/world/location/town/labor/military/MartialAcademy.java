package javelin.model.world.location.town.labor.military;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class MartialAcademy extends Academy{
	/** All martial academies. */
	public static final ArrayList<MartialAcademyData> ACADEMIES=new ArrayList<>();

	static{
		UpgradeHandler uh=UpgradeHandler.singleton;
		MartialAcademyData expertise=new MartialAcademyData(uh.combatexpertise,
				"Academy (combat expertise)",RaiseIntelligence.SINGLETON);
		expertise.upgrades.add(RaiseDexterity.SINGLETON);
		ACADEMIES.add(expertise);
		ACADEMIES.add(new MartialAcademyData(uh.powerattack,
				"Academy (power attack)",RaiseStrength.SINGLETON));
	}

	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildMartialAcademy extends BuildAcademies{
		/** Constructor. */
		public BuildMartialAcademy(){
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy(){
			return RPG.pick(ACADEMIES).generate();
		}
	}

	/** An abstract representation of a martial academy type. */
	public static class MartialAcademyData{
		String name;
		HashSet<Upgrade> upgrades;
		RaiseAbility ability;

		/**
		 * @param upgrades Upgrades taught at the {@link Academy}.
		 * @param name Name of this type of martial academy.
		 * @param ability Ability that governs this martial school.
		 */
		public MartialAcademyData(HashSet<Upgrade> upgrades,String name,
				RaiseAbility ability){
			super();
			this.name=name;
			this.upgrades=upgrades;
			this.ability=ability;
		}

		/** @return An actual instance of this type of academy. */
		public MartialAcademy generate(){
			return new MartialAcademy(upgrades,name,ability);
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public MartialAcademy(HashSet<Upgrade> upgrades,String descriptionknownp,
			RaiseAbility raise){
		super(descriptionknownp,"An academy",6,10,upgrades,raise,Warrior.SINGLETON);
	}
}
