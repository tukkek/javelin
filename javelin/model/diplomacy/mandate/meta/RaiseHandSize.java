package javelin.model.diplomacy.mandate.meta;

import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.diplomacy.mandate.Mandate;

/**
 * Raises {@link Diplomacy#handsize}.
 * 
 * @see Diplomacy#HANDSTARTING
 * @see Diplomacy#HANDMAX
 * @author alex
 */
public class RaiseHandSize extends Mandate{
	Diplomacy d;

	/** Reflection constructor. */
	public RaiseHandSize(Relationship r){
		super(r);
	}

	@Override
	public boolean validate(Diplomacy d){
		this.d=d;
		var friendly=d.getdiscovered().keySet().stream().filter(t->!t.ishostile());
		return d.handsize<Math.min(friendly.count(),Diplomacy.HANDMAX);
	}

	@Override
	public String getname(){
		return "Raise maximum hand size to "+(d.handsize+1);
	}

	@Override
	public void act(Diplomacy d){
		d.handsize+=1;
	}
}
