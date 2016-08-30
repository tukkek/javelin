package javelin.model.state;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.fight.Fight;
import javelin.controller.walker.ClearPath;
import javelin.controller.walker.ObstructedPath;
import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;
import javelin.model.TeamContainer;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Javelin's implementation of {@link Node}.
 * 
 * {@link #clone()} is used for cloning the state but it does not clone the
 * {@link Combatant} instances! You need to use {@link #clone(Combatant))}
 * afterwards to do so.
 * 
 * @see #cloneifdifferent(Combatant, Combatant)
 * 
 * @author alex
 */
public class BattleState implements Node, TeamContainer {
	public ArrayList<Combatant> blueTeam;
	public ArrayList<Combatant> redTeam;
	public ArrayList<Combatant> dead;
	public ArrayList<Meld> meld;
	/**
	 * Since it's immutable no need to clone it.
	 */
	transient public Square[][] map;
	public Combatant next;
	public String period;

	public BattleState(final ArrayList<Combatant> blueTeam,
			final ArrayList<Combatant> redTeam, ArrayList<Combatant> dead,
			final Square[][] map, String period, ArrayList<Meld> meld) {
		this.map = map;
		this.period = period;
		this.dead = (ArrayList<Combatant>) dead.clone();
		this.blueTeam = (ArrayList<Combatant>) blueTeam.clone();
		this.redTeam = (ArrayList<Combatant>) redTeam.clone();
		this.meld = (ArrayList<Meld>) meld.clone();
		checkwhoisnext();
	}

	public BattleState(Fight f) {
		map = f.map == null ? null : f.map.map;
		period = f.period;
		dead = new ArrayList<Combatant>();
		this.blueTeam = new ArrayList<Combatant>();
		this.redTeam = new ArrayList<Combatant>();
		this.meld = new ArrayList<Meld>();
		checkwhoisnext();

	}

	@Override
	public BattleState clone() {
		try {
			final BattleState clone = (BattleState) super.clone();
			clone.dead = (ArrayList<Combatant>) clone.dead.clone();
			clone.blueTeam = (ArrayList<Combatant>) blueTeam.clone();
			clone.redTeam = (ArrayList<Combatant>) redTeam.clone();
			clone.meld = (ArrayList<Meld>) meld.clone();
			checkwhoisnext();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
	}

	public void checkwhoisnext() {
		final List<Combatant> combatants = getCombatants();
		if (combatants.isEmpty()) {
			next = null;
			return;
		}
		next = combatants.get(0);
		final int ncombatants = combatants.size();
		for (int i = 1; i < ncombatants; i++) {
			final Combatant c = combatants.get(i);
			if (c.ap < next.ap) {
				next = c;
			}
		}
	}

	@Override
	public Iterable<List<ChanceNode>> getSucessors() {
		return new ActionProvider(this);
	}

	@Override
	public ArrayList<Combatant> getCombatants() {
		final ArrayList<Combatant> list =
				(ArrayList<Combatant>) blueTeam.clone();
		list.addAll(redTeam);
		return list;
	}

	public ArrayList<Combatant> getSurroundings(final Combatant surrounded) {
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

	@Override
	public List<Combatant> getRedTeam() {
		return redTeam;
	}

	/**
	 * @see BattleState#clone(Combatant)
	 */
	public Combatant clone(Combatant c) {
		final ArrayList<Combatant> team = getTeam(c);
		final int index = team.indexOf(c);
		if (index == -1) {
			return null;
		}
		c = team.get(index).clone();
		c.refresh();
		team.set(index, c);
		if (next.equals(c)) {
			next = c;
		}
		return c;
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
		c = clone(c);
		if (!blueTeam.remove(c)) {
			redTeam.remove(c);
		}
		if (c == next) {
			checkwhoisnext();
		}
	}

	/**
	 * To avoid having to implement attacks-of-opporutnity gonna simply prohibit
	 * that anything that would cause an aoo is simply prohibited. since the
	 * game is more fluid with movement/turns now this shouldn't be a problem.
	 * 
	 * Disengaging is simply forcing a 5-foot step to avoid aoo as per the core
	 * rules.
	 */
	public boolean isengaged(final Combatant c) {
		if (c.burrowed) {
			return false;
		}
		for (final Combatant nearby : getSurroundings(c)) {
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
		final ArrayList<Step> covered =
				periodperception == Javelin.PERIODEVENING
						|| periodperception == Javelin.PERIODNIGHT ? null
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
		// combatant = translatecombatant(combatant);
		return getAllTargets(combatant,
				getTeam(combatant) == blueTeam ? redTeam : blueTeam);
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
		return hasLineOfSight(me,
				new Point(target.location[0], target.location[1]));
	}

	public Vision hasLineOfSight(Combatant me, Point target) {
		return hasLineOfSight(new Point(me.location[0], me.location[1]),
				new Point(target.x, target.y), me.view(period),
				me.perceive(period));
	}

	public boolean isflanked(final Combatant target, final Combatant attacker) {
		if (attacker.burrowed || Walker.distance(target, attacker) >= 1.5) {
			return false;
		}
		final ArrayList<Combatant> attackerteam =
				blueTeam.contains(attacker) ? blueTeam : redTeam;
		final ArrayList<Combatant> defenderteam =
				blueTeam.contains(target) ? blueTeam : redTeam;
		if (attackerteam == defenderteam) {
			return false;
		}
		final int deltax = target.location[0] - attacker.location[0];
		final int deltay = target.location[1] - attacker.location[1];
		final int flankingx = target.location[0] + deltax;
		final int flankingy = target.location[1] + deltay;
		final Combatant flank = getCombatant(flankingx, flankingy);
		return flank != null && !flank.burrowed
				&& Walker.distance(target, flank) < 1.5
				&& attackerteam.contains(flank);
	}

	public int delta(final Combatant surroundinga, final Combatant surroundingb,
			final int l) {
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
		String out = blueTeam + "\n" + redTeam + "\n\n";
		for (Square[] line : map) {
			for (Square s : line) {
				out += s;
			}
			out += "\n";
		}
		return out;
	}

	/**
	 * This is necessary because sometimes you don't want to clone a unit twice
	 * (which would result in 2 different clones). For example: if you have a
	 * Caster and a Target it just could be they are both the same unit
	 * (self-targetting) and in this case you don't want to clone them twice.
	 * 
	 * @param c
	 *            If not equal, will clone and return this unit.
	 * @param same
	 *            If equal will return this unit.
	 */
	public Combatant cloneifdifferent(Combatant c, Combatant same) {
		return c.equals(same) ? same : clone(c);
	}

	public Meld findmeld(int x, int y) {
		for (Meld m : meld) {
			if (m.x == x && m.y == y) {
				return m;
			}
		}
		return null;
	}

	public void addmeld(int x, int y, Monster dead) {
		meld.add(new Meld(x, y, next.ap + 1, dead));
	}

	@Override
	public BattleState clonedeeply() {
		BattleState cl = clone();
		cl.blueTeam.clear();
		cl.redTeam.clear();
		for (Combatant c : blueTeam) {
			cl.blueTeam.add(c.clone());
		}
		for (Combatant c : redTeam) {
			cl.redTeam.add(c.clone());
		}
		return cl;
	}

	public Meld getmeld(int x, int y) {
		for (Meld m : meld) {
			if (m.x == x && m.y == y) {
				return m;
			}
		}
		return null;
	}
}
