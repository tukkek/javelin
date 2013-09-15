package tyrant.mikera.tyrant;

public class Screen extends TPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3546923567481894199L;

	public Screen(final QuestApp questapp) {
		super(questapp);

		setFont(QuestApp.mainfont);
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
	}

	public void update() {
		// override
	}
}