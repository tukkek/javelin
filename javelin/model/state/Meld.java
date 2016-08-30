package javelin.model.state;

import java.awt.Image;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.view.Images;

/**
 * A crystal that is evolved after a {@link Combatant} leaves the battle-field
 * and can be used as power-up later on.
 * 
 * @author alex
 */
public class Meld {
	public static final boolean DEBUG = false;
	public final int x;
	public final int y;
	public final float meldsat;
	public final float cr;

	public Meld(final int x, final int y, final float meldsat, Monster dead) {
		this.x = x;
		this.y = y;
		this.meldsat = meldsat;
		cr = dead == null ? 0 : dead.challengeRating;
	}

	public boolean crystalize(BattleState state) {
		return state.next.ap >= meldsat;
	}

	public Image getimage(BattleState state) {
		return state.next.ap >= meldsat ? Images.MELD : Images.DEAD;
	}
}
