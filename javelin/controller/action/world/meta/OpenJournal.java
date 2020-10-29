package javelin.controller.action.world.meta;

import javelin.controller.action.SimpleAction;
import javelin.controller.action.world.WorldAction;
import javelin.view.frame.text.TextScreen;
import javelin.view.screen.WorldScreen;

/**
 * Lets a user take notes between sessions.
 *
 * @author alex
 */
public class OpenJournal extends WorldAction implements SimpleAction{
	class JournalScreen extends TextScreen{
		JournalScreen(){
			super("Journal");
		}

		@Override
		protected void savetext(String text){
			content=text;
		}

		@Override
		protected String loadtext(){
			return content;
		}

	}

	/** Unique instance for this class. */
	private static OpenJournal singleton=null;

	/** Journal's content. */
	static public String content="This is your journal, use it to make notes about your current game!";

	/** Constructor. */
	private OpenJournal(){
		super("Journal",new int[]{'j'},new String[]{"j"});
	}

	@Override
	public void perform(WorldScreen screen){
		new JournalScreen().show();
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
	public static OpenJournal getsingleton(){
		if(singleton==null) singleton=new OpenJournal();
		return singleton;
	}
}
