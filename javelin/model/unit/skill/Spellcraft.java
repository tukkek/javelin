package javelin.model.unit.skill;

public class Spellcraft extends Skill{
	static final String[] NAMES=new String[]{"Spellcraft","Scry"};

	public Spellcraft(){
		super(NAMES,Ability.INTELLIGENCE);
		intelligent=true;
	}
}
