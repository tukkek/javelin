package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * Special map items and effects
 * 
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Special {
	public static void init() {
		Thing t = Lib.extend("base special", "base thing");
		t.set("IsInvisible", 1);
		t.set("IsDestructible", 0);
		t.set("IsPhysical", 0);
		t.set("ImageSource", "Effects");
		t.set("LevelMin", 1);
		t.set("Image", 25);
		Lib.add(t);

		t = Lib.extend("message point", "base special");
		t.set("IsInvisible", 0);
		t.set("IsDestructible", 0);
		t.set("IsMessagePoint", true);
		t.set("LevelMin", 1);
		t.set("ImageSource", "Effects");
		t.set("Image", 40);
		t.set("Message", "No message here!");
		t.set("OnEnterTrigger", new MessagePointAction());
		t.set("ASCII", "m");
		Lib.add(t);

		initStockingPoints();
		initPortalPoints();
		initBuilderPoints();
		// initAIMarkers();
	}

	private static class MessagePointAction extends Script {
		private static final long serialVersionUID = 3905525999230595378L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing tt = e.getThing("Target");
			if (!tt.isHero()) {
				return false;
			}
			tt.isRunning(false);
			if (tt.getMap().getFlag("IsHostile")) {
				return false;
			}

			// Game.warn("Message point at "+t.x+","+t.y);
			Game.messageTyrant(t.getString("Message"));

			if (!Game.isDebug()) {
				Game.messageTyrant("[Press space to continue]");
				for (char c = Game.getChar(); c != ' ';) {
					c = Game.getChar();
				}
				Game.messageTyrant("");
			}

			t.remove();
			return true;
		}
	}

	private static class CloudAction extends Script {
		private static final long serialVersionUID = 4121129221436814640L;

		@Override
		public boolean handle(Thing t, Event e) {
			int time = e.getStat("Time");
			for (int i = RPG.po(time * t.getStat("MoveSpeed"), 10000); i > 0; i--) {
				wander(t);
			}
			return doEffects(t);
		}

		private void wander(Thing t) {
			BattleMap m = t.getMap();
			if (m == null) {
				return;
			}

			int tx = t.x + RPG.r(3) - 1;
			int ty = t.y + RPG.r(3) - 1;

			if (!m.isTileBlocked(tx, ty)) {
				t.moveTo(m, tx, ty);
			}
		}

		private boolean doEffects(Thing t) {
			BattleMap m = t.getMap();
			if (m == null) {
				return true;
			}
			Thing[] things = m.getThings(t.x, t.y);

			for (Thing thing : things) {
				if (thing != t) {
					Event e = new Event("Touch");
					e.set("Target", thing);
					if (t.handle(e)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private static class CloudTouch extends Script {
		private static final long serialVersionUID = 3258416110087977270L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing target = e.getThing("Target");
			if (affects(t, target)) {

				if (t.getFlag("SingleEffect")) {
					target.message("You are touched by " + t.getTheName());
				}

				Event ee = new Event("Effect");
				ee.set("Target", target);
				t.handle(ee);

				if (t.getFlag("SingleEffect")) {
					t.die();
					return true;
				}
			}

			return false;
		}

		/**
		 * Determines whether a cloud has any affect on a given thing Uses the
		 * AffectType property of the cloud to determine which flags to check
		 * 
		 * @param cloud
		 * @param target
		 * @return True if cloud can affect the given target
		 */
		private boolean affects(Thing cloud, Thing target) {
			String s = cloud.getString("AffectType");
			return target.getFlag(s);
		}
	}

	private static class StockingAction extends Script {
		private static final long serialVersionUID = 8701791275653201559L;

		@Override
		public boolean handle(Thing t, Event e) {
			int time = e.getStat("Time");
			if (t.getFlag("StockingStock")
					|| RPG.po(time * t.getStat("StockingRate"), 1000000) > 0) {
				BattleMap m = t.getMap();
				if (m.getFlag("IsHostile")) {
					return false;
				}
				if (m.isVisible(t.x, t.y)) {
					return false;
				}
				String type = t.getString("StockingType");
				String flag = t.getString("StockingFlag");
				int level = t.getStat("StockingLevel");
				if (level <= 0) {
					level = t.getMap().getLevel();
				}
				if (!m.isBlocked(t.x, t.y)
						&& !(m.getFlaggedObject(t.x, t.y, type) != null)) {
					Thing nt = Lib.createType(type, level);
					if (flag != null) {
						String[] sf = flag.split(",");
						for (String element : sf) {
							nt.set(element, 1);
						}
					}
					m.addThing(nt, t.x, t.y);
					if (t.getFlag("StockingStock")) {
						t.incStat("StockingStock", -1);

					}
				}
			}
			return false;
		}
	}

	public static Thing messagePoint(String message) {
		Thing t = Lib.create("message point");
		t.set("Message", message);
		return t;
	}

	private static void initStockingPoints() {
		Thing t;

		t = Lib.extend("base stocking point", "base special");
		t.set("IsStockingPoint", 1);
		t.set("IsActive", 1);
		t.set("Image", 20);
		t.set("OnAction", new StockingAction());
		t.set("StockingType", "IsFood");
		t.set("StockingFlag", "IsShopOwned,IsOwned");
		t.set("StockingRate", 30);
		t.set("StockingStock", 2);
		t.set("MapColour", 0x00704040);
		t.set("Shopkeeper", null);
		Lib.add(t);

		t = Lib.extend("artifact point", "base stocking point");
		t.set("StockingType", "IsRandomArtifact");
		t.set("StockingFlag", null);
		t.set("StockingStock", 1);
		t.set("StockingRate", 0);
		t.set("Shopkeeper", 0);
		Lib.add(t);
	}

	private static void initPortalPoints() {
		Thing t = Lib.extend("base portal point", "base special");
		t.set("IsPortalPoint", 1);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("entrance point", "base portal point");
		t.set("IsEntrancePoint", 1);
		t.set("PortalName", "stairs up");
		Lib.add(t);
	}

	// private static void initAIMarkers() {
	// Thing t;
	//
	// t = Lib.extend("base marker", "base special");
	// t.set("IsMarker", 1);
	// Lib.add(t);
	//
	// t = Lib.extend("warning point", "base marker");
	// t.set("IsWarning", 1);
	// t.set("Image", 141);
	// Lib.add(t);
	//
	// t = Lib.extend("guard point", "base marker");
	// t.set("ASCII", "_");
	// t.set("LevelMin", 1);
	// t.set("OnAction", new Script() {
	// @Override
	// public boolean handle(Thing t, Event e) {
	// BattleMap m = t.getMap();
	// Thing[] ts = m.getThings(t.x, t.y);
	// // for (Thing th : ts) {
	// //
	// // // if (th.getFlag("IsBeing")) {
	// // // AI.setGuard(th,t);
	// // // }
	// // }
	// t.set("OnAction", null);
	// return false;
	// }
	// });
	// Lib.add(t);
	// }

	private static void initBuilderPoints() {
		Thing t;

		t = Lib.extend("redistribution point", "base special");
		t.set("OnAction", new Script() {
			@Override
			public boolean handle(Thing t, Event e) {
				BattleMap m = t.getMap();
				Thing[] ts = m.getThings(t.x, t.y);
				for (Thing element : ts) {
					m.addThing(element);
				}
				t.remove();
				return false;
			}
		});
		t.set("Image", 145);
		Lib.add(t);

	}

	public static void initClouds() {
		Thing t = Lib.extend("base cloud", "base thing");
		t.set("IsBlocking", 0);
		t.set("IsInvisible", 0);
		t.set("IsPhysical", 0);
		t.set("DecayRate", 1000);
		t.set("ImageSource", "Effects");
		t.set("Image", 42);
		t.set("RES:normal", 1000);
		t.set("RES:impact", 1000);
		t.set("RES:piercing", 1000);
		t.set("RES:shock", 1000);
		t.set("IsActive", 1);
		t.set("MoveSpeed", 30);
		t.set("OnAction", new CloudAction());
		t.set("OnEnterTrigger", new CloudTouch());
		t.set("OnTouch", new CloudTouch());
		t.addHandler("OnAction", Scripts.decay());
		t.set("Z", Thing.Z_OVERHEAD);
		Lib.add(t);

		t = Lib.extend("poison cloud", "base cloud");
		t.set("Image", 47);
		t.set("AffectType", "IsLiving");
		t.set("OnEffect", Scripts.addEffect("Target", "poison"));
		t.set("LevelMin", 1);
		t.set("RES:poison", 1000);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 1);
		Lib.add(t);

		t = Lib.extend("acid cloud", "base cloud");
		t.set("Image", 27);
		t.set("AffectType", "IsPhysical");
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 0);
		t.set("AreaDamage", 3);
		t.set("RES:acid", 1000);
		t.set("AreaDamageType", RPG.DT_ACID);
		t.set("AreaDamageMessage", "The acid cloud burns your flesh!");
		Lib.add(t);

		t = Lib.extend("flame cloud", "base cloud");
		t.set("Image", 7);
		t.set("AffectType", "IsPhysical");
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 0);
		t.set("RES:fire", 1000);
		t.set("AreaDamage", 4);
		t.set("AreaDamageType", RPG.DT_FIRE);
		t.set("AreaDamageMessage", "The flame cloud burns you!");
		Lib.add(t);

		t = Lib.extend("steam cloud", "base cloud");
		t.set("Image", 87);
		t.set("LevelMin", 1);
		t.set("SingleEffect", 0);
		t.set("RES:fire", 1000);
		t.set("AreaDamage", 1);
		t.set("AreaDamageType", RPG.DT_WATER);
		t.set("AreaDamageMessage", "The steam cloud burns you!");
		Lib.add(t);

		t = Lib.extend("cloud of misfortune", "base cloud");
		t.set("Image", 27);
		t.set("AffectType", "IsBeing");
		t.set("OnEffect", Scripts.addEffect("Target", "curse"));
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 1);
		Lib.add(t);

		t = Lib.extend("cloud of pestilence", "base cloud");
		t.set("Image", 27);
		t.set("AffectType", "IsLiving");
		t.set("OnEffect", Scripts.addEffect("Target", "pestilence"));
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 1);
		t.addHandler("OnAction", Scripts.generator("cloud of pestilence",
				t.getStat("DecayRate")));
		Lib.add(t);

		t = Lib.extend("cloud of confusion", "base cloud");
		t.set("Image", 27);
		t.set("AffectType", "IsLiving");
		t.set("OnEffect", Scripts.addEffect("Target", "confusion"));
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 1);
		Lib.add(t);

		t = Lib.extend("plague cloud", "base cloud");
		t.set("Image", 27);
		t.set("AffectType", "IsLiving");
		t.set("OnEffect", Scripts.addEffect("Target", "plague"));
		t.set("LevelMin", 1);
		t.set("IsWarning", 1);
		t.set("SingleEffect", 1);
		t.addHandler("OnAction",
				Scripts.generator("plague cloud", t.getStat("DecayRate")));
		Lib.add(t);

	}
}