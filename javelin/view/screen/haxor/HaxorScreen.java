package javelin.view.screen.haxor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * @see Haxor
 * @author alex
 */
public class HaxorScreen extends SelectScreen {
	transient private static final ArrayList<String> QUOTES =
			new ArrayList<String>();

	private static final String DAILYQUOTE;

	static {
		QUOTES.add("Keep up with updates at http://javelinrl.wordpress.com :)");
		QUOTES.add("Come discuss the game at www.reddit.com/r/javelinrl/ :)");
		QUOTES.add("Haxor's pet exclaims \"Kupo!\"");
		QUOTES.add("Haxor's pet exclaims \"Pika-pika!\"");
		QUOTES.add(
				"Haxor asks \"Do you feel like playing a round of Pachisi?\"");
		QUOTES.add(
				"Haxor's temple is unusually dark today... You are likely to be eaten by a grue.");
		QUOTES.add(
				"Haxor tells you again about how his son used to be an adventurer until he took an arrow to the knee.");
		QUOTES.add(
				"A night hag is visiting the Temple, she asks of you \"What can change the nature of a man?\"");
		QUOTES.add(
				"Haxor reminisces about one of his battle stories. He starts with \"War... War never changes...\"");
		QUOTES.add(
				"Haxor is working on an artifact he calls the Chrono Trigger.");
		QUOTES.add(
				"Haxor is reading maps, trying to deduce the hidden location of the Rogue Basin.");
		QUOTES.add(
				"Haxor is laughing histerically. He smiles at you and asks \"Why so serious?\"");
		QUOTES.add("Haxor says he is baking a cake but you don't see any...");
		DAILYQUOTE = getdailyquote(QUOTES);
	}

	/**
	 * @return A random element from the list, guaranteed to be the same for a
	 *         24 hour period.
	 */
	public static String getdailyquote(List<String> list) {
		Calendar now = Calendar.getInstance();
		String seed = "";
		seed += now.get(Calendar.DAY_OF_MONTH);
		seed += now.get(Calendar.MONTH) + 1;
		seed += now.get(Calendar.YEAR);
		return list.get(new Random(seed.hashCode()).nextInt(list.size() - 1));
	}

	/** See {@link SelectScreen#SelectScreen(String, Town)}. */
	public HaxorScreen() {
		super("Welcome to the Temple of Haxor!", null);
		stayopen = false;
	}

	@Override
	public boolean select(Option o) {
		double price = o.price;
		Hax h = (Hax) o;
		String error = h.validate();
		if (error != null) {
			text += "\n" + error;
			Javelin.app.switchScreen(this);
			return false;
		}
		if (price > Haxor.singleton.rubies) {
			text += "\n\"I need more rupees!!\", says Haxor with a smirk on his face.";
			Javelin.app.switchScreen(this);
			return false;
		}
		if (!h.hack(h.requirestarget ? selectmember() : null, this)) {
			return false;
		}
		Haxor.singleton.rubies -= price;
		return true;
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
		ArrayList<AttackSequence> attacks =
				new ArrayList<AttackSequence>(attacksp);
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

	void generate(WorldActor place) {
		int x = Haxor.singleton.x;
		int y = Haxor.singleton.y;
		while (x == Haxor.singleton.x && y == Haxor.singleton.y
				|| WorldActor.get(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || x >= World.SIZE || y < 0
					|| y >= World.SIZE) {
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
		return DAILYQUOTE + "\nYou have " + Haxor.singleton.rubies + " rubies.";
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> options = new ArrayList<Option>();
		// 1 ruby
		options.add(new BorrowMoney("borrow money", 'b', 1, false));
		options.add(new ChangeAvatar("change unit avatar", 'c', 1, true));
		options.add(new RemoveAbility("discard ability", 'd', 1, true));
		options.add(new Ressurect("ressurect last fallen ally", 'r', 1, false));
		SummonAlly weaksumon =
				new SummonAlly("summon ally (weak)", 's', 1, false);
		weaksumon.fixed = 1f;
		options.add(weaksumon);
		options.add(new Teleport("teleport", 't', 1, false));
		options.add(new Rebirth("upgrade cleanse", 'u', 1, true));
		// 2 rubies
		options.add(new SummonAlly("summon ally", 'S', 2, false));
		options.add(new Materialize("materialize", 'M', 2, false));
		options.add(new Win("win", 'W', 7, false));
		return options;
	}

	@Override
	public String getCurrency() {
		return "";
	}

	@Override
	public String printpriceinfo(Option o) {
		if (o.price == 0) {
			return o instanceof ChangeAvatar ? (" (free)")
					: " (free, single use)";
		}
		long price = Math.round(o.price);
		return " (" + price + " " + (price == 1 ? "ruby" : "rubies") + ")";
	}
}