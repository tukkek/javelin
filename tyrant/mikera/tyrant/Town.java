

package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * Implements generation routines for Tyrant small towns
 * e.g. the villages in North Karrain
 * 
 * Towns are constructed out of a collection of 16*16 blocks
 * using semi-random layouts
 * 
 * @author Mike
 *
 */
public class Town {

	// out of town aresa (buildarea -1)
	public static final int[] OUTER = {0, 1, 2, 7, 14};

	// inner town aresa (buildarea -2)
	public static final int[] INNER = {4, 15, 5, 3, 24};

	// build town sector of type t
	// -2 = random inner town area
	// -1 = random outer town area
	//  0 = wooded
	//  1 = cleared
	//  2 = animals
	//  3 = huts
	//  4 = block
	//  5 = hall
	//  6 = square
	//  7 = pond
	//  8 = N/S river
	//  9 = N/S river + bridge
	//  10 = E/W river
	//  11 = E/W river + bridge
	//  12 = N/S street
	//  13 = E/W street
	//  14 = orchard
	//  15 = smithy (north exit)
	//  16 = N/W river bend
	//  17 = N/E river bend
	//  18 = S/E river bend
	//  19 = S/W river bend
	//  20 = N/W/E river fork
	//  21 = N/S/E river fork
	//  22 = S/W/E river fork
	//  23 = N/S/W river fork
	//  24 = water garden

	// uber-cool array of town arrays
	public static int[][][] towns = {
			{{11, 22, 16, -2, -1}, {-2, 9, 6, 13, -2}, {-2, 8, -2, 15, -1}},
			{{8, -1, -2, -1, 7}, {9, 13, 6, -2, -1}, {17, 19, 12, -2, 14}}

			, {{-1, 17, 19, -2, 13}, {-1, -2, 9, 6, 13}}

			, {{-1, 12, -2}, {13, 6, 13}, {-2, 12, -1}}

			, {{-1, -2, 14}, {-2, 6, -2}, {7, -2, -1}}};

	public static BattleMap createTown(int w, int h) {
		BattleMap m = new BattleMap(w, h);

		m.set("Level",3);
		m.set("IsHostile", 0);
		m.set("WanderingRate", 5);
		m.set("EnterMessageFirst","This seems like a peaceful little village");

		// select a town layout to use
		int[][] layout = towns[RPG.r(towns.length)];

		// change to correct map size
		m.setSize(16 * layout[0].length, 16 * layout.length);

		buildTown(m, layout);
		
		m.addThing(Lib.createType("IsWanderer",Game.level()));

		return m;
	}

	private static void buildTown(BattleMap m, int[][] areas) {
		m.setTheme("village");
		m.set("Description", "Small Town");
		m.set("WallTile", RPG.pick(new int[]{Tile.CAVEWALL, Tile.WOODENWALL,
				Tile.STONEWALL}));

		m.fillArea(0, 0, m.width - 1, m.height - 1, m.floor());

		for (int ay = 0; ay < areas.length; ay++) {
			for (int ax = 0; ax < areas[ay].length; ax++) {
				buildArea(m, ax * 16, ay * 16, areas[ay][ax]);
				
				
				
			}
		}

		//buildArea(0,0,8);
		//buildArea(0,16,9);
		//buildArea(0,32,8);
		//buildArea(16,0,RPG.pick(OUTER));
		//buildArea(16,16,13);
		//buildArea(16,32,RPG.pick(INNER));
		//buildArea(32,0,(RPG.d(3)==1)?12:RPG.pick(INNER));
		//buildArea(32,16,6);
		//buildArea(32,32,(RPG.d(3)==1)?12:RPG.pick(INNER));
		//buildArea(48,0,3);
		//buildArea(48,16,4);
		//buildArea(48,32,RPG.pick(INNER));
		//buildArea(64,0,RPG.pick(OUTER));
		//buildArea(64,16,RPG.pick(OUTER));
		//buildArea(64,32,RPG.pick(OUTER));
		addTownies(m);

		m.setEntrance(Portal.create());
		Point p = m.findFreeSquare(0, 0, m.width - 1, 0);
		m.addThing(m.getEntrance(), p.x, p.y);

	}

