package javelin.model.unit.feat.attack.shot;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class PreciseShot extends Feat{
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON=new PreciseShot();

	private PreciseShot(){
		super("Precise shot");
		prerequisite=javelin.model.unit.feat.attack.shot.PointBlankShot.SINGLETON;
	}

	@Override
	public String inform(Combatant m){
		return "";
	}
}
