package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

public class ArenaFountain extends ArenaBuilding{
	static final String REFILLING="This fountain is refilling... be patient!";

	public boolean spent=true;
	/** TODO playtest */
	public float refillchance;

	String avatarfull;
	String avatarempty;

	public ArenaFountain(String name,String avatar,String avatarempty,
			String description){
		super(name,avatar,description);
		avatarfull=avatar;
		this.avatarempty=avatarempty;
		setspent(RPG.random()>refillchance);
	}

	public ArenaFountain(){
		this("Fountain","dungeonfountain","dungeonfountaindry",
				"Click this fountain to fully restore the active unit!");
	}

	@Override
	public void setlevel(BuildingLevel level){
		super.setlevel(level);
		refillchance=(level.level+1)/4f;
	}

	@Override
	protected boolean click(Combatant current){
		if(spent){
			Javelin.message("It's empty...",Delay.WAIT);
			return false;
		}
		setspent(true);
		List<Combatant> nearby=Fight.state.blueTeam.stream()
				.filter(c->c.getlocation().distanceinsteps(getlocation())==1)
				.collect(Collectors.toList());
		Javelin.prompt(activate(current,nearby)+"\nPress any key to continue...");
		BattleScreen.active.messagepanel.clear();
		return true;
	}

	protected String activate(Combatant current,List<Combatant> nearby){
		for(Combatant c:nearby)
			heal(Fight.state.clone(c));
		return "Nearby allies are completely restored!";
	}

	@Override
	protected void upgradebuilding(){
		// does nothing, just gets stronger
	}

	public static void heal(Combatant c){
		for(Condition co:c.getconditions())
			c.removecondition(co);
		Fountain.heal(c);
	}

	public void setspent(boolean spent){
		this.spent=spent;
		source.avatarfile=spent?avatarempty:avatarfull;
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
