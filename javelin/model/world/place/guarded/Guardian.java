package javelin.model.world.place.guarded;

import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Siege;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * Single creature that guards an {@link Item}.
 * 
 * @author alex
 */
public class Guardian extends GuardedPlace {
	/** Debug helper. */
	public static final boolean DEBUG = false;
	private Item loot;

	/** Constructor. */
	public Guardian() {
		super(null, null, 0, 0);
		descriptionknown = "A guardian (" + loot.toString().toLowerCase() + ")";
		descriptionunknown = "A guardian";
		discard = true;
	}

	@Override
	protected void generategarrison(int minel, int maxel) {
		while (garrison.isEmpty()) {
			loot = RPG.pick(Item.ALL);
			int cr = RewardCalculator.getcr(loot.price);
			Combatant c = findnativemonster(cr, this);
			if (c != null) {
				garrison.add(c);
				targetel = new Integer(ChallengeRatingCalculator.elFromCr(cr));
			}
		}
	}

	/**
	 * @param cr
	 *            Given a challenge rating target...
	 * @param spot
	 *            and a world location...
	 * @return a monster from that location and that challenge rating or
	 *         <code>null</code> if couldn't find one.
	 */
	static public Combatant findnativemonster(float cr, WorldPlace spot) {
		String[] terrains = Javelin.terrains(Javelin.terrain(spot.x, spot.y));
		while (true) {
			if (Javelin.MONSTERSBYCR.descendingKeySet().first() < cr) {
				return null;
			}
			List<Monster> candidates = Javelin.MONSTERSBYCR.get(cr);
			if (candidates != null) {
				Collections.shuffle(candidates);
				for (Monster m : candidates) {
					for (String terrain : terrains) {
						if (m.terrains.contains(terrain)) {
							return new Combatant(null, m.clone(), true);
						}
					}
				}
			}
			cr += 1;
		}
	}

	@Override
	protected Siege fight() {
		return new Siege(this) {
			@Override
			public boolean rewardgold() {
				return false;
			}
		};
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		Town.grab(loot);
		return true;
	}
}
