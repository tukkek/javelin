package javelin.controller.action.world;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javelin.controller.action.SimpleAction;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Configures {@link Combatant#automatic}.
 * 
 * @author alex
 */
public class Automate extends WorldAction implements SimpleAction {
	class AutomateWindow extends javelin.view.frame.Frame {
		ArrayList<Checkbox> boxes = new ArrayList<Checkbox>();

		public AutomateWindow() {
			super("Automatic units");
		}

		@Override
		protected Container generate() {
			boxes.clear();
			Panel container = new Panel(new GridLayout(0, 1));
			for (Combatant c : getunits()) {
				Checkbox box = new Checkbox(c.toString(), c.automatic);
				container.add(box);
				boxes.add(box);
			}
			container.add(new Label());
			final Checkbox strategic;
			if (BattleScreen.active instanceof WorldScreen) {
				strategic =
						new Checkbox("Enable strategic combat for this squad",
								Squad.active.strategic);
				container.add(strategic);
			} else {
				strategic = null;
				container.add(new Label("Changes are reset after battle."));
			}
			Button confirm = new Button("Apply");
			container.add(confirm);
			confirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (int i = 0; i < boxes.size(); i++) {
						getunits().get(i).automatic = boxes.get(i).getState();
					}
					if (strategic != null) {
						Squad.active.strategic = strategic.getState();
					}
					frame.dispose();
				}
			});
			return container;
		}

		ArrayList<Combatant> getunits() {
			return BattleScreen.active instanceof WorldScreen
					? Squad.active.members : BattleMap.blueTeam;
		}
	}

	public Automate() {
		super("Set automatic units", new int[] { 'A' }, new String[] { "A" });
	}

	@Override
	public int[] getcodes() {
		return keys;
	}

	@Override
	public String getname() {
		return name;
	}

	@Override
	public String[] getkeys() {
		return morekeys;
	}

	@Override
	public void perform(WorldScreen screen) {
		perform();
	}

	@Override
	public void perform() {
		if (BattleScreen.active.spentap != 0) {
			Game.message("Finish your turn first...", null, Delay.WAIT);
			return;
		}
		AutomateWindow w = new AutomateWindow();
		w.show();
		while (w.frame.isDisplayable() && w.frame.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// keep waiting
			}
		}
	}
}