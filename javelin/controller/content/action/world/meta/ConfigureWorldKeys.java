package javelin.controller.content.action.world.meta;

import javelin.controller.content.action.world.WorldAction;
import javelin.view.frame.keys.WorldKeysScreen;
import javelin.view.screen.WorldScreen;

/**
 * @author alex
 */
public class ConfigureWorldKeys extends WorldAction{

	/** Constructor. */
	public ConfigureWorldKeys(){
		super("Configure keys",new int[]{'K'},new String[]{"K"});
	}

	@Override
	public void perform(WorldScreen ws){
		new WorldKeysScreen().show();
	}
}
