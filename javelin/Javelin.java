package javelin;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javelin.controller.Highscore;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.fortification.Academy;
import javelin.old.Interface;
import javelin.old.messagepanel.MessagePanel;
import javelin.old.messagepanel.TextZone;
import javelin.view.Images;
import javelin.view.ScenarioSelectionDialog;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Utility class for broad-level rules and game-behavior. Add the VM argument
 * -Ddebug=true to the java command line for easier debugging and logging.
 *
 * @see #DEBUG
 * @author alex
 */
public class Javelin{
	private static final int JAVA=9;

	public enum Delay{
		NONE,WAIT,BLOCK
	}

	/**
	 * Add -Ddebug=true to the java VM command line for easier debugging and
	 * logging.
	 */
	public static final boolean DEBUG=System.getProperty("debug")!=null;
	/** TODO turn into {@link Enum} */
	public static final String PERIODMORNING="Morning";
	/** TODO turn into {@link Enum} */
	public static final String PERIODNOON="Noon";
	/** TODO turn into {@link Enum} */
	public static final String PERIODEVENING="Evening";
	/** TODO turn into {@link Enum} */
	public static final String PERIODNIGHT="Night";
	public static final String[] PERIODS=new String[]{PERIODMORNING,PERIODNOON,
			PERIODEVENING,PERIODNIGHT};
	public static final Image[] ICONS=new Image[]{Images.get("javelin")};
	/**
	 * Monster descriptions, separate from {@link Monster} data to avoid
	 * duplication in memory when using {@link Monster#clone()}.
	 *
	 * @see Combatant#clonedeeply()
	 */
	public static final TreeMap<String,String> DESCRIPTIONS=new TreeMap<>();
	/** All loaded monster mapped by challenge rating. */
	public static final TreeMap<Float,List<Monster>> MONSTERSBYCR=new TreeMap<>();
	/** All loaded XML {@link Monster}s. See {@link MonsterReader}. */
	public static final List<Monster> ALLMONSTERS=new ArrayList<>();

	static final String TITLE="Javelin";
	static final DecimalFormat COSTFORMAT=new DecimalFormat("####,###,##0");

	/** Singleton. */
	public static JavelinApp app;

	static{
		try{
			checkjava();
			UpgradeHandler.singleton.gather();
			Spell.init();
			final MonsterReader monsterdb=new MonsterReader();
			final XMLReader xml=XMLReaderFactory.createXMLReader();
			xml.setContentHandler(monsterdb);
			xml.setErrorHandler(monsterdb);
			FileReader filereader=new FileReader("monsters.xml");
			xml.parse(new InputSource(filereader));
			filereader.close();
			Organization.init();
			monsterdb.closelogs();
			SpellsFactor.init();
			Spell.init();
			Artifact.init();
			Item.init();
		}catch(final IOException e){
			e.printStackTrace();
		}catch(final SAXException e){
			e.printStackTrace();
		}
	}

