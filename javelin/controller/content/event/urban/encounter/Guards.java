package javelin.controller.content.event.urban.encounter;

import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.Commoner;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * {@link Squad} gets arrested by guards. They can fight, bribe or talk their
 * way out of the situation. If they fail, they will spend some time in jail.
 *
 * @author alex
 */
public class Guards extends UrbanEncounter{
	static final List<String> TRAITS=List.of(Trait.CRIMINAL,Trait.MILITARY);
	static final Set<Upgrade> UPGRADES=Set.of(Commoner.SINGLETON,
			Warrior.SINGLETON);

	/** Reflection constructor. */
	public Guards(Town t){
		super(t,TRAITS,Rank.VILLAGE,UPGRADES,
				"A squad in "+t+" is approached by a group of suspicious guards.");
		bribe="Bribe the guards";
		fight="Fight the guards";
		surrender="Surrender and go to jail until things are sorted out";
	}

	@Override
	protected boolean validate(Monster foe){
		return foe.alignment.ethics!=Ethics.CHAOTIC&&foe.cr<=5&&super.validate(foe);
	}

	@Override
	protected boolean rollskill(Squad s,int dc,String message){
		var diplomat=s.getbest(Skill.DIPLOMACY);
		var diplomacy=diplomat.roll(Skill.DIPLOMACY);
		if(diplomacy<10+town.population) return false;
		message+="\nThankfully, "+diplomat+" convinces them that you're harmless!";
		Javelin.message(message,true);
		return true;
	}

	@Override
	protected void surrender(){
		var rank=town.getrank().rank;
		var days=RPG.rolldice(rank,8)-rank;
		Squad.active.delay(days*24);
		notify("You spend "+days+" days in jail before being cleared.");
	}
}
