package javelin.model.item;

import javelin.model.unit.Combatant;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class RaiseScroll extends Scroll {

	public RaiseScroll() {
		super("Scroll of raise dead & restoration", 7000,
				"After battle, ressurects a killed ally");
	}

	public RaiseScroll(String string, int i, String string2) {
		super(string, i, string2);
	}

	@Override
	public boolean use(final Combatant m) {
		Game.messagepanel.clear();
		Game.message("Revive? Press y to confirm or n to let him go.", m,
				Delay.NONE);
		while (true) {
			final Character feedback = IntroScreen.feedback();
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
		m.hp = m.source.hd.countdice();
	}

	@Override
	public boolean usepeacefully(final Combatant m) {
		return false;
	}
}
