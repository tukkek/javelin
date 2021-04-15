package javelin.controller.content.action.world;

import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.view.frame.DiplomacyScreen;
import javelin.view.screen.WorldScreen;

/**
 * @see Diplomacy
 * @see DiplomacyScreen
 * @author alex
 */
public class ShowDiplomacy extends WorldAction{
	/** Constructor. */
	public ShowDiplomacy(){
		super("Diplomacy",new int[]{'D'},new String[]{"D"});
	}

	@Override
	public void perform(WorldScreen screen){
		DiplomacyScreen.open();
	}
}
