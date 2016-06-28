package javelin.model.world.location.unique;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.fight.PlanarFight;
import javelin.model.item.Key;
import javelin.model.unit.Squad;
import javelin.model.world.location.Portal;
import javelin.model.world.location.fortification.Fortification;
import javelin.view.screen.haxor.BorrowMoney;
import javelin.view.screen.haxor.HaxorScreen;

/**
 * The tower of haxor is a place for players to spend tickets the earn in
 * {@link PlanarFight}. It's largely a meta-game feature and a way to offer
 * interesting non-balance-affecting powers to the player or with minimal
 * balance implications, largely overshadowed by winning an EL-0 encounter that
 * the fight represents.
 * 
 * Only one booster hack can be activated per game - once one is acquired all
 * other boosters are removed.
 * 
 * @author alex
 */
public class Haxor extends UniqueLocation {
	/**
	 * Unique instance of Haxor, possibly restored from a save game.
	 * 
	 * @see StateManager
	 */
	transient public static Haxor singleton = null;

	/**
	 * Cheating currency.
	 * 
	 * Rubies are acquired when entering a {@link Portal} while in possession of
	 * a {@link Key}, resulting a {@link PlanarFight}.
	 */
	public int rubies = 1;

	/**
	 * See {@link BorrowMoney}.
	 */
	public int borrowed = 0;

	/**
	 * Fills {@link #options} initially.
	 */
	public Haxor() {
		super("Temple of Haxor", "Temple of Haxor", 0, 0);
	}

	/**
	 * See {@link BorrowMoney}.
	 * 
	 * @return A certain amount of gold to be borrowed.
	 */
	static public int borrow() {
		return Fortification.getspoils(
				ChallengeRatingCalculator.calculateel(Squad.active.members));
	}

	@Override
	protected void generategarrison(int minel, int maxel) {
		// undefended
	}

	@Override
	public boolean interact() {
		if (borrowed > 0 && Squad.active.gold >= borrowed) {
			Squad.active.gold -= borrowed;
			borrowed = 0;
		}
		new HaxorScreen().show();
		return true;
	}

	@Override
	public void remove() {
		// never remove
	}
}
