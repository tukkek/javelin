package javelin.controller.quality.subtype;

import javelin.controller.quality.Quality;
import javelin.model.unit.Monster;

/**
 * Elemental resistances.
 *
 * @author alex
 */
public class Elemental extends Quality{

	/** Constructor. */
	public Elemental(){
		super("elemental");
	}

	@Override
	public void add(String declaration,Monster m){
		m.immunitytocritical=true;
		m.immunitytopoison=true;
		m.immunitytoparalysis=true;
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
