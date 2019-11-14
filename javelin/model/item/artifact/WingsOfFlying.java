package javelin.model.item.artifact;

import javelin.controller.db.reader.fields.Speed;
import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;

/**
 * Turn a unit into a flying unit.
 *
 * @author alex
 */
public class WingsOfFlying extends Artifact{
	static final int FLIGHT=Math.min(60,Speed.MAXSPEED);

	int originalfly;
	int originalwalk;

	/** Constructor. */
	public WingsOfFlying(int price){
		super("Wings of flying",price,Slot.SHOULDERS);
	}

	@Override
	protected void apply(Combatant c){
		originalfly=c.source.fly;
		originalwalk=c.source.walk;
		c.source.fly=Math.max(originalfly,FLIGHT);
		c.source.walk=0;
	}

	@Override
	protected void negate(Combatant c){
		c.source.fly=originalfly;
		c.source.walk=originalwalk;
	}

}
