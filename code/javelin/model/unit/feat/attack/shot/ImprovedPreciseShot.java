package javelin.model.unit.feat.attack.shot;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class ImprovedPreciseShot extends Feat{
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON=new ImprovedPreciseShot();

	/** Constructor. */
	private ImprovedPreciseShot(){
		super("Improved Precise Shot");
		prerequisite=javelin.model.unit.feat.attack.shot.PreciseShot.SINGLETON;
	}

	@Override
	public String inform(Combatant m){
		return "";
	}

	@Override
	public boolean upgrade(Combatant m){
		return m.source.dexterity>=19&&m.source.getbab()>=11&&super.upgrade(m);
	}
}
