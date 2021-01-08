package javelin.controller.content.quality.subtype;

import javelin.controller.content.quality.Quality;
import javelin.model.unit.Monster;

/**
 * Baateze resistances.
 *
 * @author alex
 */
public class Baatezu extends Quality{

	/** Constructor. */
	public Baatezu(){
		super("baatezu");
	}

	@Override
	public void add(String declaration,Monster m){
		m.energyresistance=Integer.MAX_VALUE;
		m.immunitytopoison=true;
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
