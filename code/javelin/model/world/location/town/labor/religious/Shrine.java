package javelin.model.world.location.town.labor.religious;

import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.consumable.potion.Potion;
import javelin.model.item.consumable.potion.Vaporizer;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.abilities.spell.transmutation.ControlWeather;
import javelin.model.world.World;
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
 * Casts rituals as a service, which are target-less spells
 * ({@link ControlWeather}) or ones that affect the entire party at once
 * ({@link Bless}). Typical rituals are either instantaneous or long-lasting, as
 * anything in-between is going to be wasted while moving around the
 * {@link World#map}.
 *
 * If a {@link Spell#isritual} is also a {@link Potion} (single target), it will
 * be included as a {@link Vaporizer} (area of effect).
 *
 * @author alex
 */
public class Shrine extends Fortification{
  /** @see Spell#isritual */
  public static final List<Ritual> RITUALS=Spell.SPELLS.stream()
      .filter(s->s.isritual).map(Shrine::toritual).toList();

  static final String MENU="""
      You enter a shrine. "What can we do for you today?", says the %s.

      %s
      q - Quit

      You have $%s.
      """.trim();
  static final int CAPACITY=12;

  /** @see Spell#isritual */
  public static class Ritual implements Serializable{
    /** Ritual effect. */
    public Spell spell;
    /** Value of {@link #spell} cast as a service. */
    public int price;

    Ritual(Spell s){
      spell=s;
      price=price(s);
    }

    /** @see Spell#castpeacefully(Combatant, Combatant) */
    public String perform(){
      return spell.castpeacefully(null,null);
    }

    /** @see Spell#validate(Combatant, Combatant) */
    public boolean validate(){
      return spell.validate(null,null);
    }

    @Override
    public String toString(){
      return "Ritual: "+spell.name.toLowerCase();
    }
  }

  static class MassRitual extends Ritual{
    MassRitual(Spell s){
      super(Vaporizer.scale(s));
    }

    @Override
    public String perform(){
      return Squad.active.members.stream().map(m->spell.castpeacefully(null,m))
          .collect(joining(" "));
    }
  }

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

  /** {@link Town} {@link Labor}. */
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
          &&s.rituals.get(0).spell.casterlevel<=d.town.population;
    }
  }

  /** Rituals are spells that this shrine will cast for a fee. */
  List<Ritual> rituals=new ArrayList<>(2);
  int level=1;

  /** Constructor. */
  public Shrine(int level){
    super(null,"A shrine",0,0);
    this.level=level;
    discard=false;
    gossip=true;
    fill();
  }

  /** Constructor. */
  public Shrine(){
    this(2);
  }

  void fill(){
    while(rituals.size()<level){
      var r=RPG.pick(RITUALS);
      if(!rituals.contains(r)) rituals.add(r);
    }
    minlevel=rituals.get(0).spell.casterlevel;
    maxlevel=minlevel;
    if(level>1){
      rituals.sort(Comparator.comparing(r->r.price));
      maxlevel=rituals.get(1).spell.casterlevel;
    }
    var names=rituals.stream().map(r->r.spell.name.toLowerCase())
        .collect(joining(", "));
    descriptionknown="A shrine (%s)".formatted(names);
  }

  boolean service(int slot){
    var r=rituals.get(slot);
    if(!r.validate()||!Squad.active.pay(r.price)) return false;
    var message=r.perform();
    if(message==null) message="The ritual of %s is performed!"
        .formatted(r.spell.name.toLowerCase());
    Javelin.message(message,true);
    return true;
  }

  boolean processinput(){
    char input=InfoScreen.feedback();
    if(input=='q') return true;
    if(input=='1') return service(0);
    if(input=='2'&&level>1) return service(1);
    return false;
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    if(Squad.active.members.size()>CAPACITY){
      var crowded="Only a dozen people will fit inside the shrine at once...";
      Javelin.message(crowded,false);
      return false;
    }
    var priest=x%1==0?"priest":"priestess";
    var rituals=this.rituals.stream()
        .map(r->"%s - %s ($%s)".formatted(this.rituals.indexOf(r)+1,
            r.spell.name,Javelin.format(r.price)))
        .collect(joining("\n"));
    var m=MENU.formatted(priest,rituals,Javelin.format(Squad.active.gold));
    Javelin.app.switchScreen(new InfoScreen(m));
    while(!processinput()) continue;
    return true;
  }

  /** @return Cost for spell to be cast as a service. */
  static public int price(Spell s){
    return s.level*s.casterlevel*10+s.components;
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

  /** @return Proper ritual implementation, for testing. */
  public static Ritual toritual(Spell s){
    return s.ispotion?new MassRitual(s):new Ritual(s);
  }
}
