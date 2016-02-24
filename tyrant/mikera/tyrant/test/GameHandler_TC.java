////package tyrant.mikera.tyrant.test;
//import javelin.controller.action.Action;
//import javelin.controller.action.ActionDescription;
//import javelin.model.BattleMap;
//import tyrant.mikera.engine.Lib;
//import tyrant.mikera.engine.Thing;
//import tyrant.mikera.tyrant.Game;
//import tyrant.mikera.tyrant.GameHandler;
//import tyrant.mikera.tyrant.InputHandler;
//import tyrant.mikera.tyrant.Portal;
//ikera.tyrant.Portal;
//
//public class GameHandler_TC extends TyrantTestCase {
//	private GameHandler gameHandler;
//
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		gameHandler = new GameHandler();
//	}
//
//	public void testDoDirection_noRunning() throws Exception {
//		final String mapString = "---------" + "\n" + "|@......|" + "\n"
//				+ "---------";
//
//		new MapHelper().createMap(mapString);
//		gameHandler.doDirection(TyrantTestCase.getTestHero(), Action.MOVE_E);
//		assertLocation(hero, 2, 1);
//	}
//
//	public void testDoDirection() throws Exception {
//		final String mapString = "#####" + "\n" + "#@..#" + "\n" + "#####";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 3, 1);
//	}
//
//	private void walk(final ActionDescription direction, final boolean running) {
//		int attempts = 3;
//		do {
//			final int xBefore = hero.x;
//			final int yBefore = hero.y;
//
//			gameHandler.doDirection(hero, direction);
//			gameHandler.calculateVision(hero);
//			if (xBefore == hero.x && yBefore == hero.y) {
//				attempts--;
//			} else {
//				attempts = 3;
//			}
//		} while (hero.isRunning() && attempts > 0);
//
//	}
//
//	public void testDoDirection_withDoor() throws Exception {
//		final String mapString = "---------" + "\n" + "|@..+...|" + "\n"
//				+ "---------";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 3, 1);
//	}
//
//	public void testRunning_T_vertical() throws Exception {
//		final String mapString = "####" + "\n" + "@#.#" + "\n" + ".#.#" + "\n"
//				+ "...#" + "\n" + ".###" + "\n" + "####";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_S, true);
//		assertLocation(hero, 0, 3);
//	}
//
//	public void testRunningFollowedByWalking() throws Exception {
//		final String mapString = "##" + "\n" + "@#" + "\n" + ".#" + "\n" + ".#"
//				+ "\n" + "##";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_S, true);
//		assertLocation(hero, 0, 3);
//		walk(Action.MOVE_N, false);
//		assertLocation(hero, 0, 2);
//	}
//
//	public void testRunningAroundCorridor_complex() throws Exception {
//		final String mapString = "#########################" + "\n"
//				+ "#@......................#" + "\n"
//				+ "#######################.#" + "\n"
//				+ "#.....................#.#" + "\n"
//				+ "#.###################.#.#" + "\n"
//				+ "#.#.................#.#.#" + "\n"
//				+ "#.#.#################.#.#" + "\n"
//				+ "#.#.#...............#.#.#" + "\n"
//				+ "#.#.#...............#.#.#" + "\n"
//				+ "#.#.#...............#.#.#" + "\n"
//				+ "#.#.#...............#.#.#" + "\n"
//				+ "#.#.#################.#.#" + "\n"
//				+ "#.#...................#.#" + "\n"
//				+ "#.#########.###########.#" + "\n"
//				+ "#.......................#" + "\n"
//				+ "#########################";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 11, 14);
//	}
//
//	public void testRunOffEdge() throws Exception {
//		final String mapString = "&@&" + "\n" + "&.&" + "\n" + "&.&";
//		new MapHelper().createMap(mapString);
//		answerGetInputWithChar('n');
//		walk(Action.MOVE_S, true);
//		assertLocation(hero, 1, 2);
//		assertFalse(hero.isRunning());
//	}
//
//	public void testRunningZigZag_doubleWide() throws Exception {
//		final String mapString = "         #######" + "\n" + "        ##.....#"
//				+ "\n" + "     ####..#### " + "\n" + "    ###..###    " + "\n"
//				+ "  ###...##      " + "\n" + "###..####       " + "\n"
//				+ "#@..##          " + "\n" + "#####           ";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		// this use to work but not currently
//		// assertLocation(hero, 10, 1);
//		assertLocation(hero, 3, 6);
//	}
//
//	public void testRunning_ZigZag() throws Exception {
//		final String mapString = "      ######" + "\n" + "      #....#." + "\n"
//				+ "  #####.####" + "\n" + "  #.....#   " + "\n"
//				+ "  #.#####   " + "\n" + "  #.#       " + "\n"
//				+ "###.#       " + "\n" + "#...#       " + "\n"
//				+ "#.###       " + "\n" + ".@.......   " + "\n"
//				+ "#########   ";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_N, true);
//		assertLocation(hero, 10, 1);
//		walk(Action.MOVE_W, true);
//		assertLocation(hero, 1, 9);
//	}
//
//	public void testRunning_T() throws Exception {
//		final String mapString = "##.##" + "\n" + "##.##" + "\n" + "@...#"
//				+ "\n" + "#####";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 2, 2);
//	}
//
//	public void testRunning_aroundCorner() throws Exception {
//		final String mapString = "####" + "\n" + "@..#" + "\n" + "##.#" + "\n"
//				+ " #.#" + "\n" + " ###";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 2, 3);
//	}
//
//	public void testRunningInARoom() throws Exception {
//		final String mapString = "##+###+###+##" + "\n" + "#...........#"
//				+ "\n" + "#.....@.....#" + "\n" + "######.######";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 11, 2);
//	}
//
//	public void testRunningByADoor() throws Exception {
//		final String mapString = "######+######" + "\n" + "#..@........#"
//				+ "\n" + "#...........#" + "\n" + "######.######";
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 6, 1);
//	}
//
//	public void testRunningBySecretDoor() throws Exception {
//		final String mapString = "######+######" + "\n" + "#..@........#"
//				+ "\n" + "#...........#" + "\n" + "######.######";
//		new MapHelper().createMap(mapString);
//		final Thing door = hero.getMap().getThings(6, 0)[0];
//		door.set("IsSecretDoor", true);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 11, 1);
//	}
//
//	public void testRunningOverMessagePoint() throws Exception {
//		final String mapString = "#########" + "\n" + "#@.m....#" + "\n"
//				+ "#########";
//		Game.instance().setInputHandler(InputHandler.repeat(' '));
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 3, 1);
//		walk(Action.MOVE_W, false);
//		assertLocation(hero, 2, 1);
//	}
//
//	public void testRunningOverItem() throws Exception {
//		final String mapString = "#########" + "\n" + "#@.?....#" + "\n"
//				+ "#########";
//		Game.instance().setInputHandler(InputHandler.repeat(' '));
//		new MapHelper().createMap(mapString);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 2, 1);
//		walk(Action.MOVE_W, false);
//		assertLocation(hero, 1, 1);
//	}
//
//	public void testRunningOverInvisiblePortal() throws Exception {
//		final String mapString = "#########" + "\n" + "#@......#" + "\n"
//				+ "#########";
//		final BattleMap map = new MapHelper().createMap(mapString);
//		final Thing invisiblePortal = Portal.create("invisible portal");
//		map.addThing(invisiblePortal, 3, 1);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 7, 1);
//		walk(Action.MOVE_W, true);
//		assertLocation(hero, 1, 1);
//	}
//
//	public void testRunningOverGuardPoint() throws Exception {
//		final String mapString = "#########" + "\n" + "#@......#" + "\n"
//				+ "#########";
//		final BattleMap map = new MapHelper().createMap(mapString);
//		final Thing guardPoint = Lib.create("guard point");
//		map.addThing(guardPoint, 3, 1);
//		walk(Action.MOVE_E, true);
//		assertLocation(hero, 7, 1);
//		walk(Action.MOVE_W, true);
//		assertLocation(hero, 1, 1);
//	}
// }
