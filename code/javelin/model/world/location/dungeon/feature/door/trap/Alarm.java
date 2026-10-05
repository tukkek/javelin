package javelin.model.world.location.dungeon.feature.door.trap;

import javelin.Javelin;
import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.door.Door;

/** {@value #MESSAGE} */
public class Alarm extends DoorTrap{
  /** Singleton. */
  public static final DoorTrap INSTANCE=new Alarm();

  static final String MESSAGE="Opening the door causes a loud noise!";

  private Alarm(){
    // prevent instantiation
  }

  @Override
  public void generate(Door d){
    // activated on opening
  }

  @Override
  public void activate(Combatant opening){
    Javelin.message(MESSAGE,false);
    throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
  }
}
