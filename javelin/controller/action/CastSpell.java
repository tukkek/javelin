package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.StateSucessorProvider;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Spells with attack rolls are supposed to have critical hits too but for the
 * sake of AI speed this rule is ignored.
 * 
 * @author alex
 */
public class CastSpell extends Fire {
	private Spell casting;

	public CastSpell(String name, String key) {
		super(name, key, 's');
	}

	@Override
	public boolean perform(Combatant c, BattleMap map) {
		Game.messagepanel.clear();
		casting = null;
		ArrayList<Spell> castable = new ArrayList<Spell>();
		for (Spell s : c.spells) {
			if (!s.exhausted()) {
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
		Character feedback = IntroScreen.feedback();
		try {
			int i = Integer.parseInt(feedback.toString());
			if (i > c.spells.size()) {
				return perform(c, map);
			}
			casting = castable.get(i - 1);
		} catch (NumberFormatException e) {
			return perform(c, map);
		}
		return super.perform(c, map);
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
		active = state.translatecombatant(active);
		target = state.translatecombatant(target);
		final Spell spell = active.spells.get(spellindex);
		active.ap += spell.apcost();
		spell.used += 1;
		final List<ChanceNode> chances = new ArrayList<ChanceNode>();
		final String prefix = active + " casts " + spell.name + "!\n";
		final int touchtarget = spell.calculatetouchdc(active, target, state);
		float misschance;
		if (touchtarget == -Integer.MAX_VALUE) {
			misschance = 0;
		} else {
			misschance = bind(touchtarget / 20f);
			chances.add(new ChanceNode(state, misschance, prefix + active
					+ " misses ranged touch attack.", Delay.BLOCK));
		}
		final float hitc = 1 - misschance;
		final float affectchance = affect(target, state, spell, chances,
				prefix, hitc);
		final float savec = convertsaverolltochance(spell.calculatesavetarget(
				active, target));
		if (savec != 0) {
			chances.add(hit(active, target, state, spell, savec * affectchance,
					true, prefix));
		}
		if (savec != 1) {
			chances.add(hit(active, target, state, spell, (1 - savec)
					* affectchance, false, prefix));
		}
		if (Javelin.DEBUG) {
			StateSucessorProvider.validate(chances);
		}
		return chances;
	}

	public static float affect(Combatant target, BattleState state,
			final Spell spell, final List<ChanceNode> chances,
			final String prefix, float hitchance) {
		if (spell.friendly || target.source.sr == 0) {
			return hitchance;
		}
		final float resistchance = bind((target.source.sr - spell.casterlevel) / 20f)
				* hitchance;
		chances.add(new ChanceNode(state, resistchance, prefix + target
				+ " resists spell!", Delay.BLOCK));
		return hitchance - resistchance;
	}

	public static float bind(float misschance) {
		if (misschance > .95f) {
			misschance = .95f;
		} else if (misschance < .05) {
			misschance = .95f;
		}
		return misschance;
	}

	public static float convertsaverolltochance(final float savec) {
		if (savec == Integer.MAX_VALUE) {
			return 0;
		}
		if (savec == -Integer.MAX_VALUE) {
			return 1;
		}
		return 1 - Breath.limitd20(savec / 20f);
	}

	public static ChanceNode hit(Combatant active, Combatant target,
			BattleState state, final Spell spell, final float chance,
			final boolean saved, final String prefix) {
		state = state.clone();
		active = state.translatecombatant(active);
		target = state.translatecombatant(target);
		return new ChanceNode(state, chance, prefix
				+ spell.cast(active, target, state, saved), Delay.BLOCK);
	}

	@Override
	protected void checkhero(Thing hero) {
		// no check
	}

	@Override
	int calculatehitchance(Combatant target, Combatant active, BattleState state) {
		return casting.calculatehitdc(active, target, state);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		casting.filtertargets(combatant, targets, s);
	}

	@Override
	public List<List<ChanceNode>> getSucessors(final BattleState gameState,
			final Combatant combatant) {
		final ArrayList<List<ChanceNode>> chances = new ArrayList<List<ChanceNode>>();
		if (gameState.isEngaged(combatant)) {
			return chances;
		}
		final ArrayList<Spell> spells = combatant.spells;
		for (int i = 0; i < spells.size(); i++) {
			final Spell spell = spells.get(i);
			if (spell.exhausted()) {
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
