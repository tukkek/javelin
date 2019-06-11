package javelin.model.unit.skill;

import javelin.controller.action.Movement;
import javelin.controller.action.maneuver.DefensiveAttack;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Defending;
import javelin.model.unit.feat.skill.Acrobatic;

/**
 * Besides being rolled actively on some circumsances, provides bonuses for
 * {@link Defending} and {@link DefensiveAttack} actions and faster
 * {@link Movement#disengage(Combatant)} (tumble).
 *
 * @author alex
 */
public class Acrobatics extends Skill{
	static final String[] NAMES=new String[]{"Acrobatics","tumble","balance",
			"escape artist"};

	/** Constructor. */
	Acrobatics(){
		super(NAMES,Ability.DEXTERITY,Realm.AIR);
		usedincombat=true;
	}

	@Override
	public int getbonus(Combatant c){
		int bonus=super.getbonus(c);
		if(c.source.hasfeat(Acrobatic.SINGLETON)) bonus+=+Acrobatic.BONUS;
		return bonus;
	}
}
