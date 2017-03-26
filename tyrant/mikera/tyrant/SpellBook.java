//
// When read, the SpellBook confers the ability to cast a particular spell
// on the reader if they are sufficiently skilled
//

package tyrant.mikera.tyrant;

import java.util.ArrayList;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;


public class SpellBook {
	private static final String[] titles = {"worn tome", "tattered tome",
			"dull tome", "slim book", "greyish tome", "leather-bound tome",
			"weighty tome"};

	// create spell book of correct level value
	public static Thing create(int level) {
		return Lib.createType("IsSpellBook", level);
	}

	public static Thing create() {
		return create(Lib.currentLevel());
	}
	
	// create spell book of correct level value and order
	public static Thing create(String order, int level) {
		for (int i=0; i<30; i++) {
			Thing t=Lib.createType("IsSpellBook", level);
			if (t.getstring("Order").equals(order)) return t;
		}
		return Lib.createType("IsSpellBook", level);
	}

	public static String getOrder(Thing book) {
		Thing spell=Lib.create(book.getstring("BookSpell"));
		return spell.getstring("Order");
	}
	
	private static class ReadSpellbookScript extends Script {
		private static final long serialVersionUID = 6685634600657516648L;

        public boolean handle(Thing s, Event e) {
			Thing user = e.getThing("Reader");

			if (!s.getFlag("IsSpellBook")) {
				System.out.println("Not a spell book!");
				return false;
			}

			if (!user.isHero())
				return false;

			String spellName=s.getstring("BookSpell");
			
			// TODO implement difficulty
			if (Spell.canLearn(user,spellName)) {

				// try to learn spell
				Spell.learn(user, spellName);
	
				Item.identify(s);
				s.remove(1);
			} else {
				Game.messageTyrant("You are unable to understand this spellbook without knowledge of "+Spell.getOrder(spellName));
			}

			return false;
		}
	}

	private static class ReadRecipeBookScript extends Script {
		private static final long serialVersionUID = 3616728296604514359L;

        public boolean handle(Thing s, Event e) {
			Thing user = e.getThing("Reader");

			if (!s.getFlag("IsRecipeBook")) {
				System.out.println(s.name()+" is not a recipe book!");
				return false;
			}

			if (!user.isHero())
				return false;

			String order=s.getstring("Order");
			if (user.getStat(order)>0) {
			
				// TODO implement learning
				user.message("You discover no recipes of particular interest");
			} else {
				user.message("You need to learn "+order+" in order to undertand these recipes");
			}
			return false;
		}
	}

	private static class ReadManualScript extends Script {
		private static final long serialVersionUID = 3256723983117464881L;

        public boolean handle(Thing s, Event e) {
			Thing h = e.getThing("Reader");
			String skill = s.getstring("SkillName");
			
			if (!s.getFlag("IsManual")) {
				System.out.println("Not a manual!");
				return false;
			}

			if (!h.isHero())
				return false;

			h.incStat("APS",-2000/h.getStat(Skill.LITERACY));
			if (!Item.isIdentified(s)) {
				Item.identify(s);
				h.message("Do you want to try to learn from " + s.getTheName()
						+ "? (y/n)");
				if (Game.getOption("yn") == 'n')
					return false;
			}

			if (h.getBaseStat(RPG.ST_SKILLPOINTS) >= 2) {
				// learning cost if cursed
				if (!s.getFlag("IsBlessed")&&(s.getFlag("IsCursed") || RPG.d(4) == 1)) {
					h.message("It doesn't seem that the manual is very well written");				
					h.incStat(RPG.ST_SKILLPOINTS, -1);
				}
				
				if (RPG.test(h.getStat("IN"), 10, h, null)) {
					
					// extra cost if not bright enough
					if (!RPG.test(h.getStat("IN"), 100, h, null)) {
						h.incStat(RPG.ST_SKILLPOINTS, -1);
						h.message("You find the manual very tough going");
					}
					
					if (h.getBaseStat(RPG.ST_SKILLPOINTS) >= 1) {
						Game.messageTyrant("You teach yourself about "+skill);
						Skill.add(h,skill,1);
						
						// standard learning cost
						h.incStat(RPG.ST_SKILLPOINTS, -1);

					} else {
						h.message("You get yourself confused.... you don't manage to learn anything");
					}
				} else {
					h.message("You don't manage to make sense of "
							+ s.getTheName());
				}
			} else {
				h.message("This manual seems too hard to master right now");
			}

			return false;
		}
	}

