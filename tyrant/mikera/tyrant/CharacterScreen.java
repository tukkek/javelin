//package tyrant.mikera.tyrant;
//
//import java.awt.Graphics;
//
//import javelin.controller.Movement;
//import javelin.view.MapPanel;
//
//import tyrant.mikera.engine.Describer;
//import tyrant.mikera.engine.Description;
//import tyrant.mikera.engine.RPG;
//import tyrant.mikera.engine.Thing;
//import tyrant.mikera.tyrant.util.Text;
//
//
//
///**
// * A screen for displaying key information about the character
// * 
// * 
// * @author Mike
// * 
// */
//public class CharacterScreen extends Screen {
//	private static final long serialVersionUID = 3257845463519868464L;
//	public Thing character;
//
//	public CharacterScreen(final Thing ch) {
//		super(Game.getQuestapp());
//
//		setForeground(QuestApp.INFOTEXTCOLOUR);
//		setBackground(QuestApp.INFOSCREENCOLOUR);
//
//		setFont(QuestApp.mainfont);
//
//		character = ch;
//	}
//
//	private String statString(final String s) {
//		final int bs = character.getBaseStat(s);
//		final int ms = character.getStat(s) - bs;
//		return Text.centrePad(new Integer(bs).toString(), (ms >= 0 ? "(+" + ms
//				: "(" + ms)
//				+ ")", 8);
//	}
//
//	@Override
//	public void paint(final Graphics g) {
//		super.paint(g);
//
//		final Thing h = character;
//
//		g.setColor(QuestApp.INFOTEXTCOLOUR);
//
//		String hname = h.getString("HeroName");
//		if (hname == null) {
//			hname = h.name();
//		}
//		g.drawString(hname, 40, 20);
//		g.drawString(Text.capitalise(h.getString("Race")) + " "
//				+ Text.capitalise(h.getString("Profession")), 240, 20);
//
//		{
//			final int image = h.getImage();
//			final int sx = image % 20 * MapPanel.TILEWIDTH;
//			final int sy = image / 20 * MapPanel.TILEHEIGHT;
//			final int px = 4;
//			final int py = 4;
//			g.drawImage(QuestApp.creatures, px, py, px + MapPanel.TILEWIDTH, py
//					+ MapPanel.TILEHEIGHT, sx, sy, sx + MapPanel.TILEWIDTH, sy
//					+ MapPanel.TILEHEIGHT, null);
//		}
//
//		g.drawString("SKA: " + statString(RPG.ST_SK), 40, 40);
//		g.drawString("ST: " + statString(RPG.ST_ST), 40, 40 + 1 * 16);
//		g.drawString("AG: " + statString(RPG.ST_AG), 40, 40 + 2 * 16);
//		g.drawString("TG: " + statString(RPG.ST_TG), 40, 40 + 3 * 16);
//		g.drawString("IN: " + statString(RPG.ST_IN), 40, 40 + 4 * 16);
//		g.drawString("WP: " + statString(RPG.ST_WP), 40, 40 + 5 * 16);
//		g.drawString("CH: " + statString(RPG.ST_CH), 40, 40 + 6 * 16);
//		g.drawString("CR: " + statString(RPG.ST_CR), 40, 40 + 7 * 16);
//
//		g.drawString("Hit Points    : " + h.getStat(RPG.ST_HPS) + "/"
//				+ character.getStat(RPG.ST_HPSMAX), 40, 40 + 9 * 16);
//		g.drawString("Magic Points  : " + h.getStat(RPG.ST_MPS) + "/"
//				+ character.getStat(RPG.ST_MPSMAX), 40, 40 + 10 * 16);
//		g.drawString("Base Speed    : " + h.getStat("Speed"), 40, 40 + 11 * 16);
//		g.drawString("Move Speed    : " + Movement.calcMoveSpeed(h), 40,
//				40 + 12 * 16);
//		g.drawString("Attack Speed  : " + Combat.calcAttackSpeed(h), 40,
//				40 + 13 * 16);
//
//		g.drawString("Current Level : " + h.getStat(RPG.ST_LEVEL), 40,
//				40 + 14 * 16);
//		g.drawString("Current Exp.  : " + h.getStat(RPG.ST_EXP), 40,
//				40 + 15 * 16);
//		g.drawString("Skill Points  : " + h.getStat(RPG.ST_SKILLPOINTS), 40,
//				40 + 16 * 16);
//		g.drawString(
//				"Hunger level  : " + Text.properCase(Hero.hungerString(h)), 40,
//				40 + 17 * 16);
//		// + RPG.percentile(h.getStat(RPG.ST_HUNGER),
//		// h.getStat("HungerThreshold")) + "%", 40, 40 + 17 * 16);
//
//		// Intentional mis-indentation for temporary condition
//		// pending approval of wielded/worn section for general use.
//		if (Game.isDebug()) {
//			// what the well dressed hero is wearing
//			final int wX = 100;
//			final int wY = 40;
//			Thing wornItem = h.getWielded(RPG.WT_MAINHAND);
//			if (wornItem == null) {
//				g.drawString("Right Hand    : ", wX, wY + 19 * 16);
//			} else {
//				g.drawString("Right Hand    : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 19 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_SECONDHAND);
//			if (wornItem == null) {
//				g.drawString("Left Hand     : ", wX, wY + 20 * 16);
//			} else {
//				g.drawString("Left Hand     : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 20 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_TWOHANDS);
//			if (wornItem == null) {
//				g.drawString("Both Hands    : ", wX, wY + 21 * 16);
//			} else {
//				g.drawString("Both Hands    : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 21 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_RANGEDWEAPON);
//			if (wornItem == null) {
//				g.drawString("Ranged Weapon : ", wX, wY + 22 * 16);
//			} else {
//				g.drawString("Ranged Weapon : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 22 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_MISSILE);
//			if (wornItem == null) {
//				g.drawString("Missile       : ", wX, wY + 23 * 16);
//			} else {
//				g.drawString("Missile       : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 23 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_TORSO);
//			if (wornItem == null) {
//				g.drawString("Torso         : ", wX, wY + 24 * 16);
//			} else {
//				g.drawString("Torso         : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 24 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_LEGS);
//			if (wornItem == null) {
//				g.drawString("Legs          : ", wX, wY + 25 * 16);
//			} else {
//				g.drawString("Legs          : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 25 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_HEAD);
//			if (wornItem == null) {
//				g.drawString("Head          : ", wX, wY + 26 * 16);
//			} else {
//				g.drawString("Head          : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 26 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_BOOTS);
//			if (wornItem == null) {
//				g.drawString("Boots         : ", wX, wY + 27 * 16);
//			} else {
//				g.drawString("Boots         : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 27 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_HANDS);
//			if (wornItem == null) {
//				g.drawString("Hands         : ", wX, wY + 28 * 16);
//			} else {
//				g.drawString("Hands         : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 28 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_CLOAK);
//			if (wornItem == null) {
//				g.drawString("Cloak         : ", wX, wY + 29 * 16);
//			} else {
//				g.drawString("Cloak         : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 29 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_FULLBODY);
//			if (wornItem == null) {
//				g.drawString("Full Body     : ", wX, wY + 30 * 16);
//			} else {
//				g.drawString("Full Body     : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 30 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_BRACERS);
//			if (wornItem == null) {
//				g.drawString("Bracers       : ", wX, wY + 31 * 16);
//			} else {
//				g.drawString("Bracers       : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 31 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_BELT);
//			if (wornItem == null) {
//				g.drawString("Belt          : ", wX, wY + 32 * 16);
//			} else {
//				g.drawString("Belt          : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 32 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_RIGHTRING);
//			if (wornItem == null) {
//				g.drawString("Right Ring    : ", wX, wY + 33 * 16);
//			} else {
//				g.drawString("Right Ring    : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 33 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_LEFTRING);
//			if (wornItem == null) {
//				g.drawString("Left Ring     : ", wX, wY + 34 * 16);
//			} else {
//				g.drawString("Left Ring     : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 34 * 16);
//			}
//			wornItem = h.getWielded(RPG.WT_NECK);
//			if (wornItem == null) {
//				g.drawString("Neck          : ", wX, wY + 35 * 16);
//			} else {
//				g.drawString("Neck          : "
//						+ Describer.describe(h, wornItem,
//								Description.ARTICLE_NONE), wX, wY + 35 * 16);
//			}
//		}
//		// immunity and resistances
//		if (Game.isDebug()) {
//			final int rX = 100;
//			final int rY = 40;
//			g.drawString("Normal        : " + h.getStat("RES:normal"), rX,
//					rY + 44 * 16);
//			g.drawString("Impact        : " + h.getStat("RES:impact"), rX,
//					rY + 45 * 16);
//			g.drawString("Piercing      : " + h.getStat("RES:piercing"), rX,
//					rY + 46 * 16);
//			g.drawString("Water         : " + h.getStat("RES:water"), rX,
//					rY + 47 * 16);
//			g.drawString("Poison        : " + h.getStat("RES:poison"), rX,
//					rY + 48 * 16);
//			g.drawString("Fire          : " + h.getStat("RES:fire"), rX,
//					rY + 49 * 16);
//			g.drawString("Ice           : " + h.getStat("RES:ice"), rX,
//					rY + 50 * 16);
//			g.drawString("Chill         : " + h.getStat("RES:chill"), rX,
//					rY + 51 * 16);
//			g.drawString("Acid          : " + h.getStat("RES:acid"), rX,
//					rY + 52 * 16);
//			g.drawString("Shock         : " + h.getStat("RES:shock"), rX,
//					rY + 53 * 16);
//			g.drawString("Drain         : " + h.getStat("RES:drain"), rX,
//					rY + 54 * 16);
//			g.drawString("Disintegrate  : " + h.getStat("RES:disintegrate"),
//					rX, rY + 55 * 16);
//		}
//
//		// game stats
//		g
//				.drawString("Game Turns: " + Game.hero().getStat("TurnCount"),
//						240, 40);
//
//		g.drawString("Kill Count: " + Game.hero().getStat("KillCount"), 240,
//				40 + 1 * 16);
//
//		g.drawString("Game Time:  " + Game.hero().getStat("GameTime") / 100,
//				240, 40 + 2 * 16);
//
//		Thing w = h.getWielded(RPG.WT_MAINHAND);
//		if (w != null && w.getFlag("IsWeapon")) {
//			g
//					.drawString("Wielding " + w.getName(Game.hero()), 240,
//							40 + 9 * 16);
//		} else {
//			g.drawString("Unarmed", 240, 40 + 9 * 16);
//			w = Combat.unarmedWeapon(h);
//		}
//
//		g.drawString("Attack Cost     : " + Combat.attackCost(h, w), 240,
//				40 + 10 * 16);
//		g.drawString("Attack Skill    : " + Weapon.calcASK(w, h, null), 240,
//				40 + 11 * 16);
//		g.drawString("Attack Strength : " + Weapon.calcAST(w, h, null), 240,
//				40 + 12 * 16);
//
//		g.drawString("Defence Skill   : " + Weapon.calcDSK(h), 240,
//				40 + 13 * 16);
//
//		g.drawString("Armour Class    : " + Armour.calcArmour(h, "normal"),
//				240, 40 + 14 * 16);
//		g.drawString("Encumberance    : "
//				+ RPG.middle(0, h.getStat(RPG.ST_ENCUMBERANCE), 100) + "%",
//				240, 40 + 15 * 16);
//		g.drawString("Wealth          : " + Integer.toString(Coin.getMoney(h)),
//				240, 40 + 16 * 16);
//
//		final String god = h.getString("Religion");
//		g.drawString("You worship " + (god == null ? "no god" : god), 240,
//				40 + 17 * 16);
//
//		g.drawString("Your skills:", 450, 20);
//		final java.util.ArrayList al = Skill.getList(h);
//		final java.util.ArrayList al_core = Skill.getUnmarkedList(h);
//
//		int skillLines = 0;
//		for (int i = 0; i < al.size(); i++) {
//			final String s = (String) al.get(i);
//			final String s_core = (String) al_core.get(i);
//			if (Skill.isNotUsed(s_core)) {
//				g.setColor(QuestApp.INFOTEXTCOLOUR_GRAY);
//			} else {
//				g.setColor(QuestApp.INFOTEXTCOLOUR);
//			}
//			if (!Skill.isAbility(s_core)) {
//				g.drawString(s, 460, 40 + skillLines * 16);
//				skillLines++;
//			}
//		}
//		g.setColor(QuestApp.INFOTEXTCOLOUR);
//		// Yes, this is copy-pastage,
//		// TODO: unify this code, dont know if it is worth the effort ;)
//		g.drawString("Your abilities:", 450, 40 + skillLines * 16 + 4);
//
//		int abilityLines = 0;
//		for (int i = 0; i < al.size(); i++) {
//			final String s = (String) al.get(i);
//			final String s_core = (String) al_core.get(i);
//			if (Skill.isNotUsed(s_core)) {
//				g.setColor(QuestApp.INFOTEXTCOLOUR_GRAY);
//			} else {
//				g.setColor(QuestApp.INFOTEXTCOLOUR);
//			}
//			if (Skill.isAbility(s_core)) {
//				g.drawString(s, 460, 40 + skillLines * 16 + 4 + 20
//						+ abilityLines * 16);
//				abilityLines++;
//			}
//		}
//
//		final String bottomstring = "ESC=Exit";
//		g.drawString(bottomstring, 20, getSize().height - 10);
//	}
//
//	/*
//	 * public void addItem(Object o, String s, image i) { ListItem li=new
//	 * ListItem(o,s,i)); li.addMouseListener(new MouseAdapter() { public void
//	 * mouseClicked(MouseEvent e) { getParent().remove(thislist);
//	 * selectcommand.select(o); } }); add(li); }
//	 */
// }