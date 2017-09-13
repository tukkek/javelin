package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.MerchantScreen;
import tyrant.mikera.engine.RPG;

/**
 * A figure that travels from one city to a human (neutral) city. It can be
 * visited to buy {@link Item}s. If it reaches a human {@link Town} it grows by
 * 1 {@link Town#population} - this is an incentive for the player to protect
 * merchants.
 *
 * Unlike {@link Town} {@link Item}s, these are not crafted but sold as-is, and
 * as such are removed after purchase.
 *
 * @author alex
 */
public class Caravan extends Actor {
	public static final boolean ALLOW = true;

	static final int NUMBEROFITEMS = 6;
	static final int MINARTIFACTS = 1;
	static final int MAXARTIFACTS = 3;

	/** Selection of {@link Item}s available for purchase. */
	public ItemSelection inventory = new ItemSelection();

	int tox = -1;
	int toy = -1;
	/** Merchants are slow, act once very other turn. */
	boolean ignoreturn = true;

	/** Creates a merchant in the world map but doesn't {@link #place()} it. */
	public Caravan() {
		allowedinscenario = false;
		ArrayList<Actor> towns = World.getall(Town.class);
		Collections.shuffle(towns);
		Actor from = towns.get(0);
		x = from.x;
		y = from.y;
		displace();
		determinetarget(towns);
		while (inventory.size() < NUMBEROFITEMS) {
			Item i = RPG.pick(Item.ALL);
			if (!(i instanceof Artifact)) {
				inventory.add(i);
			}
		}
		int withartifacts = inventory.size()
				+ RPG.r(MINARTIFACTS, MAXARTIFACTS);
		while (inventory.size() < withartifacts) {
			Item i = RPG.pick(Item.ARTIFACT);
			inventory.add(i);
		}
		Key k = Key.generate();
		if (k.price > 0) {
			inventory.add(k);
		}
	}

	void determinetarget(ArrayList<Actor> towns) {
		Actor to = null;
		if (towns != null) {
			for (int i = 1; i < towns.size(); i++) {
				Town t = (Town) towns.get(i);
				if (t.garrison.isEmpty()
						&& !Incursion.crosseswater(this, t.x, t.y)) {
					to = t;
					break;
				}
			}
		}
		if (to == null) {
			while (tox == -1 || Incursion.crosseswater(this, tox, toy)) {
				tox = RPG.r(0, World.scenario.size - 1);
				toy = RPG.r(0, World.scenario.size - 1);
			}
		} else {
			tox = to.x;
			toy = to.y;
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (ignoreturn) {
			ignoreturn = false;
			return;
		}
		ignoreturn = true;
		int x = this.x + calculatedelta(this.x, tox);
		int y = this.y + calculatedelta(this.y, toy);
		if (Terrain.get(x, y).equals(Terrain.WATER)) {
			tox = -1;
			determinetarget(null);
			return;
		}
		Actor here = World.get(x, y);
		this.x = x;
		this.y = y;
		place();
		if (x == tox && y == toy) {
			if (here instanceof Town) {
				Town town = (Town) here;
				if (town.garrison.isEmpty()
						&& town.population < Growth.MAXPOPULATION) {
					town.population += 1;
					announce(town);
				}
			}
			remove();
		} else if (here != null) {
			ignoreturn = false;
			turn(0, null);// jump over other Actors
		}
	}

	void announce(Town town) {
		if (Javelin.DEBUG) {
			return;
		}
		Game.messagepanel.clear();
		Game.message(
				"A merchant arrives at " + town
						+ ", city grows! Press ENTER to continue...",
				Delay.NONE);
		while (Game.getInput().getKeyChar() != '\n') {
			// wait for ENTER
		}
		Game.messagepanel.clear();
	}

	int calculatedelta(int from, int to) {
		if (to > from) {
			return +1;
		}
		if (to < from) {
			return -1;
		}
		return 0;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return true;
	}

	@Override
	public boolean interact() {
		new MerchantScreen(this).show();
		return true;
	}

	@Override
	public Image getimage() {
		return Images.getImage("caravan");
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public String describe() {
		return "Caravan.";
	}
}
