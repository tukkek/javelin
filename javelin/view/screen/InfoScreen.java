package javelin.view.screen;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.old.Game;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Screen;
import tyrant.mikera.tyrant.util.Text;

/**
 * Fullscreen text display. For hiding the current {@link BattleScreen} or
 * {@link WorldScreen} and interacting textually with the user.
 * 
 * @author Tyrant
 * @author alex
 */
public class InfoScreen extends Screen {
	private static final long serialVersionUID = 3256727281736168249L;
	/** Screen content. */
	public String text;
	/** {@link #text} rendering style.t */
	public Font font;

	private final int border = 20;

	/** Constructor. */
	public InfoScreen(final String textp) {
		text = textp;
		font = QuestApp.mainfont;
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
		setFont(font);
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

	/**
	 * @return A non-negative integer.
	 */
	static public int numberfeedback() {
		while (true) {
			try {
				return Integer.parseInt(feedback().toString());
			} catch (final NumberFormatException e2) {
				continue;
			}
		}
	}

	/**
	 * @return Player input.
	 */
	static public Character feedback() {
		KeyEvent input = Game.getInput();
		switch (input.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			return '→';
		case KeyEvent.VK_LEFT:
			return '←';
		case KeyEvent.VK_BACK_SPACE:
			return '\b';
		case KeyEvent.VK_SHIFT:
			return feedback();
		}
		return Character.valueOf(input.getKeyChar());
	}

	/**
	 * @param string
	 *            Replace {@link #text} and update screen.
	 */
	public void print(String string) {
		text = string;
		Javelin.app.switchScreen(this);
	}
}