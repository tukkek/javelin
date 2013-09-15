package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * Show me the Money!
 * 
 * Coin includes code for managing the hero's wealth.
 * 
 * Also contains library initialisation code for coins
 * 
 * @author Mike
 */
public class Coin {
	public static final String SOVEREIGN = "sovereign";
	public static final String GOLD = "gold";
	public static final String SILVER = "silver";
	public static final String COPPER = "copper";

	public static final int SOVEREIGN_AMOUNT = 1000;
	public static final int GOLD_AMOUNT = 100;
	public static final int SILVER_AMOUNT = 10;
	public static final int COPPER_AMOUNT = 1;

	/**
	 * Rounds money probabilistically
	 * 
	 * @param v
	 *            Average amount of money
	 * @return
	 */
	public static int roundMoney(int v) {
		int m = 1;
		while (v > 100) {
			v = v / 10 + (RPG.r(10) < v % 10 ? 1 : 0);
			m = m * 10;
		}
		return v * m;
	}

	/**
	 * Rounds down money amount
	 * 
	 * Use this version when you don't want the value to change
	 * 
	 * @param v
	 * @return
	 */
	public static int roundDownMoney(int v) {
		int m = 1;
		while (v > 100) {
			v = v / 10;
			m = m * 10;
		}
		return v * m;
	}

	public static String valueString(int v) {
		int t = 0;
		while (t < 2 && v > 0 && v % 10 == 0) {
			t++;
			v /= 10;
		}
		if (v <= 0) {
			return "nothing";
		}
		switch (t) {
		case 0:
			return v + " copper";
		case 1:
			return v + " silver";
		case 2:
			return v + " gold";
		default:
			return "nothing";
		}
	}

	public static Thing createRandomMoney(int v) {
		return createMoney(v);
	}

	/**
	 * Create a single money stack. Return a rounded amount.
	 * 
	 * @param amount
	 *            Amount that will be rounded in units of copper.
	 */
	public static Thing createMoney(int amount) {
		int type = 0;

		if (amount >= 1000) {
			type++;
		}
		if (amount >= 100) {
			type++;
		}
		if (RPG.d(2) == 1 && amount >= 10) {
			type++;
		}
		for (int i = 0; i < type; i++) {
			amount /= 10;
		}

		if (amount <= 0) {
			amount = 1;
		}

		String name = "copper coin";
		switch (type) {
		case 1:
			name = "silver coin";
			break;
		case 2:
			name = "gold coin";
			break;
		case 3:
			name = "sovereign";
			break;
		}

		Thing t = Lib.create("Barricade");
		t.set("Number", amount);
		return t;
	}

	protected static int valueOf(Thing coin) {
		return coin.getStat(RPG.ST_ITEMVALUE) * coin.getNumber();
	}

	// Calculate funds available
	public static int getMoney(Thing t) {
		Thing[] cash = t.getFlaggedContents("IsMoney");
		int tot = 0;
		for (Thing element : cash) {
			tot += valueOf(element);
		}
		return tot;
	}

	/**
	 * Add these in the most weight efficent means possible.
	 */
	public static void addMoney(Thing person, int amountInCoppers) {
		if (amountInCoppers == 0) {
			return;
		}
		if (amountInCoppers < 0) {
			removeMoney(person, -amountInCoppers);
			return;
		}
		int unitAmount = COPPER_AMOUNT;
		String unit = COPPER;
		if (amountInCoppers > SOVEREIGN_AMOUNT) {
			unitAmount = SOVEREIGN_AMOUNT;
			unit = SOVEREIGN;
		} else if (amountInCoppers > GOLD_AMOUNT) {
			unitAmount = GOLD_AMOUNT;
			unit = GOLD;
		} else if (amountInCoppers > SILVER_AMOUNT) {
			unitAmount = SILVER_AMOUNT;
			unit = SILVER;
		}
		Thing money = createMoney(amountInCoppers / unitAmount, unit);
		int moneyBeingAdded = valueOf(money);
		person.addThingWithStacking(money);
		addMoney(person, amountInCoppers - moneyBeingAdded);
	}

	private static Thing createMoney(int amount, String unit) {
		String coinName = unit.equals(SOVEREIGN) ? "" : " coin";
		return Lib.create("" + amount + " " + unit + coinName);
	}

	public static Thing createLevelMoney(int level) {
		level = RPG.d(level);
		int amount = (int) (10 * level * Math.pow(1.1, level));
		if (amount <= 0) {
			amount = 1;
		}
		return createMoney(RPG.d(amount));
	}

	public static void removeMoney(Thing person, int amountInCoppers) {
		if (amountInCoppers <= 0) {
			addMoney(person, -amountInCoppers);
			return;
		}
		int funds = getMoney(person) - amountInCoppers;
		Thing[] cash = person.getFlaggedContents("IsMoney");
		// TODO: make this spend coins effectively
		for (Thing element : cash) {
			element.remove();
		}
		if (funds > 0) {
			addMoney(person, funds);
		}
	}

	public static void init() {
		Thing t = Lib.extend("base coin", "base item");
		t.set("IsMoney", 1);
		t.set("IsCoin", 1);
		t.set("IsStatusKnown", 1);
		t.set("Image", 140);
		t.set("HPS", 6);
		t.set("ItemWeight", 20);
		t.set("LevelMin", 1);
		t.set("RES:water", 100);
		t.set("Z", Thing.Z_ITEM - 2);
		t.set("Frequency", 100);
		t.set("ASCII", "$");
		Lib.add(t);

		t = Lib.extend("copper coin", "base coin");
		t.set("Value", COPPER_AMOUNT);
		t.set("Image", 144);
		t.set("ItemWeight", 20);
		Lib.add(t);

		t = Lib.extend("silver coin", "base coin");
		t.set("Value", SILVER_AMOUNT);
		t.set("Image", 143);
		t.set("ItemWeight", 30);
		t.set("LevelMin", 1);
		Lib.add(t);

		t = Lib.extend("gold coin", "base coin");
		t.set("Value", GOLD_AMOUNT);
		t.set("Image", 140);
		t.set("ItemWeight", 50);
		t.set("LevelMin", 5);
		Lib.add(t);

		t = Lib.extend("sovereign", "base coin");
		t.set("Value", SOVEREIGN_AMOUNT);
		t.set("Image", 140);
		t.set("ItemWeight", 100);
		t.set("LevelMin", 8);
		Lib.add(t);
	}
}