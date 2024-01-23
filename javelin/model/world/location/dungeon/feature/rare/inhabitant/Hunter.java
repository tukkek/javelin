package javelin.model.world.location.dungeon.feature.rare.inhabitant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * Hunters provide aid for harder {@link DungeonFloor#encounters} and make
 * farming more convenient.
 *
 * @author alex
 */
public class Hunter extends Inhabitant{
  static final Option HUNT=new Option("Join the hunt",0,'j');
  static final Option HIRE=new Option("Hire hunter",0,'h');
  static final Option ATTACK=new Option("Attack hunter",0,'a');

  class Hunt extends RandomDungeonEncounter{
    public Hunt(){
      super(Dungeon.active);
      enemies=new Combatants();
      bribe=false;
      hide=false;
    }

    void start(){
      throw new StartBattle(this);
    }

    @Override
    public boolean onend(){
      var s=Fight.state;
      Squad.active.remove(inhabitant);
      if(s.dead.contains(inhabitant)) remove();
      else if(Fight.victory){
        hunt=null;
        game=null;
      }
      return super.onend();
    }

    @Override
    public Combatants generate(ArrayList<Combatant> blueteam){
      return enemies;
    }
  }

  class Screen extends SelectScreen{
    Screen(){
      super("This %s hunter is after %s game.".formatted(
          inhabitant.toString().toLowerCase(),
          game.source.toString().toLowerCase()),null);
    }

    @Override
    public String getCurrency(){
      return "";
    }

    @Override
    public String printinfo(){
      var g=Javelin.format(Squad.active.gold);
      return hire()?"You have $%s.".formatted(g):"";
    }

    @Override
    public List<Option> getoptions(){
      var choices=new ArrayList<>(List.of(HUNT,ATTACK));
      if(hire()){
        HIRE.name="Hire hunter ($%s/day)".formatted(Javelin.format(pay()));
        choices.add(HIRE);
      }
      return choices;
    }

    @Override
    public boolean select(Option o){
      if(o==ATTACK){
        var h=new Hunt();
        h.enemies.add(Hunter.this.inhabitant);
        h.start();
      }else if(o==HIRE&&Squad.active.pay(pay())){
        Hunter.this.remove();
        Squad.active.members.add(inhabitant);
        stayopen=false;
      }else if(o==HUNT){
        var h=new Hunt();
        h.enemies.addAll(hunt.generate());
        Squad.active.add(inhabitant);
        h.start();
      }
      return true;
    }

    @Override
    public String printpriceinfo(Option o){
      return "";
    }
  }

  Combatants hunt=null;
  Combatant game=null;

  /** Constructor. */
  public Hunter(DungeonFloor f){
    super(f.level-1,f.level+1,"hunter",f);
    inhabitant.automatic=true;
  }

  Combatants track(){
    var encounters=new ArrayList<>(
        Dungeon.active.encounters.stream().filter(e->e!=null).toList());
    encounters.sort(Comparator.comparing(Combatants::getel));
    if(encounters.isEmpty()) return null;
    if(hunt==null){
      hunt=encounters.get(RPG.high(0,encounters.size()-1));
      game=hunt.stream().reduce((a,b)->a.source.cr>b.source.cr?a:b)
          .orElseThrow();
    }
    return hunt;
  }

  @Override
  public boolean activate(){
    Fountain.heal(inhabitant);//TODO damage and healing?
    if(track()==null){
      var message="The hunter leaves to find a better hunting ground...";
      Javelin.message(message,true);
      remove();
      return false;
    }
    new Screen().show();
    return true;
  }

  boolean hire(){
    var d=Skill.DIPLOMACY;
    return Squad.active.getbest(d).taketen(d)>=diplomacydc;
  }

  int pay(){
    return MercenariesGuild.getfee(inhabitant.source);
  }
}
