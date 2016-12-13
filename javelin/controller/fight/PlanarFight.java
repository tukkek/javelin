package javelin.controller.fight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
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

/**
 * Ultimate game challenge, in which a player is challenged by a team of their
 * own encounter level. Enemies are upgraded following the {@link Upgrade} list
 * of a single {@link Town} to present a thematic fluff.
 * 
 * TODO throw this away when temples are made, create TempleFight. Try to share
 * stuff with MercenariesGuild
 * 
 * @see Key
 * @see Haxor
 * 
 * @author alex
 */
class PlanarFight extends Fight {
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
		path = new HashSet<Upgrade>(
				UpgradeHandler.singleton.getfullupgrades(key.color));
	}

	@Override
	public int getel(int teamel) {
		return teamel;
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		int size = RPG.r(5 - 2, 5 + 2);
		ArrayList<Combatant> opponents = new ArrayList<Combatant>();
		for (int i = 0; i < size; i++) {
			opponents
					.add(new Combatant(
							RPG.pick(Javelin.MONSTERSBYCR
									.get(determinecr(teamel, size))).clone(),
							true));
		}
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
				if (u.upgrade(weakest.clone().clonesource())) {
					u.upgrade(weakest);
					if (u.purchaseskills) {
						weakest.source.purchaseskills(u).upgradeautomatically();
					}
					break;
				}
				j += 1;
				if (j > MAXTRIES) {
					return getmonsters(teamel);
				}
				continue;
			}
			ChallengeRatingCalculator.calculatecr(weakest.source);
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
				if (o1.source.challengerating == o2.source.challengerating) {
					return 0;
				}
				if (o1.source.challengerating > o2.source.challengerating) {
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
				opponents.add(new Combatant(m, true));
			}
			try {
				if (ChallengeRatingCalculator
						.calculateelsafe(opponents) < teamel) {
					possiblecrs.add(m.challengerating);
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
