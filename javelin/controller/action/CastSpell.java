package javelin.controller.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.view.screen.BattleScreen;
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
public class CastSpell extends Fire {
	/** Only instance of CastSpell to exist. */
	public static final CastSpell singleton = new CastSpell();
	/** Spell for {@link Fire} to perform. */
	public Spell casting;

	CastSpell() {
		super("Cast spells", "s", 's');
	}

	@Override
	public boolean perform(Combatant c, BattleMap map, Thing thing) {
		Game.messagepanel.clear();
		casting = null;
		final ArrayList<Spell> castable = new ArrayList<Spell>();
		final boolean engaged = map.getState().isengaged(c);
		for (Spell s : c.spells) {
			if (s.provokeaoo && !concentrate(c, s) && engaged) {
				continue;
			}
			if (s.canbecast(c)) {
				castable.add(s);
			}
		}
		if (castable.isEmpty()) {
			Game.message("No spells to cast right now.", null, Delay.WAIT);
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
		Game.message(list, null, Delay.NONE);
		try {
			final int i = Integer.parseInt(InfoScreen.feedback().toString());
			if (i > c.spells.size()) {
				return perform(c, map, thing);
			}
			return cast(castable.get(i - 1), c, map, thing);
		} catch (NumberFormatException e) {
			throw new RepeatTurn();
		}
	}

	/**
	 * Like {@link #perform(Combatant, BattleMap, Thing)} except skips the
	 * selection UI step.
	 */
	public boolean cast(Spell spell, Combatant c, BattleMap map, Thing thing) {
		casting = spell;
		return super.perform(c, map, thing);
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState s, BattleMap m) {
		Action.outcome(cast(combatant, targetCombatant,
				combatant.spells.indexOf(casting), s));
	}

	List<ChanceNode> cast(Combatant active, Combatant target, int spellindex,
			BattleState state) {
		state = state.clone();
		active = state.clone(active);
		target = state.cloneifdifferent(target, active);
		Spell spell = null;
		if (casting == null) {
			spell = active.spells.get(spellindex);
		} else {
			int i = active.spells.indexOf(casting);
			spell = i >= 0 ? active.spells.get(i) : casting;
		}
		// try {
		active.ap += spell.apcost;
		// } catch (NullPointerException e) {
		// System.out.println("#nullspell");
		// }
		spell.used += 1;
		final List<ChanceNode> chances = new ArrayList<ChanceNode>();
		final String prefix =
				active + " casts " + spell.name.toLowerCase() + "!\n";
		final int touchtarget = spell.hit(active, target, state);
		float misschance;
		if (touchtarget == -Integer.MAX_VALUE) {
			misschance = 0;
		} else {
			misschance = bind(touchtarget / 20f);
			chances.add(new ChanceNode(state, misschance,
					prefix + active + " misses touch attack.", Delay.BLOCK));
		}
		final float hitc = 1 - misschance;
		final float affectchance =
				affect(target, state, spell, chances, prefix, hitc);
		final float savec = savechance(active, target, spell);
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

	public static float affect(Combatant target, BattleState state,
			final Spell spell, final List<ChanceNode> chances,
			final String prefix, float hitchance) {
		if (spell.castonallies || target.source.sr == 0) {
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
	protected int calculatehitdc(Combatant target, Combatant active,
			BattleState state) {
		return casting.hit(active, target, state);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		casting.filtertargets(combatant, targets, s);
	}

	@Override
	protected boolean checkengaged(BattleState state, Combatant c) {
		return casting.provokeaoo && !concentrate(c, casting)
				&& state.isengaged(c);
	}

	static boolean concentrate(Combatant c, Spell s) {
		final int concentration = c.source.skills.concentration
				+ Monster.getbonus(c.source.constitution);
		return concentration >= s.casterlevel;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant active) {
		casting = null;
		final ArrayList<List<ChanceNode>> chances =
				new ArrayList<List<ChanceNode>>();
		boolean engaged = gameState.isengaged(active);
		final ArrayList<Spell> spells = active.spells;
		for (int i = 0; i < spells.size(); i++) {
			final Spell s = spells.get(i);
			if (s.provokeaoo && !concentrate(active, s) && engaged) {
				continue;
			}
			if (!s.castinbattle || !s.canbecast(active)) {
				continue;
			}
			final List<Combatant> targets =
					gameState.getAllTargets(active, gameState.getCombatants());
			s.filtertargets(active, targets, gameState);
			for (Combatant target : targets) {
				chances.add(cast(active, target, i, gameState));
			}
		}
		return chances;
	}

	/**
	 * Cast a combat spell.
	 * 
	 * @see #cast(Spell, Combatant, BattleMap, Thing)}
	 */
	public void cast(Spell s, Combatant user) {
		cast(s, user, BattleScreen.active.map, Game.actor);
	}
}
