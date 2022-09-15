package javelin.model.world.location.dungeon.feature.rare;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.temple.EarthTemple;
import javelin.model.world.location.dungeon.feature.Feature;

/**
 * @see EarthTemple
 * @author alex
 */
public class FruitTree extends Feature{
  static final String PROMPT="Do you want to pick a fruit from the tree?\n"
      +"Press ENTER to pick or any other key to cancel...";

  /** Constructor. */
  public FruitTree(DungeonFloor f){
    super("fruit tree");
  }

  @Override
  public boolean activate(){
    if(Javelin.prompt(PROMPT)!='\n') return false;
    ArrayList<Combatant> squad=Squad.active.members;
    var names=new ArrayList<String>(squad.size());
    for(Combatant c:squad)
      names.add(c+" ("+String.join("\n",c.printstatus(null))+")");
    var choice=Javelin.choose("Who will eat the fruit?",names,true,false);
    if(choice<0) return false;
    Fountain.heal(squad.get(choice));
    return true;
  }

}
