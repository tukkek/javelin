package javelin.model.world.location.town.labor.religious;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/**
 * Will cast Rituals (certain {@link Spell}s) for gold.
 *
 * @see Spell#isritual
 * @author alex
 */
public class Shrine extends Fortification{
  class UpgradeShrine extends BuildingUpgrade{
    public UpgradeShrine(Shrine s){
      super("",5,+5,s,Rank.VILLAGE);
      name="Upgrade shrine";
    }

    @Override
    public Location getgoal(){
      return previous;
    }

    @Override
    public void done(){
      super.done();
      var s=(Shrine)previous;
      s.level=2;
      s.fill();
    }
  }

  /**
   * Spells that are castable on shrines.
   *
   * @see Spell#isritual
   */
  public static final List<Spell> RITUALS=new ArrayList<>();

  static{
    for(var s:Spell.SPELLS) if(s.isritual) RITUALS.add(s);
  }

  /**
   * {@link Town} {@link Labor}.
   *
   * @author alex
   */
  public static class BuildShrine extends Build{
    Shrine s;

    /** Constructor. */
    public BuildShrine(){
      super(null,5,Rank.HAMLET,null);
    }

    @Override
    protected void define(){
      super.define();
      s=new Shrine(1);
      name="Build "+s.descriptionknown.toLowerCase();
    }

    @Override
    public Location getgoal(){
      return s;
    }

    @Override
    public boolean validate(District d){
      return super.validate(d)
          &&d.getlocationtype(Shrine.class).size()<d.town.getrank().rank
          &&s.rituals.get(0).casterlevel<=d.town.population;
    }
  }

  /** Rituals are spells that this shrine will cast for a fee. */
  public ArrayList<Spell> rituals=new ArrayList<>(2);
  int level=1;

  /** Constructor. */
  public Shrine(){
    this(2);
  }

  /** Constructor. */
  public Shrine(int level){
    super(null,"A shrine",0,0);
    this.level=level;
    discard=false;
    gossip=true;
    fill();
  }

  void update(){
    var cl=rituals.get(0).casterlevel;
    if(level==1){
      minlevel=maxlevel=cl;
      descriptionknown="A shrine ("+rituals.get(0).name.toLowerCase()+")";
      return;
    }
    if(price(0)>price(1)){
      var swap=new ArrayList<Spell>();
      swap.add(rituals.get(1));
      swap.add(rituals.get(0));
      rituals.clear();
      rituals.addAll(swap);
    }
    minlevel=cl;
    maxlevel=rituals.get(1).casterlevel;
    descriptionknown="A shrine ("+rituals.get(0).name.toLowerCase()+", "
        +rituals.get(1).name.toLowerCase()+")";
  }

  void fill(){
    while(rituals.size()<level){
      var ritual=RPG.pick(RITUALS);
      if(!rituals.contains(ritual)) rituals.add(ritual);
    }
    update();
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    var output="You enter a shrine. \"What can we do for you today?\", says the "
        +(RPG.r(1,2)==1?"priest":"priestess")+".\n";
    output+="\n1 - "+rituals.get(0).name+" ($"+price(0)+")";
    if(level>1) output+="\n2 - "+rituals.get(1).name+" ($"+price(1)+")";
    output+="\nq - Quit for now ";
    output+="\n\nSelect an option.";
    var screen=new InfoScreen(output);
    Javelin.app.switchScreen(screen);
    processinput();
    return true;
  }

  void processinput(){
    char input=InfoScreen.feedback();
    if(input=='1') service(0);
    else if(input=='2'&&level>1) service(1);
    else if(input=='q'){}else processinput();
  }

  boolean service(int slot){
    var price=price(slot);
    var s=rituals.get(slot);
    var squad=Squad.active;
    if(price>squad.gold) return false;
    Combatant target=null;
    if(s.castonallies){
      var i=Javelin.choose("Cast on who?",squad.members,true,false);
      if(i==-1) return false;
      target=squad.members.get(i);
    }
    if(!s.validate(null,target)) return false;
    squad.gold-=price;
    var message=s.castpeacefully(null,target);
    if(message!=null) Javelin.message(message,true);
    return true;
  }

  private int price(int i){
    var ritual=rituals.get(i);
    return ritual.level*ritual.casterlevel*10+ritual.components;
  }

  @Override
  public List<Combatant> getcombatants(){
    return garrison;
  }

  @Override
  public ArrayList<Labor> getupgrades(District d){
    var upgrades=super.getupgrades(d);
    if(level==1) upgrades.add(new UpgradeShrine(this));
    return upgrades;
  }
}
