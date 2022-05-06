package javelin.view.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.MonstersByName;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.old.RPG;

/**
 * Squad selection screen when starting a new game.
 *
 * @author alex
 */
public class SquadScreen extends InfoScreen{
  /** All units suitable for a starting {@link Squad}. */
  public static final ArrayList<Monster> CANDIDATES=new ArrayList<>();

  static final List<Float> CRS=List.of(1f,1.25f,1.5f);
  static final String KEYS="abcdefghijklmnopqrstuvwxy0123456789";
  static final String ENTRY=" %s - %s";
  static final String FORMAT="""
      Available units:

      %s

      Press letter to select character
      Press z to pick a random unit
      Press BACKSPACE to clear current selection
      Press ENTER to coninue with current selection

      Your team:

      %s
      """;

  static{
    var candidates=CRS.stream().flatMap(cr->Monster.BYCR.get(cr).stream())
        .filter(Monster::isalive).collect(Collectors.toList());
    CANDIDATES.addAll(candidates);
    CANDIDATES.sort(MonstersByName.INSTANCE);
  }

  Squad squad=new Squad(0,0,8,null);
  boolean first=true;

  /** Constructor. */
  public SquadScreen(){
    super("");
  }

  void pickrandom(){
    Monster candidate=null;
    while(candidate==null){
      if(Javelin.DEBUG&&CANDIDATES.isEmpty())
        throw new NoSuchElementException();
      candidate=RPG.pick(CANDIDATES);
      for(var m:squad.members) if(m.source.name.equals(candidate.name)){
        candidate=null;
        break;
      }
    }
    recruit(candidate);
  }

  void recruit(Monster m){
    var c=squad.recruit(m);
    c.hp=c.source.hd.maximize();
    c.maxhp=c.hp;
  }

  String printtable(){
    var mid=(int)Math.ceil(CANDIDATES.size()/2f);
    var columns=List.of(CANDIDATES.subList(0,mid),
        CANDIDATES.subList(mid,CANDIDATES.size()));
    var pad=columns.get(0).stream().map(m->m.name.length())
        .reduce((a,b)->a>b?a:b).get();
    var table=new ArrayList<String>(mid);
    for(var i=0;i<columns.get(0).size();i++){
      var a=columns.get(0).get(i);
      var keya=KEYS.charAt(i);
      var columna="%s - %s".formatted(keya,a);
      var columnb="";
      if(i<columns.get(1).size()){
        var b=columns.get(1).get(i);
        var keyb=KEYS.charAt(CANDIDATES.indexOf(b));
        columnb=ENTRY.formatted(keyb,b);
      }
      var padding=" ".repeat(pad-a.name.length());
      table.add("%s%s %s".formatted(columna,padding,columnb));
    }
    return String.join("\n",table);
  }

  void print(){
    while(!World.scenario.checkfullsquad(squad.members)){
      Javelin.app.switchScreen(this);
      var team=String.join("\n",squad.members.stream().map(Combatant::toString)
          .collect(Collectors.toList()));
      text=FORMAT.formatted(printtable(),team);
      repaint();
      var f=InfoScreen.feedback();
      if(f=='\n'&&!squad.members.isEmpty()) break;
      var i=KEYS.indexOf(f);
      if(0<=i&&i<CANDIDATES.size()) recruit(CANDIDATES.get(i));
      else if(f=='z') pickrandom();
      else if(f=='\b') squad.members.clear();
    }
  }

  /**
   * Displays this screen.
   *
   * @return {@link Monster}s selected by the player.
   */
  public Squad open(){
    print();
    World.scenario.upgradesquad(squad);
    var gold=squad.members.stream().map(c->c.source.cr-1)
        .filter(level->level>=1)
        .map(level->RewardCalculator.calculatepcequipment(Math.round(level)))
        .reduce(0,(a,b)->a+b);
    squad.gold=Javelin.round(gold);
    squad.sort();
    return squad;
  }
}
