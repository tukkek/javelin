package javelin.controller.action.world;

import java.io.File;

import javelin.Javelin;
import javelin.controller.TextReader;
import javelin.controller.action.SimpleAction;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * In-game help.
 * 
 * @author alex
 */
public class Guide extends WorldAction implements SimpleAction {
	public Guide(int vk, String name, String descriptive) {
		super(name, new int[] { vk }, new String[] { descriptive });
	}

	@Override
	public void perform(WorldScreen screen) {
		perform();
	}

	@Override
	public void perform() {
		TextReader.show(new File("doc",
				name.replaceAll(" ", "").toLowerCase() + ".txt"), "");
		Javelin.app.switchScreen(BattleScreen.active);
	}

	@Override
	public int[] getkeys() {
		return keys;
	}

	@Override
	public String getname() {
		return name;
	}

}
