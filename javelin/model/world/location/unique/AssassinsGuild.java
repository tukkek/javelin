package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.base.Dwelling;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.upgrading.AcademyScreen;

/**
 * An academy dedicated to learning how to Infiltrate.
 *
 * @author alex
 */
public class AssassinsGuild extends Academy {
	static final String DESCRITPION = "Assassins guild";

	public static class BuildAssassinsGuild extends BuildAcademy {
		public BuildAssassinsGuild() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new AssassinsGuild();
		}
	}

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
			final RecruitOption o = obj instanceof RecruitOption
					? (RecruitOption) obj : null;
			return o != null && m.name.equals(o.m.name);
		}

		@Override
		public int hashCode() {
			return m.name.hashCode();
		}
	}

	static final String[] RECRUITS = new String[] { "Small monstrous scorpion",
			"Doppelganger", "Medusa", "Rakshasa", };
	static final String[] LEVELS = new String[] { "Thug", "Cutthroat", "Ninja",
			"Shadow" };

	class AssassinGuildScreen extends AcademyScreen {
		public AssassinGuildScreen(Academy academy, Town t) {
			super(academy, t);
		}

		@Override
		public String printinfo() {
			String s = super.printinfo();
			if (!s.isEmpty()) {
				s += "\n\n";
			}
			int rank = getrank(assassinations);
			s += "You are currently a " + LEVELS[rank] + ".\n";
			if (rank < LEVELS.length - 1) {
				float next = 0;
				while (getrank(assassinations + next) == rank) {
					next += .5f;
				}
				s += "Perform " + round(next)
						+ " assassinations to proceed to the next rank.";
			}
			return s + "\n\nYou have "
					+ SelectScreen.formatcost(Squad.active.sumxp()) + "XP";
		}

		private int round(float f) {
			return Math.round(Math.round(Math.ceil(f)));
		}

		@Override
		public List<Option> getoptions() {
			List<Option> options = super.getoptions();
			Monster recruit = Javelin
					.getmonster(RECRUITS[getrank(assassinations)]);
			RecruitOption option = new RecruitOption("Recruit: " + recruit, 0,
					recruit);
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
				if (Dwelling.canrecruit(Math.round(ro.m.challengerating * 100))) {
					Dwelling.spend(ro.m.challengerating);
					Squad.active.members.add(new Combatant(ro.m.clone(), true));
					return true;
				}
				print(text + "\nNot enough XP...");
				return false;
			}
			return super.select(op);
		}
	}

	public float assassinations = 1;

	/** Constructor. */
	public AssassinsGuild() {
		super(DESCRITPION, DESCRITPION, 6, 10, Kit.ASSASSIN.upgrades, null,
				null);
		vision = 3;
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
		while (x < 0 || Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}

	@Override
	protected AcademyScreen getscreen() {
		return new AssassinGuildScreen(this, null);
	}

	public static AssassinsGuild get() {
		ArrayList<Actor> guild = World.getall(AssassinsGuild.class);
		return guild.isEmpty() ? null : (AssassinsGuild) guild.get(0);
	}

	public static void notify(float level) {
		AssassinsGuild guild = get();
		if (guild != null) {
			guild.assassinations += level;
		}
	}
}
