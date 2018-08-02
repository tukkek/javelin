package javelin.model.unit.abilities.spell.necromancy.wounds;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * See the d20 SRD for more info.
 */
public class InflictModerateWounds extends Touch {

	public InflictModerateWounds(final String name, final float incrementcost,
			final int[] spelldatap, final int level) {
		super(name, level, incrementcost, Realm.EVIL);
		spelldata = spelldatap;
		castinbattle = true;
		provokeaoo = false;
	}

	public InflictModerateWounds() {
		this("Inflict moderate wounds",
				ChallengeCalculator.ratespelllikeability(2),
				new int[] { 2, 8, 3 }, 2);
		castinbattle = true;
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final boolean saved, final BattleState s, ChanceNode cn) {
		int damage = spelldata[0] * spelldata[1] / 2 + spelldata[2];
		String info = "";
		if (saved) {
			damage = damage / 2;
			info += target + " resisted!\n";
		}
		target.damage(damage, s, target.source.energyresistance);
		cn.overlay = new AiOverlay(target);
		return info + target + " is now " + target.getstatus() + ".";
	}

	/**
	 * @return Number of d8 for damage, fixed bonus, DC.
	 */
	final int[] spelldata;

	@Override
	public int save(final Combatant caster, final Combatant target) {
		return getsavetarget(target.source.getwill(), caster);
	}

}
