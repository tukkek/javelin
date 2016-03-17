package javelin.controller.fight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.exception.UnbalancedTeamsException;
import javelin.controller.map.Map;
import javelin.controller.tournament.CrIterator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.place.Haxor;
import javelin.model.world.town.Town;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

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
public class PlanarFight implements Fight {

	public class PlanarFightScreen extends BattleScreen {
		public PlanarFightScreen(QuestApp q, BattleMap mapp) {
			super(q, mapp, true);
			Javelin.settexture(IncursionFight.INCURSIONTEXTURE);
		}

		@Override
		protected void withdraw(Combatant combatant) {
			dontflee(this);
		}

		@Override
		public String dealreward() {
			Haxor.singleton.tickets += 2;
			return "You have won the planar fight, you receive 2 Haxor tickets!\n"
					+ super.dealreward();
		}
	}

	private static final int MAXTRIES = 1000;

	final Key key;
	private ArrayList<Upgrade> path;

	public PlanarFight(Key k) {
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
		throw new RepeatTurnException();
	}

	@Override
	public int getel(JavelinApp javelinApp, int teamel) {
		return teamel;
	}

	@Override
	public BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap) {
		return new PlanarFightScreen(javelinApp, battlemap);
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
			opponent.source.customName = Realm.REALMS.get(key.color) + " "
					+ opponent.source.name.toLowerCase();
			opponent.source.customName = Character
					.toUpperCase(opponent.source.customName.charAt(0))
					+ opponent.source.customName.substring(1).toLowerCase();

		}
		int i = 0;
		while (ChallengeRatingCalculator.calculateElSafe(opponents) < teamel) {
			Combatant weakest = findweakest(opponents);
			int j = 0;
			while (true) {
				Upgrade u = RPG.pick(path);
				if (u.apply(weakest.clonedeeply())) {
					u.apply(weakest);
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

	// ArrayList<Combatant> organizeopponents(ArrayList<Combatant> input) {
	// ArrayList<Combatant> output = new ArrayList<Combatant>(input.size());
	// Combatant chosen = input.get(0);
	// for (int i = 1; i < input.size(); i++) {
	// Combatant c = input.get(i);
	// if (c.source.strength > chosen.source.strength) {
	// chosen = c;
	// }
	// }
	// output.add(chosen);
	// input.remove(chosen);
	// chosen = input.get(0);
	// for (int i = 1; i < input.size(); i++) {
	// Combatant c = input.get(i);
	// if (c.source.dexterity > chosen.source.dexterity) {
	// chosen = c;
	// }
	// }
	// output.add(chosen);
	// input.remove(chosen);
	// chosen = input.get(0);
	// for (int i = 1; i < input.size(); i++) {
	// Combatant c = input.get(i);
	// if (c.source.wisdom > chosen.source.wisdom) {
	// chosen = c;
	// }
	// }
	// output.add(chosen);
	// input.remove(chosen);
	// output.add(input.get(0));
	// return output;
	// }

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
				if (ChallengeRatingCalculator.calculateEl(opponents) < teamel) {
					possiblecrs.add(m.challengeRating);
				}
			} catch (UnbalancedTeamsException e) {
				continue;
			}
		}
		return RPG.pick(possiblecrs);
	}

	@Override
	public boolean meld() {
		return true;
	}

	@Override
	public Map getmap() {
		return null;
	}

	@Override
	public boolean friendly() {
		return false;
	}

}
