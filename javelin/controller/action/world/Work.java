// package javelin.controller.action.world;
//
// import java.awt.event.KeyEvent;
// import java.util.ArrayList;
// import java.util.List;
//
// import javelin.Javelin;
// import javelin.controller.action.world.improvement.BuildHighway;
// import javelin.controller.action.world.improvement.BuildInn;
// import javelin.controller.action.world.improvement.BuildMine;
// import javelin.controller.action.world.improvement.BuildOutpost;
// import javelin.controller.action.world.improvement.BuildRoad;
// import javelin.controller.action.world.improvement.BuildTown;
// import javelin.controller.action.world.improvement.Deforestate;
// import javelin.controller.terrain.Terrain;
// import javelin.model.unit.Combatant;
// import javelin.model.unit.Squad;
// import javelin.model.world.Improvement;
// import javelin.model.world.World;
// import javelin.model.world.location.dungeon.Dungeon;
// import javelin.model.world.location.town.Town;
// import javelin.view.screen.Option;
// import javelin.view.screen.WorldScreen;
// import javelin.view.screen.town.SelectScreen;
//
/// **
// * Special actions only a Worker can execute (see XML). These are things like
// * building roads, inns, towns, mines...
// *
// * Having more workers with you reduces the time needed to complete tasks.
// *
// * TODO how to handle random encounters during build time?
// *
// * @see Squad#work
// * @author alex
// */
// public class Work extends WorldAction {
// static final boolean DEBUG = false;
//
// static final Improvement ROAD = new BuildRoad("Build road", 7, 'r', false);
// static final Improvement HIGHWAY =
// new BuildHighway("Upgrade road", 7, 'r', false);
// static final Improvement INN = new BuildInn("Build inn", 7, 'i', false);
// static final Improvement OUTPOST =
// new BuildOutpost("Build outpost", 15, 'o', false);
// static final Improvement TOWN = new BuildTown("Build town", 30, 't', false);
// static final Improvement DEFORESTATE =
// new Deforestate("Deforestate", 10, 'd', true);
// static final Improvement MINE = new BuildMine("Build mine", 30, 'm', true);
//
// class WorkScreen extends SelectScreen {
// public WorkScreen() {
// super("Build what?", null);
// stayopen = false;
// }
//
// @Override
// public String getCurrency() {
// return "days";
// }
//
// @Override
// public String printInfo() {
// return "You have " + countworkers() + " worker(s).\n" + //
// "You have " + Squad.active.resources + " resource(s).";
// }
//
// @Override
// public List<Option> getoptions() {
// ArrayList<Option> options = new ArrayList<Option>();
// options.add(INN);
// options.add(TOWN);
// options.add(OUTPOST);
// Terrain t = Terrain.current();
// int x = Squad.active.x;
// int y = Squad.active.y;
// if (t.getspeed(x, y) != t.speedhighway) {
// if (!World.roads[x][y]) {
// options.add(ROAD);
// } else if (!World.highways[x][y]) {
// options.add(HIGHWAY);
// }
// }
// if (t.equals(Terrain.MOUNTAINS)) {
// options.add(MINE);
// } else if (t.equals(Terrain.FOREST)) {
// options.add(DEFORESTATE);
// }
// return options;
// }
//
// @Override
// public boolean select(Option o) {
// int[] cost = withresources(build((Improvement) o));
// Squad.active.hourselapsed += 24 * cost[0];
// Squad.active.resources -= cost[1];
// Squad.active.work = (Improvement) o;
// return true;
// }
//
// @Override
// public String printpriceinfo(Option o) {
// int[] cost = withresources(build((Improvement) o));
// String resources =
// cost[1] == 0 ? "" : ", " + cost[1] + " resources";
// return " (" + cost[0] + " days" + resources + ")";
// }
//
// int[] withresources(int days) {
// if (Squad.active.resources == 0 || days == 1) {
// return new int[] { days, 0 };
// }
// int resourcesindays =
// Math.round(Squad.active.resources / Town.DAILYLABOR);
// if (resourcesindays > days - 1) {
// resourcesindays = days - 1;
// }
// return new int[] { days - resourcesindays,
// Math.round(resourcesindays * Town.DAILYLABOR) };
// }
// }
//
// int build(Improvement o) {
// double days = o.price / countworkers();
// days = o.absolute ? days : days
// / Terrain.current().getspeed(Squad.active.x, Squad.active.y);
// return Math.max(1, Math.round(Math.round(days)));
// }
//
// /** Constructor. */
// public Work() {
// super("Work", new int[] { KeyEvent.VK_W }, new String[] { "w" });
// }
//
// @Override
// public void perform(WorldScreen screen) {
// if (Dungeon.active != null) {
// Javelin.message("Can't build improvements inside dungeons...",
// false);
// return;
// }
// int workers = countworkers();
// if (workers == 0) {
// Javelin.message("Take some workers from a Town first...", false);
// return;
// }
// if (Terrain.current().equals(Terrain.WATER)) {
// Javelin.message("Can't build improvements on water...", false);
// return;
// }
// new WorkScreen().show();
// }
//
// /**
// * @return Total of workers in {@link Squad#active}.
// */
// static public int countworkers() {
// int workers = 0;
// for (Combatant c : Squad.active.members) {
// if (c.source.name.equals("Worker")) {
// workers += 1;
// }
// }
// return workers == 0 && DEBUG ? 1 : workers;
// }
// }
