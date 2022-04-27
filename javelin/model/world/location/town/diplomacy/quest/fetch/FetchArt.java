package javelin.model.world.location.town.diplomacy.quest.fetch;

import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.chest.ArtDisplay;
import javelin.model.world.location.town.labor.Trait;

/**
 * @see Trait#RELIGIOUS
 * @see Dungeon#bonus
 * @author alex
 */
public class FetchArt extends FetchQuest{
  /** Constructor. */
  public FetchArt(){
    super(ArtDisplay.class);
  }
}
