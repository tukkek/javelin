package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.Map;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.state.Square;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * TODO would be cool if could generate heroes to fight against at some point
 * 
 * @see Arena
 * 
 * @author alex
 */
public class ArenaFight extends Minigame {
	static final int BOOST = 13;
	static final int MAPSIZE = 28;
	static final int TENSIONMIN = -5;
	static final int TENSIONMAX = 0;

	class ArenaSetup extends BattleSetup {
		@Override
		public void generatemap(Fight f) {
			f.map = Map.random();
			super.generatemap(f);
			Square[][] map = f.map.map;
			f.map.map = new Square[MAPSIZE][];
			Fight.state.map = f.map.map;
			for (int i = 0; i < MAPSIZE; i++) {
				f.map.map[i] = Arrays.copyOfRange(map[i], 0, MAPSIZE);
			}
			for (int x = 0; x < MAPSIZE; x++) {
				for (int y = 0; y < MAPSIZE; y++) {
					if (x == 0 || x == MAPSIZE - 1 || y == 0
							|| y == MAPSIZE - 1) {
						f.map.map[x][y].blocked = false;
					}
				}
			}
		}

		@Override
		public void place() {
			placebuildings();
			enter(getblueteam(), state.blueTeam,
					new Point(getcenterpoint(), getcenterpoint()));
		}

		int getcenterpoint() {
			return RPG.r(MAPSIZE * 1 / 4, MAPSIZE * 3 / 4);
		}

		void placebuildings() {
			Building b = new Building("locationinn");
			b.setlocation(
					new Point(RPG.r(map.map.length), RPG.r(map.map[0].length)));
			state.blueTeam.add(b);
		}
	}

	/** {@link Item} bag for {@link #gladiators}. */
	HashMap<Integer, ArrayList<Item>> items = new HashMap<Integer, ArrayList<Item>>();
	ArrayList<Combatant> fainted = new ArrayList<Combatant>();
	int tension = RPG.r(TENSIONMIN, TENSIONMAX);
	float check = -Float.MAX_VALUE;

	/** Constructor. */
	public ArenaFight() {
		meld = true;
		weather = Weather.DRY;
		period = Javelin.PERIODNOON;
		setup = new ArenaSetup();
		meld = false;
	}

	@Override
	public ArrayList<Combatant> generate(Integer el) {
		return new ArrayList<Combatant>();
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return Squad.active.members; // TODO
	}

	@Override
	public ArrayList<Item> getbag(Combatant c) {
		ArrayList<Item> bag = items.get(c.id);
		if (bag == null) {
			bag = new ArrayList<Item>();
			items.put(c.id, bag);
		}
		return bag;
	}

	@Override
	public void ready() {
		//
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		// TODO parent is enough
		super.withdraw(combatant, screen);
	}

	@Override
	public boolean onend() {
		for (Combatant c : new ArrayList<Combatant>(state.blueTeam)) {
			if (c.source.passive || c.mercenary) {
				state.blueTeam.remove(c);
			}
		}
		return false;
	}

	@Override
	public void startturn(Combatant acting) {
		super.startturn(acting);
		if (acting.ap < check) {
			return;
		}
		int elblue = CrCalculator.calculateel(state.blueTeam);
		int elred = CrCalculator.calculateel(state.redTeam);
		if (elred - elblue < tension) {
			raisetension(elblue);
			tension = RPG.r(TENSIONMIN, TENSIONMAX);
			awaken();
		}
		check = acting.ap + RPG.r(10, 40) / 10f;
	}

	void awaken() {
		for (Combatant c : new ArrayList<Combatant>(fainted)) {
			if (state.getcombatant(c.location[0], c.location[1]) != null) {
				continue;
			}
			if (RPG.r(1, 10) < 10 + c.hp) {
				fainted.remove(c);
				c.hp = 1;
				c.ap = state.next.ap;
				state.blueTeam.add(c);
				notify("New enemies enter the arena!", c.getlocation());
			}
		}
	}

	void raisetension(int elblue) {
		System.out.println("#arena raising tension: " + tension);
		ArrayList<Combatant> last = null;
		for (int el = elblue - 12; el <= elblue; el += 1) {
			ArrayList<Combatant> group;
			try {
				group = EncounterGenerator.generate(elblue,
						Arrays.asList(Terrain.ALL));
			} catch (GaveUpException e) {
				continue;
			}
			ArrayList<Combatant> redteam = (ArrayList<Combatant>) state.redTeam
					.clone();
			redteam.addAll(group);
			int tension = CrCalculator.calculateel(redteam) - elblue;
			if (tension == this.tension) {
				enter(group, state.redTeam, getmonsterentry());
				return;
			}
			if (tension > this.tension) {
				enter(last == null ? group : last, state.redTeam,
						getmonsterentry());
				return;
			}
			last = group;
		}
	}

	Point getmonsterentry() {
		Point p = new Point(RPG.r(MAPSIZE), RPG.r(MAPSIZE));
		int border = RPG.r(1, 2) == 1 ? 0 : MAPSIZE - 1;
		if (RPG.r(1, 2) == 1) {
			p.x = border;
		} else {
			p.y = border;
		}
		return p;
	}

	void enter(ArrayList<Combatant> entering, List<Combatant> team,
			Point entry) {
		LinkedList<Combatant> place = new LinkedList<Combatant>(entering);
		Collections.shuffle(place);
		Combatant last = place.pop();
		last.setlocation(entry);
		if (!team.contains(last)) {
			team.addAll(entering);
			for (Combatant c : entering) {
				c.rollinitiative();
				if (check != -Float.MAX_VALUE) {
					c.ap += state.next.ap;
					c.initialap = c.ap;
				}
			}
		}
		while (!place.isEmpty()) {
			Point p = last.getlocation();
			p.x += RPG.r(-2, +2);
			p.y += RPG.r(-2, +2);
			if (!p.validate(0, 0, MAPSIZE, MAPSIZE)
					|| state.map[p.x][p.y].blocked
					|| state.getcombatant(p.x, p.y) != null) {
				continue;
			}
			last = place.pop();
			last.setlocation(p);
		}
		if (check != -Float.MAX_VALUE) {
			notify("New enemies enter the arena!", last.getlocation());
		}
	}

	public void notify(String text, Point p) {
		BattleScreen.active.update();
		BattleScreen.active.centerscreen(p.x, p.y);
		Javelin.message(text, true);
	}

	@Override
	public void die(Combatant c) {
		if (!c.mercenary && state.blueTeam.contains(c)
				&& c.getnumericstatus() == Combatant.STATUSUNCONSCIOUS) {
			fainted.add(c);
		}
		super.die(c);
	}
}
