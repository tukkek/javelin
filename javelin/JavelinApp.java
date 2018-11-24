package javelin;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.scenario.Campaign;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.UpgradeHandler.UpgradeSet;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.unique.UniqueLocation;
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
	/** Bootstrapper for minigames. */
	public static Minigame minigame=null;
	static public WorldScreen context;

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
		javelin.controller.db.Preferences.init();// pre
		initialize();
		if(minigame==null&&!StateManager.load()){
			if(StateManager.nofile) disclaimer();
			startcampaign();
		}
		javelin.controller.db.Preferences.init();// post
		preparedebug();
		if(Javelin.DEBUG){
			Debug.oninit();
			while(true)
				loop();
		}
		/*if non-debug*/
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

	private void initialize(){
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
		World.scenario.ready(World.seed);
		if(Javelin.DEBUG){
			JavelinApp.printstatistics();
			Debug.oncampaignstart();
		}
	}

	/** stats */
	static void printstatistics(){
		if(!(World.scenario instanceof Campaign)) return;
		System.out.println();
		printoptions();
		System.out.println(Javelin.ALLMONSTERS.size()+" monsters");
		System.out.println(Item.ALL.size()-Item.ARTIFACT.size()+" items, "
				+Item.ARTIFACT.size()+" artifacts, 7 relics");
		Collection<Spell> spells=Spell.SPELLS.values();
		var upgrades=UpgradeHandler.singleton;
		upgrades.gather();
		int nupgrades=upgrades.count()-spells.size();
		int nspells=spells.size()-countsummon(spells)+1;
		int nskills=upgrades.countskills();
		int nkits=Kit.KITS.size();
		System.out.println(nupgrades+" upgrades, "+nspells+" spells, "+nskills
				+" skills, "+nkits+" kits");
		HashSet<Class<? extends Actor>> locationtypes=new HashSet<>();
		int uniquelocations=0;
		for(Actor a:World.getactors()){
			if(!(a instanceof Location)) continue;
			locationtypes.add(a.getClass());
			if(a instanceof UniqueLocation) uniquelocations+=1;
		}
		System.out.println(locationtypes.size()-uniquelocations
				+" world location types, "+uniquelocations+" unique locations");
		Deck.printstats();
		int maps=Terrain.UNDERGROUND.getmaps().size();
		for(Terrain t:Terrain.NONUNDERGROUND)
			maps+=t.getmaps().size();
		System.out.println(maps+" battle maps");
	}

	static void printoptions(){
		var allupgrades=UpgradeHandler.singleton.getall(false);
		ArrayList<Upgrade> upgradelist=new ArrayList<>();
		HashMap<String,ItemSelection> allitems=Item.getall();
		List<String> primary=Arrays.asList(
				new String[]{"earth","wind","fire","water","good","evil","magic"});
		for(String realm:primary){
			printrealm(allupgrades,allitems,realm);
			upgradelist.addAll(allupgrades.get(realm));
		}
		ArrayList<String> extrarealms=new ArrayList<>(allupgrades.keySet());
		extrarealms.sort(null);
		for(String realm:extrarealms)
			if(!primary.contains(realm)){
				printrealm(allupgrades,allitems,realm);
				upgradelist.addAll(allupgrades.get(realm));
			}
		for(Kit k:Kit.KITS)
			for(Upgrade u:k.basic)
				if(!(u instanceof Summon)&&!upgradelist.contains(u))
					throw new RuntimeException("Unregistered upgrade: "+u);
	}

	static void printrealm(HashMap<String,UpgradeSet> allupgrades,
			HashMap<String,ItemSelection> allitems,String realm){
		HashSet<Upgrade> upgrades=allupgrades.get(realm);
		int count=1;
		System.out.println(realm);
		for(Upgrade u:upgrades){
			System.out.println("\t"+count+" - "+u);
			count+=1;
		}
		System.out.println();
		ItemSelection inventory=allitems.get(realm);
		for(int i=0;inventory!=null&&i<inventory.size();i++){
			Item item=inventory.get(i).clone();
			System.out.println("\t"+count+" - "+item+" ($"+item.price+")");
			count+=1;
		}
		System.out.println();
	}

	static int countsummon(Collection<Spell> spells){
		int summon=0;
		for(Spell s:spells)
			if(s instanceof Summon) summon+=1;
		return summon;
	}

	@SuppressWarnings("deprecation")
	void preparedebug(){
		if(Debug.gold!=null) Squad.active.gold=Debug.gold;
		if(Debug.xp!=null) for(final Combatant m:Squad.active.members)
			m.xp=new BigDecimal(Debug.xp/100f);
	}
}