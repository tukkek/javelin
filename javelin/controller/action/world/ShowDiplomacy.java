package javelin.controller.action.world;

import javelin.model.town.diplomacy.Diplomacy;
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
