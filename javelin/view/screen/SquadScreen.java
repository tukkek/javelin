package javelin.view.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.comparator.MonstersByName;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
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
  /** Minimum starting party encounter level. */
  public static final float EL=ChallengeCalculator
      .calculateelfromcrs(List.of(1f,1f,1f,1f));

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
    while(squad.getel()<EL){
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

  void upgrade(Squad s){
    var advancement=new HashMap<Combatant,Upgrade>(s.members.size());
    for(var m:s.members){
      var kits=Kit.getpreferred(m.source,false).stream()
          .flatMap(k->k.getupgrades().stream())
          .filter(u->u instanceof ClassLevelUpgrade).toList();
      advancement.put(m,RPG.pick(kits));
    }
    while(s.getel()<EL){
      var members=new Combatants(s.members);
      members.sort(CombatantByCr.SINGLETON);
      var m=members.get(0);
      advancement.get(m).upgrade(m);
      ChallengeCalculator.calculatecr(m.source);
    }
    var base=RewardCalculator.calculatepcequipment(1);
    squad.gold=squad.members.stream().mapToInt(
        m->RewardCalculator.calculatepcequipment(Math.round(m.source.cr))-base)
        .sum();
    squad.gold=Javelin.round(squad.gold);
  }

  /** @return Units selected by the player. */
  public Squad open(){
    print();
    if(squad.getel()<EL) upgrade(squad);
    squad.sort();
    return squad;
  }
}
