package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.model.world.Squad;
import javelin.model.world.Squad.Transport;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.Option;

/**
 * Lets a {@link Squad} rent a mode of transportation.
 * 
 * @author alex
 */
public class TransportScreen extends PurchaseScreen {
	private static final Option RETURN = new Option("Return (refund)", 0);
	private static final Option AIRSHIP = new Option("Airship", 5000);
	private static final Option CARRIAGE = new Option("Carriage", 100);
	public static final int CARRIAGEMAINTENANCE = 2;
	public static final int AIRSHIPMAINTENANCE = 16;

	public TransportScreen(String name, Town t) {
		super(name, t);
	}

	@Override
	public List<Option> getOptions() {
		ArrayList<Option> list = new ArrayList<Option>();
		list.add(RETURN);
		if (Squad.active.transport == Transport.NONE) {
			list.add(CARRIAGE);
			list.add(AIRSHIP);
		}
		return list;
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		if (o == RETURN) {
			if (Squad.active.transport == Transport.CARRIAGE) {
				Squad.active.gold += CARRIAGE.price - CARRIAGEMAINTENANCE;
			} else if (Squad.active.transport == Transport.AIRSHIP) {
				Squad.active.gold += AIRSHIP.price - AIRSHIPMAINTENANCE;
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
