package javelin.model.unit.feat.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.skill.Disguise;
import javelin.model.unit.skill.Skill;

/**
 * @see Disguise
 * @author alex
 */
public class Deceitful extends Feat{
	/** Unique instance of this feat. */
	public static final Feat SINGLETON=new Deceitful();
	/**
	 * Technically +2, rising to +4 at 10 ranks but this causes problems with
	 * {@link #read(Monster)}.
	 */
	public static final int BONUS=+3;

	private Deceitful(){
		super("Deceitful");
		arena=false;
	}

	@Override
	public void read(Monster m){
		super.read(m);
		Acrobatic.normalize(Skill.BLUFF,BONUS,m);
		Acrobatic.normalize(Skill.DISGUISE,BONUS,m);
	}
}
