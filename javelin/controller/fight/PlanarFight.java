package javelin.controller.fight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.fight.tournament.CrIterator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Ultimate game challenge, in which a player is challenged by a team of their
 * own encounter level. Enemies are upgraded following the {@link Upgrade} list
 * of a single {@link Town} to present a thematic fluff.
 * 
 * @see Key
 * @see Haxor
 * 
 * @author alex
 */
public class PlanarFight extends Fight {
	/** See {@link Haxor#rubies}. */
	public static final int RUPEES = 4;

	private static final int MAXTRIES = 1000;

	final Key key;
	private HashSet<Upgrade> path;

	public PlanarFight(Key k) {
		texture = IncursionFight.INCURSIONTEXTURE;
		meld = true;
		hide = false;
		bribe = false;
		this.key = k;
		UpgradeHandler.singleton.gather();
		switch (key.color) {
		case WATER:
			path = UpgradeHandler.singleton.water;
			break;
		case EVIL:
			path = UpgradeHandler.singleton.evil;
			break;
		case EARTH:
			path = UpgradeHandler.singleton.earth;
			break;
		case MAGICAL:
			path = UpgradeHandler.singleton.magic;
			break;
		case FIRE:
			path = UpgradeHandler.singleton.fire;
			break;
		case WIND:
			path = UpgradeHandler.singleton.wind;
			break;
		case GOOD:
			path = UpgradeHandler.singleton.good;
			break;
		default:
			throw new RuntimeException("Unknown key type " + key);
		}
	}

	static public void dontflee(BattleScreen s) {
		Game.message("Cannot flee!", null, Delay.BLOCK);
		s.checkblock();
		throw new RepeatTurn();
	}

	@Override
	public int getel(int teamel) {
		return teamel;
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		int size = RPG.r(5 - 2, 5 + 2);
		ArrayList<Combatant> opponents = new ArrayList<Combatant>();
		for (int i = 0; i < size; i++) {
			opponents
					.add(new Combatant(null,
							RPG.pick(Javelin.MONSTERSBYCR
									.get(determinecr(teamel, size))).clone(),
							true));
		}
		// opponents = organizeopponents(opponents);
		for (int i = 0; i < size; i++) {
			Combatant opponent = opponents.get(i);
			key.color.baptize(opponent);
		}
		int i = 0;
		while (ChallengeRatingCalculator.calculateel(opponents) < teamel) {
			Combatant weakest = findweakest(opponents);
			int j = 0;
			while (true) {
				Upgrade u = RPG.pick(new ArrayList<Upgrade>(path));
				if (u.upgrade(weakest.clonedeeply())) {
					u.upgrade(weakest);
					break;
				}
				j += 1;
				if (j > MAXTRIES) {
					return getmonsters(teamel);
				}
				continue;
			}
			ChallengeRatingCalculator.calculateCr(weakest.source);
			i += 1;
			if (i > MAXTRIES) {
				return getmonsters(teamel);
			}
		}
		return opponents;
	}

	@Override
	public String dealreward() {
		Haxor.singleton.rubies += RUPEES;
		return "You have won the planar fight, you receive 4 Haxor rubies!\n"
				+ super.dealreward();
	}

	Combatant findweakest(ArrayList<Combatant> opponents) {
		opponents.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				if (o1.source.challengeRating == o2.source.challengeRating) {
					return 0;
				}
				if (o1.source.challengeRating > o2.source.challengeRating) {
					return 1;
				}
				return -1;
			}
		});
		return opponents.get(0);
	}

	float determinecr(int teamel, int size) {
		List<Float> possiblecrs = new ArrayList<Float>();
		for (Monster m : new CrIterator(Javelin.MONSTERSBYCR)) {
			ArrayList<Combatant> opponents = new ArrayList<Combatant>();
			for (int i = 0; i < size; i++) {
				opponents.add(new Combatant(null, m, true));
			}
			try {
				if (ChallengeRatingCalculator.calculateelsafe(opponents) < teamel) {
					possiblecrs.add(m.challengeRating);
				}
			} catch (UnbalancedTeams e) {
				continue;
			}
		}
		return RPG.pick(possiblecrs);
	}

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		PlanarFight.dontflee(screen);
	}
}
