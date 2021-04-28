package javelin.model.world.location.town.quest.find;

import javelin.model.item.Item;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.quest.Quest;
import javelin.model.world.location.town.quest.fetch.FetchQuest;

/**
 * A loosely-aggregated type of {@link Quest#LONG}-{@link Quest#term} quest.
 * Similar to {@link FetchQuest}s but do not revolve around {@link Item}s.
 *
 * @author alex
 */
public class FindQuest extends Quest{
	protected FindQuest(Town t){
		super(t);
		term=LONG;
	}
}
