package javelin.model.world.location.town.diplomacy.quest.find;

import javelin.model.item.Item;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.diplomacy.quest.fetch.FetchQuest;

/**
 * A loosely-aggregated type of {@link Quest#LONG}-{@link Quest#duration} quest.
 * Similar to {@link FetchQuest}s but do not revolve around {@link Item}s.
 *
 * @author alex
 */
public abstract class FindQuest extends Quest{
  @Override
  protected void define(Town t){
    super.define(t);
    duration=SHORT;
  }
}
