package javelin.controller.action.maneuver;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.action.CastSpell;
import javelin.controller.action.Fire;
import javelin.controller.action.ai.AbstractAttack;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurnException;
import javelin.model.BattleMap;
import javelin.model.feat.Feat;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * This greatly helps feature maneuvers in Javelin - it was adopted with some
 * modifications.
 * 
 * http://www.dnd-wiki.org/wiki/Simplified_Special_Attacks_%283.
 * 5e_Variant_Rule%29#Trip
 * 
 * @author alex
 */
public abstract class Maneuver extends Fire implements AiAction {
	private final Feat prerequisite;
	private int featbonus;

	public Maneuver(String name, String key, char confirm, Feat prerequisite,
			int featbonus) {
		super(name, key, confirm);
		this.prerequisite = prerequisite;
		this.featbonus = featbonus;
	}

	@Override
	public void checkengaged(BattleState state, Combatant c) {
		// engaged is fine
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, BattleMap map) {
		Action.outcome(maneuver(combatant, targetCombatant, battleState));
	}

	@Override
	protected void checkhero(Thing hero) {
		if (!checkhero(hero.combatant)) {
			Game.message("Needs the " + prerequisite + " feat...", null,
					Delay.WAIT);
			throw new RepeatTurnException();
		}
	}

	public boolean checkhero(Combatant combatant) {
		return combatant.source.hasfeat(prerequisite);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (final Combatant target : new ArrayList<Combatant>(targets)) {
			final int deltax = combatant.location[0] - target.location[0];
			final int deltay = combatant.location[1] - target.location[1];
			if (-1 <= deltax && deltax <= +1 && //
					-1 <= deltay && deltay <= +1 && //
					validatetarget(target)) {
				continue;
			}
			targets.remove(target);
		}
	}

	abstract boolean validatetarget(Combatant target);

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant combatant) {
		final ArrayList<List<ChanceNode>> outcomes =
				new ArrayList<List<ChanceNode>>();
		if (!checkhero(combatant)) {
			return outcomes;
		}
		final ArrayList<Combatant> targets = gameState.getCombatants();
		filtertargets(combatant, targets, gameState);
		for (final Combatant target : targets) {
			outcomes.add(maneuver(combatant, target, gameState));
		}
		return outcomes;
	}

	List<ChanceNode> maneuver(Combatant combatant, Combatant target,
			BattleState battleState) {
		battleState = battleState.clone();
		combatant = battleState.clone(combatant);
		target = battleState.clone(target);
		combatant.ap += .5f;
		final int savebonus = calculatesavebonus(target);
		final float savechance = calculatesavechance(combatant, savebonus);
		int touchattackbonus = Monster.getbonus(target.source.dexterity);
		if (touchattackbonus < 0) {
			touchattackbonus = 0;
		}
		final float misschance = calculatemisschance(combatant, target,
				battleState, touchattackbonus);
		final ArrayList<ChanceNode> chances = new ArrayList<ChanceNode>();
		final float failurechance = savechance + (1 - savechance) * misschance;
		chances.add(miss(combatant, target, battleState, failurechance));
		chances.add(hit(combatant, target, battleState, 1 - failurechance));
		return chances;
	}

	public float calculatesavechance(Combatant current, final int savebonus) {
		return calculatesavechance(
				10 + current.source.getbaseattackbonus() / 2
						+ getattackerbonus(current) + size(current) + featbonus,
				savebonus);
	}

	public int calculatesavebonus(Combatant target) {
		return getsavebonus(target) - size(target);
	}

	abstract ChanceNode miss(Combatant combatant, Combatant target,
			BattleState battleState, float chance);

	abstract ChanceNode hit(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, float chance);

	public float calculatesavechance(final int dc, final int bonus) {
		return CastSpell.bind(1 - (dc - bonus) / 20f);
	}

	float calculatemisschance(final Combatant combatant,
			final Combatant targetCombatant, final BattleState battleState,
			final int touchattackbonus) {
		return AbstractAttack.misschance(battleState, combatant,
				targetCombatant, touchattackbonus,
				combatant.source.melee.get(0).get(0), 0);
	}

	static int size(final Combatant combatant) {
		return combatant.source.size - Monster.MEDIUM;
	}

	abstract int getsavebonus(Combatant targetCombatant);

	abstract int getattackerbonus(Combatant combatant);

	@Override
	protected int calculatehitchance(Combatant target, Combatant active,
			BattleState state) {
		return Math.round(
				20 - (1 - calculatemisschance(target, active, state, 0)) * 20);
	}
}
