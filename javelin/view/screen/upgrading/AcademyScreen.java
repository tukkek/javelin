package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;

/**
 * @see Academy
 * @author alex
 */
public class AcademyScreen extends UpgradingScreen{
  static final Option WAIT=new Option("Wait until next training is completed",0,
      'w',100);

  /** {@link Location} being represented. */
  protected Academy academy;

  /** Constructor. */
  public AcademyScreen(Academy academy,Town t){
    super(academy.descriptionknown,t);
    this.academy=academy;
    stayopen=true;
    //TODO skip confirmation since it's always 1 hero
  }

  @Override
  protected void registertrainee(Order trainee){
    academy.training.add(trainee);
    var c=((TrainingOrder)trainee).trained;
    var s=Squad.active;
    s.equipment.remove(c);
    s.remove(c);
  }

  @Override
  protected void onexit(ArrayList<TrainingOrder> trainees){
    var s=Squad.active;
    if(s.members.size()!=trainees.size()) return;
    academy.stash+=s.gold;
    var p=academy.parking;
    if(p==null||s.transport.price>p.price) academy.parking=s.transport;
  }

  @Override
  protected ArrayList<Upgrade> getupgrades(){
    return new ArrayList<>(academy.upgrades);
  }

  @Override
  public String printinfo(){
    var g="Your squad currently has $%s."
        .formatted(Javelin.format(Squad.active.gold));
    var training="";
    if(!academy.training.queue.isEmpty())
      training="Currently training: %s.".formatted(academy.training);
    return "%s %s".formatted(g,training);
  }

  @Override
  public TrainingOrder createorder(Combatant c,Combatant original,float xpcost){
    return new TrainingOrder(c,Squad.active.equipment.get(c),c.toString(),
        xpcost,original);
  }

  @Override
  public ArrayList<Combatant> gettrainees(){
    return Squad.active.members;
  }

  @Override
  public int getgold(){
    return Squad.active.gold;
  }

  @Override
  public void pay(int goldpieces){
    Squad.active.gold-=goldpieces;
  }

  @Override
  public List<Option> getoptions(){
    var options=super.getoptions();
    if(!academy.training.isempty()){
      WAIT.key='w';
      if(options.stream().filter(o->o.key==WAIT.key).findAny().isPresent())
        WAIT.key='W';
      options.add(WAIT);
    }
    return options;
  }

  @Override
  public boolean select(Option op){
    if(op==WAIT){
      var done=academy.training.next().completionat;
      var s=Squad.active;
      s.delay(done-s.gettime());
      stayopen=false;
      return true;
    }
    return super.select(op);
  }
}
