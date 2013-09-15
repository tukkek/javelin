//
// Graphical component to display an area of text
//

package tyrant.mikera.tyrant;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javelin.controller.db.Preferences;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.util.Text;

public class TextZone extends Component {
	public static final String BLACK = "\\b";
	private final int BORDER = 5;
	private static final long serialVersionUID = 3256443603391033401L;
	public String text;
	public Font font;
	public static int linelength;
	Color fontcolor;

	public TextZone() {
		this("");
		String color = Preferences.getString("ui.textcolor").toUpperCase();
		try {
			fontcolor = (Color) Color.class.getField(color).get(null);
		} catch (Exception e) {
			System.err.println("Could not load the color " + color
					+ ", please review your preferences file.");
			System.exit(0);
		}
	}

	public TextZone(final String t) {
		text = t;
		font = QuestApp.mainfont;
		setFont(font);
	}

	public void setText(final String s) {
		text = new String(s);
	}

	public String getText() {
		return text;
	}

	@Override
	public void paint(final Graphics g) {
		final FontMetrics met = g.getFontMetrics(g.getFont());

		// g.setColor(QuestApp.INFOTEXTCOLOUR);

		final Rectangle r = getBounds();
		final int charsize = met.charWidth(' ');

		int y;
		int lineinc;
		{
			linelength = (r.width - BORDER * 2) / charsize;
			y = BORDER + met.getMaxAscent();
			lineinc = met.getMaxAscent() + met.getMaxDescent();
		}

		final String[] st = Text.separateString(text, '\n');
		int height = 0;

		for (final String s : st) {
			final String[] lines = Text.wrapString(s, linelength);

			for (final String line : lines) {
				height += lineinc;
			}
		}

		final int scroll = RPG.max(0, height - getHeight());

		for (final String s : st) {
			final String[] lines = Text.wrapString(s, linelength);

			for (String line : lines) {
				g.setColor(fontcolor);
				g.getFont().deriveFont(Font.BOLD, g.getFont().getSize());
				if (line.startsWith(BLACK)) {
					line = line.replace(BLACK, "");
					g.setColor(Color.BLACK);
				}
				g.drawString(line, BORDER, y - scroll);
				y += lineinc;
			}
		}
	}
}
