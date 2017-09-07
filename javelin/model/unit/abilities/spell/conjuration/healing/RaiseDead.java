package javelin.model.unit.abilities.spell.conjuration.healing;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.InfoScreen;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class RaiseDead extends Spell {

	protected static final float RESTORATIONCR = ChallengeRatingCalculator
			.ratespelllikeability(4);

	/** Constructor. */
	public RaiseDead() {
		super("Raise dead", 5, ChallengeRatingCalculator.ratespelllikeability(5)
				+ RESTORATIONCR, Realm.GOOD);
		components = 5000;
		isscroll = true;
		castinbattle = false;
	}

	public RaiseDead(String name, int levelp, float incrementcost,
			Realm realmp) {
		super(name, levelp, incrementcost, realmp);
	}

	@Override
	public boolean validate(Combatant caster, Combatant target) {
		Game.messagepanel.clear();
		Game.message(
				"Revive " + target
						+ "? Press y to confirm or n to let go of this unit.",
				Delay.NONE);
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
