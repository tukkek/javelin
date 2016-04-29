package javelin.model.world.place.guarded;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.CastSpells;
import javelin.controller.walker.Walker;
import javelin.model.item.scroll.Scroll;
import javelin.model.spell.ScrollSpell;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

/**
 * Once it's garrison is defeated a priest will offer a wish to the heroes in
 * return - a {@link ScrollSpell} (currently only obtainable this way) or map
 * knowledge.
 * 
 * @author alex
 */
public class Shrine extends GuardedPlace {
	static public final boolean DEBUG = false;
	final ScrollSpell spell = new ScrollSpell(
			(Scroll) RPG.pick(new ArrayList<Scroll>(Scroll.SCROLLS)).clone(),
			this);

	public Shrine() {
		super(null, "A seer's shrine", 5, 10);
		descriptionknown = "A seer's shrine (" + spell.name + ")";
		if (DEBUG) {
			garrison.clear();
		}
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String output;
		output = "You enter a shrine. \"Thank you for rescuing me\", says the "
				+ (RPG.r(1, 2) == 1 ? "priest" : "priestess")
				+ ". \"I'll grant you one wish in return\":\n";
		output += "\nl - Learn spell: " + spell.name + " ("
				+ Math.round(spell.cr * 100) + "XP)";
		output += "\np - Pay me a tribute ($"
				+ PurchaseScreen.formatcost(getspoils()) + ")";
		output += "\ns - Show path to closest unknown town";
		output += "\nq - Quit for now ";
		output += "\n\nSelect an option.";
		InfoScreen screen = new InfoScreen(output);
		Javelin.app.switchScreen(screen);
		char input = ' ';
		List<Character> options = Arrays.asList('l', 's', 'q', 'p');
		while (!options.contains(input)) {
			input = InfoScreen.feedback();
		}
		if (input == 'l') {
			boolean learnspell = learnspell(screen);
			if (learnspell) {
				remove();
			}
			return learnspell;
		}
		if (input == 's') {
			showtown(screen);
			remove();
			return true;
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		if (input == 'q') {
			return false;
		}
		throw new RuntimeException("Unknown wish " + input + " #shrine");
	}

	void showtown(InfoScreen screen) {
		ArrayList<WorldActor> towns = WorldPlace.getall(Town.class);
		Town closest = null;
		for (WorldActor p : towns) {
			if (WorldScreen.discovered.contains(new Point(p.x, p.y))) {
				continue;
			}
			if (closest == null
					|| distance(p.x, p.y) < distance(closest.x, closest.y)) {
				closest = (Town) p;
			}
		}
		if (closest == null) {
			fail(screen, "All towns have been discovered.");
		} else {
			walktownypath(closest);
		}
	}

	void walktownypath(Town closest) {
		int x = this.x;
		int y = this.y;
		walk: while (x != closest.x || y != closest.y) {
			ArrayList<Point> points = new ArrayList<Point>(4);
			points.add(new Point(x + 1, y));
			points.add(new Point(x - 1, y));
			points.add(new Point(x, y + 1));
			points.add(new Point(x, y - 1));
			Collections.shuffle(points);
			for (Point p : points) {
				if (iscloser(x, y, p, closest)) {
					x = p.x;
					y = p.y;
					WorldScreen.discovered.add(p);
					continue walk;
				}
			}
			throw new RuntimeException(
					"Should have found path discovery step #shrine");
		}
	}

	boolean iscloser(int x, int y, Point p, Town closest) {
		return Walker.distance(p.x, p.y, closest.x, closest.y) < Walker
				.distance(x, y, closest.x, closest.y);
	}

	boolean learnspell(InfoScreen screen) {
		ArrayList<Combatant> candidates = new ArrayList<Combatant>();
		for (Combatant c : Squad.active.members) {
			if (c.xp.floatValue() >= spell.cr && spell.apply(c.clonedeeply())) {
				candidates.add(c);
			}
		}
		if (candidates.isEmpty()) {
			fail(screen, "No one here can learn that spell right now.");
			return false;
		}
		Combatant caster = candidates.get(CastSpells.choose(
				"Who will learn " + spell + "?", candidates, true, true));
		spell.apply(caster);
		caster.xp.subtract(new BigDecimal(spell.cr));
		return true;
	}

	void fail(InfoScreen screen, String string) {
		screen.print(string);
		screen.feedback();
	}

}
