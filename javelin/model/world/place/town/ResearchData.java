package javelin.model.world.place.town;

import java.io.Serializable;

import javelin.controller.upgrade.Upgrade;
import javelin.model.world.place.town.research.Grow;
import javelin.model.world.place.town.research.Research;
import javelin.view.screen.town.option.RecruitOption;

/**
 * Utility container for a {@link Town} to hold a {@link Research} {@link #hand}
 * and a {@link ResearchQueue}.
 * 
 * @author alex
 */
public class ResearchData implements Serializable {
	public static final int NATIVEUPGRADE = 1;

	public static final int MONSTERLAIR = 5;

	/**
	 * Represents a few {@link Research} options that the player or
	 * {@link #automanage}r can use to advance a town. Grow should always be the
	 * first option.
	 * 
	 * A value of <code>null</code> means that the hand is initially empty, that
	 * it's card has been spent or that there are no more valid cards of that
	 * type.
	 * 
	 * These are the card types by index for non-hostile (human) {@link Town}s:
	 * 
	 * 0 - {@link Grow}
	 * 
	 * 1 - Native (from {@link #realm}) {@link Upgrade}.
	 * 
	 * 2 - Foreign {@link Upgrade}
	 * 
	 * 3 - Native {@link Item}.
	 * 
	 * 4 - Foreign {@link Item}
	 * 
	 * 5 - Native {@link Monster} lair, see {@link #lairs}
	 * 
	 * 6 - Special card
	 * 
	 * 7 - {@link Accommodations} upgrade.
	 * 
	 * 8 - {@link Transport} upgrade.
	 * 
	 * These are the card types by index for hostile (monster) {@link Town}s:
	 * 
	 * 0 - {@link Grow}
	 * 
	 * 1 - Native {@link Monster} lair, see {@link #lairs}
	 * 
	 * 2-7 - {@link RecruitOption} for it to recruit an unit into
	 * {@link Town#garrison}
	 * 
	 * @see Research#draw(Town)
	 */
	public Research[] hand = new Research[9];

	/**
	 * @see ResearchQueue
	 */
	public ResearchQueue queue = new ResearchQueue();

}