	/**
	 * Build a small temple within the town.
	 * 
	 * (x,y) gives the top left corner of a 16*16 area chosen for the
	 * construction of the temple. All construction should take place within
	 * this area, plus a 1-tile wile border around this area should be left
	 * unblocked to allow player movement around the temple.
	 * 
	 * @param m
	 *            The map to build the temple on
	 * @param x
	 *            Left edge of 16*16 area
	 * @param y
	 *            Top edge of 16*16 area
	 * 
	 * @author ppirrip
	 * @since 05-08-04
	 * 
	 * - Added a healer to the temple to see potions.
	 *   @see Person.java
	 * - Added a (blank) sign next to the door.
	 * - Removed the small room on the side.
	 * - Added some common constants for code maintaince. 
	 */
	public static void buildTemple(BattleMap m, int x, int y) {
		// construct the main hall
		int left = RPG.d(2, 3); 
		int right = RPG.d(2, 3);
		// common used constants
		final int tmpX = x + 8;
		final int tmpY1 = y + 3;
		final int tmpY2 = y + 12;

		// Add the stone floor.
		m.fillArea(tmpX - left, tmpY1, tmpX + right, tmpY2,
						Tile.STONEFLOOR);
		// Make the stone wall border
		m.fillBorder(tmpX - left, tmpY1, tmpX + right, tmpY2,
				Tile.STONEWALL);
		// Add some water as decleration.
		m.fillBorder(tmpX-left+1, tmpY1+1, tmpX+right-1 , tmpY1+1,
				Tile.RIVER);
		// Add a door and a blank sign
		m.fillArea(tmpX, tmpY2, tmpX, tmpY2,Tile.STONEFLOOR);
		m.addThing(Lib.create("ornate door"), tmpX, tmpY2);		
		m.addThing(Lib.create("blank sign"), tmpX+1, tmpY2+1);
		/* 
		 *  Add the healer
		 *  The AI of the healer just like the shopkeeper.
		 *  TODO: Might change to teacher script later.
		 */
		final int tmpY3 = y + 7;
		Thing healer = Lib.create("healer");
		AI.setGuard(healer, m, tmpX-1, tmpY3-1, tmpX+1, tmpY3+1);
		m.addThing(healer, tmpX, tmpY3, tmpX, tmpY3);

		// Debug Message
		// So I know a temple has created
		//System.out.println("Set a fire in the temple [ppirrip]");

		/*
		 * Fires in the room
		 * TODO: Why fire won't display anymore?
		 */  
		//m.addThing(Fire.create(5), tmpX-2, tmpY3-2); 

		// TODO: add a random number there to general
		//       different potions.
		for (int xx = tmpX-left+1; xx <= tmpX+right-1; xx++)
				m.addThing(stockingPoint(healer,"IsPotion", 10), xx, tmpY1+2);
		
		// Add a holy well
		m.addThing(Lib.create("well"), tmpX, tmpY3);

		//	  random rotation of the area
		m.rotateArea(x, y, 16, RPG.r(4));
	}

	public static void buildWaterGarden(BattleMap m, int x, int y) {
		// Water area, (7,7) is the centre
		m.fillArea(x + 1, y + 1, x + 13, y + 13, Tile.RIVER);

		// island
		m.fillArea(x + 3, y + 3, x + 11, y + 11, m.floor());

		// bridges
		m.fillArea(x + 1, y + 6, x + 13, y + 8, m.floor());
		m.fillArea(x + 6, y + 1, x + 8, y + 13, m.floor());

		switch (RPG.d(4)) {
			case 1 :
				String s = RPG.pick(new String[]{"red apple tree",
						"potted flower", "potted plant", "plant"});
				for (int rx = 5; rx <= 9; rx += 2) {
					for (int ry = 5; ry <= 9; ry += 2) {
						m.addThing(s, x + rx, y + ry);
					}
				}
				break;
			case 2 :
				for (int rx = 5; rx <= 9; rx += 2) {
					for (int ry = 5; ry <= 9; ry += 2) {
						m.setTile(x + rx, y + ry, Tile.RIVER);
					}
				}
				break;
			case 3 :
				addRoom(m, x + 5, y + 5, x + 9, y + 9, 0, -1);
				Thing teach = Lib.create("teacher");
				m.addThing(teach, x + 7, y + 7);
				AI.setGuard(teach, m, x + 7, y + 7);
				break;
			case 4 :
				m.addThing("fountain",x+7,y+7);
				break;
		}

	}

