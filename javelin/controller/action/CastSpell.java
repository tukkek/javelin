package javelin.controller.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.InfoScreen;

/**
 * Spells with attack rolls are supposed to have critical hits too but for the
 * sake of AI speed this rule is ignored.
 * 
 * @author alex
 */
public class CastSpell extends Fire implements AiAction {
	/** Only instance of CastSpell to exist. */
	public static final CastSpell singleton = new CastSpell();
	/** Spell for {@link Fire} to perform. */
	public Spell casting;

	CastSpell() {
		super("Cast spells", "s", 's');
	}

	@Override
	public boolean perform(Combatant c) {
		Game.messagepanel.clear();
		casting = null;
		final ArrayList<Spell> castable = new ArrayList<Spell>();
		final boolean engaged = Fight.state.isengaged(c);
		for (Spell s : c.spells) {
			if (engaged && s.provokeaoo && !c.source.concentrate(s)) {
				continue;
			}
			if (s.canbecast(c)) {
				castable.add(s);
			}
		}
		if (castable.isEmpty()) {
			Game.message("No spells can be cast right now.", Delay.WAIT);
			return false;
		}
		castable.sort(new Comparator<Spell>() {
			@Override
			public int compare(Spell o1, Spell o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		String list = "Choose a spell:\n";
		for (int i = 0; i < castable.size(); i++) {
			list += "[" + (i + 1) + "] " + castable.get(i) + "\n";
		}
		Game.message(list, Delay.NONE);
		try {
			final int i = Integer.parseInt(InfoScreen.feedback().toString());
			if (i > c.spells.size()) {
				return perform(c);
			}
			return cast(castable.get(i - 1), c);
		} catch (NumberFormatException e) {
			Game.messagepanel.clear();
			throw new RepeatTurn();
		}
	}

	/**
	 * Like {@link #perform(Combatant, BattleMap, Thing)} except skips the
	 * selection UI step.
	 */
	public boolean cast(Spell spell, Combatant c) {
		casting = spell;
		return super.perform(c);
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState s) {
		Action.outcome(cast(combatant, targetCombatant,
				combatant.spells.indexOf(casting), s));
	}

	List<ChanceNode> cast(Combatant caster, Combatant target, int spellindex,
			BattleState state) {
		state = state.clone();
		caster = state.clone(caster);
		caster.clonesource();
		target = state.cloneifdifferent(target, caster);
		if (target != caster) {
			target.clonesource();
		}
		Spell spell = null;
		if (casting == null) {
			spell = caster.spells.get(spellindex);
		} else {
			int i = caster.spells.indexOf(casting);
			spell = i >= 0 ? caster.spells.get(i) : casting;
		}
		caster.ap += spell.apcost;
		spell.used += 1;
		final List<ChanceNode> chances = new ArrayList<ChanceNode>();
		final String prefix = caster + " casts " + spell.name.toLowerCase()
				+ "!\n";
		final int touchtarget = spell.hit(caster, target, state);
		float misschance;
		if (touchtarget == -Integer.MAX_VALUE) {
			misschance = 0;
		} else {
			misschance = bind(touchtarget / 20f);
			chances.add(new ChanceNode(state, misschance,
					prefix + caster + " misses touch attack.", Delay.BLOCK));
		}
		final float hitc = 1 - misschance;
		final float affectchance = affect(caster, target, state, spell, chances,
				prefix, hitc);
		final float savec = savechance(caster, target, spell);
		if (savec != 0) {
			chances.add(hit(caster, target, state, spell, savec * affectchance,
					true, prefix));
		}
		if (savec != 1) {
			chances.add(hit(caster, target, state, spell,
					(1 - savec) * affectchance, false, prefix));
		}
		if (Javelin.DEBUG) {
			ActionProvider.validate(chances);
		}
		return chances;
	}

	/**
	 * @param active
	 *            Caster.
	 * @return The chance the target {@link Combatant} has of rolling a saving
	 *         throw for resisting the current {@link Spell}.
	 */
	public static float savechance(Combatant active, Combatant target,
			final Spell spell) {
		return convertsavedctochance(spell.save(active, target));
	}

	static float affect(Combatant caster, Combatant target, BattleState state,
			final Spell spell, final List<ChanceNode> chances,
			final String prefix, float hitchance) {
		if (spell.castonallies || target.source.sr == 0
				|| caster.equals(target)) {
			return hitchance;
		}
		final float resistchance = bind(
				(target.source.sr - spell.casterlevel) / 20f) * hitchance;
		chances.add(new ChanceNode(state, resistchance,
				prefix + target + " resists spell!", Delay.BLOCK));
		return hitchance - resistchance;
	}

	static float convertsavedctochance(final int savedc) {
		if (savedc == Integer.MAX_VALUE) {
			return 0;
		}
		if (savedc == Integer.MIN_VALUE) {
			return 1;
		}
		return 1 - bind(savedc / 20f);
	}

	static ChanceNode hit(Combatant active, Combatant target, BattleState state,
			final Spell spell, final float chance, final boolean saved,
			String prefix) {
		state = state.clone();
		active = state.clone(active);
		target = state.cloneifdifferent(target, active);
		String message = spell.cast(active, target, state, saved);
		if (message == null || message.isEmpty()) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		return new ChanceNode(state, chance, prefix + message, Delay.BLOCK);
	}

	@Override
	protected void checkhero(Combatant hero) {
		// no check
	}

	@Override
	protected int calculatehitdc(Combatant active, Combatant target,
			BattleState s) {
		return casting.hit(active, target, s);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		casting.filtertargets(combatant, targets, s);
	}

	@Override
	protected boolean checkengaged(BattleState state, Combatant c) {
		return casting.provokeaoo && !c.source.concentrate(casting)
				&& state.isengaged(c);
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant active,
			final BattleState gameState) {
		casting = null;
		final ArrayList<List<ChanceNode>> chances = new ArrayList<List<ChanceNode>>();
		boolean engaged = gameState.isengaged(active);
		final ArrayList<Spell> spells = active.spells;
		for (int i = 0; i < spells.size(); i++) {
			final Spell s = spells.get(i);
			if (s.provokeaoo && !active.source.concentrate(s) && engaged) {
				continue;
			}
			if (!s.castinbattle || !s.canbecast(active)) {
				continue;
			}
			final List<Combatant> targets = gameState.gettargets(active,
					gameState.getcombatants());
			s.filtertargets(active, targets, gameState);
			for (Combatant target : targets) {
				chances.add(cast(active, target, i, gameState));
			}
		}
		return chances;
	}
}
