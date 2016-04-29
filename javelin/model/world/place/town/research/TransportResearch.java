package javelin.model.world.place.town.research;

import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.Transport;
import javelin.view.screen.town.ResearchScreen;

/**
 * Allows for upgrading {@link Town#transport}.
 * 
 * @author alex
 */
public class TransportResearch extends Research {

	Transport transport;

	/**
	 * @param transport
	 *            Tech level to upgrade to.
	 */
	public TransportResearch(Transport transport) {
		super("Transport: " + transport, transport.getprice());
		this.transport = transport;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.transport = transport;
	}

	@Override
	protected boolean isrepeated(Town t) {
		return false;
	}

}