	private static void buildArea(BattleMap m, int x, int y, int t) {
		if (t == -1)
			t = RPG.pick(OUTER);
		if (t == -2)
			t = RPG.pick(INNER);

		switch (t) {

			// wooded area
			case 0 : {
				for (int i = 0; i < 20; i++) {
					m.addThing(Lib.create("tree"), x, y, x + 15, y + 15);
				}

				switch (RPG.d(4)) {
					case 1 :
						m.addThing(Lib.createType("IsWell", m.getLevel()), x,
								y, x + 15, y + 15);
						break;
					case 2 :
						m.addThing(
								Lib.createType("IsGravestone", m.getLevel()),
								x, y, x + 15, y + 15);
						break;

					default :
						m.clearArea(x + 5, y + 5, x + 10, y + 9);
						m
								.fillArea(x + 5, y + 5, x + 10, y + 9,
										Tile.WOODENWALL);
						m
								.fillArea(x + 6, y + 6, x + 9, y + 8,
										Tile.WOODENFLOOR);
						m.setTile(x + 5, y + 7, Tile.WOODENFLOOR);
						m.addThing(Door.createDoor(5), x + 5, y + 7);
						m.addThing(Lib.createType("IsEquipment", 5), x + 5,
								y + 7);
				}

				m.addThing(Lib.create("ranger"), x, y, x + 15, y + 15);

				break;
			}

			// hut
			case 3 : {
				int hw = RPG.r(3) + 3;
				int hh = RPG.r(3) + 3;
				int hx = x + 1 + RPG.r(14 - hw);
				int hy = y + 1 + RPG.r(14 - hh);
				int dx = RPG.r(2) * 2 - 1;
				int dy = RPG.r(2) * 2 - 1;
				if (RPG.d(2) == 1)
					dx = 0;
				else
					dy = 0;
				addRoom(m, hx, hy, hx + hw, hy + hh, dx, dy);
				break;
			}

			// block
			case 4 : {
				addRoom(m, x + 8 - RPG.d(2, 3), y + 8 - RPG.d(2, 3), x + 8,
						y + 8, 0, -1);
				addRoom(m, x + 8 - RPG.d(2, 3), y + 8, x + 8, y + 8
						+ RPG.d(2, 3), 0, 1);
				addRoom(m, x + 8, y + 8 - RPG.d(2, 3), x + 8 + RPG.d(2, 3),
						y + 8, 0, -1);
				addRoom(m, x + 8, y + 8, x + 8 + RPG.d(2, 3), y + 8
						+ RPG.d(2, 3), 0, 1);
				break;
			}

			// temple hall
			case 5 : {
				buildTemple(m, x, y);
				break;
			}

			// central crossroads with shops
			case 6 : {
				addShop(m, x, y, x + RPG.d(3, 2), y + RPG.d(3, 2), 1, 0, 4);
				addShop(m, x + 15 - RPG.d(3, 2), y, x + 15, y + RPG.d(3, 2), 0,
						1, 0);
				addShop(m, x, y + 15 - RPG.d(3, 2), x + RPG.d(3, 2), y + 15, 0,
						-1, 2);
				addShop(m, x + 15 - RPG.d(3, 2), y + 15 - RPG.d(3, 2), x + 15,
						y + 15, -1, 0, 3);
				break;
			}

			// pond
			case 7 : {
				m.setTile(x + 8, y + 8, Tile.SEA);
				m.fractalize(x + 4, y + 4, x + 11, y + 11, 4);
				for (int i = 0; i < 10; i++) {
					m.addThing(Lib.create("bush"), x, y, x + 15, y + 15);
				}
				break;
			}

			// river
			case 8 :
			case 9 :
			case 10 :
			case 11 : {
				// N/S river
				m.makeRandomPath(x + 8, y, x + 8, y + 15, x, y, x + 15, y + 15,
						Tile.RIVER, false);
				m.spreadTiles(x, y, x + 15, y + 15, Tile.RIVER, m.floor());
				if ((t & 1) == 1) {
					// add the bridge
					m.replaceTiles(x, y + 7, x + 15, y + 7 + RPG.r(3),
							Tile.RIVER, Tile.STONEFLOOR);
				}

				// rotate for E/W river
				if ((t & 2) > 0)
					m.rotateArea(x, y, 16, 1);
				break;
			}

			// NS and EW streets
			case 12 :
			case 13 : {
				int p1, p2;
				p1 = 7 - RPG.d(3);
				p2 = 7 + RPG.d(3);
				addFeature(m, x, y + RPG.r(2), x + p1, y + 5 + RPG.r(3), 0, 1,
						0);
				addFeature(m, x + p1, y + RPG.r(2), x + p2, y + 5 + RPG.r(3),
						0, 1, 0);
				addFeature(m, x + p2, y + RPG.r(2), x + 14, y + 5 + RPG.r(3),
						0, 1, 0);

				p1 = 8 - RPG.d(4);
				p2 = 8 + RPG.d(4);
				addFeature(m, x, y + 9 + RPG.r(3), x + p1, y + 14 - RPG.r(2),
						0, -1, 0);
				addFeature(m, x + p1, y + 9 + RPG.r(3), x + p2, y + 14
						- RPG.r(2), 0, -1, 0);
				addFeature(m, x + p2, y + 9 + RPG.r(3), x + 14, y + 14
						- RPG.r(2), 0, -1, 0);

				// rotate for N/S street
				if (t == 12)
					m.rotateArea(x, y, 16, 1);
				break;
			}

			// orchard
            case 14 : {
                boolean mixed = false;
                Thing tree = null;
                if (RPG.r(3) == 2) {
                     // the orchard will be of mixed species
                    mixed = true;
                } else {
                    // pick a single species for the orchard
                    tree = Lib.createType("IsFruitTree",1);
                }
				for (int i = 0; i < 20; i++) {
					int px = x + 1 + RPG.r(14);
					int py = y + 1 + RPG.r(14);
                    if (!m.isBlocked(px, py))
                        if (mixed) {
                            m.addThing(Lib.create("[IsFruitTree]"), px, py);
                        } else {
                            m.addThing(tree.cloneType(), px, py);
                        }
				}
				break;
			}

			// smithy
			case 15 : {
				addShop(m, x + 5, y + 5, x + 10, y + 10, 0, -1, 5);
				m.rotateArea(x, y, 16, RPG.r(4));
				break;
			}

			// river bends
			case 16 :
			case 17 :
			case 18 :
			case 19 : {
				m.makeRandomPath(x + 8, y, x, y + 8, x, y, x + 15, y + 15,
						Tile.RIVER, false);
				m.spreadTiles(x, y, x + 15, y + 15, Tile.RIVER, m.floor());

				// rotate clockwise to correct orientation
				// 16 = N/W position etc..
				m.rotateArea(x, y, 16, t - 16);
				break;
			}

			// river forks
			case 20 :
			case 21 :
			case 22 :
			case 23 : {
				m.makeRandomPath(x + 8, y, x, y + 8, x, y, x + 15, y + 15,
						Tile.RIVER, false);
				m.makeRandomPath(x + 8, y, x + 15, y + 8, x, y, x + 15, y + 15,
						Tile.RIVER, false);
				m.spreadTiles(x, y, x + 15, y + 15, Tile.RIVER, m.floor());

				// rotate clockwise to correct orientation
				// 20 = N/W/E position etc..
				m.rotateArea(x, y, 16, t - 20);
				break;
			}
			case 24 :
				buildWaterGarden(m, x, y);
				break;

		}
	}

