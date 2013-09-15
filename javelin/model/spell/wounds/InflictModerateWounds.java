package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class InflictModerateWounds extends Ray {

	public InflictModerateWounds(final String name, final float incrementcost,
			final int[] spelldatap, final int casterlevelp) {
		super(name, incrementcost, false, false, casterlevelp);
		spelldata = spelldatap;
	}

	public InflictModerateWounds(final String name, final float incrementcost) {
		this(name, incrementcost, new int[] { 1, 1, 10 + 2 + 1 }, 3);
	}

	public InflictModerateWounds(final String string) {
		this(string + "inflict moderate wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(2));
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		int damage = spelldata[0] * spelldata[1] / 2 + spelldata[2];
		String info = "";
		if (saved) {
			damage = damage / 2;
			info += target + " resisted!\n";
		}
		target.damage(damage, s, target.source.resistance);
		return info + target + " is now " + target.getStatus() + ".";
	}

	/**
	 * @return Number of d8 for damage, fixed bonus, DC.
	 */
	final int[] spelldata;

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		final int will = target.source.will();
		return will == Integer.MAX_VALUE ? will : save(spelldata[2], will);
	}

	@Override
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) throws NotPeaceful {
		throw new RuntimeException();
	}
}
