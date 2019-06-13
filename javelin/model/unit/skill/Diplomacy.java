package javelin.model.unit.skill;

/**
 * TODO diplomacy, bluff and intimidate should be interchangeable, allowing any
 * squad member to use any at any given task. would be nice to have it codified
 * and applied.
 *
 * @author alex
 */
public class Diplomacy extends Skill{
	static final String[] NAMES=new String[]{"Diplomacy","gather information",};

	public Diplomacy(){
		super(NAMES,Ability.CHARISMA);
		intelligent=true;
	}
}
