package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Provide darkvision.
 *
 * @see Monster#vision
 * @author alex
 */
public class GogglesOfNight extends Gear{

	private int originalvision;

	/** Constructor. */
	public GogglesOfNight(int price){
		super("Googles of night",price,Slot.EYES);
	}

	@Override
	protected void apply(Combatant c){
		originalvision=c.source.vision;
		c.source.vision=Monster.VISION_DARK;
	}

	@Override
	protected void negate(Combatant c){
		c.source.vision=originalvision;
	}

}
