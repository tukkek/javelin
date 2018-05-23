package javelin.old;

import javelin.view.screen.InfoScreen;

public class Screen extends TPanel {
	public Screen() {
		setFont(QuestApp.mainfont);
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
	}

	public Character getInput() {
		return InfoScreen.feedback();
	}

	public void close() {
		// nothing by default
	}
}