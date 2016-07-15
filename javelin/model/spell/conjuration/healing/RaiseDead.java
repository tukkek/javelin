package javelin.model.spell.conjuration.healing;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.view.screen.InfoScreen;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class RaiseDead extends Spell {

	protected static final float RESTORATIONCR =
			SpellsFactor.ratespelllikeability(4);

	/** Constructor. */
	public RaiseDead() {
		super("Raise dead", 5,
				SpellsFactor.ratespelllikeability(5) + RESTORATIONCR,
				Realm.GOOD);
		components = 5000;
		isscroll = true;
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		Game.messagepanel.clear();
		Game.message("Revive? Press y to confirm or n to let go of this unit.",
				target, Delay.NONE);
		while (true) {
			final Character feedback = InfoScreen.feedback();
			if (feedback == 'y') {
				return true;
			}
			if (feedback == 'n') {
				return false;
			}
		}
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.hp = target.source.hd.count();
		return null;
	}

}
