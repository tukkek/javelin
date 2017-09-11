package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.action.target.RangedTarget;
import javelin.controller.ai.ChanceNode;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Prone;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;

/**
 * needs to take into account range increments, each full range increment
 * imposes a cumulative -2 penalty on the attack roll. Actually doesn't sound
 * too important since most weapons used have really long ranges for our combats
 * 
 * also -4 if in melée with another enemy (many more details @
 * http://www.d20pfsrd.com/gamemastering/combat
 * 
 * @author alex
 * @see RangedTarget
 */
public class RangedAttack extends AbstractAttack {
	static final public RangedAttack SINGLETON = new RangedAttack();

	private RangedAttack() {
		super("Ranged attack");
	}

	@Override
	protected boolean isMelee() {
		return false;
	}

	@Override
	List<AttackSequence> getattacks(final Combatant active) {
		return active.source.ranged;
	}

	@Override
	public int getpenalty(final Combatant attacker, final Combatant target,
			final BattleState s) {
		int penalty = super.getpenalty(attacker, target, s);
		if (!attacker.source.hasfeat(PreciseShot.SINGLETON)
				&& s.isengaged(target)) {
			penalty += 4;
		}
		if (!attacker.source.hasfeat(ImprovedPreciseShot.SINGLETON)
				&& s.haslineofsight(attacker,
						target) == javelin.model.state.BattleState.Vision.COVERED) {
			penalty += 4;
		}
		if (target.hascondition(Prone.class) != null) {
			penalty += 2;
		}
		if (ispointblankshot(attacker, target)) {
			penalty -= 1;
		}
		return penalty;
	}

	static boolean ispointblankshot(final Combatant attacker,
			final Combatant target) {
		return attacker.source.hasfeat(PointBlankShot.SINGLETON)
				&& Walker.distance(attacker, target) <= 6;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant active,
			final BattleState gameState) {
		if (gameState.isengaged(active)) {
			return Collections.EMPTY_LIST;
		}
		final ArrayList<List<ChanceNode>> successors = new ArrayList<List<ChanceNode>>();
		for (final Combatant target : gameState.gettargets(active)) {
			for (final Integer attack : getcurrentattack(active)) {
				final BattleState newstate = gameState.clone();
				final Combatant newactive = newstate.clone(active);
				newactive.currentranged.setcurrent(attack,
						newactive.source.ranged);
				successors.add(attack(newstate, newactive, target,
						newactive.currentranged, 0));
			}
		}
		return successors;
	}

	@Override
	public boolean cleave() {
		return false;
	}

	@Override
	protected int getdamagebonus(Combatant attacker, Combatant target) {
		return ispointblankshot(attacker, target) ? 1 : 0;
	}
}