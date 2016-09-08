package javelin.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Examine;
import javelin.controller.upgrade.Spell;
import javelin.model.condition.Breathless;
import javelin.model.feat.attack.martial.CombatExpertise;
import javelin.model.feat.attack.martial.ImprovedFeint;
import javelin.model.feat.attack.martial.ImprovedGrapple;
import javelin.model.feat.attack.martial.ImprovedTrip;
import javelin.model.item.Item;
import javelin.model.spell.Summon;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.TPanel;

/**
 * Bottom panel for the {@link WorldScreen}.
 * 
 * @author alex
 */
public class StatusPanel extends TPanel {
	private static final long serialVersionUID = 3905800885761095223L;
	static final int boxborder = 1;
	static final Color powercolor = new Color(0, 128, 60);
	static int charwidth = 0;
	static int charheight = 0;
	static int charmaxascent = 0;
	int nextLine;

	/** Constructor. */
	public StatusPanel() {
		super();
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
		Combatant hero = BattlePanel.current;
		if (hero == null || hero.source == null) {
			return;
		}
		if (Examine.lastlooked != null) {
			hero = Examine.lastlooked;
		}
		String helper = "";
		for (final char c : (maininfo(hero) + movementdata(hero)
				+ attackdata(hero) + passivedata(hero) + spelldata(hero)
				+ itemdata(hero)).toCharArray()) {
			helper += c;
			if (Character.valueOf('\n').equals(c) || helper.length() == 26) {
				paintLabel(g, helper, 10, getNextLine());
				helper = "";
			}
		}
		paintLabel(g, helper, 10, getNextLine());
	}

	private String itemdata(Combatant combatant) {
		List<Item> equipment = Javelin.app.fight.getbag(combatant);
		ArrayList<String> listing = new ArrayList<String>();
		for (Item i : equipment) {
			String extra = i.canuse(combatant);
			listing.add(i.name.replaceAll("Potion of", "")
					+ (extra == null ? "" : "*"));
		}
		return listlist("Items", listing);
	}

	private String spelldata(Combatant combatant) {
		ArrayList<String> listing = new ArrayList<String>();
		for (Spell s : combatant.spells) {
			if (s instanceof Summon && combatant.summoned) {
				continue;
			}
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

	String listlist(String title, List<String> listing) {
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

	String movementdata(final Combatant combatant) {
		ArrayList<String> data = new ArrayList<String>(2);
		if (combatant.source.fly > 0) {
			data.add("Fly");
		} else if (combatant.source.swim > 0) {
			data.add("Swim");
		}
		if (combatant.source.burrow > 0) {
			data.add(combatant.burrowed ? "Burrowed" : "Burrow");
		}
		if (data.isEmpty()) {
			return "";
		}
		String output = data.get(0);
		data.remove(0);
		for (String s : data) {
			output += ", " + s.toLowerCase();
		}
		return output + "\n\n";
	}

	String attackdata(final Combatant combatant) {
		String status = "";
		if (combatant.hasattacktype(true)) {
			status += "Mêlée\n";
		}
		if (combatant.hasattacktype(false)) {
			status += "Ranged\n";
		}
		status += "\n";
		if (!combatant.source.breaths.isEmpty()) {
			status += combatant.hascondition(Breathless.class) == null
					? "Breath\n" : "Breathless\n";
		}
		if (combatant.source.touch != null) {
			status += combatant.source.touch + "\n";
		}
		if (combatant.source.hasfeat(CombatExpertise.SINGLETON)) {
			status += "Defensive\n";
		}
		if (combatant.source.hasfeat(ImprovedFeint.SINGLETON)) {
			status += "Feint\n";
		}
		if (combatant.source.hasfeat(ImprovedGrapple.SINGLETON)) {
			status += "Grapple\n";
		}
		if (combatant.source.hasfeat(ImprovedTrip.SINGLETON)) {
			status += "Trip\n";
		}
		return status;
	}

	String maininfo(Combatant combatant) {
		final String customname = combatant.source.customName;
		String status = combatant.getstatus();
		String ap = "";
		if (combatant.ap < Float.MAX_VALUE) {
			ap = "AP: "
					+ new BigDecimal(combatant.ap + BattleScreen.active.spentap)
							.setScale(1, RoundingMode.HALF_UP)
					+ "\n";
		}
		;
		return (customname != null ? customname : combatant.source.name) + "\n"
				+ Character.toUpperCase(status.charAt(0)) + status.substring(1)
				+ "\n" + ap + "\n";
	}

	String passivedata(final Combatant combatant) {
		String status = "";
		if (combatant.source.fasthealing > 0) {
			status +=
					"Fast healing "
							+ new BigDecimal(100.0
									* new Double(combatant.source.fasthealing)
									/ new Double(combatant.maxhp)).setScale(0,
											RoundingMode.HALF_UP)
							+ "%\n";
		}
		return status.isEmpty() ? "" : "\n" + status;
	}

	private int getNextLine() {
		return nextLine += 15;
	}

	static void paintBar(final Graphics g, final int x, final int y,
			final int w, final int h, final Color f, final Color b,
			float amount) {
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

	static int paintLabel(final Graphics g, final String s, final int x,
			final int y) {
		g.setColor(QuestApp.INFOTEXTCOLOUR);

		g.drawString(s, x, y + charmaxascent - charheight / 2);

		return charwidth * s.length();
	}

	// paint a boxed area, raised or lowered
	static void paintBox(final Graphics g, final int x, final int y,
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