package javelin.view.mappanel;

import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.StatisticsScreen;

public class BattleMouse extends Mouse {

	public BattleMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (BattleMap.redTeam.contains(Game.hero().combatant)) {
			return;
		}
		Tile t = (Tile) e.getSource();
		Combatant c = BattlePanel.state.getCombatant(t.x, t.y);
		if (e.getButton() == e.BUTTON3 && c != null) {
			JTextArea label = new JTextArea(StatisticsScreen.gettext(c, false));
			Font f = label.getFont();
			label.setEditable(false);
			label.setFont(new Font(Font.MONOSPACED, f.getStyle(), f.getSize()));
			JOptionPane.showMessageDialog(Javelin.app.frame, label,
					c.toString(), JOptionPane.QUESTION_MESSAGE);
			return;
		}
		super.mouseClicked(e);
	}
}