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

import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.util.Text;

public class TextZone extends Component {
	public static final String BLACK = "\\b";
	private final int BORDER = 5;
	private static final long serialVersionUID = 3256443603391033401L;
	public String text;
	public Font font;
	public static int linelength;
	static public Color fontcolor;
	// static public Color color;

	public TextZone() {
		this("");
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

		g.setColor(fontcolor);
		for (final String s : st) {
			final String[] lines = Text.wrapString(s, linelength);

			for (String line : lines) {
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
