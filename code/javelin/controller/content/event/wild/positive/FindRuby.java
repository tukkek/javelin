package javelin.controller.content.event.wild.positive;

import java.util.Set;

import javelin.Javelin;
import javelin.controller.content.event.wild.WildEvent;
import javelin.controller.content.terrain.Terrain;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

public class FindRuby extends WildEvent{
	static final String PROMPT="You come across a small set of ruins. Do you want to explore them?\n"
			+"Press e to explore or i to ignore and move along...";

	public FindRuby(PointOfInterest l){
		super("Find ruby",l);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return Terrain.get(location.x,location.y).equals(Terrain.DESERT);
	}

	@Override
	public void happen(Squad s){
		Character input=Javelin.prompt(PROMPT,Set.of('e','i'));
		if(input=='i') return;
		remove=true;
		s.delay(RPG.rolldice(2,4));
		if(s.getbest(Skill.PERCEPTION).roll(Skill.PERCEPTION)<20){
			Javelin.message("They're empty. You only waste your time...",true);
			return;
		}
		Javelin.message("You find a hidden ruby!",true);
		new Ruby().grab(s);
	}
}
