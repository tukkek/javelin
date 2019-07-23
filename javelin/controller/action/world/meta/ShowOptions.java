package javelin.controller.action.world.meta;

import javelin.controller.action.SimpleAction;
import javelin.controller.action.world.WorldAction;
import javelin.view.frame.keys.PreferencesScreen;
import javelin.view.screen.WorldScreen;

/**
 * Opens up a configuration dialog where the user can adjust his preferences.
 *
 * TODO rename to ShowOptions
 *
 * @author alex
 */
public class ShowOptions extends WorldAction implements SimpleAction{
	/** Unique instance of this class. */
	private static ShowOptions singleton=null;

	/** Constructor. */
	private ShowOptions(){
		super("Configure options",new int[]{'o'},new String[]{"o"});
	}

	@Override
	public void perform(WorldScreen screen){
		new PreferencesScreen().show();
	}

	@Override
	public void perform(){
		perform(null);
	}

	@Override
	public int[] getcodes(){
		return keys;
	}

	@Override
	public String getname(){
		return name;
	}

	@Override
	public String[] getkeys(){
		return morekeys;
	}

	/**
	 * @return Unique instance for this class.
	 */
	public static ShowOptions getsingleton(){
		if(singleton==null) singleton=new ShowOptions();
		return singleton;
	}
}
