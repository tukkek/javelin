package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;

/**
 * Lets a {@link Squad} rent a mode of transportation.
 * 
 * @author alex
 */
public class TransportScreen extends PurchaseScreen {
	private static final Option REFUND = new Option("Return (refund)", 0, 'r');
	private static final Option CARRIAGE = new Option("Carriage",
			Transport.CARRIAGE.price, 'c');
	private static final Option SHIP = new Option("Ship", Transport.SHIP.price,
			's');
	private static final Option AIRSHIP = new Option("Airship",
			Transport.AIRSHIP.price, 'a');

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
		list.add(REFUND);
		if (Squad.active.transport != null) {
			return list;
		}
		// if (Transport.CARRIAGE.equals(town.transport)) {
		// list.add(CARRIAGE);
		// return list;
		// }
		// if (Transport.SHIP.equals(town.transport)) {
		// list.add(CARRIAGE);
		// list.add(SHIP);
		// return list;
		// }
		list.add(CARRIAGE);
		list.add(SHIP);
		list.add(AIRSHIP);
		return list;
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		if (o == REFUND) {
			return refund(Squad.active);
		}
		if (o == CARRIAGE) {
			Squad.active.transport = Transport.CARRIAGE;
		} else if (o == SHIP) {
			Squad.active.transport = Transport.SHIP;
		} else if (o == AIRSHIP) {
			Squad.active.transport = Transport.AIRSHIP;
		} else {
			throw new RuntimeException("[TransportScreen] Unknown option");
		}
		Squad.active.updateavatar();
		return true;
	}

	/**
	 * @return Forces the {@link Squad} to abandon its {@link Transport} but
	 *         pays a refund for it.
	 * @see Squad#transport
	 */
	static public boolean refund(Squad s) {
		if (s.transport == null) {
			return false;
		}
		s.gold += s.transport.price * .9;
		s.transport = null;
		s.updateavatar();
		return true;
	}

	@Override
	public String printpriceinfo(Option o) {
		return o == REFUND ? "" : super.printpriceinfo(o);
	}
}
