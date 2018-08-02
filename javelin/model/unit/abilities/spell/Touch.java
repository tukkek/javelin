package javelin.model.unit.abilities.spell;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
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
			return Integer.MIN_VALUE;
		}
		int touchac = target.getac() - target.source.armor;
		int attackbonus = active.source.getbab()
				- Monster.getbonus(active.source.strength);
		return touchac - attackbonus;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant target : new ArrayList<>(targets)) {
			if (isfar(combatant, target)) {
				targets.remove(target);
			}
		}
	}

	public static boolean isfar(Combatant combatant, Combatant target) {
		Point a = combatant.getlocation();
		Point b = target.getlocation();
		return a.distanceinsteps(b) > 1;
	}
}