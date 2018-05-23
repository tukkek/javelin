package javelin.model.world.location.town.labor.religious;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/**
 * Will cast Rituals (certain {@link Spell}s) for gold.
 *
 * @see Spell#isritual
 * @author alex
 */
public class Shrine extends Fortification {
	public class UpgradeShrine extends BuildingUpgrade {
		public UpgradeShrine(Shrine s) {
			super("", 5, +5, s, Rank.VILLAGE);
			name = "Upgrade shrine";
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public void done() {
			super.done();
			Shrine s = (Shrine) previous;
			s.level = 2;
			s.fill();
		}
	}

	static final List<Spell> RITUALS = new ArrayList<>();

	static {
		UpgradeHandler.singleton.gather();
		for (Spell p : UpgradeHandler.singleton.getspells()) {
			if (p.isritual) {
				RITUALS.add(p);
			}
		}
	}

	public static class BuildShrine extends Build {
		Shrine s;

		public BuildShrine() {
			super(null, 5, null, Rank.HAMLET);
		}

		@Override
		protected void define() {
			super.define();
			s = new Shrine(1);
			name = "Build " + s.descriptionknown.toLowerCase();
		}

		@Override
		public Location getgoal() {
			return s;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d)
					&& d.getlocationtype(Shrine.class)
							.size() < d.town.getrank().rank
					&& s.rituals.get(0).casterlevel <= d.town.population;
		}
	}

	/** Rituals are spells that this shrine will cast for a fee. */
	final ArrayList<Spell> rituals = new ArrayList<>(2);
	int level = 1;

	public Shrine() {
		this(2);
		allowedinscenario = false;
	}

	/** Constructor. */
	public Shrine(int level) {
		super(null, "A shrine", 0, 0);
		this.level = level;
		discard = false;
		gossip = true;
		fill();
	}

	void update() {
		Integer cl = rituals.get(0).casterlevel;
		if (level == 1) {
			minlevel = maxlevel = cl;
			descriptionknown = "A shrine (" + rituals.get(0).name.toLowerCase()
					+ ")";
			return;
		}
		if (price(0) > price(1)) {
			ArrayList<Spell> swap = new ArrayList<>();
			swap.add(rituals.get(1));
			swap.add(rituals.get(0));
			rituals.clear();
			rituals.addAll(swap);
		}
		minlevel = cl;
		maxlevel = rituals.get(1).casterlevel;
		descriptionknown = "A shrine (" + rituals.get(0).name.toLowerCase()
				+ ", " + rituals.get(1).name.toLowerCase() + ")";
	}

	void fill() {
		while (rituals.size() < level) {
			Spell ritual = RPG.pick(RITUALS);
			if (!rituals.contains(ritual)) {
				rituals.add(ritual);
			}
		}
		update();
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String output;
		output = "You enter a shrine. \"What can we do for you today?\", says the "
				+ (RPG.r(1, 2) == 1 ? "priest" : "priestess") + ".\n";
		output += "\n1 - " + rituals.get(0).name + " ($" + price(0) + ")";
		if (level > 1) {
			output += "\n2 - " + rituals.get(1).name + " ($" + price(1) + ")";
		}
		output += "\np - Pillage this temple ($" + Javelin.format(getspoils())
				+ ")";
		output += "\nq - Quit for now ";
		output += "\n\nSelect an option.";
		InfoScreen screen = new InfoScreen(output);
		Javelin.app.switchScreen(screen);
		processinput();
		return true;
	}

	void processinput() {
		char input = InfoScreen.feedback();
		if (input == '1') {
			service(0);
		} else if (input == '2' && level > 1) {
			service(1);
		} else if (input == 'p') {
			pillage();
		} else if (input == 'q') {
			return;
		} else {
			processinput();
		}
	}

	boolean service(int slot) {
		int price = price(slot);
		Spell s = rituals.get(slot);
		if (price > Squad.active.gold) {
			return false;
		}
		Combatant target = null;
		if (s.castonallies) {
			int i = Javelin.choose("Cast on who?", Squad.active.members, true,
					false);
			if (i == -1) {
				return false;
			}
			target = Squad.active.members.get(i);
		}
		if (!s.validate(null, target)) {
			return false;
		}
		s.castpeacefully(null, target);
		Squad.active.gold -= price;
		return true;
	}

	private int price(int i) {
		Spell ritual = rituals.get(i);
		return ritual.level * ritual.casterlevel * 10 + ritual.components;
	}

	public static void fail(InfoScreen screen, String string) {
		screen.print(string);
		InfoScreen.feedback();
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (level == 1) {
			upgrades.add(new UpgradeShrine(this));
		}
		return upgrades;
	}
}
