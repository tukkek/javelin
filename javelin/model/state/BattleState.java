package javelin.model.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.ai.StateSucessorProvider;
import javelin.controller.walker.ClearPath;
import javelin.controller.walker.ObstructedPath;
import javelin.controller.walker.Walker;
import javelin.controller.walker.Walker.Step;
import javelin.model.TeamContainer;
import javelin.model.unit.Combatant;

public class BattleState implements Node, TeamContainer {
	public ArrayList<Combatant> blueTeam = new ArrayList<Combatant>();
	public ArrayList<Combatant> redTeam = new ArrayList<Combatant>();
	public ArrayList<Combatant> dead;
	/**
	 * Since it's immutable no need to clone it.
	 */
	transient public Square[][] map;
	public Combatant next;
	public String period;

	public BattleState(final List<Combatant> blueTeam,
			final List<Combatant> redTeam, ArrayList<Combatant> dead,
			final Square[][] map, String period) {
		this.map = map;
		this.period = period;
		this.dead = (ArrayList<Combatant>) dead.clone();
		setupstate(blueTeam, redTeam);
	}

	@Override
	public BattleState clone() {
		try {
			final BattleState clone = (BattleState) super.clone();
			clone.blueTeam = new ArrayList<Combatant>();
			clone.redTeam = new ArrayList<Combatant>();
			clone.dead = (ArrayList<Combatant>) clone.dead.clone();
			clone.map = map;
			clone.setupstate(blueTeam, redTeam);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
	}

	public void setupstate(final List<Combatant> blueTeam,
			final List<Combatant> redTeam) {
		for (final Combatant c : blueTeam) {
			this.blueTeam.add(updateclone(c));
		}
		for (final Combatant c : redTeam) {
			this.redTeam.add(updateclone(c));
		}
		checkwhoisnext();
	}

	public Combatant updateclone(Combatant c) {
		c = c.clone();
		c.refresh();
		return c;
	}

	public void checkwhoisnext() {
		final List<Combatant> combatants = getCombatants();
		if (combatants.isEmpty()) {
			next = null;
			return;
		}
		next = combatants.get(0);
		for (int i = 1; i < combatants.size(); i++) {
			final Combatant c = combatants.get(i);
			if (c.ap < next.ap) {
				next = c;
			}
		}
	}

	/**
	 * TODO could instead increment a turn at each {@link BattleState} instance.
	 * Is it possible? Would need soem revision of places where a new instance
	 * is created just in case.
	 */
	public BattleState(final BattleState state, final int newturn) {
		this(state.blueTeam, state.redTeam, state.dead, state.map, state.period);
	}

	@Override
	public Iterable<List<ChanceNode>> getSucessors() {
		return new StateSucessorProvider(this);
	}

	@Override
	public void changePlayer() {
		// not needed? TODO
	}

	@Override
	public ArrayList<Combatant> getCombatants() {
		final ArrayList<Combatant> list = new ArrayList<Combatant>();
		list.addAll(redTeam);
		list.addAll(blueTeam);
		return list;
	}

	public ArrayList<Combatant> getSurroudings(final Combatant surrounded) {
		final ArrayList<Combatant> surroundings = new ArrayList<Combatant>();
		final int[] location = surrounded.location;
		final ArrayList<Combatant> combatents = new ArrayList<Combatant>();
		combatents.addAll(blueTeam);
		combatents.addAll(redTeam);
		location: for (final Combatant c : combatents) {
			for (int x = location[0] - 1; x <= location[0] + 1; x++) {
				for (int y = location[1] - 1; y <= location[1] + 1; y++) {
					if (x == location[0] && y == location[1]) {
						/* center */
						continue;
					}
					if (c.location[0] == x && c.location[1] == y) {
						surroundings.add(c);
						continue location;
					}
				}
			}
		}
		return surroundings;
	}

	@Override
	public List<Combatant> getBlueTeam() {
		return blueTeam;
	}

	// /**
	// * TODO needs to go after combatant/monster separation
	// */
	// private ArrayList<Monster> convertlist(final List<Combatant> team) {
	// final ArrayList<Monster> list = new ArrayList<Monster>() {
	// @Override
	// public boolean remove(final Object arg0) {
	// throw new RuntimeException("Remove directly from state list");
	// }
	// };
	// for (final Combatant c : team) {
	// list.add(c.monster);
	// }
	// return list;
	// }

	@Override
	public List<Combatant> getRedTeam() {
		return redTeam;
	}

	public Combatant translatecombatant(final Combatant target) {
		for (final Combatant c : getCombatants()) {
			if (c.id == target.id) {
				return c;
			}
		}
		throw new RuntimeException("Unknown combatant!");
	}

	public Combatant getCombatant(final int x, final int y) {
		for (final Combatant c : getCombatants()) {
			if (c.location[0] == x && c.location[1] == y) {
				return c;
			}
		}
		return null;
	}

	public void remove(Combatant c) {
		c = translatecombatant(c);
		if (!blueTeam.remove(c)) {
			redTeam.remove(c);
		}
		if (c == next) {
			checkwhoisnext();
		}
	}

	public boolean isEngaged(final Combatant c) {
		for (final Combatant nearby : getSurroudings(c)) {
			if (!c.isAlly(nearby, this)) {
				return true;
			}
		}
		return false;
	}

	public enum Vision {
		CLEAR, COVERED, BLOCKED,
	}

	/**
	 * @param range
	 *            How many 5-feet squares ahead the active combatant can see.
	 * @param periodperception
	 *            Represents the light perception for the active combatant since
	 *            some monsters may have the darkvision quality or similar. If
	 *            night or evening the character will not able to see past
	 *            obstacles in the map.
	 */
	public Vision hasLineOfSight(final Point me, final Point target, int range,
			String periodperception) {
		final ArrayList<Step> clear = new ClearPath(me, target, this).walk();
		final ArrayList<Step> covered = periodperception == Javelin.PERIOD_EVENING
				|| periodperception == Javelin.PERIOD_NIGHT ? null
				: new ObstructedPath(me, target, this).walk();
		if (clear == null && covered == null) {
			return Vision.BLOCKED;
		}
		final ArrayList<Step> steps;
		if (clear == null) {
			steps = covered;
		} else if (covered == null) {
			steps = clear;
		} else if (clear.size() <= covered.size()) {
			steps = clear;
		} else {
			steps = covered;
		}
		if (steps.size() >= range) {
			return Vision.BLOCKED;
		}
		return steps == clear ? Vision.CLEAR : Vision.COVERED;
	}

	public List<Combatant> getTargets(Combatant combatant) {
		combatant = translatecombatant(combatant);
		return getAllTargets(combatant, blueTeam.contains(combatant) ? redTeam
				: blueTeam);
	}

	public List<Combatant> getAllTargets(Combatant combatant,
			List<Combatant> team) {
		final List<Combatant> targets = new ArrayList<Combatant>();
		for (final Combatant targetc : team) {
			if (hasLineOfSight(combatant, targetc) != Vision.BLOCKED) {
				targets.add(targetc);
			}
		}
		return targets;
	}

	public Vision hasLineOfSight(Combatant me, Combatant target) {
		return hasLineOfSight(new Point(me.location[0], me.location[1]),
				new Point(target.location[0], target.location[1]),
				me.view(period), me.perceive(period));
	}

	public boolean isflanked(final Combatant target, final Combatant attacker) {
		if (Walker.distance(target, attacker) >= 1.5) {
			return false;
		}
		final ArrayList<Combatant> attackerteam = blueTeam.contains(attacker) ? blueTeam
				: redTeam;
		final ArrayList<Combatant> defenderteam = blueTeam.contains(target) ? blueTeam
				: redTeam;
		if (attackerteam == defenderteam) {
			return false;
		}
		final int deltax = target.location[0] - attacker.location[0];
		final int deltay = target.location[1] - attacker.location[1];
		final int flankingx = target.location[0] + deltax;
		final int flankingy = target.location[1] + deltay;
		final Combatant flank = getCombatant(flankingx, flankingy);
		return flank == null || Walker.distance(target, flank) >= 1.5 ? false
				: attackerteam.contains(flank);
	}

	public int delta(final Combatant surroundinga,
			final Combatant surroundingb, final int l) {
		return surroundinga.location[l] - surroundingb.location[l];
	}

	public boolean isaligned(final int deltax, final int deltay) {
		return deltax == 2 && deltay == 0 || deltay == 2 && deltax == 0;
	}

	public boolean isdiagonal(final int deltax, final int deltay) {
		return deltax == 2 && deltay == 2;
	}

	public ArrayList<Combatant> getTeam(Combatant c) {
		return blueTeam.contains(c) ? blueTeam : redTeam;
	}

	@Override
	public String toString() {
		String out = "";
		for (Square[] line : map) {
			for (Square s : line) {
				out += s;
			}
			out += "\n";
		}
		return out;
	}
}
