package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.QuestApp;

/**
 * @see Incursion
 * @author alex
 */
public class IncursionFight implements Fight {
	public static final Image INCURSIONTEXTURE =
			QuestApp.getImage("/images/texture2.png");

	private final class IncursionScreen extends BattleScreen {
		final private Incursion i;

		private IncursionScreen(QuestApp q, BattleMap mapp,
				Incursion incursion) {
			super(q, mapp, true);
			i = incursion;
			Javelin.settexture(IncursionFight.INCURSIONTEXTURE);
		}

		@Override
		public void onEnd() {
			if (BattleMap.redTeam.isEmpty()) {
				i.remove();
			} else {
				for (Combatant incursant : new ArrayList<Combatant>(i.squad)) {
					Combatant alive = null;
					for (Combatant inbattle : BattleMap.combatants) {
						if (inbattle.id == incursant.id) {
							alive = inbattle;
							break;
						}
					}
					if (alive == null) {
						i.squad.remove(incursant);
					}
				}
			}
			super.onEnd();
		}

	}

	public final Incursion incursion;

	/**
	 * @param incursion
	 */
	public IncursionFight(final Incursion incursion) {
		this.incursion = incursion;
	}

	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		return new IncursionScreen(javelinApp, battlemap, incursion);
	}

	@Override
	public int getel(final JavelinApp javelinApp, final int teamel) {
		throw new RuntimeException(
				"Shouldn't have to generate an incursion fight.");
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return Incursion.getsafeincursion(incursion.squad);
	}

	@Override
	public boolean meld() {
		return true;
	}

	@Override
	public Map getmap() {
		return null;
	}

	@Override
	public boolean friendly() {
		return false;
	}

	@Override
	public boolean rewardgold() {
		return true;
	}

	@Override
	public boolean hide() {
		/* let player flee it he wants to but don't hide */
		return false;
	}

	@Override
	public boolean canbribe() {
		return true;
	}

	@Override
	public void bribe() {
		incursion.remove();
	}
}