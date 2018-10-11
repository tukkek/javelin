package javelin.model.unit.feat.skill;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.skill.Acrobatics;
import javelin.model.unit.skill.Perception;
import javelin.model.unit.skill.Skill;

/**
 * +2 on {@link Perception} and sense motive rolls by Pathfinder rules.
 *
 * @see Acrobatics
 * @author alex
 */
public class Alertness extends Feat{
	/** Single instance. */
	public static final Feat SINGLETON=new Alertness();
	/** +1 since we don't support Sense Motive in the game. */
	public static final int BONUS=+4;

	/** Constructor. */
	private Alertness(){
		super("alertness");
		arena=false;
	}

	@Override
	public void read(Monster m){
		super.read(m);
		Acrobatic.normalize(Skill.PERCEPTION,BONUS,m);
	}
}
