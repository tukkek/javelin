/*
 * Created on 01-Jul-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Scripts {

	private static class AddEffectScript extends Script {
		private static final long serialVersionUID = 9106886823123784009L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing effect = getThing("Effect");
			int chance = getStat("Chance");

			Thing tt = (Thing) e.get(getString("TargetProperty"));

			// check for effect resistance
			String rstat = effect.getString("ResistStat");
			if (rstat != null) {
				int res = tt.getStat(rstat);
				if (RPG.test(res, effect.getStat("ResistDifficulty"))) {
					tt.message(effect.getString("ResistMessage"));
					return false;
				}
			}

			// add effect if probability check passes
			if (e != null && RPG.r(100) < chance) {
				tt.addAttribute(new Thing(effect));
			}
			return false;
		}
	}

	private static class AddThingScript extends Script {
		private static final long serialVersionUID = 3257004345781465909L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing tt = (Thing) e.get(getString("TargetProperty"));
			if (e != null && RPG.r(100) < getStat("Chance")) {
				tt.addThing(Lib.create(getString("ThingName")));
			}
			return false;
		}
	}

	public static Script addThing(String targetProperty, String thingName) {
		return addThing(targetProperty, thingName, 100);
	}

	public static Script addThing(String targetProperty, String thingName,
			int prob) {
		AddThingScript aeh = new AddThingScript();
		aeh.set("TargetProperty", targetProperty);
		aeh.set("ThingName", thingName);
		aeh.set("Chance", prob);
		return aeh;
	}

	public static Script addEffect(String targetProperty, Thing effect) {
		return addEffect(targetProperty, effect, 100);
	}

	public static Script addEffect(String targetProperty, String effectName) {
		return addEffect(targetProperty, Lib.create(effectName), 100);
	}

	public static Script damage(String dt, int dam, String desc, int prob) {
		Script s = new Script() {
			private static final long serialVersionUID = 3258130258556696629L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing target = e.getThing("Target");
				int dam = getStat("Damage");
				int prob = getStat("DamageProbability");
				if (RPG.r(100) >= prob) {
					return false;
				}
				String dt = getString("DamageType");
				String desc = getString("DamageDescription");

				Game.instance().pushMessages();
				dam = Damage.inflict(target, dam, dt);
				java.util.ArrayList al = Game.instance().popMessages();
				if (dam > 0 && desc != null) {
					Game.messageTyrant(target.getTheName() + " " + target.is()
							+ " " + desc);
				}
				Game.message(al);

				return false;
			}
		};
		s.set("DamageType", dt);
		s.set("Damage", dam);
		s.set("DamageDescription", desc);
		s.set("DamageProbability", prob);

		return s;
	}

	public static Script areaDamage(String dt, int dam, int rate, String desc,
			String flag) {
		Script s = new Script() {
			private static final long serialVersionUID = 3258130258556696629L;

			@Override
			public boolean handle(Thing t, Event e) {
				BattleMap m = t.getMap();
				if (m == null) {
					return false;
				}
				Thing tt = m.getObjects(t.x, t.y);
				if (tt == t) {
					tt = tt.next;
				}
				if (tt != null) {
					String flag = getString("DamageFlag");
					while (tt != null && (flag == null || tt.getFlag(flag))) {
						int hits = RPG.round(getStat("DamageRate")
								* e.getStat("Time") / 1000000.0);
						if (hits > 0) {
							int dam = 0;
							String dt = getString("DamageType");
							int st = getStat("Damage");
							for (int i = 0; i < hits; i++) {
								dam += Damage.inflict(tt, st, dt);
							}
							if (dam > 0) {
								tt.message(getString("DamageDescription"));
							}

						}
						tt = tt.next;
						if (tt == t) {
							tt = tt.next;
						}
					}
				}

				return false;
			}
		};
		s.set("DamageType", dt);
		s.set("Damage", dam);
		s.set("DamageDescription", desc);
		s.set("DamageRate", rate);
		s.set("DamageFlag", flag);
		return s;
	}

	/**
	 * Creates a Script that will add an effect to a target Thing when invoked
	 * 
	 * @param targetProperty
	 *            Then name of the event property that contains the target
	 * @param effect
	 *            The effect to be added to the target
	 * @param prob
	 *            Probability of effect being applied
	 * @return A handler script that will add the specified effect to the target
	 */
	public static Script addEffect(String targetProperty, Thing effect, int prob) {
		AddEffectScript aeh = new AddEffectScript();
		aeh.set("TargetProperty", targetProperty);
		aeh.set("Effect", new Thing(effect));
		aeh.set("Chance", prob);
		return aeh;
	}

	public static Script statGain(String targetProperty, String stats, int max) {
		return statGain(targetProperty, stats, max, 100);
	}

	public static Script statGain(String targetProperty, String stats, int max,
			int chance) {
		Script sc = new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing tt = (Thing) e.get(getString("TargetProperty"));
				if (tt == null) {
					throw new Error("No target for statGain!");
				}

				String stats = getString("Stats");
				String[] ss = stats.split(",");
				String stat = ss[RPG.r(ss.length)];

				// work out max, with bonus/penalty for item status
				int max = getStat("Max");
				if (t.getFlag("IsBlessed")) {
					max = max * 3 / 2;
				}
				if (t.getFlag("IsCursed")) {
					max = max / 2;
				}

				int averageStat = Being.averageStat(tt);
				int current = tt.getStat(stat);
				boolean canGain = max > averageStat;
				int chance = getStat("Chance");

				// can increase a single stat too high
				// higher chance increases maximum
				if (current * 100.0 > max * (120.0 + chance)) {
					canGain = false;
				}

				if (canGain && RPG.d(100) <= chance) {
					Being.gainStat(tt, stat);
				} else if ((tt.getFlag(Skill.PERCEPTION) || Item
						.isIdentified(t)) && canGain) {
					String sn = new String[] { "skill", "strength", "agility",
							"toughness", "intelligence", "willpower",
							"charisma", "craft" }[Being.statIndex(stat)];
					tt.message("You feel good about your " + sn);
				}
				return false;
			}
		};
		sc.set("Stats", stats);
		sc.set("Chance", chance);
		sc.set("Max", max);
		sc.set("TargetProperty", targetProperty);

		return sc;
	}

	public static Script heal(String targetProperty, int amount, int chance) {
		Script script = new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing target = (Thing) e.get(getString("TargetProperty"));
				int amount = getStat("Increment");
				int chance = getStat("Chance");
				if (!target.getFlag("IsBeing")) {
					return false;
				}
				if (RPG.d(100) > chance) {
					return false;
				}
				if (target.getFlag("IsUndead")) {
					amount = -amount;
				}
				int hps = target.getStat("HPS");
				int hpsmax = target.getStat("HPSMAX");
				int gain = RPG.min(amount, hpsmax - hps);
				if (gain > 0) {
					if (gain > hps) {
						target.message("You feel much better!");
					} else if (gain + hps == hpsmax) {
						target.message("You feel superb");
					} else {
						target.message("You feel better");
					}
				} else if (hps == hpsmax) {
					target.message("You couldn't feel any better!");
				} else if (gain < 0) {
					target.message("You feel strangely worse");
				}
				target.incStat("HPS", gain);
				return false;
			}
		};
		script.set("Increment", amount);
		script.set("Chance", chance);
		script.set("TargetProperty", targetProperty);

		return script;
	}

	public static Script cure(int amount) {
		Script s = new CureScript();
		s.set("Strength", amount);
		return s;
	}

	private static class CureScript extends Script {
		private static final long serialVersionUID = 3978138846573377330L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing target = e.getThing("Target");
			int eff = e.getStat("Strength");

			int heal = eff;
			Poison.cure(target, heal);

			return false;
		}
	}

	public static Script incStat(String targetProperty, String stats,
			int amount, String message) {
		Script sc = new Script() {
			private static final long serialVersionUID = 3691036565141074487L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing tt = (Thing) e.get(getString("TargetProperty"));
				String stats = getString("Stats");
				String[] ss = stats.split(",");
				String stat = ss[RPG.r(ss.length)];

				tt.incStat(stat, getStat("Increment"));
				tt.message(getString("IncMessage"));
				return false;
			}
		};
		sc.set("Stats", stats);
		sc.set("Increment", amount);
		sc.set("IncMessage", message);
		sc.set("TargetProperty", targetProperty);

		return sc;
	}

	public static Script spellEffect(String targetProperty, String spell,
			int chance) {
		Script sc = new Script() {
			private static final long serialVersionUID = 3257849865827530038L;

			@Override
			public boolean handle(Thing t, Event e) {
				if (RPG.r(100) >= getStat("Chance")) {
					return false;
				}

				Thing tt = (Thing) e.get(getString("TargetProperty"));
				Thing s = getThing("Spell");
				Spell.castAtObject(s, null, tt);
				return false;
			}
		};
		Thing s = Lib.create(spell);
		sc.set("Spell", s);
		sc.set("Chance", chance);
		sc.set("TargetProperty", targetProperty);

		return sc;
	}

	private static class GeneratorScript extends Script {
		private static final long serialVersionUID = 7473182575628000762L;

		@Override
		public boolean handle(Thing t, Event e) {
			BattleMap map = t.getMap();
			if (battlemap == null) {
				return false;
			}
			if (battlemap.getFlag("IsWorldMap")) {
				return false;
			}

			int level = getStat("GenerationLevel");
			if (level == 0) {
				level = Game.level();
			}

			int time = e.getStat("Time");
			int gr = getStat("GenerationRate");
			if (gr > 0) {
				String s = getString("GenerationType");
				int inPlace = getStat("GenerationInPlace");
				for (int i = RPG.po(time * gr, 1000000); i > 0; i--) {
					Thing nt;
					if (s == null) {
						nt = Lib.createMonster(level);
					} else {
						String[] ss = s.split(",");
						nt = Lib.create(ss[RPG.r(ss.length)], level);
					}
					boolean added = false;
					if (inPlace > 0) {
						battlemap.addThing(nt, t.getMapX(), t.getMapY());
					} else {
						added = battlemap.addBlockingThing(nt, t.getMapX() - 1,
								t.getMapY() - 1, t.getMapX() + 1,
								t.getMapY() + 1);
					}

					if (added && t.getFlag("IsBeing") && nt.getFlag("IsBeing")
							&& nt.getLevel() > t.getLevel() - 10) {
						// AI.setFollower(nt,t);
					}
				}
			}
			return false;
		}
	}

	private static class DecayScript extends Script {
		private static final long serialVersionUID = -8785297945265824277L;

		@Override
		public boolean handle(Thing t, Event e) {
			int time = e.getStat("Time");
			BattleMap map = t.getMap();
			int x = t.getMapX();
			int y = t.getMapY();

			boolean whileCarried = getFlag("WhileCarried");
			if (!whileCarried && battlemap != t.place) {
				return false;
			}

			if (t.getFlag("DecayRate")) {
				int dr = t.getStat("DecayRate");
				if (RPG.r(1000000) < dr * time) {
					String s = t.getString("DecayType");
					String dmes = t.getString("DecayMessage");
					if (dmes != null && t.isVisible(Game.hero())) {
						Game.messageTyrant(t.getTheName() + " " + dmes);
					}

					if (s != null) {
						boolean blocked = battlemap.isBlocked(x, y);

						Thing nt = Lib.create(s);
						battlemap.addThing(nt, x, y);
						if (blocked) {
							nt.displace();
						}
					}
					t.die();
					return true;
				}
			}

			if (t.getFlag("LifeTime")) {
				int left = t.getStat("LifeTime") - time;
				t.set("LifeTime", left);
				if (left <= 0) {
					t.remove();
					return true;
				}
			}
			return false;
		}
	}

	private static class CombinedScript extends Script {
		private static final long serialVersionUID = 543897876490907635L;

		@Override
		public boolean handle(Thing t, Event e) {
			EventHandler[] ss = (EventHandler[]) get("Scripts");
			for (EventHandler element : ss) {
				if (element != null && element.handle(t, e)) {
					return true;
				}
			}
			return false;
		}
	}

	public static Script combine(EventHandler a, EventHandler b) {
		if (a instanceof CombinedScript) {
			CombinedScript as = (CombinedScript) a;
			EventHandler[] ss = (EventHandler[]) as.get("Scripts");
			int n = ss.length;
			EventHandler[] ns = new EventHandler[n + 1];
			System.arraycopy(ss, 0, ns, 0, n);
			ns[n] = b;
			CombinedScript cs = new CombinedScript();
			cs.set("Scripts", ns);
			return cs;
		}
		EventHandler[] ss = new EventHandler[] { a, b };
		CombinedScript cs = new CombinedScript();
		cs.set("Scripts", ss);
		return cs;
	}

	public static Script generator(String type, int rate, int level) {
		GeneratorScript gs = new GeneratorScript();
		gs.set("GenerationType", type);
		gs.set("GenerationRate", rate);
		gs.set("GenerationLevel", level);
		return gs;
	}

	public static Script generator(String type, int rate) {
		GeneratorScript gs = new GeneratorScript();
		gs.set("GenerationType", type);
		gs.set("GenerationRate", rate);
		return gs;
	}

	/**
	 * Wrapper for GeneratorScript to generate an item in the same location as
	 * the thing generating it. Use for bushes, vines and patches to leave the
	 * generated fruit in the bush. The player won't "see" it until he walks
	 * over the bush.
	 * 
	 * @param The
	 *            thing to generate
	 * @param The
	 *            rate at which to generate it
	 * @return A Script object suitable for use as an argument to addHandler().
	 */
	public static Script generatorInPlace(String type, int rate) {
		GeneratorScript script = new GeneratorScript();
		script.set("GenerationType", type);
		script.set("GenerationRate", rate);
		script.set("GenerationInPlace", 1);
		return script;
	}

	public static Script statusSwitch(Script blessed, Script normal,
			Script cursed) {
		Script s = new Script() {
			private static final long serialVersionUID = 3544957670705740086L;

			@Override
			public boolean handle(Thing t, Event e) {
				if (t.getFlag("IsCursed")) {
					return getHandler("WhenCursed").handle(t, e);
				} else if (t.getFlag("IsBlessed")) {
					return getHandler("WhenBlessed").handle(t, e);
				} else {
					return getHandler("WhenNormal").handle(t, e);
				}
			}
		};
		s.set("WhenBlessed", blessed);
		s.set("WhenNormal", normal);
		s.set("WhenCursed", cursed);
		return s;
	}

	public static Script decay() {
		return decay(false);
	}

	public static Script die() {
		return new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				t.die();
				return false;
			}
		};
	}

	public static Script returnTrue(Script s) {
		Script ns = new Script() {

			@Override
			public boolean handle(Thing t, Event e) {
				Script innerScript = (Script) get("Script");
				innerScript.handle(t, e);
				return true;
			}
		};
		ns.set("Script", s);
		return ns;
	}

	public static Script message(String message) {
		Script s = new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing tt = e.getThing("Target");
				tt.message(getString("Message"));
				return false;
			}
		};
		s.set("Message", message);
		return s;
	}

	public static Script decay(boolean whileCarried) {

		Script s = new DecayScript();
		s.set("WhileCarried", whileCarried);
		return s;
	}
}
