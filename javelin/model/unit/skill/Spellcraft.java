package javelin.model.unit.skill;

import javelin.model.Realm;

public class Spellcraft extends Skill{
	static final String[] NAMES=new String[]{"Spellcraft","Scry"};

	public Spellcraft(){
		super(NAMES,Ability.INTELLIGENCE,Realm.MAGIC);
		intelligent=true;
	}
}