	// adds a town feature in specified area
	public static void addFeature(BattleMap m, int x1, int y1, int x2, int y2,
			int dx, int dy, int t) {
		// randomize feature
		if (t == 0) {
			t = RPG.d(10);
		}

		switch (t) {
			case 1 :
				addShop(m, x1, y1, x2, y2, dx, dy, 0);
				break;

			default :
				addRoom(m, x1, y1, x2, y2, dx, dy);
				break;
		}

	}

	public static void addGraveYard(BattleMap m, int x1, int y1, int x2, int y2) {
		for (int i = 0; i < ((x2 - x1 + 1) * (y2 - y1 + 1)) / 8; i++) {
			m.addThing(Lib.createType("IsGraveStone", m.getLevel()), x1, y1,
					x2, y2);
		}

		for (int i = 0; i < ((x2 - x1 + 1) * (y2 - y1 + 1)) / 18; i++) {
			m.addThing(Lib.createType("potted flower", m.getLevel()), x1, y1,
					x2, y2);
		}
	}

	public static void addTownies(BattleMap m) {
		for (int i = 0; i <= 10; i++) {
			Point ts = m.findFreeSquare();
			m.addThing(Lib.create("farmer"), ts.x, ts.y);
		}
		for (int i = 0; i <= 6; i++) {
			Point ts = m.findFreeSquare();
			m.addThing(Lib.create("village woman"), ts.x, ts.y);
		}
		for (int i = 0; i <= 4; i++) {
			Point ts = m.findFreeSquare();
			m.addThing(Lib.create("village girl"), ts.x, ts.y);
		}
		for (int i = 0; i <= 8; i++) {
			Point ts = m.findFreeSquare();
			m.addThing(Lib.create("guard"), ts.x, ts.y);
		}

		Point ts = m.findFreeSquare();
		m.addThing(Lib.create("teacher"), ts.x, ts.y);
	}

