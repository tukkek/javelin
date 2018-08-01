package javelin.controller.wish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * A trick to be done by expending a variable amount of {@link Ruby} instances.
 *
 * @see WishScreen
 * @author alex
 */
public abstract class Wish extends Option {
	public static class WishScreen extends SelectScreen {
		static final Option ADD = new Option("Add 1 ruby to this wish", 0, '+');
		static final Option REMOVE = new Option("Remove 1 ruby from this wish",
				0, '-');

		int rubies;
		int maxrubies;

		/** See {@link SelectScreen#SelectScreen(String, Town)}. */
		public WishScreen(int rubies) {
			super("Make your wish:", null);
			this.rubies = rubies;
			maxrubies = rubies;
			stayopen = false;
		}

		@Override
		public boolean select(Option o) {
			if (add(o) || remove(o)) {
				stayopen = true;
				return true;
			}
			double price = o.price;
			Wish w = (Wish) o;
			String error = w.validate();
			if (error != null) {
				text += "\n" + error;
				Javelin.app.switchScreen(this);
				return false;
			}
			if (price > rubies) {
				text += "\n\nNot enough rubies...";
				Javelin.app.switchScreen(this);
				return false;
			}
			if (!w.wish(w.requirestarget ? selectmember() : null)) {
				return false;
			}
			pay(w.pay());
			stayopen = false;
			return true;
		}

		boolean remove(Option o) {
			if (o != REMOVE) {
				return false;
			}
			if (rubies > 1) {
				rubies -= 1;
			}
			return true;
		}

		boolean add(Option o) {
			if (o != ADD) {
				return false;
			}
			if (rubies < maxrubies) {
				rubies += 1;
			}
			return true;
		}

		void pay(double nrubies) {
			for (int i = 0; i < nrubies; i++) {
				Squad.active.equipment.pop(Ruby.class);
			}
		}

		Combatant selectmember() {
			Combatant target;
			ArrayList<String> members = new ArrayList<>();
			for (Combatant c : Squad.active.members) {
				members.add(c.toString());
			}
			target = Squad.active.members.get(Javelin
					.choose("Select a squad member:", members, true, true));
			return target;
		}

		Character print() {
			Javelin.app.switchScreen(this);
			return feedback();
		}

		@Override
		public String printinfo() {
			return "You are wishing " + rubies + " of your " + maxrubies
					+ " rubies.";
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<>();
			options.add(new ChangeAvatar('c', this));
			options.add(new RevealFloor('f', this));
			options.add(new Gold('g', this));
			options.add(new Heal('h', this));
			options.add(new ConjureMasterKey('k', this));
			options.add(new Ressurect('r', this));
			options.add(new SummonAlly('s', this));
			options.add(new Teleport('t', this));
			options.add(new Rebirth('u', this));
			if (Campaign.class.isInstance(World.scenario)) {
				options.add(new Win('w', this));
			}
			options.add(ADD);
			options.add(REMOVE);
			return options;
		}

		@Override
		public String getCurrency() {
			return "";
		}

		@Override
		public String printpriceinfo(Option o) {
			if (o == ADD || o == REMOVE) {
				return "";
			}
			if (o.price == 0) {
				return o instanceof ChangeAvatar ? " (free)"
						: " (free, single use)";
			}
			long price = Math.round(o.price);
			return " (" + price + " " + (price == 1 ? "ruby" : "rubies") + ")";
		}

		@Override
		protected Comparator<Option> sort() {
			return new Comparator<Option>() {
				@Override
				public int compare(Option o1, Option o2) {
					return o1.key.compareTo(o2.key);
				}
			};
		}
	}

	boolean requirestarget;
	WishScreen screen;
	int wishprice;

	/**
	 * See {@link Option#Option(String, double, Character)}.
	 *
	 * @param requirestargetp
	 *            <code>true</code> if should ask for a {@link Squad}
	 *            {@link Combatant} as target of this effect.
	 */
	Wish(String name, Character keyp, int price, boolean requirestargetp,
			WishScreen screen) {
		super(name, price, keyp);
		wishprice = price;
		requirestarget = requirestargetp;
		this.screen = screen;
	}

	int pay() {
		return wishprice;
	}

	/**
	 * @param target
	 *            Provided if this hack {@link #requirestarget}.
	 * @return <code>true</code> if successful and rubies can be deduced.
	 */
	abstract boolean wish(Combatant target);

	/**
	 * @return A String indicating why this can't be chosen, <code>null</code>
	 *         to proceed normally.
	 */
	String validate() {
		return null;
	}
}
