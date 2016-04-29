package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Spells with attack rolls are supposed to have critical hits too but for the
 * sake of AI speed this rule is ignored.
 * 
 * @author alex
 */
public class CastSpell extends Fire implements AiAction {
	private Spell casting;

	public CastSpell(String name, String key) {
		super(name, key, 's');
	}

	@Override
	public boolean perform(Combatant c, BattleMap map, Thing thing) {
		Game.messagepanel.clear();
		casting = null;
		ArrayList<Spell> castable = new ArrayList<Spell>();
		boolean engagned = map.getState().isEngaged(c);
		for (Spell s : c.spells) {
			if (engagned && !concentrate(c, s)) {
				continue;
			}
			if (s.canbecast(c) && s.aggressive) {
				castable.add(s);
			}
		}
		if (castable.isEmpty()) {
			Game.message("No spells to cast right now.", null, Delay.WAIT);
			return false;
		}
		String list = "Choose a spell:\n";
		for (int i = 0; i < castable.size(); i++) {
			list += "[" + (i + 1) + "] " + castable.get(i) + "\n";
		}
		Game.message(list, null, Delay.NONE);
		try {
			final int i = Integer.parseInt(InfoScreen.feedback().toString());
			if (i > c.spells.size()) {
				return perform(c, map, thing);
			}
			casting = castable.get(i - 1);
		} catch (NumberFormatException e) {
			throw new RepeatTurnException();
			// continue
		}
		return super.perform(c, map, thing);
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState s, BattleMap m) {
		Action.outcome(cast(combatant, targetCombatant,
				combatant.spells.indexOf(casting), s));
	}

	public static List<ChanceNode> cast(Combatant active, Combatant target,
			final int spellindex, BattleState state) {
		state = state.clone();
		active = state.clone(active);
		target = state.cloneifdifferent(target, active);
		final Spell spell = active.spells.get(spellindex);
		active.ap += spell.apcost();
		spell.used += 1;
		final List<ChanceNode> chances = new ArrayList<ChanceNode>();
		final String prefix =
				active + " casts " + spell.name.toLowerCase() + "!\n";
		final int touchtarget = spell.calculatetouchdc(active, target, state);
		float misschance;
		if (touchtarget == -Integer.MAX_VALUE) {
			misschance = 0;
		} else {
			misschance = bind(touchtarget / 20f);
			chances.add(new ChanceNode(state, misschance,
					prefix + active + " misses ranged touch attack.",
					Delay.BLOCK));
		}
		final float hitc = 1 - misschance;
		final float affectchance =
				affect(target, state, spell, chances, prefix, hitc);
		final float savec = convertsavedctochance(
				spell.calculatesavetarget(active, target));
		if (savec != 0) {
			chances.add(hit(active, target, state, spell, savec * affectchance,
					true, prefix));
		}
		if (savec != 1) {
			chances.add(hit(active, target, state, spell,
					(1 - savec) * affectchance, false, prefix));
		}
		if (Javelin.DEBUG) {
			ActionProvider.validate(chances);
		}
		return chances;
	}

	public static float affect(Combatant target, BattleState state,
			final Spell spell, final List<ChanceNode> chances,
			final String prefix, float hitchance) {
		if (spell.friendly || target.source.sr == 0) {
			return hitchance;
		}
		final float resistchance =
				bind((target.source.sr - spell.casterlevel) / 20f) * hitchance;
		chances.add(new ChanceNode(state, resistchance,
				prefix + target + " resists spell!", Delay.BLOCK));
		return hitchance - resistchance;
	}

	public static float convertsavedctochance(final int savedc) {
		if (savedc == Integer.MAX_VALUE) {
			return 0;
		}
		if (savedc == -Integer.MAX_VALUE) {
			return 1;
		}
		return 1 - bind(savedc / 20f);
	}

	public static ChanceNode hit(Combatant active, Combatant target,
			BattleState state, final Spell spell, final float chance,
			final boolean saved, final String prefix) {
		state = state.clone();
		active = state.clone(active);
		target = state.cloneifdifferent(target, active);
		return new ChanceNode(state, chance,
				prefix + spell.cast(active, target, state, saved), Delay.BLOCK);
	}

	@Override
	protected void checkhero(Thing hero) {
		// no check
	}

	@Override
	protected int calculatehitchance(Combatant target, Combatant active,
			BattleState state) {
		return casting.calculatehitdc(active, target, state);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		casting.filtertargets(combatant, targets, s);
	}

	@Override
	public void checkengaged(BattleState state, Combatant c) {
		if (!concentrate(c, casting)) {
			super.checkengaged(state, c);
		}
	}

	boolean concentrate(Combatant c, Spell s) {
		final int concentration = c.source.skills.concentration
				+ Monster.getbonus(c.source.constitution);
		return concentration >= s.casterlevel;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant combatant) {
		final ArrayList<List<ChanceNode>> chances =
				new ArrayList<List<ChanceNode>>();
		boolean engaged = gameState.isEngaged(combatant);
		final ArrayList<Spell> spells = combatant.spells;
		for (int i = 0; i < spells.size(); i++) {
			final Spell spell = spells.get(i);
			if (engaged && !concentrate(combatant, spell)) {
				continue;
			}
			if (!spell.canbecast(combatant)) {
				continue;
			}
			final List<Combatant> targets = gameState.getAllTargets(combatant,
					gameState.getCombatants());
			spell.filtertargets(combatant, targets, gameState);
			for (Combatant target : targets) {
				chances.add(cast(combatant, target, i, gameState));
			}
		}
		return chances;
	}
}
