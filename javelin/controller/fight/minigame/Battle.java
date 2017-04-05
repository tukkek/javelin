package javelin.controller.fight.minigame;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.map.plain.Field;
import javelin.model.unit.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.unique.minigame.Battlefield;

/**
 * Not to be confused with the generic battle controller {@link Fight}.
 * 
 * @see Battlefield
 * @author alex
 */
public class Battle extends Minigame {
	ArrayList<Combatant> blueteam;
	ArrayList<Combatant> monsters;

	/**
	 * @param blueteam
	 *            Allied team.
	 * @param monsters
	 *            Opponents.
	 */
	public Battle(ArrayList<Combatant> blueteam, ArrayList<Combatant> monsters) {
		this.blueteam = blueteam;
		this.monsters = monsters;
		this.map = new Field();
	}

	@Override
	public boolean onend() {
		if (!victory) {
			Javelin.message("You lost...", true);
			return false;
		}
		CountingSet counter = new CountingSet();
		counter.casesensitive = true;
		for (Combatant c : state.blueTeam) {
			if (!c.summoned) {
				counter.add(c.source.toString());
			}
		}
		Battlefield b = (Battlefield) World.getall(Battlefield.class).get(0);
		b.survivors.clear();
		for (String monstertype : counter.getelements()) {
			b.survivors.put(monstertype, counter.getcount(monstertype));
		}
		final String text = "You won!\n"
				+ "The survivors will be available for you to recruit at the battlefield location.";
		Javelin.message(text, true);
		return false;
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return monsters;
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return blueteam;
	}
}
