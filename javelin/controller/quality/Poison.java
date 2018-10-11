package javelin.controller.quality;

import javelin.controller.db.reader.fields.Damage;
import javelin.model.unit.Monster;

/**
 * Just used to accept poison as a valid listed quality. {@link Damage} takes
 * care of reading poison damage.
 *
 * @author alex
 */
public class Poison extends Quality{
	/** Constructor. */
	public Poison(){
		super("poison");
	}

	@Override
	public void add(String declaration,Monster m){
		// does nothing
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
