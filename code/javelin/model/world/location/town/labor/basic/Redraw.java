package javelin.model.world.location.town.labor.basic;

import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.labor.Labor;

/**
 * Discards all {@link Labor} options and redraws.
 *
 * @see Governor
 * @author alex
 */
public class Redraw extends Labor{
	/** Constructor. */
	public Redraw(){
		super("Redraw",0,Rank.HAMLET);
		automatic=false;
	}

	@Override
	protected void define(){
		cost=Math.min(town.population,town.getgovernor().gethandsize());
	}

	@Override
	public void done(){
		for(Labor l:town.getgovernor().gethand())
			l.discard();
		town.getgovernor().redraw();
	}
}
