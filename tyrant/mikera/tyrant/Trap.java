package tyrant.mikera.tyrant;

import java.util.ArrayList;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Trap {
	public static Thing create() {
		return createTrap(Lib.currentLevel());
	}

	public static Thing create(int l) {
		return createTrap(l);
	}

	public static Thing createTrap(int l) {
		return Lib.createType("IsTrap", l);
	}

	public static void setActivated(Thing t, boolean b) {
		t.set("IsActivated", b);
	}

	public static boolean isActivated(Thing t) {
		return t.getFlag("IsActivated");
	}

	// trigger in specific location
	public static void trigger(Thing t) {
		if (t.handles("OnEnterTrigger")) {
			Event e = new Event("EnterTrigger");
			Thing target = t.getMap().getMobile(t.x, t.y);
			e.set("Target", target);
			t.visibleMessage(t.getAName() + " is triggered!");

			if ((target != null) && target.isHero()) {
				target.isRunning(false);
			}
			t.handle(e);
		}
	}

	public static boolean applyDisarmTraps(Thing b) {
		Game.messageTyrant("Disarm trap in which direction?");
		Point p = Game.getDirection();

		Thing trap =
				b.getMap().getFlaggedObject(b.x + p.x, b.y + p.y, "IsTrap");
		if ((trap != null) && trap.isVisible(Game.hero())) {
			return tryDisarm(b, trap);
		}
		Game.messageTyrant("No trap found.");
		return false;
	}

	public static boolean tryDisarm(Thing b, Thing trap) {
		b.incStat("APS", -200);

		int skill = b.getStat(Skill.DISARM) * b.getStat("CR");
		int diff = trap.getStat("Level") * 5;
		int r = 0;
		if (RPG.test(skill, diff)) {
			r++;
		}
		if (RPG.test(skill, diff)) {
			r++;
		}

		switch (r) {
		case 0:
			Game.messageTyrant("You trigger the trap by accident!");
			Trap.trigger(trap);
			return true;
		case 1:
			Game.messageTyrant("You fail to disarm the trap");
			b.incStat("APS", -200);
			return true;
		case 2:
			Game.messageTyrant("You disarm the trap successfully");
			Trap.disarm(trap);
			return true;
		}

		return true;
	}

	public static void disarm(Thing trap) {
		trap.die();
	}

	public static void init() {
		Thing t;

		t = Lib.extend("base trap", "base secret");
		t.set("IsTrap", 1);
		t.set("ImageSource", "Scenery");
		t.set("IsDestructible", 0);
		t.set("Image", 164);
		t.set("IsWarning", 1);
		t.set("IsInvisible", (Game.isDebug()) ? 0 : 1);
		t.set("Frequency", 50);
		t.set("IsActivated", 1);
		t.set("Uses", 1);
		t.set("LevelMin", 1);
		t.set("Z", Thing.Z_FLOOR);
		t.set("ASCII", "^");
		Lib.add(t);

		initSpecialTraps();
		initSpellTraps();

		RuneTrap.init();
	}

	protected static class SpellTrapTrigger extends Script {
		private static final long serialVersionUID = 4049353132389774130L;

		@Override
		public boolean handle(Thing rt, Event e) {
			BattleMap map = rt.getMap();
			int tx = rt.x;
			int ty = rt.y;

			rt.remove();
			Thing temp = Game.actor;

			if (map != null) {
				Thing sp = Spell.create(rt.getString("TrapSpell"));
				Spell.castAtLocation(sp, null, map, tx, ty);
				Thing actor = (Thing) rt.get("Actor");
				if (actor != null) {
					actor.message("Your power is drained by the runes");
					actor.incStat("MPS", -sp.getStat("SpellCost"));
				}
			}
			Game.actor = temp;
			return false;
		}

	}

	protected static class PitTrapTrigger extends Script {
		private static final long serialVersionUID = 3257008748072613943L;

		@Override
		public boolean handle(Thing trap, Event e) {
			BattleMap map = trap.getMap();

			Thing tt = e.getThing("Target");

			Thing pit = Lib.create("pit");
			pit.set("DestinationLevel",
					RPG.middle(1, map.getLevel() + RPG.d(3) - RPG.d(3), 50));

			// replave the trap object with the pit portal
			trap.replaceWith(pit);
			pit = trap;

			if (tt != null) {
				Portal.travel(pit, tt);

				// land in random square
				BattleMap pitMap = tt.getMap();
				Point p = pitMap.findFreeSquare();
				if (p == null) {
					throw new Error("Unable to find a free square in pit");
				}
				if ((p != null) && (tt != null)) {
					tt.message("You have fallen into a pit!");
					tt.moveTo(pitMap, p.x, p.y);

				}
				return true;
			}

			return false;
		}

	}

	protected static class AmbushTrapTrigger extends Script {
		private static final long serialVersionUID = 3258135751886516790L;

		@Override
		public boolean handle(Thing t, Event e) {
			BattleMap map = t.getMap();
			int level = map.getLevel();
			Thing a = Lib.createType("IsMonster", level);

			Thing tt = e.getThing("Target");

			boolean ambush = false;
			for (int i = RPG.d(6); i > 0; i--) {
				ambush |= map.addBlockingThing(Lib.create(a.name()), t.x - 1,
						t.y - 1, t.x + 1, t.y + 1);
			}
			if (tt != null) {
				if (ambush) {
					tt.message("You have been ambushed!");
				} else {
					tt.message("You feel you are being watched");
				}
			}
			t.remove();

			return false;
		}

	}

	protected static class RockfallTrapTrigger extends Script {
		private static final long serialVersionUID = 4121129234254214969L;

		@Override
		public boolean handle(Thing rt, Event e) {
			BattleMap map = rt.getMap();
			int tx = rt.x;
			int ty = rt.y;

			Thing tt = e.getThing("Target");

			rt.remove();
			Lib.create("pit");

			if (tt != null) {
				tt.message("You are hit by falling rocks!");
				Damage.inflict(tt, RPG.d(20), "impact");
			}

			map.addThing(Lib.create("rock"), tx, ty);
			map.addThing(Lib.create("stone"), tx, ty);

			return false;
		}

	}

	protected static class AttributeTrapTrigger extends Script {
		private static final long serialVersionUID = 3256721771276022839L;

		@Override
		public boolean handle(Thing rt, Event e) {
			Thing tt = e.getThing("Target");

			rt.remove();

			if (tt != null) {
				tt.message(getString("Message"));
				tt.addAttribute(Lib.create(getString("Effect")));
			}

			return false;
		}

	}

	public static void initSpellTrap(Thing s) {
		if (s.getStat("SpellUsage") == Spell.SPELL_OFFENCE) {
			String sn = s.name();
			Thing t = Lib.extend(sn + " trap", "base trap");
			t.set("TrapSpell", sn);
			t.set("Image", 47);
			t.set("Level", s.getLevel());
			t.set("LevelMin", s.getLevel());
			t.set("OnEnterTrigger", new SpellTrapTrigger());

			t.set("DeathDecoration", "10% wand of " + sn);
			Lib.add(t);
		}
	}

	public static void initSpellTraps() {
		ArrayList spells = Spell.getSpellNames();

		for (int i = 0; i < spells.size(); i++) {
			Thing s = Spell.create((String) spells.get(i));
			initSpellTrap(s);
		}
	}

	public static void initSpecialTraps() {
		Thing t;

		t = Lib.extend("pit trap", "base trap");
		t.set("OnEnterTrigger", new PitTrapTrigger());
		Lib.add(t);

		t = Lib.extend("ambush trap", "base trap");
		t.set("OnEnterTrigger", new AmbushTrapTrigger());
		Lib.add(t);

		t = Lib.extend("rockfall trap", "base trap");
		t.set("OnEnterTrigger", new RockfallTrapTrigger());
		Lib.add(t);

		t = Lib.extend("blindness trap", "base trap");
		{
			AttributeTrapTrigger att = new AttributeTrapTrigger();
			att.set("Message", "You are blinded by a sudden flash!");
			att.set("Effect", "curse of blindness");
			t.set("OnEnterTrigger", att);
		}
		Lib.add(t);

		t = Lib.extend("poison trap", "base trap");
		{
			AttributeTrapTrigger att = new AttributeTrapTrigger();
			att.set("Message", "You are hit by a jet of poisonous gas!");
			att.set("Effect", "poison");
			t.set("OnEnterTrigger", att);
			t.set("DeathDecoration", "potion of poison");
		}
		Lib.add(t);

		t = Lib.extend("curse trap", "base trap");
		{
			AttributeTrapTrigger att = new AttributeTrapTrigger();
			att.set("Message",
					"You feel like this is not going to be a good day...");
			att.set("Effect", "curse");
			t.set("OnEnterTrigger", att);
		}
		Lib.add(t);

	}
}