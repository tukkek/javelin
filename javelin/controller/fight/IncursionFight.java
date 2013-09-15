package javelin.controller.fight;

import java.awt.Image;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.exception.RepeatTurnException;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Incursion;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

public class IncursionFight implements Fight {
	public static final Image INCURSIONTEXTURE = QuestApp
			.getImage("/images/texture2.png");

	private final class IncursionScreen extends BattleScreen {

		private IncursionScreen(QuestApp q, BattleMap mapp) {
			super(q, mapp);
			Javelin.settexture(INCURSIONTEXTURE);
		}

		@Override
		protected void flee(Combatant c) {
			Game.message("Cannot flee from incursions!", null, Delay.BLOCK);
			checkblock();
			throw new RepeatTurnException();
		}
	}

	private final Incursion incursion;

	/**
	 * @param incursion
	 */
	public IncursionFight(final Incursion incursion) {
		this.incursion = incursion;
		incursion.remove();
	}

	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		return new IncursionScreen(javelinApp, battlemap);
	}

	@Override
	public int getel(final JavelinApp javelinApp, final int teamel) {
		return incursion.el;
	}

	@Override
	public List<Monster> getmonsters(int teamel) {
		return null;
	}
}