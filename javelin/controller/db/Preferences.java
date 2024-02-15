package javelin.controller.db;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.MissingResourceException;
import java.util.Properties;

import javelin.Debug;
import javelin.Javelin.Delay;
import javelin.controller.Audio;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.old.messagepanel.TextZone;
import javelin.view.KeysScreen;
import javelin.view.frame.keys.BattleKeysScreen;
import javelin.view.frame.keys.WorldKeysScreen;
import javelin.view.frame.text.PreferencesScreen;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;

/**
 * Used to read the file "preferences.properties". See the file
 * "preferences.properties" for more details on standard options and this link
 * for development options:
 *
 * https://github.com/tukkek/javelin/wiki/Development-options
 *
 * @see PreferencesScreen
 * @author alex
 */
public class Preferences{
  /** Configuration file key for {@link #monitorperformance}. */
  public static final String KEYCHECKPERFORMANCE="ai.checkperformance";
  /** Configuration file key for {@link #maxthreads}. */
  public static final String KEYMAXTHREADS="ai.maxthreads";
  static final String FILE="preferences.properties";
  /** Key for {@link #tilesizebattle}. */
  public static final String KEYTILEBATTLE="ui.battletile";
  /** Key for {@link #tilesizeworld}. */
  public static final String KEYTILEWORLD="ui.worldtile";
  /** Key for {@link #tilesizedungeons}. */
  public static final String KEYTILEDUNGEON="ui.dungeontile";

  static Properties properties=new Properties();

  /** If <code>true</code> will use {@link AiCache}. */
  public static boolean aicacheenabled;
  /**
   * In some system will prevent the CPU temperature from going above this
   * value.
   */
  public static Integer maxtemperature;
  /** Max {@link BattleAi} thinking time. */
  public static int maxmilisecondsthinking;
  /** Thread limit for {@link ThreadManager}. */
  public static int maxthreads;
  /** If <code>true</code> {@link ThreadManager} will be self-monitoring. */
  public static boolean monitorperformance;
  /** Time to wait for {@link Delay#WAIT}. */
  public static int messagewait;
  /** Font color. */
  public static String textcolor;
  /** How often to save the game, in minutes. */
  public static int saveinterval;
  /**
   * If <code>true</code> backups the game on initialization.
   *
   * @see StateManager
   */
  public static int backupinterval;
  /** Tile size for {@link BattleScreen}. */
  public static int tilesizebattle;
  /** Tile size for {@link WorldScreen}. */
  public static int tilesizeworld;
  /** Tile size for {@link DungeonScreen}. */
  public static int tilesizedungeons;
  /**
   * External {@link Audio} player (command).
   *
   * TODO shouldn't be necessary if and when using JavaFX
   */
  public static String player;
  /** {@link Audio} volume. */
  public static String volume;

  static{
    load();
  }

