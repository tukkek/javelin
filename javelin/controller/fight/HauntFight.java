package javelin.controller.fight;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.BattleSetup;
import javelin.controller.Point;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.Location;

public class HauntFight extends Siege {
	static final ArrayList<Point> DELTAS = new ArrayList<Point>();
	static {
		for (int x = -1; x <= +1; x++) {
			for (int y = -1; y <= +1; y++) {
				DELTAS.add(new Point(x, y));
			}
		}
	}

	public class HauntSetup extends BattleSetup {
		@Override
		public void place() {
			clear(Fight.state.blueTeam);
			clear(Fight.state.redTeam);
			place(Fight.state.blueTeam, map.startingareablue);
			place(Fight.state.redTeam, map.startingareared);
		}

		void place(ArrayList<Combatant> team, ArrayList<Point> startingarea) {
			Collections.shuffle(team);
			teamplacement: for (Combatant c : team) {
				Collections.shuffle(startingarea);
				for (Point p : new ArrayList<Point>(startingarea)) {
					Collections.shuffle(DELTAS);
					for (Point delta : DELTAS) {
						int x = p.x + delta.x;
						int y = p.y + delta.y;
						if (map.validatecoordinate(x, y) && place(c, x, y)) {
							startingarea.add(new Point(x, y));
							continue teamplacement;
						}
					}
				}
				throw new RuntimeException(
						"Couldn't place all combatants on map " + map.name);
			}
		}

		private boolean place(Combatant c, int x, int y) {
			if (map.map[x][y].blocked
					|| Fight.state.getcombatant(x, y) != null) {
				return false;
			}
			c.location[0] = x;
			c.location[1] = y;
			return true;
		}

		void clear(ArrayList<Combatant> team) {
			for (Combatant c : team) {
				c.location[0] = -1;
				c.location[1] = -1;
			}
		}
	}

	public HauntFight(Location l) {
		super(l);
		bribe = false;
		hide = false;
		setup = new HauntSetup();
	}
}
