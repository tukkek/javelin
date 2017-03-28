package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.Stealth;
import javelin.model.feat.skill.Deceitful;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Academy;
import javelin.model.world.location.town.Dwelling;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.upgrading.AcademyScreen;

/**
 * An academy dedicated to learning how to Infiltrate.
 *
 * @author alex
 */
public class AssassinsGuild extends Academy {
	public class RecruitOption extends Option {
		/** Unit type to recruited. */
		public Monster m;

		/** Constructor. */
		public RecruitOption(String name, double d, Monster m) {
			super(name, d);
			this.m = m;
		}

		@Override
		public boolean equals(final Object obj) {
			final RecruitOption o = obj instanceof RecruitOption ? (RecruitOption) obj : null;
			return o != null && m.name.equals(o.m.name);
		}

		@Override
		public int hashCode() {
			return m.name.hashCode();
		}
	}

	static final String[] RECRUITS = new String[] { "Small monstrous scorpion", "Doppelganger", "Medusa", "Rakshasa", };
	static final String[] LEVELS = new String[] { "Cutthroat", "Footpad", "Ninja", "Shadow" };

	class AssassinGuildScreen extends AcademyScreen {
		public AssassinGuildScreen(Academy academy, Town t) {
			super(academy, t);
		}

		@Override
		public String printinfo() {
			int rank = getrank(assassinations);
			String s = "You are currently a " + LEVELS[rank] + ".\n";
			if (rank < LEVELS.length - 1) {
				float next = 0;
				while (getrank(assassinations + next) == rank) {
					next += .5f;
				}
				s += "Perform " + round(next) + " assassinations or " + round(next * 2)
						+ " sabotages to proceed to the next rank.";
			}
			return s + "\n\nYou have " + Dwelling.sumxp() + "XP";
		}

		private int round(float f) {
			return Math.round(Math.round(Math.ceil(f)));
		}

		@Override
		public List<Option> getoptions() {
			List<Option> options = super.getoptions();
			Monster recruit = Javelin.getmonster(RECRUITS[getrank(assassinations)]);
			RecruitOption option = new RecruitOption("Recruit: " + recruit, 0, recruit);
			option.key = 'r';
			options.add(option);
			return options;
		}

		@Override
		public String printpriceinfo(Option o) {
			if (o instanceof RecruitOption) {
				RecruitOption ro = (RecruitOption) o;
				return " (" + Math.round(ro.m.challengerating * 100) + "XP)";
			}
			return super.printpriceinfo(o);
		}

		@Override
		public boolean select(Option op) {
			if (op instanceof RecruitOption) {
				RecruitOption ro = (RecruitOption) op;
				if (Dwelling.canbuy(Math.round(ro.m.challengerating * 100))) {
					Dwelling.spend(ro.m.challengerating);
					Squad.active.members.add(new Combatant(ro.m.clone(), true));
					return true;
				} else {
					print(text + "\nNot enough XP...");
					return false;
				}
			}
			return super.select(op);
		}
	}

	static final String DESCRITPION = "Assassins guild";

	public float assassinations = 1;

	/** Constructor. */
	public AssassinsGuild() {
		super(DESCRITPION, DESCRITPION, 6, 10, new HashSet<Upgrade>(), RaiseDexterity.SINGLETON, Expert.SINGLETON);
		vision = 3;
		upgrades.add(Disguise.SINGLETON);
		upgrades.add(Stealth.SINGLETON);
		upgrades.add(RaiseCharisma.SINGLETON);
		upgrades.add(RaiseDexterity.SINGLETON);
		upgrades.add(new FeatUpgrade(Deceitful.SINGLETON));
		upgrades.add(Expert.SINGLETON);
		// sort(upgrades);
	}

	public int getrank(double a) {
		a = Math.floor(a);
		if (a <= 5) {
			return 0;
		}
		if (a <= 10) {
			return 1;
		}
		if (a <= 15) {
			return 2;
		}
		return 3;
	}

	@Override
	protected void generate() {
		while (x < 0 || Terrain.get(x, y).equals(Terrain.PLAIN) || Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}

	@Override
	protected AcademyScreen getscreen() {
		return new AssassinGuildScreen(this, null);
	}

	public static AssassinsGuild get() {
		ArrayList<WorldActor> guild = WorldActor.getall(AssassinsGuild.class);
		return guild.isEmpty() ? null : (AssassinsGuild) guild.get(0);
	}

	public static void notify(float level) {
		AssassinsGuild guild = get();
		if (guild != null) {
			guild.assassinations += level;
		}
	}
}
