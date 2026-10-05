package javelin.model.unit.skill;

/**
 * TODO once we have surprise rounds in the game, a player should be able to use
 * Stealh instead of Perception (was hidden enough for others not to notice him)
 */
public class Stealth extends Skill{
	static final String[] NAMES=new String[]{"Stealth","hide","move silently",
			"pick pocket"};

	/** Constructor. */
	public Stealth(){
		super(NAMES,Ability.DEXTERITY);
	}
}
