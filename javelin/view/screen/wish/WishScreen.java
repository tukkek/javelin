package javelin.view.screen.wish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Ruby;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * @see Haxor
 * @author alex
 */
public class WishScreen extends SelectScreen {
	static final Option ADD = new Option("Add 1 ruby to this wish", 0, '+');
	static final Option REMOVE = new Option("Remove 1 ruby from this wish", 0,
			'-');

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
		Wish h = (Wish) o;
		String error = h.validate();
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
		if (!h.wish(h.requirestarget ? selectmember() : null)) {
			return false;
		}
		pay(price);
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

	public void pay(double nrubies) {
		for (int i = 0; i < nrubies; i++) {
			Squad.active.equipment.pop(Ruby.class);
		}
	}

	Combatant selectmember() {
		Combatant target;
		ArrayList<String> members = new ArrayList<String>();
		for (Combatant c : Squad.active.members) {
			members.add(c.toString());
		}
		target = Squad.active.members.get(
				Javelin.choose("Select a squad member:", members, true, true));
		return target;
	}

	Character print() {
		Javelin.app.switchScreen(this);
		return feedback();
	}

	void removeaattack(ArrayList<AttackSequence> attacksp) {
		List<AttackSequence> removable = selectattacks(attacksp);
		attacksp.remove(removable.get(
				Javelin.choose("Remove which attack?", removable, true, true)));
	}

	void listremovals(Combatant target, ArrayList<String> types) {
		if (!target.source.breaths.isEmpty()) {
			types.add("Breath");
		}
		if (!selectattacks(target.source.melee).isEmpty()) {
			types.add("Mêléé attack");
		}
		if (!selectattacks(target.source.ranged).isEmpty()) {
			types.add("Ranged attack");
		}
		if (!target.spells.isEmpty()) {
			types.add("Spell");
		}
	}

	List<AttackSequence> selectattacks(ArrayList<AttackSequence> attacksp) {
		ArrayList<AttackSequence> attacks = new ArrayList<AttackSequence>(
				attacksp);
		for (AttackSequence attack : attacksp) {
			if (attack.powerful || attack.rapid) {
				attacks.remove(attack);
			}
		}
		if (attacks.size() == 1) {
			attacks.clear();
		}
		return attacks;
	}

	void generate(Actor place) {
		int x = Squad.active.x;
		int y = Squad.active.y;
		int size = World.scenario.size;
		while (x == Squad.active.x && y == Squad.active.y
				|| World.get(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || x >= size || y < 0 || y >= size) {
				generate(place);
				return;
			}
		}
		place.x = x;
		place.y = y;
		place.place();
	}

	@Override
	public String printinfo() {
		return "You are wishing " + rubies + " of your " + maxrubies
				+ " rubies.";
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> options = new ArrayList<Option>();
		// 1 ruby
		options.add(new ChangeAvatar("change unit avatar", 'c', 1, true, this));
		options.add(new RemoveAbility("discard ability", 'd', 1, true, this));
		options.add(new Ressurect("ressurect last fallen ally", 'r', 1, false,
				this));
		SummonAlly weaksumon = new SummonAlly("summon ally (weak)", 's', 1,
				false, this);
		weaksumon.fixed = 1f;
		options.add(weaksumon);
		options.add(new Teleport("teleport", 't', 3, false, this));
		options.add(new Rebirth("upgrade cleanse", 'u', 1, true, this));
		options.add(new SummonAlly("summon ally", 'S', 2, false, this));
		options.add(new Gold(this));
		options.add(new ConjureMasterKey(this));
		if (Campaign.class.isInstance(World.scenario)) {
			options.add(new Win("win", 'w', 7, false, this));
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