	public static void init() {
		Thing t = Lib.extend("base book", "base item");
		t.set("Frequency", 50);
		t.set("IsReadable", 1);
		t.set("IsBook", 1);
        t.set("RES:water",0);
		t.set("ItemWeight", 1500);
		t.set("ASCII","+");
		Lib.add(t);

		t = Lib.extend("base spellbook", "base book");
		t.set("IsSpellBook", 1);
		t.set("OnRead", new ReadSpellbookScript());
		t.set("IsMagicItem", 1);
		t.set("Image", 285);
		t.set("HPS", 12);
		t.set("ValueBase", 1000);
		t.set("ItemWeight", 2000);
		t.set("Frequency", 60);
		Lib.add(t);

		initSpellBooks();
		initManuals();
		initRecipeBooks();
	}

	private static void addManual(String s) {
		
		Thing t=Lib.extend(s+" manual","base manual");
		t.set("UName","heavy manual");
		t.set("OnRead",new ReadManualScript());
		t.set("SkillName",s);
		t.set("LevelMin",RPG.d(30));
		t.set("Image", 284);
		t.set("Frequency",20);
		Lib.add(t);
	}

	private static void initManuals() {
		Thing t = Lib.extend("base manual", "base book");
		t.set("ValueBase", 4000);
		t.set("ItemWeight", 3500);
		t.set("IsManual",1);
		Lib.add(t);
		
		String[] skills=Skill.fullList();
		
		for (int i=0; i<skills.length; i++) {
			addManual(skills[i]);
		}
	}
	
	private static void initRecipeBooks() {
		Thing t = Lib.extend("base recipe book", "base book");
		t.set("ValueBase", 1000);
		t.set("UName","strange recipe book");
		t.set("ItemWeight", 3500);
		t.set("Image",285);
		t.set("Frequency",30);
		t.set("IsRecipeBook",1);
		t.set("OnRead",new ReadRecipeBookScript());
		Lib.add(t);
		
		String[] recipeOrders={Skill.HERBLORE,Skill.ALCHEMY,Skill.BLACKMAGIC};
		for (int ord=0; ord<recipeOrders.length; ord++) {
			String order=recipeOrders[ord];
			for (int i=1; i<=16; i++) {
				t=Lib.extend(order+" "+Text.roman(i)+" recipe book","base recipe book");
				t.set("LevelMin",i*3);
				t.set("Order",order);
				Lib.add(t);
			}
		}
	}

	private static void initSpellBooks() {
		// assemble list of titles
		ArrayList ta = new ArrayList();
		for (int i = 0; i < titles.length; i++) {
			ta.add(titles[i]);
		}

		// assemble list of spells
		ArrayList sa = Spell.getSpellNames();
		if (sa.size() == 0)
			throw new Error("SpellBook: Spells not yet generated");

		int ns = sa.size();

		for (int i = 0; i < ns; i++) {
			String spellName = (String) sa.get(i);
			String title = (String) ta.get(RPG.r(ta.size()));

			Thing spell = Spell.create(spellName);

			Thing t = Lib.extend("xxx", "base spellbook");
			t.set("Name", "spellbook of " + spellName);
			t.set("Image", spellBookImage(spell) );
			t.set("NamePlural", "spellbooks of " + spellName);
			t.set("UName", title);
			t.set("BookSpell", spellName);
			t.set("Order",spell.getstring("Order"));
			t.set("DeathDecoration",spellName);

			t.set("LevelMin", spell.getLevel());
			t.multiplyStat("Frequency", spell.getStat("Frequency") / 100.0);
			t.set("LevelMax", spell.getLevel() + 20);

			Lib.add(t);
		}
	}
	
	private static int spellBookImage(Thing s) {
		String order=s.getstring("Order");
		if (order.equals(Skill.HOLYMAGIC)) {
			return 287;
		} else if (order.equals(Skill.BLACKMAGIC)) {
			return 286;
		} else if (order.equals(Skill.TRUEMAGIC)) {
			return 285;
		}
        throw new Error("Spell book order not identified ["+order+"]");
	}

}