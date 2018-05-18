package javelin.model.world.location.order;

import java.util.ArrayList;

import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.view.screen.upgrading.UpgradingScreen;

/**
 * Represent a {@link Combatant} in training.
 * 
 * @see Upgrade
 * @see UpgradingScreen
 * @author alex
 */
public class TrainingOrder extends Order {
	/**
	 * How many days it takes to upgrade per full level (100XP). The official
	 * rules say one week per character level. However, Javelin's goal is to
	 * advance to level 20 in the span of a year.
	 *
	 * This may need to be adjusted to balance fun to play (faster) and the
	 * overarching design (slower). Currently, it's jusst a median between the
	 * tow.
	 */
	public static final float UPGRADETIME = (400 / 20 + 7) / 2;

	/** * The upgraded unit. */
	final public Combatant trained;
	/** Cost in challenge rating. */
	final public float xpcost;
	/** Upgraded unit's equipment. See {@link Squad#equipment}; */
	final public ArrayList<Item> equipment;
	/** The non-upgraded creature in case this needs to be canceled. */
	public Combatant untrained;

	/** Constructor. See {@link Order#Order(long, String)}. */
	public TrainingOrder(Combatant trained, ArrayList<Item> equipment,
			String namep, float xpcostp, Combatant original) {
		super(Math.round(xpcostp * 24 * UPGRADETIME / World.scenario.boost),
				namep);
		this.trained = trained;
		this.equipment = equipment;
		this.xpcost = xpcostp;
		this.untrained = original;
	}
}
