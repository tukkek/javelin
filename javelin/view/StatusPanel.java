package javelin.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.upgrade.Spell;
import javelin.model.condition.Breathless;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.TPanel;

public class StatusPanel extends TPanel {
	private static final long serialVersionUID = 3905800885761095223L;
	public static final int boxborder = 1;
	public static final Color powercolor = new Color(0, 128, 60);
	public static int charwidth = 0;
	public static int charheight = 0;
	public static int charmaxascent = 0;
	private int nextLine;

	public StatusPanel() {
		super(Game.getQuestapp());
		setBackground(QuestApp.PANELCOLOUR);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(208, 272);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		nextLine = 0;
		final Combatant hero = Game.hero().combatant;
		if (hero == null || hero.source == null) {
			return;
		}
		String helper = "";
		for (final char c : (maininfo(hero) + movementdata(hero)
				+ attackdata(hero) + passivedata(hero) + spelldata(hero) + itemdata(hero))
				.toCharArray()) {
			helper += c;
			if (Character.valueOf('\n').equals(c) || helper.length() == 26) {
				paintLabel(g, helper, 10, getNextLine());
				helper = "";
			}
		}
		paintLabel(g, helper, 10, getNextLine());
	}

	private String itemdata(Combatant combatant) {
		List<Item> equipment = Squad.active.equipment.get(combatant.toString());
		ArrayList<String> listing = new ArrayList<String>();
		for (Item i : equipment) {
			listing.add(i.name.replaceAll("Potion of", ""));
		}
		return listlist("Items", listing);
	}

	private String spelldata(Combatant combatant) {
		ArrayList<String> listing = new ArrayList<String>();
		for (Spell s : combatant.spells) {
			// if (!s.exhausted()) {
			String spellname = s.name;
			if (spellname.length() >= 14) {
				spellname = s.name.substring(0, 14);
			}
			listing.add(spellname + s.showleft());
			// }
		}
		return listlist("Spells", listing);
	}

	public String listlist(String title, List<String> listing) {
		if (listing.isEmpty()) {
			return "";
		}
		String text = title + "\n";
		Collections.sort(listing);
		for (String i : listing) {
			text += " " + i.trim().toLowerCase() + "\n";
		}
		return "\n" + text;
	}

	public String movementdata(final Combatant combatant) {
		if (combatant.source.fly > 0) {
			return "Flight\n\n";
		}
		if (combatant.source.swim > 0) {
			return "Swim\n\n";
		}
		return "";
	}

	public String attackdata(final Combatant combatant) {
		String status = "";
		if (combatant.hasAttackType(true)) {
			status += "Mêlée\n";
		}
		if (combatant.hasAttackType(false)) {
			status += "Ranged\n";
		}
		if (!combatant.source.breaths.isEmpty()
				&& !combatant.hascondition(Breathless.class)) {
			status += "Breath\n";
		}
		return status;
	}

	public String maininfo(Combatant combatant) {
		final String customname = combatant.source.customName;
		String status = combatant.getStatus();
		return (customname != null ? customname : combatant.source.name)
				+ "\n"
				+ Character.toUpperCase(status.charAt(0))
				+ status.substring(1)
				+ "\nAP: "
				+ new BigDecimal(combatant.ap)
						.setScale(1, RoundingMode.HALF_UP) + "\n\n";
	}

	public String passivedata(final Combatant combatant) {
		String status = "";
		if (combatant.source.fasthealing > 0) {
			status += "Fast healing "
					+ new BigDecimal(100.0
							* new Double(combatant.source.fasthealing)
							/ new Double(combatant.maxhp)).setScale(0,
							RoundingMode.HALF_UP) + "%\n";
		}
		return status.isEmpty() ? "" : "\n" + status;
	}

	private int getNextLine() {
		return nextLine += 15;
	}

	public void paintStat(final Graphics g, final String s, final int x,
			final int y) {
		paintLabel(g, s + ": " + Game.hero().getStat(s), x, y);
	}

	public static void paintBar(final Graphics g, final int x, final int y,
			final int w, final int h, final Color f, final Color b, float amount) {
		if (amount > 1) {
			amount = 1;
		}
		final int hh = h / 4;
		g.setColor(f);
		g.fillRect(x, y, (int) (w * amount), h);
		g.setColor(f.brighter());
		g.fillRect(x, y, (int) (w * amount), hh);
		g.setColor(f.darker());
		g.fillRect(x, y + 3 * hh, (int) (w * amount), h - 3 * hh);
		g.setColor(b);
		g.fillRect(x + (int) (w * amount), y, (int) (w * (1 - amount)), h);
		paintBox(g, x, y, w, h, false);
	}

	public static int paintLabel(final Graphics g, final String s, final int x,
			final int y) {
		g.setColor(QuestApp.INFOTEXTCOLOUR);

		g.drawString(s, x, y + charmaxascent - charheight / 2);

		return charwidth * s.length();
	}

	// paint a boxed area, raised or lowered
	public static void paintBox(final Graphics g, final int x, final int y,
			final int w, final int h, final boolean raised) {
		if (raised) {
			g.setColor(QuestApp.PANELHIGHLIGHT);
		} else {
			g.setColor(QuestApp.PANELSHADOW);
		}

		g.fillRect(x, y, w, boxborder);
		g.fillRect(x, y, boxborder, h);

		if (!raised) {
			g.setColor(QuestApp.PANELHIGHLIGHT);
		} else {
			g.setColor(QuestApp.PANELSHADOW);
		}

		g.fillRect(x + 1, y + h - boxborder, w - 1, boxborder);
		g.fillRect(x + w - boxborder, y + 1, boxborder, h - 1);
	}
}