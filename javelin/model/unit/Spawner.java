package javelin.model.unit;

import javelin.controller.BattleSetup;
import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Rush;

/**
 * Special type of unit (more like a building) used in {@link Rush}es.
 * 
 * @author alex
 */
public class Spawner extends Monster {
	/** Type of unit to spawn. */
	public Monster summon;
	/** Counter used to spawn units for the computer player. */
	public float mana = 0;

	/**
	 * @param summon
	 *            Type of monster this spawner summons.
	 * @param blueteam
	 *            <code>true</code> if should draw a blue avatar.
	 */
	@SuppressWarnings("deprecation")
	public Spawner(Monster summon, boolean blueteam) {
		this.summon = summon;
		name = "Summoner (" + summon + ")";
		avatarfile = blueteam ? "spawnerblue" : "spawnerred";
		challengerating = summon.challengerating;
		size = Monster.HUGE;
		group = "Spawner";
		type = "Construct";
		initiative = 0;
		int cr = Math.max(1, Math.round(challengerating) / 2);
		ac = 10 + cr;
		hd.add(cr, 10, 0);

		strength = 0;
		dexterity = 0;
		constitution = 0;
		intelligence = 0;
		wisdom = 0;
		charisma = 0;

		fort = cr;
		ref = -5;
		will = -5;
	}

	/**
	 * @return A new unit out of this source.
	 */
	public Combatant getcombatant() {
		Combatant spawner = new Combatant(clone(), false);
		spawner.ap = Float.MAX_VALUE;
		spawner.initialap = 0;
		spawner.hp = 10 * Math.round(challengerating);
		spawner.maxhp = spawner.hp;
		return spawner;
	}

	/**
	 * @param spawner
	 *            Brings forth a spawn.
	 * @return
	 * @see #summon
	 */
	public Combatant spawn(Combatant spawner, Float currentturn) {
		Combatant summon = new Combatant(this.summon.clone(), true);
		Point near = BattleSetup.getrandompoint(Fight.state,
				new Point(spawner.location[0], spawner.location[1]));
		BattleSetup.add(summon, near);
		summon.rollinitiative();
		if (currentturn == null) {
			Fight.state.next();
			currentturn = Fight.state.next.ap;
		}
		summon.ap = Math.max(summon.ap + currentturn, currentturn);
		return summon;
	}
}
