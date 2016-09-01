package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Split squad into two.
 * 
 * @author alex
 */
public class Divide extends WorldAction {

	/** Constructor. */
	public Divide() {
		super("Divide squad", new int[] {}, new String[] { "d" });
	}

	@Override
	public void perform(final WorldScreen screen) {
		if (Dungeon.active != null) {
			throw new RepeatTurn();
		}
		clear();
		final String in =
				"Press each member's number to switch his destination squad.\n"
						+ "Press c to cancel or ENTER when done.\n"
						+ "The left column is your current squad, the right one is the new squad.\n"
						+ "To join two squads later just place them in the same square.\n";
		Javelin.prompt(in);
		char input = ' ';
		final ArrayList<Combatant> indexreference =
				new ArrayList<Combatant>(Squad.active.members);
		final ArrayList<Combatant> oldsquad =
				new ArrayList<Combatant>(Squad.active.members);
		final ArrayList<Combatant> newsquad = new ArrayList<Combatant>();
		while (input != '\n') {
			clear();
			final ArrayList<String> oldcolumn = new ArrayList<String>();
			final ArrayList<String> newcolumn = new ArrayList<String>();
			log(indexreference, oldsquad, oldcolumn);
			log(indexreference, newsquad, newcolumn);
			String text = "";
			int nlines = Math.max(oldcolumn.size(), newcolumn.size());
			nlines = Math.min(6, nlines);
			for (int i = 0; i < nlines; i++) {
				String oldtd = i < oldcolumn.size() ? oldcolumn.get(i) : "";
				while (oldtd.length() < WorldScreen.SPACER.length()) {
					oldtd += " ";
				}
				final String newtd =
						i < newcolumn.size() ? newcolumn.get(i) : "";
				text += oldtd + newtd + "\n";
			}
			input = Javelin.prompt(text);
			if (input == 'c') {
				return;
			}

			Combatant swap;
			try {
				swap = indexreference
						.get(Integer.parseInt(Character.toString(input)) - 1);
			} catch (final IndexOutOfBoundsException e) {
				continue;
			} catch (final NumberFormatException e) {
				continue;
			}
			List<Combatant> from;
			List<Combatant> to;
			if (oldsquad.contains(swap)) {
				from = oldsquad;
				to = newsquad;
			} else {
				to = oldsquad;
				from = newsquad;
			}
			from.remove(swap);
			to.add(swap);
		}
		if (oldsquad.isEmpty() || newsquad.isEmpty()) {
			return;
		}
		input = ' ';
		int gold = Squad.active.gold / 2;
		final int increment = Squad.active.gold / 10;
		while (input != '\n') {
			clear();
			input = Javelin
					.prompt("How much gold do you want to transfer to the new squad? Use the + and - keys to change and ENTER to confirm.\n"
							+ gold);
			if (input == '+') {
				gold += increment;
				if (gold > Squad.active.gold) {
					gold = Squad.active.gold;
				}
			} else if (input == '-') {
				gold -= increment;
				if (gold < 0) {
					gold = 0;
				}
			}
		}
		WorldActor nearto = findtown(Squad.active.x, Squad.active.y);
		int x, y;
		Squad s = new Squad(0, 0, Squad.active.hourselapsed,
				Squad.active.lasttown);
		s.members = newsquad;
		s.gold = gold;
		s.strategic = Squad.active.strategic;
		s.move(true, Terrain.current(), Squad.active.x, Squad.active.y);
		placement: for (x = Squad.active.x - 1; x <= Squad.active.x + 1; x++) {
			for (y = Squad.active.y - 1; y <= Squad.active.y + 1; y++) {
				if (!World.istown(x, y, false)
						&& (nearto == null || findtown(x, y) instanceof Town)
						&& (s.swim() || !World.seed.map[x][y]
								.equals(Terrain.WATER))) {
					s.x = x;
					s.y = y;
					break placement;
				}
			}
		}
		Squad.active.members = oldsquad;
		Squad.active.gold -= gold;
		s.place();
		for (final Combatant m : newsquad) {
			final ArrayList<Item> items = Squad.active.equipment.get(m.id);
			Squad.active.equipment.remove(m.id);
			s.equipment.put(m.id, items);
		}
		Squad.active.updateavatar();
	}

	WorldActor findtown(int xp, int yp) {
		ArrayList<Town> towns = new ArrayList<Town>();
		for (int x = xp - 1; x <= xp + 1; x++) {
			for (int y = yp - 1; y <= yp + 1; y++) {
				WorldActor t = WorldActor.get(x, y);
				if (t instanceof Town) {
					towns.add((Town) t);
				}
			}
		}
		if (towns.isEmpty()) {
			return null;
		}
		for (Town t : towns) {
			if (!t.ishostile()) {
				return t;
			}
		}
		return towns.get(0);
	}

	static void clear() {
		BattleScreen.active.messagepanel.clear();
	}

	void log(final List<Combatant> indexreference,
			final List<Combatant> oldsquad, final List<String> oldcolumn) {
		if (oldsquad.isEmpty()) {
			oldcolumn.add("Empty");
		} else {
			for (final Combatant m : oldsquad) {
				oldcolumn.add("[" + (indexreference.indexOf(m) + 1) + "] " + m);
			}
		}
	}
}
