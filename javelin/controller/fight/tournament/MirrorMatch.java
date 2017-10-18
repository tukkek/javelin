package javelin.controller.fight.tournament;

import java.util.ArrayList;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.ExhibitionFight;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;

/**
 * Fight against your own squad.
 * 
 * @author alex
 */
public class MirrorMatch extends Match {
	/** Constructor. */
	public MirrorMatch() {
		super();
		name = "Mirror match";
	}

	@Override
	public void start() {
		throw new StartBattle(new ExhibitionFight() {
			@Override
			public ArrayList<Combatant> getmonsters(Integer teamel) {
				ArrayList<Combatant> monsters = new ArrayList<Combatant>();
				for (Combatant c : Squad.active.members) {
					c = c.clone();
					c.clonesource();
					c.source.customName = null;
					c.newid();
					monsters.add(c);
				}
				return monsters;
			}
		});
	}
}
