package javelin.model.world.place.unique;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.PlanarFight;
import javelin.model.world.Squad;
import javelin.model.world.World;
import javelin.model.world.place.guarded.GuardedPlace;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.haxor.BorrowMoney;
import javelin.view.screen.haxor.ChangeAvatar;
import javelin.view.screen.haxor.HaxorScreen;
import javelin.view.screen.haxor.Materialize;
import javelin.view.screen.haxor.Rebirth;
import javelin.view.screen.haxor.RemoveAbility;
import javelin.view.screen.haxor.Ressurect;
import javelin.view.screen.haxor.SummonAlly;
import javelin.view.screen.haxor.Win;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;

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
	transient public static Haxor singleton = null;

	/**
	 * Cheating currency.
	 */
	public int tickets = 0;
	/**
	 * These are the {@link Option}s that aren't fixed.
	 * 
	 * TODO rename to boosters
	 * 
	 * @see HaxorScreen#getoptions()
	 */
	public List<Option> options = new ArrayList<Option>();

	/**
	 * See {@link BorrowMoney}.
	 */
	public int borrowed = 0;

	/**
	 * Fills {@link #options} initially.
	 */
	public Haxor() {
		super("Temple of Haxor", "Temple of Haxor", 0, 0);
		options.add(new Ressurect("Ressurect fallen ally", 0, false));
		options.add(new SummonAlly("Summon ally", 0, false));
		options.add(new BorrowMoney("Borrow money", 0, false));
		options.add(new ChangeAvatar("Change unit avatar", 0, true));
		options.add(new Rebirth("Rebirth", 1, true));
		options.add(new RemoveAbility("Remove ability", 1, true));
		options.add(new Materialize("Materialize", 1, false));
		options.add(new Win("Win", 2 * 7, false));
		realm = null;
		gossip = true;
	}

	/**
	 * See {@link BorrowMoney}.
	 * 
	 * @return A certain amount of gold to be borrowed.
	 */
	static public int borrow() {
		return GuardedPlace.getspoils(ChallengeRatingCalculator
				.calculateElSafe(Squad.active.members));
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
		new HaxorScreen("Welcome to the Temple of Haxor!", null).show();
		return true;
	}

	@Override
	public void remove() {
		// never remove
	}

	public static void spawn(Point easya) {
		int[] haxor = null;
		while (haxor == null
				|| WorldScreen.getactor(easya.x + haxor[0],
						easya.y + haxor[1]) != null
				|| easya.x + haxor[0] < 0 || easya.y + haxor[1] < 0
				|| easya.x + haxor[0] >= World.MAPDIMENSION
				|| easya.y + haxor[1] >= World.MAPDIMENSION) {
			haxor = new int[] { RPG.r(2, 3), RPG.r(2, 3) };
			if (RPG.r(1, 2) == 1) {
				haxor[0] = -haxor[0];
			}
			if (RPG.r(1, 2) == 1) {
				haxor[1] = -haxor[1];
			}
		}
		singleton = new Haxor();
		singleton.x = haxor[0] + easya.x;
		singleton.y = haxor[1] + easya.y;
		singleton.place();
	}
}
