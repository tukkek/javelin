package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.Point;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.fight.setup.LocationFightSetup;
import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

/**
 * Will place weaker monsters in front and stronger monsters in the back. For
 * example: 1, 2 ad 3 for red team and 4, 5 and 6 for blue tema in ascending
 * order of power.
 * 
 * @author alex
 */
public class TownSiege extends Siege {
	static final int LAYERSPERTEAM = 3;

	public class TownSiegeMap extends LocationMap {
		TreeMap<Integer, List<Point>> PLACEMENT = new TreeMap<Integer, List<Point>>();
		int grassratio = RPG.r(3, 6);

		public TownSiegeMap() {
			super("town-hamlet");
			floor = Images.getImage("terraintowngrass");
			obstacle = Images.getImage("terrainbush");
			wall = Images.getImage("terrainshipfloor");
			for (int i = 1; i <= LAYERSPERTEAM * 2; i++) {
				PLACEMENT.put(i, new ArrayList<Point>());
			}
		}

		@Override
		protected Square processtile(int x, int y, char c) {
			Square s = super.processtile(x, y, c);
			if (Character.isDigit(c)) {
				int index = Integer.parseInt(Character.toString(c));
				PLACEMENT.get(index).add(new Point(x, y));
				if (index > LAYERSPERTEAM) {
					c = '.';
				}
			}
			if (c == '.' && RPG.r(1, 10) <= grassratio) {
				s.obstructed = true;
			}
			return s;
		}
	}

	public class Setup extends LocationFightSetup {
		TownSiegeMap siegemap;
		Town t;

		Setup(TownSiegeMap map, Town t) {
			super(map);
			siegemap = map;
			this.t = t;
		}

		@Override
		public void place() {
			clear(Fight.state.blueTeam);
			clear(Fight.state.redTeam);
			placeteam(Fight.state.redTeam, 1);
			placeteam(Fight.state.blueTeam, 1 + LAYERSPERTEAM);
		}

		void placeteam(ArrayList<Combatant> team, int layer) {
			team = new ArrayList<Combatant>(team);
			team.sort(CombatantByCr.SINGLETON);
			for (int slice = LAYERSPERTEAM; slice > 0; slice--) {
				if (team.isEmpty()) {
					continue;
				}
				List<Combatant> view = new ArrayList<Combatant>(
						team.subList(0, team.size() / slice));
				team.removeAll(view);
				place(view, siegemap.PLACEMENT.get(layer));
				layer += 1;
			}
		}
	}

	public TownSiege(Town t) {
		super(t);
		TownSiegeMap map = new TownSiegeMap();
		this.map = map;
		setup = new Setup(map, t);
	}
}
