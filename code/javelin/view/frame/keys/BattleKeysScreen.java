package javelin.view.frame.keys;

import java.util.ArrayList;

import javelin.controller.content.action.Action;
import javelin.controller.content.action.ActionAdapter;
import javelin.controller.content.action.ActionDescription;
import javelin.controller.content.action.ActionMapping;
import javelin.view.KeysScreen;

/**
 * Configures battle keys.
 *
 * @author alex
 */
public class BattleKeysScreen extends KeysScreen{
	/** Constructor. */
	public BattleKeysScreen(){
		super("Battle keys","keys.battle");
	}

	@Override
	public ArrayList<ActionDescription> getactions(){
		ArrayList<ActionDescription> list=new ArrayList<>(
				ActionMapping.ACTIONS.length);
		for(Action c:ActionMapping.ACTIONS)
			if(!(c instanceof ActionAdapter)&&c.getMainKey()!=null) list.add(c);
		return list;
	}

}
