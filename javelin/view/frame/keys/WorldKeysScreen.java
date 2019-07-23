package javelin.view.frame.keys;

import java.util.ArrayList;

import javelin.controller.action.ActionDescription;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.meta.help.Guide;
import javelin.view.KeysScreen;

/**
 * Configures world screen keys.
 *
 * @author alex
 */
public class WorldKeysScreen extends KeysScreen{
	/** Constructor. */
	public WorldKeysScreen(){
		super("World keys","keys.world");
	}

	@Override
	public ArrayList<ActionDescription> getactions(){
		ArrayList<ActionDescription> list=new ArrayList<>(
				WorldAction.ACTIONS.length);
		for(WorldAction a:WorldAction.ACTIONS)
			if(!(a instanceof Guide)) list.add(a);
		return list;
	}

}
