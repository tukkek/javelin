package javelin.controller;

import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.exception.TooDeepException;
import javelin.model.unit.Monster;

public class TeamPopulator {
	final TreeMap<String, Integer> hits = new TreeMap<String, Integer>();
	public static final TeamPopulator SINGLETON = new TeamPopulator();

	private TeamPopulator() {
		// prevents instantiation
	}

	// public int fillTeams() throws GaveUpException {
	// final Float[] crs = getAllAvaiableCrs();
	// final int floor = ChallengeRatingCalculator.elFromCr(crs[0]);
	// /**
	// * avoid the 1st CR, we`re never getting it as an EL if there are no 1
	// * monster groups.SINGLETON.hits
	// */
	// final int el = Roller.rollDie(6
	// + ChallengeRatingCalculator.elFromCr(crs[crs.length - 1])
	// - floor)
	// + floor - 1;
	// fillTeams(el);
	// return el;
	// }

	// private void fillTeams(final int el) throws GaveUpException {
	// for (final List<Monster> team : new List[] { BattleMap.blueTeam,
	// BattleMap.redTeam }) {
	// fillTeam(el, team);
	// }
	// }

	// public void fillTeam(final int el, final List<Monster> team)
	// throws GaveUpException {
	// final Float[] allCrs = getAllAvaiableCrs();
	// boolean ok = false;
	// team: for (int maxtries = 0; maxtries < 1000; maxtries++) {
	// List<MonsterGroup> monsterGroups;
	// final ArrayList<MonsterGroup> list = new ArrayList<MonsterGroup>();
	// try {
	// monsterGroups = new MonsterGroup(0).getAll(list);
	// } catch (final TooDeepException e) {
	// monsterGroups = list;
	// continue;
	// }
	// float nMonsters = 0;
	// for (final MonsterGroup mg : monsterGroups) {
	// nMonsters += mg.number;
	// }
	// /**
	// * TODO externalize min/max?
	// */
	// if (nMonsters > 7 || nMonsters < 1) {
	// continue;
	// }
	// final int[] crs = new int[monsterGroups.size()];
	// for (int i = 0; i < monsterGroups.size(); i++) {
	// crs[i] = allCrs.length - 1;
	// }
	// for (int tries = 0; tries < 1000; tries++) {
	// team.clear();
	// for (int i = 0; i < crs.length; i++) {
	// final List<Monster> candidate = Javelin.monsters
	// .get(allCrs[crs[i]]);
	//
	// if (candidate != null) {
	// for (int j = 0; j < monsterGroups.get(i).number; j++) {
	// team.add(RPG.pick(candidate).clone(true));
	// }
	// }
	// }
	// final int currentEl;
	// try {
	// currentEl = ChallengeRatingCalculator.calculateEl(team);
	// } catch (final UnbalancedTeamsException e) {
	// continue;
	// }
	// if (currentEl == el) {
	// ok = true;
	// break team;
	// }
	// final int limit, increment;
	// if (currentEl < el) {
	// limit = allCrs.length - 1;
	// increment = 1;
	// } else {
	// limit = 0;
	// increment = -1;
	// }
	// int index;
	// int tries2 = 0;
	// do {
	// if (tries2++ == 1000) {
	// continue team;
	// }
	// index = Roller.rollDie(crs.length) - 1;
	// } while (crs[index] == limit);
	//
	// crs[index] += increment;
	// continue;
	// }
	// /**
	// * if after all those tries we still haven`t found our EL it`s
	// * probably cause we can`t with these monster groups.
	// */
	// continue;
	// }
	//
	// if (!ok) {
	// throw new GaveUpException();
	// }
	//
	// for (final Monster m : team) {
	// updateMessageMap(hits, m);
	// }
	// }

	private Float[] getAllAvaiableCrs() {
		return Javelin.MONSTERS.keySet().toArray(new Float[0]);
	}

	private void updateMessageMap(final TreeMap<String, Integer> map,
			final Monster m) {
		final String message = "CR" + m.challengeRating;
		final Integer old = map.get(message);

		map.put(message, old == null ? 1 : old + 1);
	}

	public class MonsterGroup {
		final public Integer number;
		final public MonsterGroup[] composite;

		public MonsterGroup(final int depth) throws TooDeepException {
			if (depth == 10) {
				throw new TooDeepException();
			}
			switch (Roller.rollDie(4)) {
			case 1:
				number = Roller.rollDie(6) + 1;
				composite = null;
				break;
			case 2:
			case 3:
				number = null;
				final int d = depth + 1;
				composite = new MonsterGroup[] { new MonsterGroup(d),
						new MonsterGroup(d) };
				break;
			case 4:
				number = 1;
				composite = null;
				break;
			default:
				throw new RuntimeException("Unexpected value");
			}
		}

		@Override
		public String toString() {
			return String.valueOf(number);
		}

		public List<MonsterGroup> getAll(final List<MonsterGroup> mgs) {
			if (composite == null) {
				mgs.add(this);
			} else {
				for (final MonsterGroup mg : composite) {
					mg.getAll(mgs);
				}
			}

			return mgs;
		}
	}

	// public static void main(final String[] args) throws GaveUpException {
	// final TreeMap<Long, String> orderesResults = new TreeMap<Long, String>();
	// for (int max = 7; max < 11; max++) {
	// for (int el = -4; el < 25; el++) {
	// for (int i = 0; i < 10; i++) {
	// SINGLETON.fillTeams(el);
	//
	// for (final List<Monster> team : new List[] { Map.blueTeam,
	// Map.redTeam }) {
	// team.clear();
	// }
	// }
	// }
	//
	// final double[] eval = new double[SINGLETON.hits.values().size()];
	// int i = 0;
	// for (final int value : SINGLETON.hits.values()) {
	// eval[i++] = value;
	// }
	//
	// final long variance = Math.round(new Variance().evaluate(eval));
	// final String result = "Variance: " + variance + " (min" + 1
	// + ",max" + 7 + ").\tHits: " + SINGLETON.hits;
	// System.out.println(result);
	// orderesResults.put(variance, result);
	// SINGLETON.hits.clear();
	// }
	//
	// System.out.println("\nResults");
	// for (final String result : orderesResults.values()) {
	// System.out.println(result);
	// }
	// }
}