	/**
	 * First method to be called.
	 *
	 * @param args See {@link #DEBUG}.
	 */
	public static void main(final String[] args){
		Thread.currentThread().setName("Javelin");
		ScenarioSelectionDialog.choose(args);
		app=new JavelinApp();
		final JFrame f=new JFrame(TITLE);
		f.setBackground(java.awt.Color.black);
		f.addWindowListener(StateManager.SAVEONCLOSE);
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setFocusTraversalKeysEnabled(false);
		f.setIconImages(Arrays.asList(ICONS));
		app.frame=f;
		app.setVisible(false);
		f.add(app);
		f.setSize(app.getPreferredSize().width,app.getPreferredSize().height);
		f.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(final KeyEvent e){
				Interface.userinterface.go(e);
			}
		});
		app.setVisible(true);
		f.setVisible(true);
		app.init();
	}

	private static void checkjava(){
		String[] version=System.getProperty("java.version").split("\\.");
		if(Integer.parseInt(
				version[0])!=1) /* java 2.x? Don't even try to guess what to do... */
			return;
		int major=Integer.parseInt(version[1]);
		if(major>=JAVA) return;
		String error;
		error="Javelin needs Java "+JAVA+" or newer to run properly.";
		error+="\nYou currently have Java "+major+" installed.";
		error+="\nPlease update Java in order play Javelin and to install the newest security updates.";
		error+="\n\nThe following webpage has further information on updating Java on all major operating systems:";
		error+="\nwww.techhelpkb.com/how-to-update-java-on-your-computer";
		JOptionPane.showMessageDialog(null,error);
		System.exit(1);
	}

	/**
	 * TODO move to a new class with proper enum?
	 *
	 * @return {@link #PERIODEVENING}, {@link #PERIODMORNING},
	 *         {@link #PERIODNIGHT} or {@value #PERIODNOON}.
	 */
	public static String getDayPeriod(){
		if(Javelin.app.fight!=null) return Javelin.app.fight.period;
		final long hourofday=getHour();
		if(hourofday<6) return PERIODNIGHT;
		if(hourofday<12) return PERIODMORNING;
		if(hourofday<18) return PERIODNOON;
		return PERIODEVENING;
	}

	/**
	 * If {@link Squad#active} is <code>null</code> for any reason, will return
	 * zero.
	 *
	 * @return Hour of the day, from 0 to 23.
	 */
	public static long getHour(){
		return Squad.active==null?0:Squad.active.hourselapsed%24;
	}

	/**
	 * This is also the only function that should write to {@link Squad#active} so
	 * it should be called with extreme caution to avoid changing it in the middle
	 * of an action.
	 *
	 * @return Next squad to act.
	 */
	public static Squad act(){
		Squad next=nexttoact();
		Squad.active=next;
		if(WorldScreen.lastday==-1)
			WorldScreen.lastday=Math.ceil(Squad.active.hourselapsed/24.0);
		return next;
	}

	/**
	 * @return Next squad to act.
	 * @see Squad#hourselapsed
	 */
	public static Squad nexttoact(){
		Squad next=null;
		for(final Actor a:World.getall(Squad.class)){
			Squad s=(Squad)a;
			if(next==null||s.hourselapsed<next.hourselapsed) next=s;
		}
		return next;
	}

	/**
	 * Pure fluff/flavor.
	 *
	 * @return Welcomes the playet to the game based on the current time of the
	 *         day.
	 */
	public static String welcome(){
		final String period=getDayPeriod();
		String flavor;
		if(period==PERIODMORNING)
			flavor="What dangers lie ahead..?";
		else if(period==PERIODNOON)
			flavor="Onwards to victory!";
		else if(period==PERIODEVENING)
			flavor="Cheers!";
		else if(period==PERIODNIGHT)
			flavor="What a horrible night to suffer an invasion...";
		else
			throw new RuntimeException("No welcome message");
		return "Welcome! "+flavor
				+"\n\n(press h at the overworld or battle screens for help)";
	}

	/**
	 * Once the player has no more {@link Squad}s and {@link Combatant}s under his
	 * control call this to stop the current game, invalidate the save file,
	 * record the highscore and exit the application.
	 */
	public static void lose(){
		if(Academy.train()) return;
		Javelin.app.switchScreen(BattleScreen.active);
		StateManager.clear();
		BattleScreen.active.messagepanel.clear();
		String sadface="You have lost all your units! Game over T_T\n\n";
		Javelin.message(sadface+Highscore.record(),Javelin.Delay.NONE);
		while(InfoScreen.feedback()!='\n')
			continue;
		System.exit(0);
	}

	/**
	 * @param rolltohit The number that needs to be rolled on a d20 for this
	 *          action to succeed.
	 * @return A textual representation of how easy or hard this action is to
	 *         achieve.
	 */
	static public String translatetochance(int rolltohit){
		if(rolltohit<=4) return "effortless";
		if(rolltohit<=8) return "easy";
		if(rolltohit<=12) return "fair";
		if(rolltohit<=16) return "hard";
		return "unlikely";
	}

	/**
	 * Utility function for user-input selection.
	 *
	 * TODO due to the UI being poor, if this method runs out of alphanumeric
	 * characters to use, it will display more choices but not allow the player to
	 * actually select them.
	 *
	 * @param prompt Text to show the user.
	 * @param names Will show each's {@link Object#toString()} as an option.
	 * @param fullscreen <code>true</code> to open in a new screen. Otherwise uses
	 *          the message panel.
	 * @param forceselection If <code>false</code> will allow the user to abort
	 *          the operation.
	 * @return The index of the selected element or -1 if aborted. Won't return an
	 *         invalid index other than -1.
	 */
	static public int choose(String prompt,List<?> names,boolean fullscreen,
			boolean forceselection){
		if(!forceselection) prompt+=" (q to quit)";
		prompt+="\n\n";
		int nnames=names.size();
		boolean multicolumn=nnames>20;
		ArrayList<Object> options=new ArrayList<>();
		for(int i=0;i<nnames;i++){
			boolean leftcolumn=i%2==0;
			String name=names.get(i).toString();
			options.add(name);
			String item="["+SelectScreen.getkey(i)+"] "+name;
			if(multicolumn&&leftcolumn) while(item.length()<50)
				item+=" ";
			prompt+=item;
			if(!multicolumn||!leftcolumn) prompt+="\n";
		}
		if(fullscreen)
			app.switchScreen(new InfoScreen(prompt));
		else{
			app.switchScreen(BattleScreen.active);
			MessagePanel.active.clear();
			Javelin.message(prompt,Javelin.Delay.NONE);
		}
		while(true)
			try{
				Character c=InfoScreen.feedback();
				if(!forceselection&&(c=='q'||c==InfoScreen.ESCAPE)) return -1;
				int selected=SelectScreen.convertkeytoindex(c);
				if(0<=selected&&selected<names.size()) return selected;
			}catch(Exception e){
				continue;
			}
	}

	/**
	 * Main output function for {@link WorldScreen}. Waits for user input for
	 * confirmation.
	 *
	 * @param text Prints this message in the status panel.
	 * @param requireenter If <code>true</code> will wait for the player to press
	 *          ENTER, otherwise any key will do.
	 * @return the key pressed by the user as confirmation for seeing the message.
	 */
	public static KeyEvent message(String text,boolean requireenter){
		MessagePanel.active.clear();
		Javelin.message(
				text+"\nPress "+(requireenter?"ENTER":"any key")+" to continue...",
				Javelin.Delay.NONE);
		KeyEvent input=Javelin.input();
		while(requireenter&&input.getKeyChar()!='\n')
			input=Javelin.input();
		MessagePanel.active.clear();
		return input;
	}

	/**
	 * Prompts a message in the {@link WorldScreen}.
	 *
	 * @param prompt Text to show.
	 * @return Any {@link InfoScreen#feedback()}.
	 */
	static public Character prompt(final String prompt,boolean center){
		MessagePanel.active.clear();
		BattleScreen.active.center();
		Javelin.message(prompt,Javelin.Delay.NONE);
		if(center) BattleScreen.active.center();
		return InfoScreen.feedback();
	}

	/**
	 * @param prompt Shows this message...
	 * @return and returns the user input.
	 */
	static public Character prompt(final String prompt){
		return prompt(prompt,false);
	}

	public static String describedifficulty(int dc){
		if(dc<=0) return "very easy";
		if(dc<=5) return "easy";
		if(dc<=10) return "average";
		if(dc<=15) return "tough";
		if(dc<=20) return "challenging";
		if(dc<=25) return "formidable";
		if(dc<=30) return "heroic";
		return "nearly impossible";
	}

	/**
	 * @param message Shows this in a fullscreen, requires enter to leave.
	 */
	public static void show(String message){
		InfoScreen s=new InfoScreen("");
		s.print(message);
		while(s.getInput()!='\n'){
			// wait for enter
		}
	}

	public static char promptscreen(String prompt){
		InfoScreen s=new InfoScreen("");
		s.print(prompt);
		return InfoScreen.feedback();
	}

	/**
	 * TODO a collection would make more sense
	 */
	public static List<Monster> getmonsterbytype(MonsterType type){
		ArrayList<Monster> monsters=new ArrayList<>();
		for(Monster m:Javelin.ALLMONSTERS)
			if(m.type==type) monsters.add(m);
		return monsters;
	}

	/**
	 * @return Textual representation of the givne {@link Option#price}.
	 */
	static public String format(double gold){
		return COSTFORMAT.format(gold);
	}

	/**
	 * Example: 194,151 will be "rounded" to 190,000.
	 *
	 * A 10% loss of precision is expected at most but it tends to normalize (and
	 * lessen with the game's exponential increase as challenge levels rise). The
	 * loss of precision is easily worth the much higher readability.
	 *
	 * @param gold A value in gold or other unit that needn't be precise.
	 *
	 * @return The input value, but rounded off as to be more legible.
	 */
	static public int round(int gold){
		if(gold<=100) return gold;
		int roundto=1;
		while(roundto*100<gold)
			roundto=roundto*10;
		return roundto*Math.round((float)gold/roundto);
	}

	/**
	 * @param name Monster type. Example: orc, kobold, young white dragon... Case
	 *          insensitive.
	 * @return A clone.
	 * @see Monster#clone()
	 */
	public static Monster getmonster(String name){
		Monster monster=null;
		for(Monster m:ALLMONSTERS)
			if(m.name.equalsIgnoreCase(name)){
				monster=m.clone();
				break;
			}
		if(monster==null) return null;
		ChallengeCalculator.calculatecr(monster);
		return monster;
	}

	public static void redraw(){
		BattleScreen.active.mappanel.refresh();
		MessagePanel.active.repaint();
	}

	public static KeyEvent input(){
		if(MessagePanel.active!=null) MessagePanel.active.repaint();
		Interface.userinterface.getinput();
		return Interface.userinterface.keyevent;
	}

	/**
	 * Main output function for {@link BattleScreen}s.
	 *
	 * @param message Text to be printed.
	 * @param t TODO remove
	 * @param See {@link Javelin.Delay}.
	 */
	public static void message(final String out,final Javelin.Delay d){
		MessagePanel.active.add(out);
		switch(d){
			case WAIT:
				try{
					redraw();
					Thread.sleep(Preferences.MESSAGEWAIT);
				}catch(final InterruptedException e){
					e.printStackTrace();
				}
				MessagePanel.active.clear();
				break;
			case BLOCK:
				MessagePanel.active.add("\n"+TextZone.BLACK+"-- ENTER --");
				Javelin.delayblock=true;
				break;
		}
	}

	public static boolean delayblock=false;
}
