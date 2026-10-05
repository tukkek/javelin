package javelin.model.world.location.dungeon.feature.common;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.old.RPG;

/**
 * Representes a semi-permanent safe place to rest inside a
 * {@link DungeonFloor}. May not last forever and total safety isn't 100%
 * guaranteed!
 *
 * @author alex
 */
public class Campfire extends Feature{
  static final String PROMPT="This room seems safe to rest in. Do you want to set up camp?\n"
      +"Press ENTER to camp, any other key to cancel...";

  /** Constructor. */
  public Campfire(DungeonFloor f){
    super("campfire");
    remove=false;
  }

  /** @return If <code>true</code>, will {@link #remove()}. */
  protected boolean compromise(){
    return RPG.chancein(20);
  }

  @Override
  public boolean activate(){
    if(Javelin.prompt(PROMPT)!='\n') return false;
    WorldMove.abort=true;
    if(compromise()){
      remove();
      Javelin.message("This safe resting spot has been compromised!",true);
      throw new StartBattle(Dungeon.active.dungeon.fight());
    }
    Lodge.rest(1,Lodge.RESTPERIOD,true,Lodge.LODGE);
    return true;
  }

}
