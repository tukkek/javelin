// package javelin.controller;
//
// import java.util.List;
// import java.util.TreeMap;
//
// import javelin.Javelin;
// import javelin.controller.exception.TooDeepException;
// import javelin.model.unit.Monster;
//
// public class TeamPopulator {
// final TreeMap<String, Integer> hits = new TreeMap<String, Integer>();
// public static final TeamPopulator SINGLETON = new TeamPopulator();
//
// private TeamPopulator() {
// // prevents instantiation
// }
//
// private Float[] getAllAvaiableCrs() {
// return Javelin.MONSTERSBYCR.keySet().toArray(new Float[0]);
// }
//
// private void updateMessageMap(final TreeMap<String, Integer> map,
// final Monster m) {
// final String message = "CR" + m.challengeRating;
// final Integer old = map.get(message);
//
// map.put(message, old == null ? 1 : old + 1);
// }
//
// public class MonsterGroup {
// final public Integer number;
// final public MonsterGroup[] composite;
//
// public MonsterGroup(final int depth) throws TooDeepException {
// if (depth == 10) {
// throw new TooDeepException();
// }
// switch (Roller.rollDie(4)) {
// case 1:
// number = Roller.rollDie(6) + 1;
// composite = null;
// break;
// case 2:
// case 3:
// number = null;
// final int d = depth + 1;
// composite = new MonsterGroup[] { new MonsterGroup(d),
// new MonsterGroup(d) };
// break;
// case 4:
// number = 1;
// composite = null;
// break;
// default:
// throw new RuntimeException("Unexpected value");
// }
// }
//
// @Override
// public String toString() {
// return String.valueOf(number);
// }
//
// public List<MonsterGroup> getAll(final List<MonsterGroup> mgs) {
// if (composite == null) {
// mgs.add(this);
// } else {
// for (final MonsterGroup mg : composite) {
// mg.getAll(mgs);
// }
// }
//
// return mgs;
// }
// }
//
// // public static void main(final String[] args) throws GaveUpException {
// // final TreeMap<Long, String> orderesResults = new TreeMap<Long, String>();
// // for (int max = 7; max < 11; max++) {
// // for (int el = -4; el < 25; el++) {
// // for (int i = 0; i < 10; i++) {
// // SINGLETON.fillTeams(el);
// //
// // for (final List<Monster> team : new List[] { Map.blueTeam,
// // Map.redTeam }) {
// // team.clear();
// // }
// // }
// // }
// //
// // final double[] eval = new double[SINGLETON.hits.values().size()];
// // int i = 0;
// // for (final int value : SINGLETON.hits.values()) {
// // eval[i++] = value;
// // }
// //
// // final long variance = Math.round(new Variance().evaluate(eval));
// // final String result = "Variance: " + variance + " (min" + 1
// // + ",max" + 7 + ").\tHits: " + SINGLETON.hits;
// // System.out.println(result);
// // orderesResults.put(variance, result);
// // SINGLETON.hits.clear();
// // }
// //
// // System.out.println("\nResults");
// // for (final String result : orderesResults.values()) {
// // System.out.println(result);
// // }
// // }
// }
