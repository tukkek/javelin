package tyrant.mikera.tyrant;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javelin.Javelin;
import tyrant.mikera.tyrant.util.Text;

public class InfoScreen extends Screen {
	private static final long serialVersionUID = 3256727281736168249L;
	public String text;
	public Font font;

	private final int border = 20;

	public InfoScreen(final QuestApp q, final String textp) {
		super(q);
		text = textp;
		font = QuestApp.mainfont;
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
		setFont(font);
	}

	public InfoScreen(final String text) {
		this(Javelin.app, text);
	}

	public void activate() {
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final FontMetrics met = g.getFontMetrics(g.getFont());
		final Rectangle r = getBounds();
		final int charsize = met.charWidth(' ');
		final int linelength = (r.width - border * 2) / charsize;
		int y = border + met.getMaxAscent();
		final int lineinc = met.getMaxAscent() + met.getMaxDescent();
		for (final String s : Text.separateString(text, '\n')) {
			for (final String line : Text.wrapString(s, linelength)) {
				g.setColor(QuestApp.INFOTEXTCOLOUR);
				g.drawString(line, border, y);
				y += lineinc;
			}
		}
	}
}