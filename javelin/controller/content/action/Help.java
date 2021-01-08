package javelin.controller.content.action;

import java.util.LinkedList;

import javelin.Debug;
import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * Shows keyboard commands.
 *
 * @author alex
 */
public class Help extends Action{
	/** Constructor. */
	public Help(){
		super("help",new String[]{"h","?"});
		allowburrowed=true;
	}

	@Override
	public boolean perform(Combatant hero){
		help(ActionMapping.ACTIONS);
		return false;
	}

	/**
	 * @param actions Show a help screen for these given actions.
	 */
	static public void help(ActionDescription[] actions){
		String text="These are all the available commands on this screen.\n"
				+"Movement is also used to attack adjacent enemies, enter locations, interact with objects, etc.\n\n";
		LinkedList<String> commands=new LinkedList<>();
		for(final ActionDescription a:actions){
			final String[] keys=a.getDescriptiveKeys();
			if(keys.length==0) continue;
			commands.add(print(a));
		}
		LinkedList<String> columna=new LinkedList<>();
		LinkedList<String> columnb=new LinkedList<>();
		int padding=-1;
		while(!commands.isEmpty()){
			String a=commands.pop();
			columna.add(a);
			if(a.length()>padding) padding=a.length();
			if(!commands.isEmpty()) columnb.add(commands.removeLast());
		}
		while(!columna.isEmpty()){
			text+=pad(columna.pop(),padding);
			if(!columnb.isEmpty()) text+=columnb.removeLast();
			text+="\n";
		}
		text+="\nKeep up-to-date with new releases at javelinrl.wordpress.com\n"
				+"or come discuss the game at reddit.com/r/javelinrl :)";
		if(Javelin.DEBUG){
			var isworld=BattleScreen.active instanceof WorldScreen;
			var d=isworld?Debug.onworldhelp():Debug.onbattlehelp();
			if(!d.isEmpty()) text=d;
		}
		Javelin.app.switchScreen(new InfoScreen(text));
		Javelin.input();
		Javelin.app.switchScreen(BattleScreen.active);
	}

	private static String pad(String s,int padding){
		while(s.length()<padding+10)
			s+=" ";
		return s;
	}

	private static String print(ActionDescription a){
		String text="";
		boolean first=true;
		for(final String key:a.getDescriptiveKeys()){
			if(key.contains("arrow")) continue;
			if(first)
				first=false;
			else
				text+=" or ";
			text+=key;
		}
		return text+": "+a.getDescriptiveName();
	}
}
