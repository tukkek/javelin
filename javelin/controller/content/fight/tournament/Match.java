package javelin.controller.content.fight.tournament;

import javelin.controller.content.fight.ExhibitionFight;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.exception.battle.StartBattle;

/**
 * Similar to a {@link RandomEncounter} but has higher difficulty to compensate
 * for {@link ExhibitionFight}s not being fatal.
 *
 * @author alex
 */
public class Match extends Exhibition{
	/** Constructor. */
	public Match(){
		super("Match");
	}

	@Override
	public void start(){
		throw new StartBattle(new ExhibitionFight());
	}
}
