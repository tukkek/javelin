package javelin.controller.fight.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.map.location.LocationMap;
import javelin.model.unit.Combatant;

public class LocationFightSetup extends BattleSetup {
	static final ArrayList<Point> DELTAS = new ArrayList<Point>();
	static {
		for (int x = -1; x <= +1; x++) {
			for (int y = -1; y <= +1; y++) {
				DELTAS.add(new Point(x, y));
			}
		}
	}

	final LocationMap map;

	/**
	 * @param hauntFight
	 */
	public LocationFightSetup(LocationMap map) {
		this.map = map;
	}

	@Override
	public void place() {
		clear(Fight.state.blueTeam);
		clear(Fight.state.redTeam);
		place(Fight.state.blueTeam, map.startingareablue);
		place(Fight.state.redTeam, map.startingareared);
	}

	protected void place(List<Combatant> team, List<Point> startingarea) {
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

	protected boolean place(Combatant c, int x, int y) {
		if (map.map[x][y].blocked || Fight.state.getcombatant(x, y) != null) {
			return false;
		}
		c.location[0] = x;
		c.location[1] = y;
		return true;
	}

	protected void clear(ArrayList<Combatant> team) {
		for (Combatant c : team) {
			c.location[0] = -1;
			c.location[1] = -1;
		}
	}
}