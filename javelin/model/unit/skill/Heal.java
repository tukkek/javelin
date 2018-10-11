package javelin.model.unit.skill;

import javelin.model.Realm;

public class Heal extends Skill{
	public Heal(){
		super("Heal",Ability.WISDOM,Realm.WATER);
		intelligent=true;
	}
}
