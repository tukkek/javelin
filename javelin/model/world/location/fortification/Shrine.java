package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

/**
 * Will cast Rituals (certain {@link Spell}s) for gold.
 * 
 * @see Spell#isritual
 * @author alex
 */
public class Shrine extends Fortification {
	static final List<Spell> RITUALS = new ArrayList<Spell>();

	static {
		UpgradeHandler.singleton.gather();
		for (Spell p : UpgradeHandler.singleton.getspells()) {
			if (p.isritual) {
				RITUALS.add(p);
			}
		}
	}

	/** Rituals are spells that this shrine will cast for a fee. */
	final ArrayList<Spell> rituals = new ArrayList<Spell>(2);

	/** Constructor. */
	public Shrine() {
		super(null, "A seer's shrine", 0, 0);
		while (rituals.size() < 2) {
			Spell ritual = RPG.pick(RITUALS);
			if (!rituals.contains(ritual)) {
				rituals.add(ritual);
			}
		}
		if (price(0) > price(1)) {
			ArrayList<Spell> swap = new ArrayList<Spell>();
			swap.add(rituals.get(1));
			swap.add(rituals.get(0));
			rituals.clear();
			rituals.addAll(swap);
		}
		minlevel = rituals.get(0).casterlevel;
		maxlevel = rituals.get(1).casterlevel;
		descriptionknown =
				"A seer's shrine (" + rituals.get(0).name.toLowerCase() + ", "
						+ rituals.get(1).name.toLowerCase() + ")";
		discard = false;
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
		output += "\n2 - " + rituals.get(1).name + " ($" + price(1) + ")";
		output += "\np - Pillage this temple ($"
				+ PurchaseScreen.formatcost(getspoils()) + ")";
		output += "\nq - Quit for now ";
		output += "\n\nSelect an option.";
		InfoScreen screen = new InfoScreen(output);
		Javelin.app.switchScreen(screen);
		char input = ' ';
		List<Character> options = Arrays.asList('1', '2', 'q', 'p');
		while (!options.contains(input)) {
			input = InfoScreen.feedback();
		}
		if (input == '1') {
			return service(0);
		}
		if (input == '2') {
			return service(1);
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		if (input == 'q') {
			return false;
		}
		fail(screen, output);
		throw new RuntimeException("Unknown wish " + input + " #shrine");
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
		screen.feedback();
	}

}
