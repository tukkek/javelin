package javelin.model.state;

import java.awt.Image;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.Combatant;
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

	public Meld(final int x, final int y, final float meldsat) {
		this.x = x;
		this.y = y;
		this.meldsat = meldsat;
	}

	public boolean crystalize(BattleState state) {
		return state.next.ap >= meldsat;
	}

	public Image getimage(BattleState state) {
		return state.next.ap >= meldsat ? Images.crystal : Images.dead;
	}

	public void activate(Combatant hero) {
		Game.message(hero + " powers up!", Delay.BLOCK);
		Javelin.getCombatant(hero.id).meld();
		Fight.state.meld.remove(this);
	}
}
