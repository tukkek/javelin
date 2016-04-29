package javelin.model.item.scroll;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class RaiseScroll extends Scroll {

	protected static final float RESTORATIONCOST =
			SpellsFactor.ratespelllikeability(4);

	public RaiseScroll() {
		super("Scroll of raise dead & restoration", 7000, Item.GOOD, 5,
				SpellsFactor.ratespelllikeability(5) + RESTORATIONCOST);
	}

	public RaiseScroll(String string, int i, ItemSelection good, int level,
			float cost) {
		super(string, i, good, level, cost);
	}

	public boolean revive(final Combatant m) {
		Game.messagepanel.clear();
		Game.message("Revive? Press y to confirm or n to let him go.", m,
				Delay.NONE);
		while (true) {
			final Character feedback = InfoScreen.feedback();
			if (feedback == 'y') {
				givelife(m);
				return true;
			}
			if (feedback == 'n') {
				return false;
			}
		}
	}

	protected void givelife(final Combatant m) {
		m.hp = m.source.hd.count();
	}

	@Override
	public boolean usepeacefully(final Combatant m) {
		return false;
	}
}
