package javelin.controller.quality.subtype;

import javelin.controller.quality.Quality;
import javelin.controller.quality.resistance.EnergyImmunity;
import javelin.model.unit.Monster;

/**
 * Alias for fire and cold subtypes. Currently only adds {@link EnergyImmunity}.
 *
 * @author alex
 */
public class Subtype extends Quality{
	/** Constructor. */
	public Subtype(){
		super("subtype");
	}

	@Override
	public void add(String declaration,Monster m){
		if(declaration.contains("fire")||declaration.contains("cold"))
			m.energyresistance=Integer.MAX_VALUE;
	}

	@Override
	public boolean has(Monster m){
		return false;
	}

	@Override
	public float rate(Monster m){
		return 0;
	}

}
