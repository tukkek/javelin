package tyrant.mikera.tyrant;

import javelin.Javelin;
import javelin.view.screen.IntroScreen;

public class Screen extends TPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3546923567481894199L;

	public Screen() {
		setFont(QuestApp.mainfont);
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
	}

	public void update() {
		// override
	}

	public void refresh() {
		Javelin.app.switchScreen(this);
	}

	public Character getInput() {
		return IntroScreen.feedback();
	}

	public void close() {
		// nothing by default
	}
}