package javelin.model.world.location.order;

import java.util.ArrayList;

import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.upgrading.UpgradingScreen;

/**
 * Represent a {@link Combatant} in training.
 * 
 * @see Upgrade
 * @see UpgradingScreen
 * @author alex
 */
public class TrainingOrder extends Order {
	/** * The upgraded unit. */
	final public Combatant trained;
	/** Cost in challenge rating. */
	final public float xpcost;
	/** Upgraded unit's equipment. See {@link Squad#equipment}; */
	final public ArrayList<Item> equipment;
	/** The non-upgraded creature in case this needs to be canceled. */
	public Combatant untrained;

	/** Constructor. See {@link Order#Order(long, String)}. */
	public TrainingOrder(long completionat, Combatant trained,
			ArrayList<Item> equipment, String namep, float xpcostp,
			Combatant original) {
		super(completionat, namep);
		this.trained = trained;
		this.equipment = equipment;
		this.xpcost = xpcostp;
		this.untrained = original;
	}

}
