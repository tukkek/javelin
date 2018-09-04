package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;

public class ArenaFountain extends ArenaBuilding{
	static final String REFILLING="This fountain is refilling... be patient!";

	public boolean spent=true;
	/** TODO playtest */
	public float refillchance;

	public ArenaFountain(){
		super("Fountain","dungeonfountain",
				"Click this fountain to fully restore the active unit!");
		setspent(RPG.random()>refillchance);
	}

	@Override
	public void setlevel(BuildingLevel level){
		super.setlevel(level);
		refillchance=(level.level+1)/4f;
	}

	@Override
	protected boolean click(Combatant current){
		if(spent){
			Javelin.prompt("This fountain is empty...");
			return false;
		}
		for(Combatant c:Fight.state.blueTeam)
			if(c.getlocation().distanceinsteps(getlocation())==1) restore(c);
		setspent(true);
		MessagePanel.active.clear();
		Javelin.message("Nearby allies are completely restored!",
				Javelin.Delay.BLOCK);
		return true;
	}

	@Override
	protected void upgradebuilding(){
		// does nothing, just gets stronger
	}

	void restore(Combatant current){
		heal(Fight.state.clone(current));
	}

	public static void heal(Combatant c){
		for(Condition co:c.getconditions())
			c.removecondition(co);
		Fountain.heal(c);
	}

	public void setspent(boolean spent){
		this.spent=spent;
		source.avatarfile=spent?"dungeonfountaindry":"dungeonfountain";
	}

	@Override
	public String getactiondescription(Combatant current){
		return spent?REFILLING+" (or click to upgrade)"
				:super.getactiondescription(current);
	}

	public static final ArrayList<ArenaFountain> get(){
		ArrayList<ArenaFountain> fountains=new ArrayList<>();
		for(Combatant c:Fight.state.blueTeam)
			if(c instanceof ArenaFountain) fountains.add((ArenaFountain)c);
		return fountains;
	}
}
