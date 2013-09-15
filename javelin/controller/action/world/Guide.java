package javelin.controller.action.world;

import java.io.File;

import javelin.Javelin;
import javelin.controller.TextReader;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.world.WorldScreen;

public class Guide extends WorldAction {

	public Guide(int vk, String name, String descriptive) {
		super(name, new int[] { vk }, new String[] { descriptive });
	}

	@Override
	public void perform(WorldScreen screen) {
		TextReader.show(new File("doc", name.replaceAll(" ", "").toLowerCase()
				+ ".txt"), "");
		Javelin.app.switchScreen(BattleScreen.active);
	}

}
