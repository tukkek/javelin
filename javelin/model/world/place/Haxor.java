package javelin.model.world.place;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.fight.PlanarFight;
import javelin.model.world.Incursion;
import javelin.model.world.WorldMap;
import javelin.view.screen.HaxorScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;

/**
 * The tower of haxor is a place for players to spend tickets the earn in
 * {@link PlanarFight}. It's largely a meta-game feature and a way to offer
 * interesting non-balance-affecting powers to the player or with minimal
 * balance implications, largely overshadowed by winning an EL-0 encounter that
 * the fight represents.
 * 
 * TODO
 * 
 * remove upgrades 1
 * 
 * summon lair 2
 * 
 * summon dungeon 2
 * 
 * @author alex
 */
public class Haxor extends WorldPlace {
	transient public static Haxor singleton = null;

	/**
	 * Cheating currency.
	 */
	public int tickets = 1;
	/**
	 * These are the {@link Option}s that aren't fixed.
	 * 
	 * @see HaxorScreen#getOptions()
	 */
	public List<Option> options = new ArrayList<Option>();

	static {
		singleton = new Haxor();
	}

	/**
	 * Fills {@link #options} initially.
	 */
	public Haxor() {
		super("ruin", "Temple of Haxor");
		options.add(HaxorScreen.RESSURECT);
		options.add(HaxorScreen.SUMMONALLY);
	}

	@Override
			List<WorldPlace> getall() {
		ArrayList<WorldPlace> list = new ArrayList<WorldPlace>();
		list.add(singleton);
		return list;
	}

	@Override
	public void enter() {
		new HaxorScreen("Welcome to the Temple of Haxor!", null).show();
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
				|| easya.x + haxor[0] >= WorldMap.MAPDIMENSION
				|| easya.y + haxor[1] >= WorldMap.MAPDIMENSION) {
			haxor = new int[] { RPG.r(3, 5), RPG.r(3, 5) };
			if (RPG.r(1, 2) == 1) {
				haxor[0] = -haxor[0];
			}
			if (RPG.r(1, 2) == 1) {
				haxor[1] = -haxor[1];
			}
		}
		singleton.x = haxor[0] + easya.x;
		singleton.y = haxor[1] + easya.y;
		singleton.place();
	}

	@Override
	public String toString() {
		return "Tower of Haxor";
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return Incursion.ignoreincursion(attacker);
	}

	@Override
	public boolean ignore(Incursion attacker) {
		return true;
	}
}
