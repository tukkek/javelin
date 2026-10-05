package javelin.model.world.location.town.diplomacy.quest.fetch;

import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.chest.GemDisplay;
import javelin.model.world.location.town.labor.Trait;

/**
 * @see Trait#NATURAL
 * @see Wilderness#goals
 * @author alex
 */
public class FetchGem extends FetchQuest{
  /** Constructor. */
  public FetchGem(){
    super(GemDisplay.class);
  }
}
