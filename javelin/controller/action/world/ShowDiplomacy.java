package javelin.controller.action.world;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.world.World;
import javelin.view.frame.DiplomacyScreen;
import javelin.view.screen.WorldScreen;

/**
 * @see Diplomacy
 * @see DiplomacyScreen
 * @author alex
 */
public class ShowDiplomacy extends WorldAction{
	static final boolean DEBUG=true;

	/** Constructor. */
	public ShowDiplomacy(){
		super("Diplomacy",new int[]{'D'},new String[]{"D"});
	}

	@Override
	public void perform(WorldScreen screen){
		var d=Diplomacy.instance;
		if(d==null){
			var mode=World.scenario.toString().toLowerCase();
			Javelin.message("Diplomacy not enabled on "+mode+" mode...",false);
			return;
		}
		if(Javelin.DEBUG&&DEBUG){
			d.reputation=Diplomacy.TRIGGER;
			d.draw();
		}
		DiplomacyScreen.open();
	}
}