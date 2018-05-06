package javelin.model.unit.abilities.spell;

import java.util.ArrayList;
import java.util.List;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Base implementation for touch spells.
 * 
 * @see Ray
 * @author alex
 */
public abstract class Touch extends Spell {

	/**
	 * Constructor.
	 * 
	 * @param realmp
	 */
	public Touch(String name, int level, float incrementcost, Realm realmp) {
		super(name, level, incrementcost, realmp);
		castinbattle = true;
	}

	@Override
	public int hit(Combatant active, Combatant target, BattleState s) {
		if (castonallies && active.isally(target, s)) {
			return -Integer.MAX_VALUE;
		}
		return (target.ac() - target.source.armor)
				- (active.source.getbaseattackbonus()
						+ Monster.getbonus(active.source.strength));
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			if (isfar(combatant, target)) {
				targets.remove(target);
			}
		}
	}

	public static boolean isfar(Combatant combatant, Combatant target) {
		int deltax = target.location[0] - combatant.location[0];
		int deltay = target.location[1] - combatant.location[1];
		return deltax > 1 || deltax < -1 || deltay > 1 || deltay < -1;
	}
}