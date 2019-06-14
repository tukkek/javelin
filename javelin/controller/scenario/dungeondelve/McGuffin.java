package javelin.controller.scenario.dungeondelve;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.view.screen.WorldScreen;

public class McGuffin extends Item{
	static final String DESCRIPTION="This is the artifact you've been looking for, bring it back to the surface!\n\n"
			+"It sure is pretty to look at, isn't it? ^^";

	public McGuffin(){
		super("The McGuffin",0,false);
		usedinbattle=false;
		usedoutofbattle=true;
		consumable=false;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		Javelin.app.switchScreen(WorldScreen.current);
		WorldScreen.current.center();
		Javelin.message(DESCRIPTION,false);
		return true;
	}
}