	public static Thing stockingPoint(Thing sk,String type, int level) {
		Thing t = Lib.create("base stocking point");
		t.set("Name", "shop stocking point");
		t.set("StockingType", type);
		t.set("StockingLevel", level);
		t.set("Shopkeeper",sk);
		return t;
	}

	// Create standard shops
	//
	// note that areas are likely to be rotated
	// this will mess up GuardAI(x1,y1,x2,y2) objects
	// hence use secret markers to mark out the shop area
	public static void addShop(BattleMap m, int x1, int y1, int x2, int y2, int dx,
			int dy, int t) {
		int doorx, doory;
		doorx = (dx == 0)
				? RPG.rspread(x1 + 1, x2 - 1)
				: ((dx == -1) ? x1 : x2);
		doory = (dy == 0)
				? RPG.rspread(y1 + 1, y2 - 1)
				: ((dy == -1) ? y1 : y2);
		m.fillArea(x1, y1, x2, y2, Tile.FLOOR);
		m.fillBorder(x1, y1, x2, y2, Tile.CAVEWALL);
		m.setTile(doorx, doory, Tile.FLOOR);

		Thing door = Lib.create("shop door");

		if (t == 0) {
			t = RPG.d(10);
		}

		switch (t) {
			// magic type shop
			case 1 : {
				Thing wiz = Lib.create("wizard");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {

						m.addThing(stockingPoint(wiz,"IsMagicItem", RPG.d(15)), x, y);
					}

				AI.setGuard(wiz, m, x1, y1, x2, y2);
				m.addThing(wiz, (x1 + x2) / 2, (y1 + y2) / 2);
				m.addThing(Lib.create("magic shop sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}

			// weapon store
			case 2 : {
				Thing guard = Lib.create("guard");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(guard,"IsWeapon", RPG.d(2,6)), x, y);
					}

				AI.setGuard(guard, m, x1, y1, x2, y2);
				m.addThing(guard, (x1 + x2) / 2, (y1 + y2) / 2);

				m.addThing(Lib.create("armoury sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}

			// food store
			case 3 : {
				Thing shopkeeper = Lib.create("shopkeeper");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(shopkeeper,"IsShopFood", 10), x, y);
					}

				AI.setGuard(shopkeeper, m, x1, y1, x2, y2);
				m.addThing(shopkeeper, (x1 + x2) / 2, (y1 + y2) / 2);

				m.addThing(Lib.create("food shop sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}

			// Smithy
			// note that door is assumed to face north
			case 5 : {
				Thing smith = Lib.create("blacksmith");
				AI.setGuard(smith, m, x1, y1, x2, y2);

				m.addThing(smith, (x1 + x2) / 2, (y1 + y2) / 2);
				
				
				m.addThing(Fire.create(5), x2 - 1, y2 - 1);
				m.addThing(Fire.create(5), x2 - 2, y2 - 1);

				m.addThing(Lib.create("smithy sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}

			// Goblin store
			case 6 : {
				door = Lib.create("goblin door");
				door.set("IsLocked", 0);

				Thing gobbo = Lib.create("goblin shopkeeper");
				AI.setGuard(gobbo, m, x1, y1, x2, y2);
				gobbo.set("IsHostile", 0);
				//gobbo.setPersonality(new Personality(Personality.CHATTER,
				//		Personality.CHATTER_GOBLIN));
				m.addThing(gobbo, (x1 + x2) / 2, (y1 + y2) / 2);

				for (int x = x1 + 1; x < x2; x++) {
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(gobbo,"IsMagicItem", 12), x, y);
					}
				}
				break;
			}

			// equipment store
			// note that door is assumed to face north
			case 7 : {
				Thing shopkeeper = Lib.create("shopkeeper");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(shopkeeper,"IsEquipment", 10), x, y);
					}

				AI.setGuard(shopkeeper, m, x1, y1, x2, y2);
				m.addThing(shopkeeper, (x1 + x2) / 2, (y1 + y2) / 2);

				m.addThing(Lib.create("armoury sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}
			
			// armour store
			// note that door is assumed to face north
			case 8 : {
				Thing shopkeeper = Lib.create("blacksmith");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {
						if (RPG.d(2)==1) {
							m.addThing(stockingPoint(shopkeeper,"IsArmour", 10), x, y);
						}
					}

				AI.setGuard(shopkeeper, m, x1, y1, x2, y2);
				m.addThing(shopkeeper, (x1 + x2) / 2, (y1 + y2) / 2);

				m.addThing(Lib.create("smithy sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
				break;
			}

			// Rune store
			case 9 : {
				door = Lib.create("goblin door");
				door.set("IsLocked", 0);

				Thing sk = Lib.create(RPG.pick(new String[] {"goblin shopkeeper","learned sage","shopkeeper","wizard"}));
				AI.setGuard(sk, m, x1, y1, x2, y2);
				m.addThing(sk, (x1 + x2) / 2, (y1 + y2) / 2);

				for (int x = x1 + 1; x < x2; x++) {
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(sk,"IsRunestone", RPG.d(12)), x, y);
					}
				}
				break;
			}
			
			// General store
			default : {
				Thing shopkeeper = Lib.create("shopkeeper");
				for (int x = x1 + 1; x < x2; x++)
					for (int y = y1 + 1; y < y2; y++) {
						m.addThing(stockingPoint(shopkeeper,"IsStoreItem", 10), x, y);
					}

				AI.setGuard(shopkeeper, m, x1, y1, x2, y2);
				m.addThing(shopkeeper, (x1 + x2) / 2, (y1 + y2) / 2);

				m.addThing(Lib.create("store sign"), doorx + dx + dy
						* (RPG.r(2) * 2 - 1), doory + dy + dx
						* (RPG.r(2) * 2 - 1));
			}

		}

		m.addThing(door, doorx, doory);
	}

	public static void addBuilding(BattleMap m) {
		int w = RPG.d(2, 4) + 3;
		int h = RPG.d(2, 4) + 3;
		int x1 = RPG.r(m.width - w);
		int y1 = RPG.r(m.height - h);
		int x2 = x1 + w;
		int y2 = y1 + h;
		int dx = RPG.r(3) - 1;
		int dy = RPG.r(3) - 1;
		if (RPG.d(2) == 1) {
			dx = 0;
		} else {
			dy = 0;
		}

		if (m.countTiles(x1, y1, x2, y2, m.floor()) != (w + 1) * (h + 1))
			return;
		addRoom(m, x1 + 1, y1 + 1, x2 - 1, y2 - 1, dx, dy);
	}

	public static void addRoom(BattleMap m, int x1, int y1, int x2, int y2, int dx,
			int dy) {
		int doorx, doory;
		doorx = (dx == 0)
				? RPG.rspread(x1 + 1, x2 - 1)
				: ((dx == -1) ? x1 : x2);
		doory = (dy == 0)
				? RPG.rspread(y1 + 1, y2 - 1)
				: ((dy == -1) ? y1 : y2);
		addStandardRoom(m, x1, y1, x2, y2, doorx, doory);
	}

	// add a standard town room
	// all kinds of fun stuff inside
	public static void addStandardRoom(BattleMap m, int x1, int y1, int x2, int y2, int doorx,
			int doory) {
		m.fillArea(x1, y1, x2, y2, Tile.FLOOR);
		m.fillBorder(x1, y1, x2, y2, m.wall());

		boolean secret = false;
		Thing door = Lib.create("door");

		switch (RPG.d(15)) {
			case 1 : {
				door.set("IsLocked", 1);
				Thing it = Food.createFood(0);
				it.set("IsOwned", 1);
				m.addThing(it, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				break;
			}
			case 2 : { // alarmed room
				door.set("IsLocked", 1);
				Thing it = Lib.createItem(8);
				it.set("IsOwned", true);
				m.addThing(it, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				// addThing(new MapStateTrap(1), doorx, doory);
				break;
			}
			case 3 : { // rats!
				Thing it = Lib.createItem(3);
				m.addThing(Secret.hide(it), x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				for (int i = RPG.d(8); i > 0; i--)
					m.addThing(Lib.create("sewer rat"), x1 + 1, y1 + 1, x2 - 1,
							y2 - 1);
				break;
			}
			case 4 : { // table
				m.addThing(Lib.create("table"), x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				break;
			}
			case 5 : {
				if (RPG.d(6) == 1) {
					Thing it = Lib.createItem(3);
					m.addThing(Secret.hide(it), x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				}
				door = null;
				break;
			}
			case 6 :
			case 7 :
			case 8 :
			case 9 :
				door.set("IsLocked", (RPG.d(3) == 1) ? 1 : 0);
				decorateRoom(m, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				break;
			case 10 :
				door = Lib.create("stable door");
				door.set("IsLocked", 1);
				m.addThing(Lib.create("[IsAnimal]"), x1, y1, x2, y2);
				if (RPG.d(4)==1) m.addThing(Lib.create("[IsAnimal]"), x1, y1, x2, y2);
				break;
			case 11 : {
				if (RPG.d(4)==1) {
					Thing it = Lib.createItem(10);
					m.addThing(Secret.hide(it), x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				}
				decorateRoom(m, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
				
				secret=true;
				break;
			}
		}

		if (secret) {
			m.addThing(Lib.create("secret door"), doorx, doory);
		} else {
			m.setTile(doorx, doory, Tile.FLOOR);
			if (door != null)
				m.addThing(door, doorx, doory);
		}
	}

	// decorate room, given co-ordinates of interior space
	public static void decorateRoom(BattleMap m, int x1, int y1, int x2, int y2) {
		int w = x2 - x1 + 1;
		int h = y2 - y1 + 1;

		switch (RPG.d(12)) {
			case 1 :
				for (int lx = x1; lx <= x2; lx++) {
					for (int ly = y1; ly <= y2; ly++) {
						// TODO: something interesting
					}
				}
				break;
			case 2 :
				if (w > 3 && h >= 3) {
                    Point location = createPersonAndTable("townswoman", m, x1, y1, x2, y2);
					m.addThing(Lib.create("stool"), location.x + 1 - 2 * RPG.r(2), location.y);
					m.addThing(Lib.create("stool"), location.x, location.y + 1 - 2 * RPG.r(2));
					m.addThing(Food.createFood(0), x1, y1, x2, y2);
					m.addThing(Food.createFood(0), x1, y1, x2, y2);
				}
				break;

			case 3 :
				if (w > 3 && h >= 3) {
                    Point location = createPersonAndTable(RPG.pick(new String[]{"wizard", "priest"}), m, x1, y1, x2, y2);
					m.addThing(Lib.create("stool"), location.x + 1 - 2 * RPG.r(2), location.y + 1 - 2 * RPG.r(2));
					m.addThing(Lib.createMagicItem(3), x1, y1, x2, y2).set("IsOwned", 1);
					m.addThing(Lib.createMagicItem(3), x1, y1, x2, y2).set("IsOwned", 1);
					m.addThing(Scroll.createScroll(6), x1, y1, x2, y2).set("IsOwned", 1);
					m.addThing(Scroll.createScroll(8), x1, y1, x2, y2).set("IsOwned", 1);
				}
				break;
			case 4 :
				if ((w > 3) && (h >= 3)) {
					m.fillArea(x1 + 1, y1 + 1, x2 - 1, y2 - 1, Tile.STREAM);
				}
				break;
			case 5 :
				m.addThing("potted plant", x1, y1, x2, y2);
				m.addThing("chest", x1, y1, x2, y2).set("IsOwned", 1);
				break;
			case 6 :
				m.addThing("potted flower", x1, y1, x2, y2);
				m.addThing("potted flower", x1, y1, x2, y2);
				break;
			case 7 :
				Thing sage=m.addThing("learned sage", x1, y1, x2, y2);
				AI.setGuard(sage,m,x1,y1,x2,y2);
				m.addThing(Lib.createMagicItem(3), x1, y1, x2, y2).set(
						"IsOwned", 1);
				m.addThing(Scroll.createScroll(6), x1, y1, x2, y2).set(
						"IsOwned", 1);
				m.addThing(Scroll.createScroll(8), x1, y1, x2, y2).set(
						"IsOwned", 1);
				break;
			case 8 :
				m.addThing("pit trap", x1, y1, x2, y2);
				break;
			case 9 :
				m.addThing(Lib.createType("IsBook"), x1, y1, x2, y2).set("IsOwned",1);
				break;				
				
			default:
				m.addThing(Lib.createItem(RPG.d(12)),x1,y1,x2,y2).set("IsOwned",1);
				break;
		}
	}

	private static Point createPersonAndTable(String toCreate, BattleMap m, int x1, int y1, int x2, int y2) {
        Thing p = Lib.create(toCreate);
        AI.setGuard(p, m, x1, y1, x2, y2);
        m.addThing(p, x1, y1, x2, y2);
        int tx = RPG.rspread(x1 + 1, x2 - 1);
        int ty = RPG.rspread(y1 + 1, y2 - 1);
        m.addThing(Lib.create("table"), tx, ty);
        return new Point(tx, ty);
    }

    public static void decorateTown(BattleMap m, int x1, int y1, int x2, int y2) {
		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++) {
				if (m.isBlocked(x, y))
					continue;
				if (!(m.getTile(x, y) == m.floor()))
					continue;
                switch (RPG.d(200)) {
                	case 1 :
                	case 2 :
                	case 3 :
                		m.addThing(Lib.create("plant"), x, y);
                		break;
                	case 4:
                		m.addThing("menhir",x,y);
                		break;
                	case 5:
                		m.addThing("[IsHerb]",x,y);
                		break;
						
                }
			}
	}
	
	public static BattleMap buildNyckMap() {
		BattleMap m=new BattleMap(31,31);
		m.set("Description","A Strange Icy Place");
		m.set("WallTile",Tile.ICEWALL);
		m.set("FloorTile",Tile.ICEFLOOR);
		
		Maze.buildMaze(m,0,0,30,30);
		m.fillArea(11,21,19,29,m.wall());
		int x=RPG.r(15)*2+1;
		m.setTile(x,0,m.floor());
		Thing e=Portal.create("invisible portal");
		m.addThing(e,x,0);
		m.setEntrance(e);
		
		m.fillArea(11,21,19,29,m.floor());
		
		m.fillBorder(13,23,17,27,m.wall());
		m.setTile(15,23,m.floor());
		m.addThing(Fire.create(5),15,25);
		Thing nyck=Lib.create("Jolly Old Nyck");
		m.addThing(nyck,15,26);
		AI.setGuard(nyck,m,14,24,16,26);
		return m;
	}

}