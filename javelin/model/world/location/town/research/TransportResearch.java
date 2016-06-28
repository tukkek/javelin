package javelin.model.world.location.town.research;

import javelin.model.unit.transport.Transport;
import javelin.model.world.location.town.Town;
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
		super("Transport: " + transport, transport.research);
		this.transport = transport;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.transport = transport;
	}

	@Override
	public boolean isrepeated(Town t) {
		return false;
	}

}
