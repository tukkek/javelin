package javelin.model.unit.abilities.spell.conjuration.healing;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.unit.attack.Combatant;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class Ressurect extends RaiseDead {
	/** Constructor. */
	public Ressurect() {
		super("Ressurection", 7,
				ChallengeCalculator.ratespelllikeability(7)
						+ RaiseDead.RESTORATIONCR,
				Realm.GOOD);
		components = 10000;
		isscroll = true;
		castinbattle = false;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.hp = target.maxhp;
		return null;
	}
}
