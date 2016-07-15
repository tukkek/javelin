package javelin.controller.fight;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.QuestApp;

/**
 * @see Incursion
 * @author alex
 */
public class IncursionFight extends Fight {
	/** See {@link Javelin#settexture(Image)}. */
	public static final Image INCURSIONTEXTURE =
			QuestApp.getImage("/images/texture2.png");

	/** Incursion being fought. */
	public final Incursion incursion;

	/**
	 * @param incursion
	 */
	public IncursionFight(final Incursion incursion) {
		this.incursion = incursion;
		texture = IncursionFight.INCURSIONTEXTURE;
		meld = true;
		hide = false;
	}

	@Override
	public int getel(final int teamel) {
		throw new RuntimeException(
				"Shouldn't have to generate an incursion fight.");
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return Incursion.getsafeincursion(incursion.squad);
	}

	@Override
	public void bribe() {
		incursion.remove();
	}

	@Override
	public void onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		super.onEnd(screen, originalTeam, s);
		if (BattleMap.redTeam.isEmpty()) {
			incursion.remove();
		} else {
			for (Combatant incursant : new ArrayList<Combatant>(
					incursion.squad)) {
				Combatant alive = null;
				for (Combatant inbattle : BattleMap.combatants) {
					if (inbattle.id == incursant.id) {
						alive = inbattle;
						break;
					}
				}
				if (alive == null) {
					incursion.squad.remove(incursant);
				}
			}
		}
	}

	@Override
	public List<Combatant> generate(int teamel, Terrain terrain) {
		List<Combatant> foes = super.generate(teamel, terrain);
		incursion.squad = Incursion.getsafeincursion(foes);
		return foes;
	}
}