  synchronized static void load(){
    try{
      properties.clear();
      properties.load(new FileReader(FILE));
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  static String getstring(String key){
    try{
      return properties.getProperty(key);
    }catch(MissingResourceException e){
      return null;
    }
  }

  static Integer getinteger(String key,Integer fallback){
    var value=getstring(key);
    /* Don't inline. */
    if(value==null) return fallback;
    return Integer.parseInt(value);
  }

  /**
   * @return the entire text of the properties file.
   */
  synchronized public static String getfile(){
    try{
      var s="";
      var reader=new BufferedReader(new FileReader(FILE));
      var line=reader.readLine();
      while(line!=null){
        s+=line+"\n";
        line=reader.readLine();
      }
      reader.close();
      return s;
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  /**
   * TODO would be better to have an option #setpreference(String key,String
   * value) that would read the file, replace a line with the given key and save
   * it in the proper order instead of forever appending to the file. It could
   * be used in a few places this is being used instead.
   *
   * @param content Overwrite the properties file with this content and reloads
   *   all options.
   */
  synchronized public static void savefile(String content){
    try{
      write(content,FILE);
      load();
      setup();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * @param content Write content...
   * @param f to this file.
   * @throws IOException
   */
  public static void write(String content,String f) throws IOException{
    var writer=new FileWriter(f);
    writer.write(content);
    writer.close();
  }

  /**
   * Initializes or reloads all preferences.
   */
  public static void setup(){
    aicacheenabled="true".equals(getstring("ai.cache"));
    maxtemperature=getinteger("ai.maxtemperature",0);
    maxmilisecondsthinking=Math.round(1000*getFloat("ai.maxsecondsthinking"));
    var cpus=Runtime.getRuntime().availableProcessors();
    maxthreads=Preferences.getinteger(KEYMAXTHREADS,cpus);
    if(maxthreads>cpus) maxthreads=cpus;
    monitorperformance="true"
        .equals(Preferences.getstring(KEYCHECKPERFORMANCE));
    if(monitorperformance){
      if(ThreadManager.performance==Integer.MIN_VALUE)
        ThreadManager.performance=0;
    }else ThreadManager.performance=Integer.MIN_VALUE;

    saveinterval=getinteger("fs.saveinterval",9);
    backupinterval=getinteger("fs.backupinterval",15);

    tilesizeworld=getinteger(KEYTILEWORLD,32);
    tilesizebattle=getinteger(KEYTILEBATTLE,32);
    tilesizedungeons=getinteger(KEYTILEDUNGEON,32);
    messagewait=Math.round(1000*getFloat("ui.messagedelay"));
    textcolor=getstring("ui.textcolor").toUpperCase();
    try{
      TextZone.fontcolor=(Color)Color.class.getField(Preferences.textcolor)
          .get(null);
    }catch(Exception e){
      TextZone.fontcolor=Color.BLACK;
    }
    player=getstring("io.player");
    volume=getstring("io.volume");

    initkeys("keys.world",new WorldKeysScreen());
    initkeys("keys.battle",new BattleKeysScreen());

    readdebug();
  }

  private static void initkeys(String propertyname,KeysScreen worldKeyScreen){
    var keys=getstring(propertyname);
    if(keys==null) return;
    var actions=worldKeyScreen.getactions();
    for(var i=0;i<keys.length();i++)
      actions.get(i).setMainKey(Character.toString(keys.charAt(i)));
  }

  static void readdebug(){
    Debug.disablecombat="false".equals(getstring("cheat.combat"));
    Debug.xp=getinteger("cheat.xp",null);
    Debug.gold=getinteger("cheat.gold",null);
    Debug.labor=getboolean("cheat.labor");
    Debug.showmap=getboolean("cheat.world");
    Debug.period=getstring("cheat.period");
    Debug.weather=getstring("cheat.weather");
    Debug.season=getstring("cheat.season");
    Debug.unlocktemples=getboolean("cheat.temples");
    Debug.bypassdoors=getboolean("cheat.doors");
    initdebug();
  }

  private static boolean getboolean(String key){
    var value=getstring(key);
    return value!=null&&!"false".equals(value);
  }

  static void initdebug(){
    if(Debug.showmap&&BattleScreen.active!=null
        &&BattleScreen.active.getClass().equals(WorldScreen.class))
      for(var x=0;x<World.SIZE;x++)
        for(var y=0;y<World.SIZE;y++) WorldScreen.discover(x,y);
    if(World.seed!=null)
      for(Actor a:World.getall(Squad.class)) initsquaddebug((Squad)a);
    if(Debug.weather!=null){
      Debug.weather=Debug.weather.toLowerCase();
      Weather.current=Weather.read(0); // tests cheat.weather value
    }
    if(Debug.season!=null)
      Season.current=Season.valueOf(Debug.season.toUpperCase());
  }

  static void initsquaddebug(Squad s){
    if(Debug.gold!=null) s.gold=Debug.gold;
    if(Debug.xp!=null)
      for(Combatant c:s.members) c.xp=new BigDecimal(Debug.xp/100f);
  }

  private static float getFloat(String key){
    return Float.parseFloat(getstring(key));
  }

  /**
   * Reads and writes to actual file.
   *
   * @param key Replaces the line with this option or creates a new line at the
   *   end of the properties file.
   * @param value Option value.
   */
  synchronized static public void setoption(String key,Object value){
    var from=getfile();
    var to="";
    var replaced=false;
    for(String line:from.split("\n")) if(line.startsWith(key)){
      to+=key+"="+value+"\n";
      replaced=true;
    }else to+=line+"\n";
    if(!replaced) to+="\n"+key+"="+value+"\n";
    savefile(to);
  }
}
