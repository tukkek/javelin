package javelin.model.world.location.dungeon.feature.inhabitant;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

public class Prisoner extends Inhabitant{
	public Prisoner(){
		super(Dungeon.active.level+Difficulty.VERYEASY+1,
				Dungeon.active.level+Difficulty.EASY,"prisoner");
	}

	@Override
	public boolean activate(){
		MasterKey key=Squad.active.equipment.get(MasterKey.class);
		String message="You find a is a "+inhabitant+" prisoner here.\n";
		if(key==null){
			message+="If you had a master key you could free him...";
			Javelin.message(message,false);
			return false;
		}
		message+="Do you want to free him with your master key?\n"
				+"Press f to free prisoner, any other key to cancel...";
		if(Javelin.prompt(message,false)!='f') return false;
		if(RPG.chancein(2)){
			Javelin.message("You are interrupted by a group of guards!",false);
			throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
		}
		Squad.active.equipment.remove(key);
		Combatant prisoner=Squad.active.recruit(inhabitant.source);
		prisoner.hp=prisoner.maxhp
				*RPG.r(Combatant.STATUSWOUNDED,Combatant.STATUSSCRATCHED)
				/Combatant.STATUSUNHARMED;
		prisoner.hp=Math.max(1,prisoner.hp);
		Dungeon.active.features.remove(this);
		return true;
	}
}
