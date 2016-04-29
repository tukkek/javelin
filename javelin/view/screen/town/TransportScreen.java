package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.model.world.Squad;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.Transport;
import javelin.view.screen.Option;

/**
 * Lets a {@link Squad} rent a mode of transportation.
 * 
 * @author alex
 */
public class TransportScreen extends PurchaseScreen {
	public static final int COSTAIRSHIP = 5000;
	public static final int COSTCARRIAGE = 100;
	public static final int MAINTENANCECARRIAGE = 2;
	public static final int MAINTENANCEAIRSHIP = 16;

	private static final Option RETURN = new Option("Return (refund)", 0, 'r');
	private static final Option AIRSHIP =
			new Option("Airship", COSTAIRSHIP, 'a');
	private static final Option CARRIAGE =
			new Option("Carriage", COSTCARRIAGE, 'c');

	/**
	 * @param description
	 *            Title.
	 * @param t
	 *            Town the active {@link Squad} is in.
	 */
	public TransportScreen(String description, Town t) {
		super(description, t);
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> list = new ArrayList<Option>();
		list.add(RETURN);
		if (Squad.active.transport != Transport.NONE) {
			return list;
		}
		if (town.transport.equals(Transport.CARRIAGE)) {
			list.add(CARRIAGE);
			return list;
		}
		list.add(CARRIAGE);
		list.add(AIRSHIP);
		return list;
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		if (o == RETURN) {
			if (Squad.active.transport == Transport.CARRIAGE) {
				Squad.active.gold += CARRIAGE.price / 2;
			} else if (Squad.active.transport == Transport.AIRSHIP) {
				Squad.active.gold += AIRSHIP.price / 2;
			}
			Squad.active.transport = Transport.NONE;
			Squad.active.updateavatar();
			return true;
		}
		if (o == CARRIAGE) {
			Squad.active.transport = Transport.CARRIAGE;
		} else if (o == AIRSHIP) {
			Squad.active.transport = Transport.AIRSHIP;
		} else {
			throw new RuntimeException("[TransportScreen] Uknown option");
		}
		Squad.active.updateavatar();
		return true;
	}

	@Override
	public String printpriceinfo(Option o) {
		return o == RETURN ? "" : super.printpriceinfo(o);
	}
}
