package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class InflictModerateWounds extends Ray {

	public InflictModerateWounds(final String name, final float incrementcost,
			final int[] spelldatap, final int casterlevelp) {
		super(name, incrementcost, false, false, casterlevelp);
		spelldata = spelldatap;
	}

	public InflictModerateWounds(final String name, final float incrementcost) {
		this(name, incrementcost, new int[] { 2, 8, 3 }, 2);
	}

	public InflictModerateWounds() {
		this("Inflict moderate wounds",
				SpellsFactor.ratetouchconvertedtoray(2));
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
		return save(spelldata[2], target.source.will(), caster);
	}

	@Override
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) throws NotPeaceful {
		throw new RuntimeException();
	}
}
