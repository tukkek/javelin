package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.fight.Fight;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.map.Map;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import tyrant.mikera.engine.RPG;

/**
 * @see Arena
 * 
 * @author alex
 */
public class ArenaFight extends Minigame {
	static final int BOOST = 13;
	static final int MAPSIZE = 28;

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
						f.map.map[x][y].blocked = true;
					}
				}
			}
		}

		@Override
		public void place() {
			Building b = new Building("locationinn");
			b.setlocation(
					new Point(RPG.r(map.map.length), RPG.r(map.map[0].length)));
			state.redTeam.add(b);
		}
	}

	static BuildingLevel[] BUILDINGLEVELS = new BuildingLevel[] {
			new BuildingLevel(0, 5, 70, 60, 5, 0),
			new BuildingLevel(1, 10, 110, 90, 7, 7500 * BOOST),
			new BuildingLevel(2, 15, 240, 180, 8, 25000 * BOOST),
			new BuildingLevel(3, 20, 600, 540, 8, 60000 * BOOST), };

	static class BuildingLevel {
		int level;
		int repair;
		int hp;
		int damagethresold;
		int hardness;
		int cost;

		public BuildingLevel(int level, int repair, int hp, int damagethresold,
				int hardness, int cost) {
			super();
			this.level = level;
			this.repair = repair;
			this.hp = hp;
			this.damagethresold = damagethresold;
			this.hardness = hardness;
			this.cost = cost;
		}
	}

	class Building extends Combatant {
		int level;
		int damagethresold;

		public Building(String avatar) {
			super(Javelin.getmonster("Building"), false);
			source.passive = true;
			source.avatarfile = avatar;
			source.immunitytocritical = true;
			source.immunitytomind = true;
			source.immunitytoparalysis = true;
			source.immunitytopoison = true;
			setlevel(BUILDINGLEVELS[0]);
		}

		void setlevel(BuildingLevel level) {
			this.level = level.level;
			maxhp = level.hp;
			hp = maxhp;
			damagethresold = level.damagethresold;
			source.dr = level.hardness;
			source.fasthealing = level.repair;
		}

		void upgrade() {
			setlevel(BUILDINGLEVELS[level + 1]);
		}

		/** TODO use */
		Integer getupgradecost() {
			int next = level + 1;
			return next < BUILDINGLEVELS.length ? BUILDINGLEVELS[next].cost
					: null;
		}

		@Override
		public void act(BattleState s) {
			s.clone(this).ap += 1;
		}
	}

	/** {@link Item} bag for {@link #gladiators}. */
	HashMap<Integer, ArrayList<Item>> items = new HashMap<Integer, ArrayList<Item>>();

	/** Constructor. */
	public ArenaFight() {
		meld = true;
		weather = Weather.DRY;
		period = Javelin.PERIODNOON;
		setup = new ArenaSetup();
	}

	@Override
	public ArrayList<Combatant> generate(Integer el) {
		// if (map == null) {
		// drawmap();
		// }
		// if (period == null) {
		// drawperiod();
		// }
		// if (weather == null) {
		// drawweather();
		// }
		// redteam = generate(CrCalculator.calculateel(blueteam));
		return new ArrayList<Combatant>();
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return Squad.active.members; // TODO
	}

	@Override
	public boolean onend() {
		// TODO
		return false;
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
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

}
