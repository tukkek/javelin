package javelin.controller.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javelin.controller.db.Properties;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.MessagePanel;

/**
 * Uses {@value #LINUXTEMPARATUREFILE} on Linux to wait for cooldown if an
 * option is set on preferences.properties.
 * 
 * @see Properties
 * 
 * @author alex
 */
public class TemperatureManager {

	private static final String LINUXTEMPARATUREFILE =
			"/sys/class/thermal/thermal_zone0/temp";
	public static final int CPU_COOLING;

	static {
		int maxtemperature = Properties.getInteger("ai.maxtemperature", 0);
		CPU_COOLING =
				new File(LINUXTEMPARATUREFILE).exists() ? maxtemperature : 0;
	}

	public static void cooldown() {
		if (CPU_COOLING == 0) {
			return;
		}
		try {
			Integer lasttemperature = null;
			for (int temperature =
					sense(); temperature > CPU_COOLING; temperature = sense()) {
				ThreadManager.interrupt();
				if (lasttemperature == null) {
					lasttemperature = temperature;
					MessagePanel mp = (MessagePanel) Game.messagepanel;
					String text = mp.textzone.getText();
					if (!text.endsWith("\n")) {
						Game.messagepanel.add("\n");
					}
					Game.messagepanel.add("Cooling...");
					Game.messagepanel.getPanel().repaint();
				} else if (temperature < lasttemperature) {
					Game.messagepanel.add(" " + temperature + "°C...");
					lasttemperature = temperature;
					Game.messagepanel.getPanel().repaint();
				}
				Thread.sleep(1000);
				// Game.messagepanel.clear();
				// Game.messagepanel.add(text);
			}
		} catch (Exception e) {
			System.err.println("TemperatureManager: " + e.getMessage());
			return;
		}
	}

	public static int sense() throws IOException, FileNotFoundException {
		final BufferedReader reader =
				new BufferedReader(new FileReader(LINUXTEMPARATUREFILE));
		final int temperature = Integer.parseInt(reader.readLine()) / 1000;
		reader.close();
		return temperature;
	}

}
