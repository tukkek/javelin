package javelin.controller.event.wild.positive;

import java.util.Set;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

public class FindRuby extends WildEvent{
	static final String PROMPT="You come across a small set of ruins. Do you want to explore them?\n"
			+"Press e to explore or i to ignore and move along...";

	public FindRuby(){
		super("Find ruby");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		return Terrain.get(l.x,l.y).equals(Terrain.DESERT);
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		Character input=Javelin.prompt(PROMPT,Set.of('e','i'));
		if(input=='i') return;
		remove=true;
		s.hourselapsed+=RPG.rolldice(2,4);
		if(s.getbest(Skill.PERCEPTION).roll(Skill.PERCEPTION)<20){
			Javelin.message("They're empty. You only waste your time...",true);
			return;
		}
		Javelin.message("You find a hidden ruby!",true);
		new Ruby().grab(s);
	}
}
