package javelin.controller;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.content.fight.Fight;
import javelin.controller.db.Preferences;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.view.mappanel.MapPanel;
import javelin.view.screen.BattleScreen;

/// TASK report global classes such as Fight.current; BattlaScreen.active;
/// fight.map; teams-names; platform and version...
public class Report implements UncaughtExceptionHandler{
  /// Singleton
  static public final Report INSTANCE=new Report();

  static final List<String> QUOTES=List.of("A wild error appears!",
      "You were eaten by a grue.","So again it has come to pass...",
      "Mamma mia!");
  static final String MESSAGE="""
      %s

      An error unfortunately ocurred.

      Please send a copy of `error.txt` or a screen-grab of the next message
      to one of these channels to help get it fixed for future releases:
      * Discord at https://discord.gg/5sXXcwPCSP
      * Steam at https://steamcommunity.com/app/5259370/discussions/
      * Mail at javelinrl@gmail.com

      Thank you for your patience and support!

      If your current session fails to load when you restart then try-copying
      the latest file from Backup to the main game-folder as `campaign.save`.
      """.trim();

  static String query(String text){
    return System.getProperty("os.%s".formatted(text));
  }

  static String trace(Throwable t){
    var c=t.getClass().getSimpleName();
    var trace=String.join("\n",Arrays.stream(t.getStackTrace())
        .map(StackTraceElement::toString).toList());
    var format="""
        ## %s (%s)
        %s
        """.trim();
    return format.formatted(t.getMessage(),c,trace);
  }

  static String format(Throwable t){
    var texts=new ArrayList<String>(2);
    for(;t!=null;t=t.getCause()) texts.add(trace(t));
    var format="""
        # Exception
        %s
        """.trim();
    return format.formatted(String.join("\n\n",texts));
  }

  static String name(Object o){
    return o==null?"null":o.getClass().getSimpleName();
  }

  static String view(){
    var s=BattleScreen.active;
    var p=MapPanel.get();
    var m=p==null?null:p.mouse;
    var format="""
        # View
        * Screen is %s
        * Map-Panel is %s
        * Mouse is %s
        """.trim();
    return format.formatted(name(s),name(p),name(m));
  }

  static String version(){
    var version=JavelinApp.version.trim();
    if(version.isEmpty()) return "";
    var format="""
        # Version
        %s
        """.trim();
    return format.formatted(version);
  }

  static String probe(){
    var format="""
        # System
        %s %s (%s).
        """.trim();
    return format.formatted(query("name"),query("version"),query("arch"));
  }

  static String call(List<Combatant> team){
    var names=team.stream().map(c->c.source.name).toList();
    return String.join("; ",Javelin.group(names));
  }

  static String fight(){
    var f=Fight.current;
    if(f==null) return "";
    var format="""
        # Fight
        * Fight is %s
        * Map is %s
        * Teams are: %s
        """.trim();
    var m=f.map;
    var s=Fight.state;
    var teams=s==null?List.of("")
        :List.of(s.redteam,s.blueteam).stream().map(Report::call).toList();
    return format.formatted(name(f),name(m),String.join(" versus ",teams));
  }

  static String delve(){
    var texts=new ArrayList<String>();
    for(var floor=Dungeon.active;floor!=null;floor=floor.climb())
      texts.add("* %s".formatted(floor.toString()));
    if(texts.isEmpty()) return "";
    Collections.reverse(texts);
    texts.add(0,"# Dungeon");
    return String.join("\n",texts);
  }

  static String report(Throwable t){
    var version=version();
    var error=format(t);
    var view=view();
    var dungeon=delve();
    var fight=fight();
    var system=probe();
    var sections=List.of(version,error,view,dungeon,fight,system).stream()
        .filter(text->!text.isEmpty()).toList();
    return String.join("\n\n",sections);
  }

  void handle(Throwable e,Thread thread){
    if(Thread.currentThread()!=thread) return; //no sub-thread dialogs
    var quote=RPG.pick(QUOTES);
    var m=MESSAGE.formatted(quote);
    JOptionPane.showMessageDialog(Javelin.app,m);
    var message=report(e);
    System.err.println(message);
    try{
      Preferences.write(message,"error.txt");
    }catch(IOException e1){
      // ignore
    }
    JOptionPane.showMessageDialog(Javelin.app,message);
    System.exit(20201202);
  }

  /// Reports an error and quits.
  static public void handle(Throwable t){
    INSTANCE.handle(t,Thread.currentThread());
  }

  @Override
  public void uncaughtException(Thread t,Throwable e){
    handle(e,t);
  }
}
