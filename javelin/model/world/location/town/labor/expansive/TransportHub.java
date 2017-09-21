package javelin.model.world.location.town.labor.expansive;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.transport.FlyingNimbus;
import javelin.model.transport.Transport;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

public class TransportHub extends Location {
	static final Option REFUND = new Option("Return vehicle (refund)", 0, 'r');
	static final Option CARRIAGE = new Option("Carriage",
			Transport.CARRIAGE.price, 'c');
	static final Option SHIP = new Option("Ship", Transport.SHIP.price, 's');
	static final Option AIRSHIP = new Option("Airship", Transport.AIRSHIP.price,
			'a');
	static final Option[] TRANSPORTATION = new Option[] { CARRIAGE, SHIP,
			AIRSHIP };
	static final String[] NAMES = new String[] { "Stables", "Docks",
			"Magic dock" };

	class ShowTransport extends PurchaseScreen {
		TransportHub stables;

		/**
		 * @param description
		 *            Title.
		 * @param t
		 *            Town the active {@link Squad} is in.
		 */
		public ShowTransport(TransportHub s) {
			super("You enter the " + s.getname(), null);
			stables = s;
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> list = new ArrayList<Option>();
			list.add(REFUND);
			if (Squad.active.transport == null) {
				for (int i = 0; i <= stables.level; i++) {
					list.add(TRANSPORTATION[i]);
				}
			}
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
		public boolean refund(Squad s) {
			if (s.transport == null || s.transport instanceof FlyingNimbus) {
				print(text + "\nYou don't have a vehicle...");
				return false;
			}
			Point p = Squad.active.getlocation();
			if (Terrain.get(p.x, p.y).equals(Terrain.WATER)) {
				print(text + "\nYou need to be on land for that...");
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

	public static class BuildTransportHub extends Build {
		public BuildTransportHub() {
			super("Build stables", 2, null, Rank.HAMLET);
		}

		@Override
		public Location getgoal() {
			TransportHub hub = new TransportHub();
			hub.cost += cost;
			return hub;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d)
					&& d.getlocationtype(TransportHub.class).isEmpty();
		}

		@Override
		protected Point getsitelocation() {
			ArrayList<Point> sites = town.getdistrict().getfreespaces();
			for (Point p : sites) {
				if (searchforwater(p)) {
					return p;
				}
			}
			return sites.get(0);
		}
	}

	class DocksUpgrade extends BuildingUpgrade {
		public DocksUpgrade(TransportHub hub) {
			this(10, 1, hub, Rank.VILLAGE);
			name = "Upgrade " + hub.getname().toLowerCase() + " to docks";
		}

		public DocksUpgrade(int cost, int upgradelevel, TransportHub previous,
				Rank minimumsize) {
			super("", cost - previous.cost, upgradelevel, previous,
					minimumsize);
		}

		@Override
		protected void define() {
			// already defined
		}

		@Override
		public void done() {
			super.done();
			TransportHub hub = (TransportHub) previous;
			hub.level = upgradelevel;
			hub.cost += cost;
		}

		@Override
		public Location getgoal() {
			return previous;
		}
	}

	class MagicDockUpgrade extends DocksUpgrade {
		public MagicDockUpgrade(TransportHub hub) {
			super(20, 2, hub, Rank.CITY);
			name = "Upgrade " + hub.getname().toLowerCase() + " to magic dock";
		}
	}

	int cost = 0;
	int level = 0;

	public TransportHub() {
		super(NAMES[0]);
		allowentry = false;
		sacrificeable = true;
		discard = false;
		gossip = true;
	}

	@Override
	public Integer getel(int attackerel) {
		return Integer.MIN_VALUE;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	public String getname() {
		return NAMES[level];
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		new ShowTransport(this).show();
		return true;
	}

	@Override
	public Image getimage() {
		return Images.getImage(
				"location" + getname().replaceAll(" ", "").toLowerCase());
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (searchforwater(getlocation())) {
			if (level == 0) {
				upgrades.add(new DocksUpgrade(this));
			} else if (level == 1) {
				upgrades.add(new MagicDockUpgrade(this));
			}
		} else if (level == 0) {
			upgrades.add(new MagicDockUpgrade(this));
		}
		return upgrades;
	}

	static boolean searchforwater(Point p) {
		return Terrain.search(p, Terrain.WATER, 1, World.getseed()) > 0;
	}
}
