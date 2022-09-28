package javelin.model.world.location.dungeon.feature.rare;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.old.RPG;

/**
 * Allows a {@link Combatant} to learn a set of {@link Upgrade}s.
 *
 * Initially selected {@link Upgrade}s on-the-fly to ensure at least one was
 * available but that turned out poorly, especially for {@link DungeonDelve}
 * where the player depends on Stones to advance units. Now randomly adds
 * upgrades from one of the predefined sets in {@link UpgradeHandler}, making it
 * a strategic gamble as well.
 *
 * @author alex
 */
public class LearningStone extends Feature{
  static final String TAKE="Take the stone as treasure";

  /**
   * Once a learning stone is removed from its pedestal, it can be sold as
   * treasure.
   *
   * @author alex
   */
  public class InertLearningStone extends Item{
    InertLearningStone(){
      super("Inert learning stone",gold,false);
      usedinbattle=false;
      usedoutofbattle=false;
    }
  }

  /**
   * <code>false</code> before {@link Feature#activate()} so that
   * {@link #toString()} won't spoil the {@link Kit} from far away.
   */
  protected boolean revealed=false;

  List<Upgrade> upgrades=new ArrayList<>();
  final String type;
  /** {@link Difficulty#MODERATE} value in gold. */
  int gold;

  /** Constructor. */
  public LearningStone(DungeonFloor f){
    super("learning stone");
    remove=false;
    gold=RewardCalculator.getgold(Math.max(1,f.level-4));
    var kit=RPG.pick(Kit.KITS);
    upgrades.addAll(kit.getupgrades());
    type=kit.name.toLowerCase();
  }

  @Override
  public boolean activate(){
    if(Javelin.prompt("Will you use this "+getname()+"?\n"
        +"Press ENTER to confirm or any other key to cancel...")!='\n')
      return false;
    revealed=true;
    WorldMove.abort=true;
    var options=Squad.active.members.stream().filter(c->!c.mercenary)
        .map(c->c+" ("+c.gethumanxp()+")").collect(Collectors.toList());
    if(remove) options.add(TAKE);
    var prompt="This is a "+getname()+". Who will touch it?";
    var choice=Javelin.choose(prompt,options,true,false);
    if(choice<0) return false;
    if(choice==options.indexOf(TAKE)){
      new InertLearningStone().grab();
      return true;
    }
    var student=Squad.active.members.get(choice);
    AdventurersGuild.train(student,upgrades,student.xp.floatValue());
    return true;
  }

  String getname(){
    var name="learning stone";
    if(revealed) name+=" ("+type+")";
    return name;
  }

  static boolean accept(Upgrade u){
    for(var c:Squad.active.members) if(u.validate(c,false)) return true;
    return false;
  }

  @Override
  public String toString(){
    return "Learning stone ("+type+")";
  }
}
