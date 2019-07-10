package javelin;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javelin.controller.ContentSummary;
import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.generator.WorldGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.QuestApp;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Application and game life-cycle.
 *
 * TODO refactor a time manager out of this which works with
 * TimeManager#tick(int time,WorldState world)?
 *
 * @author alex
 */
public class JavelinApp extends QuestApp{
	static final String CRASHMESSAGE="\n\nUnfortunately an error ocurred.\n"
			+"Please send a screenshot of the next message or the log file (error.txt)\n"
			+"to one of the channels below, so we can get this fixed on future releases.\n\n"
			+"If for any reason your current game fails to load when you restart Javelin\n"
			+"you can find backups on the \"backup\" folder. Just move one of them to the\n"
			+"main folder and rename it to \"javelin.save\" to restore your progress.\n\n"
			+"Post to our reddit forum -- http://www.reddit.com/r/javelinrl\n"
			+"Leave a comment on our website -- http://javelinrl.wordpress.com\n"
			+"Or write us an e-mail -- javelinrl@gmail.com\n";
	static final String[] CRASHQUOTES=new String[]{"A wild error appears!",
			"You were eaten by a grue.","So again it has come to pass...",
			"Mamma mia!",};

	/** Lower-case operating system name. */
	public static final String SYSTEM=System.getProperty("os.name").toLowerCase();

	/** Bootstrapper for minigames. */
	public static Minigame minigame=null;
	/** TODO change into actual context (separate from UI code). */
	static public WorldScreen context;

	/** Controller. */

	/**
	 * Controller for active battle. Should be <code>null</code> at any point a
	 * battle is not occurring.
	 */
	public Fight fight;
	/** Root window. */
	public JFrame frame;

	static String version="Version: ?";

	@Override
	public void run(){
		Preferences.setup();// pre
		initialize();
		if(minigame==null&&!StateManager.load()){
			if(StateManager.nofile) disclaimer();
			startcampaign();
		}
		Preferences.setup();// post
		preparedebug();
		if(Javelin.DEBUG) while(true)
			loop();
		while(true)
			try{
				loop();
			}catch(RuntimeException e){
				handlefatalexception(e);
			}
	}

	void loop(){
		try{
			if(minigame!=null){
				if(!minigame.start()) System.exit(1);
				throw new StartBattle(minigame);
			}
			if(Dungeon.active==null)
				JavelinApp.context=new WorldScreen(true);
			else
				Dungeon.active.activate(true);
			while(true){
				switchScreen(JavelinApp.context);
				JavelinApp.context.turn();
			}
		}catch(final StartBattle e){
			if(Debug.disablecombat) return;
			fight=e.fight;
			try{
				e.battle();
			}catch(final EndBattle end){
				EndBattle.end();
			}
		}
	}

	/**
	 * @param e Show this error to the user and log it.
	 */
	static public void handlefatalexception(RuntimeException e){
		String quote=CRASHQUOTES[RPG.r(CRASHQUOTES.length)];
		JOptionPane.showMessageDialog(Javelin.app,quote+CRASHMESSAGE);
		if(!version.endsWith("\n")) version+="\n";
		System.err.print(version);
		e.printStackTrace();
		String system="System: ";
		for(String info:new String[]{"os.name","os.version","os.arch"})
			system+=System.getProperty(info)+" ";
		system+="\n";
		String error=version+system+"\n"+printstacktrace(e);
		Throwable t=e;
		HashSet<Throwable> errors=new HashSet<>(2);
		while(t.getCause()!=null&&errors.add(t)){
			t=t.getCause();
			error+=System.lineSeparator()+printstacktrace(t);
		}
		try{
			Preferences.write(error,"error.txt");
		}catch(IOException e1){
			// ignore
		}
		JOptionPane.showMessageDialog(Javelin.app,error);
		System.exit(1);
	}

	/** @return A pretty-printed stack trace. */
	public static String printstacktrace(Throwable e){
		String error=e.getMessage()+" ("+e.getClass()+")";
		error+="\n";
		for(StackTraceElement stack:e.getStackTrace())
			error+=stack.toString()+"\n";
		return error;
	}

	void disclaimer(){
		while(TextReader
				.show(new File("README.txt"),
						"This message will only be shown once, press ENTER to continue.")
				.getKeyCode()!=KeyEvent.VK_ENTER){
			// wait for enter
		}
	}

	static void initialize(){
		String readme=TextReader.read(new File("README.txt"));
		System.out.println(readme.replaceAll("\n\n","\n"));
		try{
			version=TextReader.read(new File("doc","VERSION.txt"));
		}catch(RuntimeException e){
			//file is generated, might not be there at all
		}
		ThreadManager.determineprocessors();
	}

	void startcampaign(){
		World.scenario.setup();
		WorldGenerator.build();
		World.scenario.ready();
		if(Javelin.DEBUG){
			new ContentSummary().produce();
			Debug.oncampaignstart();
		}
	}

	void preparedebug(){
		if(Debug.gold!=null) Squad.active.gold=Debug.gold;
		if(Debug.xp!=null) for(final Combatant m:Squad.active.members)
			m.xp=new BigDecimal(Debug.xp/100f);
	}
}