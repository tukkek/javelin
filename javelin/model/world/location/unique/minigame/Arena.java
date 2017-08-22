package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.comparator.ItemPriceComparator;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.frame.arena.ArenaWindow;

/**
 * The Arena can be seen as a mini-game or a separate game mode for Javelin.
 * Players who are not looking for long-term strategy or a complete RPG campaign
 * experience are probably going to feel more at home in the Arena, where they
 * can focus on combat and growing in power much faster than in the main game
 * world - even players who enjoy Javelin's campaign mode will probably feel
 * tempted to just spend some time in the Arena now and again, which is a more
 * light-hearted game experience. If you think you'd enjoy it the Arena can be
 * accessed at any point in time (unless in battle), just press h to see the
 * corresponding key to it. Once you get used to the coin and betting system
 * you'll be able to ascend in power very fast!
 * 
 * The design parameters for the arena is: {@value Arena#COINSPERCR}
 * {@link #coins} per CR. Each coin value in gold is defined by
 * {@link #getcoins(int)}.
 * 
 * @author alex
 */
public class Arena extends UniqueLocation {
	/**
	 * How much a hireling costs per challenge rating ({@value #COINSPERCR}
	 * coins).
	 */
	public static final int COINSPERCR = 5;
	/** Number of {@link #coins} to start with. */
	public static final int STARTINGCOINS = COINSPERCR * 2;
	static final String DESCRIPTION = "The arena";

	/** Roster of permanent player units. */
	public ArrayList<Combatant> gladiators = new ArrayList<Combatant>();
	/** Arena's currency. */
	public int coins = STARTINGCOINS;
	/** Redeemable items. */
	public ItemSelection items = new ItemSelection();

	/** {@link Item} bag for {@link #gladiators}. */
	HashMap<Integer, ArrayList<Item>> equipment =
			new HashMap<Integer, ArrayList<Item>>();

	/** Constructor. */
	public Arena() {
		super(DESCRIPTION, DESCRIPTION, 0, 0);
		stock();
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// don't
	}

	@Override
	public List<Combatant> getcombatants() {
		return gladiators;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		clearequipment();
		ArenaWindow w = new ArenaWindow(this);
		w.show();
		w.defer();
		if (w.action != null) {
			w.action.run();
		}
		return true;

	}

	void clearequipment() {
		cleaning: for (Integer id : new ArrayList<Integer>(
				equipment.keySet())) {
			for (Combatant c : gladiators) {
				if (c.id == id) {
					continue cleaning;
				}
			}
			equipment.remove(id);
		}
	}

	/**
	 * @return The Arena. A <code>null</code> result could indicate the game
	 *         hasn't yet been loaded or the world isn't generated.
	 */
	public static Arena get() {
		ArrayList<Actor> actors = World.getall(Arena.class);
		return actors.isEmpty() ? null : (Arena) actors.get(0);
	}

	/**
	 * @param gold
	 *            Given a value in gold.
	 * @return a value in {@link #coins}.
	 */
	public static int getcoins(int gold) {
		return Math.max(1, ChallengeRatingCalculator.goldtocr(gold));
	}

	/**
	 * @param i
	 *            Adds items...
	 * @param gladiator
	 *            to this gladitor's bag.
	 */
	public void additem(Item i, Combatant gladiator) {
		ArrayList<Item> bag = equipment.get(gladiator.id);
		if (bag == null) {
			bag = new ArrayList<Item>();
			equipment.put(gladiator.id, bag);
		}
		bag.add(i.clone());
	}

	/**
	 * @return The {@link Combatant}'s bag.
	 */
	public ArrayList<Item> getitems(Combatant c) {
		ArrayList<Item> bag = equipment.get(c.id);
		if (bag == null) {
			bag = new ArrayList<Item>();
			equipment.put(c.id, bag);
		}
		return bag;
	}

	/**
	 * Fills {@link #items} with random items.
	 */
	public void stock() {
		while (items.size() < 9) {
			items.add(Item.ALL.random().clone());
		}
		items.sort(ItemPriceComparator.SINGLETON);
	}

	@Override
	protected void generate() {
		while (x < 0 || Terrain.get(x, y).equals(Terrain.MARSH)
				|| Terrain.get(x, y).equals(Terrain.MOUNTAINS)
				|| Terrain.get(x, y).equals(Terrain.DESERT)) {
			super.generate();
		}
	}
}