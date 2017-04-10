package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import tyrant.mikera.engine.RPG;

/**
 * Teaches spells from a certain school of magic or specialization.
 *
 * @author alex
 */
public class MagesGuild extends Academy {
	public static final ArrayList<Guild> GUILDS = new ArrayList<Guild>();

	public static class BuildMagesGuild extends BuildAcademies {
		public BuildMagesGuild() {
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy getacademy() {
			return RPG.pick(GUILDS).generate();
		}
	}

	public static class Guild {
		String name;
		HashSet<Upgrade> upgrades;
		RaiseAbility ability;

		Guild(String name, HashSet<Upgrade> upgrades, RaiseAbility ability) {
			this.name = name;
			this.upgrades = upgrades;
			this.ability = ability;
		}

		public MagesGuild generate() {
			return new MagesGuild(name, upgrades, ability);
		}
	}

	static {
		UpgradeHandler uh = UpgradeHandler.singleton;
		uh.gather();

		RaiseAbility cha = RaiseCharisma.SINGLETON;
		GUILDS.add(new Guild("Compulsion guild", uh.schoolcompulsion, cha));
		GUILDS.add(new Guild("Conjuration guild", uh.schoolconjuration, cha));
		GUILDS.add(new Guild("Abjuration guild", uh.schoolabjuration, cha));

		RaiseAbility wisdom = RaiseWisdom.SINGLETON;
		GUILDS.add(new Guild("Healing guild", uh.schoolrestoration, wisdom));
		GUILDS.add(new Guild("Totem guild", uh.schooltotem, wisdom));
		GUILDS.add(new Guild("Restoration guild", uh.schoolhealwounds, wisdom));
		GUILDS.add(new Guild("Divination guild", uh.schooldivination, wisdom));

		RaiseAbility i = RaiseIntelligence.SINGLETON;
		GUILDS.add(new Guild("Necromancy guild", uh.schoolnecromancy, i));
		GUILDS.add(new Guild("Wounding guild", uh.schoolwounding, i));
		GUILDS.add(new Guild("Evocation guild", uh.schoolevocation, i));
		GUILDS.add(new Guild("Transmutation guild", uh.schooltransmutation, i));
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raiseWisdom
	 */
	public MagesGuild(String knownnamep, HashSet<Upgrade> spells,
			RaiseAbility raise) {
		super(knownnamep, "Mages guild", 0, 0, spells, raise,
				Aristocrat.SINGLETON);
		ArrayList<Spell> ascending = new ArrayList<Spell>(spells.size());
		for (Upgrade u : spells) {
			if (u instanceof Spell) {
				ascending.add((Spell) u);
			}
		}
		ascending.sort(new Comparator<Spell>() {
			@Override
			public int compare(Spell o1, Spell o2) {
				return o1.casterlevel - o2.casterlevel;
			}
		});
		minlevel = ascending.get(0).casterlevel;
		maxlevel = ascending.get(ascending.size() - 1).casterlevel;
		if (maxlevel > 10) {
			maxlevel = 10;
		}
	}

	@Override
	public void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				if (!(o1 instanceof Spell)) {
					return 1;
				}
				if (!(o2 instanceof Spell)) {
					return -1;
				}
				Spell s1 = (Spell) o1;
				Spell s2 = (Spell) o2;
				return s1.casterlevel - s2.casterlevel;
			}
		});
	}
}
