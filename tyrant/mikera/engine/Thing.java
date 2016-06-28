/**
 * 
 */

package tyrant.mikera.engine;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javelin.controller.Movement;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Being;
import tyrant.mikera.tyrant.Combat;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.EventHandler;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Item;
import tyrant.mikera.tyrant.util.Text;
import tyrant.org.newdawn.slick.util.pathfinding.AStarPathFinder;
import tyrant.org.newdawn.slick.util.pathfinding.Mover;
import tyrant.org.newdawn.slick.util.pathfinding.TileBasedMap;

/**
 * The Thing class represents all objects that can appear on a Tyrant map. Thing
 * supports an arbitrary, mutable list of properties inherited from BaseObject.
 * 
 * It also implements an inventory, which contains other Things
 * 
 * It also has the ability to be affected by property modifiers
 * 
 * Note that Thing is final for performance reasons (good JVMs should be able to
 * optimise all the small accessor methods)
 * 
 * @author Mike
 */
public class Thing extends BaseObject implements Description, ThingOwner {

	/**
	 * Hack to enable visual elements to set an override image.
	 * 
	 * TODO 2.0 visual overhaul
	 */
	public Image javelinimage = null;

	private static final long serialVersionUID = 2056412474365358346L;

	/**
	 * Next Thing in linked list for map squares
	 */
	public Thing next;

	/**
	 * Place where this thing can be found Generally either: - Another Thing
	 * (item in inventory) - A Map
	 */
	public ThingOwner place;

	/**
	 * x-position of thing
	 * 
	 * Precise definition dependant on the owner e.g. if place is an instanc of
	 * Map Then x is the column number of the Thing's location
	 */
	public int x;

	/**
	 * y-position of thing
	 * 
	 * Precise definition dependant on the owner e.g. if place is an instanc of
	 * Map Then y is the column number of the Thing's location
	 */
	public int y;

	/**
	 * @author Mike Stores an array of things contained or owned by the current
	 *         thing Used for: - Items in inventory - Game effects
	 */
	private Thing[] inv;

	/**
	 * Count of active items in the Thing's inv array
	 */
	private int invcount;

	/**
	 * Optional list of modifiers that are affecting this thing
	 */
	private HashMap modifiers = null;

	public Combatant combatant;

	// Z ordering constanrs
	public static final int Z_ELSEWHERE = -10;
	public static final int Z_FLOOR = 0;
	public static final int Z_ONFLOOR = 5;
	public static final int Z_ITEM = 20;
	public static final int Z_MOBILE = 40;
	public static final int Z_OVERHEAD = 60;
	public static final int Z_SYSTEM = 80;

	public Thing() {
		super();
	}

	public Thing(final BaseObject baseObject) {
		super(baseObject);
	}

	public Thing(final HashMap propertiesToCopy, final BaseObject parent) {
		super(propertiesToCopy, parent);
	}

	public Thing(final Thing t) {
		super(t);
	}

	@Override
	public String toString() {
		return getString("Name");
	}

	public int invCount() {
		return invcount;
	}

	public ThingOwner owner() {
		return place;
	}

	/**
	 * Calculate total weight of inventory items
	 */
	public int getInventoryWeight() {
		int result = 0;
		for (int i = 0; i < invcount; i++) {
			result = result + inv[i].getWeight();
		}
		return result;
	}

	/**
	 * Get the complete contents list for a thing i.e. all items contained by
	 * the current Thing
	 */
	public Thing[] getItems() {
		sortItems();
		if (invcount == 0) {
			return new Thing[0];
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getFlag("IsItem")) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	public int countItems(final String name) {
		int c = 0;
		for (int i = 0; i < invcount; i++) {
			if (name.equals(inv[i].name())) {
				c += inv[i].getStat("Number");
			}
		}
		return c;
	}

	public int countIdentifiedItems(final String name) {
		int c = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (name.equals(inv[i].name()) && Item.isIdentified(t)) {
				c += inv[i].getStat("Number");
			}
		}
		return c;
	}

	/**
	 * Removes a number of items from the Thing, up to a specified number.
	 * 
	 * @param name
	 *            The name of the type of items to remove
	 * @param num
	 *            The maximum number to remove
	 * @return The actual number removed
	 */
	public int removeItems(final String name, int num) {
		int c = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (name.equals(t.name())) {
				final int snum = t.getStat("Number");
				if (snum >= num) {
					c += num;
					t.remove(num);
					break;
				}
				t.remove();
				num -= snum;
				c += snum;
				i--; // step back one since stack is removed
			}
		}
		return c;
	}

	/**
	 * Gets an inventory item with a particular name
	 * 
	 * Returns null if not found
	 * 
	 * @author Mike
	 */
	public Thing getContents(final String name) {
		for (int i = 0; i < invcount; i++) {
			if (inv[i].name().equals(name)) {
				return inv[i];
			}
		}
		return null;
	}

	public Thing getWeapon(final int wt) {
		for (int i = 0; i < invcount; i++) {
			if (inv[i].y == wt) {
				return inv[i];
			}
		}
		return null;
	}

	public Thing[] getUsableContents() {
		if (invcount == 0) {
			return null;
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getFlag("IsUsable") || t.handles("OnUse")) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	// get all contents with a given stat value
	public Thing[] getFlaggedContents(final String stat) {
		if (invcount == 0) {
			return new Thing[0];
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getStat(stat) > 0) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	/**
	 * Get a list of the contents that match a specific type from a thing e.g.
	 * all money items owned by the current Thing
	 */
	public Thing[] getFlaggedItems(final String flag) {
		// This is an alternate implementation of getFlaggedContents
		// It is here for performance testing purposes and will probably go away
		// soon.
		// Do not use this method in your code -Rick
		if (invcount == 0) {
			return null;
		}
		int count = 0;
		Thing t = null;
		for (int i = 0; i < invcount; i++) {
			t = inv[i];
			if (t.getFlag(flag)) {
				count++;
			}
		}
		if (count > 0) {
			int insertionIndex = 0;
			final Thing[] result = new Thing[count];
			for (int i = 0; i < invcount; i++) {
				t = inv[i];
				if (t.getFlag(flag)) {
					result[insertionIndex] = t;
					insertionIndex++;
				}
			}
			return result;
		}
		return null;
	}

	// get all contents with a given stat value
	public Thing[] getContents(final String stat, final Object val) {
		if (invcount == 0) {
			return null;
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (val.equals(t.get(stat))) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	// get all contents that can be worn/wielded with 'w'
	public Thing[] getWieldableContents() {
		if (invcount == 0) {
			return null;
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getFlag("WieldType")) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	// time effect - pass to active sub-items
	public void inventoryAction(final Event ae) {
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			t.action(ae);
		}
	}

	public Thing getThing(final int i) {
		return i >= 0 & i < invcount ? inv[i] : null;
	}

	public int getUsage(final int i) {
		return i >= 0 & i < invcount ? inv[i].y : RPG.WT_ERROR;
	}

	public void ensureSize(final int s) {
		if (inv != null && s <= inv.length) {
			return;
		}
		final int invsize = s + 5;
		final Thing[] newinv = new Thing[invsize];
		if (inv != null) {
			System.arraycopy(inv, 0, newinv, 0, invcount);
		}
		inv = newinv;
	}

	// all removes go through here eventually....
	// part of ThingOwner interface
	@Override
	public void removeThing(final Thing thing) {
		final int pos = thing.x;
		if (inv == null) {
			throw new Error("Empty Inventory bug!");
		}
		if (thing.place != this) {
			throw new Error("Thing in wrong place!");
		}
		if (inv[pos] != thing) {
			throw new Error("Thing in wrong position!");
		}
		if (pos < invcount - 1) {
			System.arraycopy(inv, pos + 1, inv, pos, invcount - pos - 1);
		}

		// not using anymore, remove modifiers
		setUsage(thing, RPG.WT_NONE);
		thing.unApplyModifiers("CarriedModifiers", this);

		inv[invcount - 1] = null;
		thing.place = null;
		thing.next = null;
		invcount = invcount - 1;
		for (int i = 0; i < invcount; i++) {
			inv[i].x = i; // new positions
		}
	}

	public void swapItems(final int a, final int b) {
		if (a >= invcount || b >= invcount || a < 0 || b < 0) {
			return;
		}
		final Thing ti = inv[a];
		inv[a] = inv[b];
		inv[b] = ti;
		inv[a].x = a;
		inv[b].x = b;
	}

	public void sortItems() {
		if (inv == null) {
			return;
		}
		Arrays.sort(inv, new Comparator() {
			@Override
			public int compare(final Object a, final Object b) {
				final Thing ta = (Thing) a;
				final Thing tb = (Thing) b;

				if (tb == null) {
					return -1;
				}
				if (ta == null) {
					return 1;
				}

				return tb.y - ta.y;
			}
		});
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] != null) {
				inv[i].x = i;
			}
		}
	}

	public Thing addThingWithStacking(final Thing thing) {
		// see if we can stack with existing items
		if (thing.getFlag("IsItem")) {
			for (int i = 0; i < invcount; i++) {
				final Thing t = inv[i];
				if (t.y > 0 && t.y != RPG.WT_MISSILE) {
					continue;
				}
				if (thing.stackWith(t)) {
					return t;
				}
			}
		}

		if (thing.place != this) {
			return addThing(thing);
		}
		return thing;
	}

	public Thing separate(final int n) {
		final int number = getStat("Number");
		if (n >= number) {
			return this;
		}
		if (n <= 0) {
			return null;
		}

		incStat("Number", -n);

		Thing t = (Thing) clone();
		t.set("Number", n);
		if (place instanceof Thing) {
			t = ((Thing) place).addThing(t);
		} else if (place instanceof BattleMap) {
			t = ((BattleMap) place).addThing(t, x, y);
		}
		return t;
	}

	public Thing restack() {
		if (place instanceof Thing) {
			final Thing p = (Thing) place;
			return p.addThingWithStacking(this);
		}
		return this;
	}

	public Thing addThing(final Thing thing) {
		if (thing == null) {
			return null;
		}
		thing.remove();

		// see if we can stack with existing items
		if (thing.getFlag("NoStack")) {
			final String cancel = thing.getString("CancelEffect");

			for (int i = 0; i < invcount; i++) {
				final String n = inv[i].name();
				if (thing.name().equals(n)) {
					return null;
				}

				// if cancel effect in place, remove thing
				// note: need i-- as inv array is changed
				if (n.equals(cancel)) {
					inv[i--].remove();
				}
			}
		}

		ensureSize(invcount + 1);
		inv[invcount] = thing;
		thing.place = this;
		thing.next = null; // don't care about list
		thing.x = invcount; // might be useful to know slot!
		setUsage(thing, RPG.WT_NONE); // item not in use
		invcount = invcount + 1;
		thing.applyModifiers("CarriedModifiers", this);

		return thing;
	}

	public boolean clearUsage(final int wt) {
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			final int it = t.y;
			if (it == wt && t.getFlag("IsItem")) {

				if (!t.getFlag("IsCursed")) {
					// update usage, change modifiers if needed
					message("You remove your " + t.getName(Game.hero()));
					setUsage(t, RPG.WT_NONE);
					t.restack();
				} else {
					message("You are unable to remove your "
							+ t.getName(Game.hero()) + "!");
					return false;
				}
			}
		}
		return true;
	}

	// generic thing properties
	public boolean isMobile() {
		return getFlag("IsMobile");
	}

	public boolean isRunning() {
		return getFlag("IsRunMode");
	}

	public boolean isBlocking() {
		return getFlag("IsBlocking");
	}

	public Thing holder() {
		return place instanceof Thing ? (Thing) place : null;
	}

	public boolean handle(final Event e) {
		final EventHandler eh = getHandler(e.handlerName());
		if (eh != null) {
			if (eh.handle(this, e)) {
				return true;
			}
		}
		return false;
	}

	public boolean handles(final String handlerName) {
		Game.assertTrue(handlerName.startsWith("On"));
		return containsKey(handlerName);
	}

	public void action(final Event ae) {
		// inventoryAction(ae);

		// set thing as actor
		Game.actor = this;

		// if (handle(ae)) {
		// return;
		// }
	}

	public void multiplyStat(final String s, final double v) {
		set(s, RPG.round(getBaseStat(s) * v));
	}

	// object state functions
	public boolean isIdentified() {
		return Item.isIdentified(this);
	}

	public int getWeight() {
		return getStat("ItemWeight") * getStat("Number");
	}

	public int getNumber() {
		return getBaseStat("Number");
	}

	public int getImage() {
		return getStat("Image");
	}

	public int getZ() {
		return getStat("Z");
	}

	public void setPersonality(final Script t) {
		set("OnChat", t);
	}

	// add attribute and activate
	public void addAttribute(final Thing a) {
		addThing(a);

		// message and set usage if add successful
		// (may not work due to stacking constraints)
		if (a.place == this) {
			if (isHero()) {
				final String mes = a.getString("AttributeAddMessage");
				if (mes != null) {
					message(mes);
				}
			}
			setUsage(a, RPG.WT_EFFECT);
		}
	}

	public ThingOwner getPlace() {
		return place;
	}

	public Thing getParent() {
		if (place instanceof Thing) {
			return (Thing) place;
		}
		return null;
	}

	// Description interface
	@Override
	public String getName(final int number, final int article) {
		return Describer.describe(Game.hero(), this, article, number);
	}

	public String getPronoun(final int number, final int acase) {
		return Describer.getPronoun(number, acase, getNameType(), getGender());
	}

	// return nametype and gender for a generic thing
	// override as necessary.
	public int getNameType() {
		return getStat("NameType");
	}

	public int getGender() {
		return getStat("Gender");
	}

	@Override
	public String getDescriptionText() {
		final String s = (String) get("Description");
		return s != null ? s : Text.capitalise(getName(Game.hero()) + ".");
	}

	public Description getDescription() {
		return this;
	}

	/**
	 * Returns true if this thing is the current hero
	 * 
	 * Needs to be fast - called very often!
	 * 
	 * @return
	 */
	public final boolean isHero() {
		return this == Game.hero();
	}

	public String is() {
		return isHero() || getNumber() > 1 ? "are" : "is";
	}

	public String verb(final String v) {
		return isHero() || getNumber() > 1 ? v : v + "s";
	}

	public String getName(final Thing person, final int quantity) {
		return Describer.describe(person, this, Description.ARTICLE_NONE,
				quantity);
	}

	public String name() {
		return getString("Name");
	}

	public void message(final String m) {
		if (isHero()) {
			Game.messageTyrant(m);
		}
	}

	public String getName() {
		return getName(Game.hero());
	}

	public String getAName() {
		return getAName(Game.hero());
	}

	public String getTheName() {
		return getTheName(Game.hero());
	}

	public String getYourName() {
		return getYourName(Game.hero());
	}

	public String getName(final Thing person) {
		if (person.combatant == null) {
			return "";
		}
		return person.combatant.source.name;
	}

	public String getAName(final Thing person) {
		return Describer.describe(person, this, Description.ARTICLE_INDEFINITE);
	}

	public String getTheName(final Thing person) {
		return Describer.describe(person, this, Description.ARTICLE_DEFINITE);
	}

	public String getYourName(final Thing person) {
		return Describer.describe(person, this, Description.ARTICLE_POSSESIVE);
	}

	public String getFullName(final Thing person) {
		return Describer.describe(person, this);
	}

	// return describing adjectives if applicable
	public String getAdjectives() {
		final String adjective = getString("Adjective");
		String adj = adjective;
		if (adj == null) {
			adj = "";
		} else {
			adj = adj + " ";
		}

		if (getFlag("IsItem")) {
			if (getFlag("IsStatusKnown")) {
				if (getFlag("IsCursed")) {
					adj += "cursed ";
				} else if (getFlag("IsBlessed")) {
					adj += "blessed ";
				}
			}
			if (adjective == null && getFlag("IsRunic")) {
				adj += "runic ";
			}
		}
		return adj.equals("") ? null : adj;
	}

	public String getSingularName() {
		if (!Item.isIdentified(this)) {
			final String uname = getString("UName");
			if (uname != null) {
				return uname;
			}
		}
		return getString("Name");
	}

	public String getPluralName() {
		if (!Item.isIdentified(this)) {
			String uname = (String) get("UNamePlural");
			if (uname != null) {
				return uname;
			}
			uname = getString("UName");
			if (uname != null) {
				return uname + "s";
			}
		}
		final String pn = getString("NamePlural");
		return pn != null ? pn : getString("Name") + "s";
	}

	public int getQuality() {
		return getStat("Quality");
	}

	@Override
	public final BattleMap getMap() {
		return place == null ? null : place.getMap();
	}

	public Thing[] inv() {
		return getInventory();
	}

	public boolean hasInventory() {
		return inv != null;
	}

	public final int getMapX() {
		if (place instanceof BattleMap) {
			return x;
		}
		return place == null ? -1 : ((Thing) place).getMapX();
	}

	// can thing "see" specified map location?
	public boolean canSee(final BattleMap m, final int tx, final int ty) {
		final int vr = Being.calcViewRange(this);
		if (vr <= 0) {
			return false;
		}
		if (vr * vr < (x - tx) * (x - tx) + (y - ty) * (y - ty)) {
			return false;
		}
		if (m == null || m != place) {
			return false;
		}
		return m.isLOS(x, y, tx, ty);
	}

	// can thing see other thing?
	public boolean canSee(final Thing t) {
		if (t.place != place) {
			return false;
		}

		// fast path for seeing if hero is visible
		if (t.isHero()) {
			return ((BattleMap) place).isHeroLOS(x, y);
		}

		// fast path if we are the hero
		if (isHero()) {
			return ((BattleMap) place).isVisible(t.x, t.y);
		}

		return canSee(getMap(), t.x, t.y);
	}

	/**
	 * Calculate LOS on current map for this thing
	 * 
	 * @param period
	 */
	public void calculateVision() {
		final BattleMap map = getMap();
		if (map != null) {
			map.calcVisible(this);
		}
	}

	public final int getMapY() {
		if (place instanceof BattleMap) {
			return y;
		}
		return place == null ? -1 : ((Thing) place).getMapY();
	}

	public void remove() {
		if (place != null) {
			place.removeThing(this);
		}
	}

	public Thing remove(final int n) {
		if (n < 1) {
			throw new Error("Thing.remove(): can't remove " + n + " objects");
		}
		final int number = getNumber();
		if (number <= 0) {
			throw new Error(
					"Thing.remove(): stack contains " + number + " objects");
		}
		if (n == number) {
			remove();
			return this;
		} else if (n < number) {
			final Thing t = (Thing) clone();

			set("Number", number - n);
			t.set("Number", n);
			return t;
		} else {
			throw new Error("Thing: tring to remove more than entire stack!");
		}
	}

	public void replaceWith(final String s) {
		this.replaceWith(Lib.create(s));
	}

	public void die() {
		Combat.die(this);
	}

	// move to square, landing on floor
	public void moveTo(final BattleMap m, final int tx, final int ty) {
		Movement.moveTo(this, m, tx, ty);
	}

	/**
	 * Move an object randomly to an adjacent, unblocked square.
	 * 
	 * @return True if object was moved from original place
	 */
	public boolean displace() {
		if (!(place instanceof BattleMap)) {
			return false;
		}
		final boolean blocking = isBlocking();
		final BattleMap m = (BattleMap) place;

		// try random move first
		if (true) {
			int nx = x + 2 * RPG.r(2) - 1;

			int ny = y + 2 * RPG.r(2) - 1;
			if (RPG.d(2) == 1) {
				if (RPG.d(2) == 1) {
					nx = x;
				} else {
					ny = y;
				}
			}
			if (!m.isTileBlocked(nx, ny)
					&& !(blocking && m.isBlocked(nx, ny))) {
				moveTo(m, nx, ny);
				return true;
			}
		}

		// count possible moves
		int choices = 0;
		for (int nx = x - 1; nx <= x + 1; nx++) {
			for (int ny = y - 1; ny <= y + 1; ny++) {
				if (nx == x && ny == y) {
					continue;
				}
				if (!m.isTileBlocked(nx, ny)
						&& !(blocking && m.isBlocked(nx, ny))) {
					choices++;
				}
			}
		}

		// select a possible move randomly
		if (choices == 0) {
			return false;
		}
		choices = RPG.d(choices);
		for (int nx = x - 1; nx <= x + 1; nx++) {
			for (int ny = y - 1; ny <= y + 1; ny++) {
				if (nx == x && ny == y) {
					continue;
				}
				if (!m.isTileBlocked(nx, ny)
						&& !(isBlocking() && m.isBlocked(nx, ny))) {
					choices--;
					if (choices <= 0) {
						moveTo(m, nx, ny);
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isWithin(final ThingOwner thing) {
		for (Thing head = this; head != null; head = (Thing) head.place) {
			if (head.place == thing) {
				return true;
			}
			if (!(head.place instanceof Thing)) {
				return false;
			}
		}
		return false;
	}

	public boolean isTransparent() {
		return !getFlag("IsViewBlocking");
	}

	// returns true f the Thing is damaged
	// used for Items mainly
	public boolean isDamaged() {
		return getFlag("Damage");
	}

	// Message to be given if Thing is visible to the player
	public void visibleMessage(final String s) {
		if (isVisible(Game.hero()) && !isInvisible()) {
			Game.messageTyrant(s);
		}
	}

	// returns true if item can currently be seen
	// doesn't account for invisible items,
	// i.e. assumes hero can see through all invisibilities
	public boolean isVisible(final Thing toThing) {
		if (place instanceof BattleMap) {
			// can see it if it is in a visible square
			return toThing.canSee(this);
		}
		// can see it if we are carrying it!
		return toThing.isHero() && rootThing() == toThing;
	}

	public Thing rootThing() {
		return place instanceof Thing ? ((Thing) place).rootThing() : this;
	}

	// returns true if the item should be displayed
	public boolean isInvisible() {
		return getFlag("IsInvisible");
	}

	// clone method. Creates displaced copy of this
	@Override
	public Object clone() {
		final Thing t = new Thing(super.getLocal(), super.getInherited());
		t.place = null;
		t.next = null;

		if (inv != null) {
			t.inv = inv.clone();
			for (int x = 0; x < invcount; x++) {
				t.inv[x] = (Thing) inv[x].clone();
				t.inv[x].place = t;
			}
		}

		return t;
	}

	// check if stacking possible
	public boolean canStackWith(final Thing other) {
		if (this == other) {
			return false;
		}
		if (!other.getFlag("IsItem")) {
			return false;
		}
		if (other == this) {
			throw new Error("Can't do canStackWith(self)!!");
		}
		if (!equalsIgnoreNumber(other)) {
			return false;
		}
		if (!sameContents(other)) {
			return false;
		}
		return true;
	}

	public boolean sameContents(final Thing other) {
		if (invcount == 0 && other.invcount == 0) {
			return true;
		}
		if (invcount != other.invcount) {
			return false;
		}
		final int n = invcount;
		for (int i = 0; i < n; i++) {
			if (!inv[i].name().equals(other.inv[i].name())) {
				return false;
			}
		}

		return true;
	}

	public boolean stackWith(final Thing thing) {
		if (!canStackWith(thing)) {
			return false;
		}
		thing.incStat("Number", getStat("Number"));
		remove();
		return true;
	}

	public int getResistance(final String dt) {
		return getStat("RES:" + dt);
	}

	public int getLevel() {
		return getStat("Level");
	}

	// checks if being has item in possession
	public boolean hasItem(final String s) {
		for (int i = 0; i < invcount; i++) {
			if (inv[i].name().equals(s)) {
				return true;
			}
		}
		return false;
	}

	public Thing getItem(final String s) {
		for (int i = 0; i < invcount; i++) {
			if (inv[i].name().equals(s)) {
				return inv[i];
			}
		}
		return null;
	}

	/**
	 * checks if a thing is wielded
	 * 
	 * @return the fact
	 */
	public boolean isWielded(final Thing item) {
		final boolean result = false;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getFlag("IsItem") && t.y > 0 && t == item) {
				return true;
			}
		}
		return result;
	}

	/**
	 * Returns all wielded items/effects
	 * 
	 * @return Array of wielded items
	 */
	public Thing[] getWielded() {
		if (invcount == 0) {
			return new Thing[0];
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.y > 0) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	/**
	 * Returns all wielded items/effects
	 * 
	 * @return Array of wielded items
	 */
	public Thing[] getWieldedItems() {
		if (invcount == 0) {
			return new Thing[0];
		}
		final Thing[] temp = new Thing[invcount];
		int tempcount = 0;
		for (int i = 0; i < invcount; i++) {
			final Thing t = inv[i];
			if (t.getFlag("IsItem") && t.y > 0) {
				temp[tempcount] = t;
				tempcount++;
			}
		}
		final Thing[] result = new Thing[tempcount];
		if (tempcount > 0) {
			System.arraycopy(temp, 0, result, 0, tempcount);
		}
		return result;
	}

	// return the wielded / worn item in particular location
	public Thing getWielded(final int wt) {
		Thing w = getWeapon(wt);

		if (w == null) {
			if (wt == RPG.WT_MAINHAND) {
				w = getWeapon(RPG.WT_TWOHANDS);
			}
			if (wt == RPG.WT_TORSO || wt == RPG.WT_LEGS) {
				w = getWeapon(RPG.WT_FULLBODY);
			}
		}
		return w;
	}

	private static final Thing[] noThings = {};

	public Thing[] getInventory() {
		return inv == null ? noThings : inv;
	}

	public void dropThing(final Thing thing) {
		thing.remove();
		if (place instanceof BattleMap) {
			((BattleMap) place).addThing(thing, x, y);
		}
	}

	// wields the item in appropriate slot
	// unwields other items to allow this
	public boolean wield(final Thing t) {
		return wield(t, t.getStat("WieldType"));
	}

	public boolean wield(final Thing t, final int wt) {
		// already there?
		if (t.y == wt) {
			return true;
		}

		if (!clearUsage(wt)) {
			return false;
		}

		// remove other items if cannot be wielded together
		// remeber to avoid quick boolean evaluation!
		if (wt == RPG.WT_TWOHANDS) {
			if (!clearUsage(RPG.WT_MAINHAND) | !clearUsage(RPG.WT_SECONDHAND)) {
				return false;
			}
		}
		if (wt == RPG.WT_MAINHAND) {
			if (!clearUsage(RPG.WT_TWOHANDS)) {
				return false;
			}
		}
		if (wt == RPG.WT_SECONDHAND) {
			if (!clearUsage(RPG.WT_TWOHANDS)) {
				return false;
			}
		}

		if (wt == RPG.WT_FULLBODY) {
			if (!clearUsage(RPG.WT_TORSO) | !clearUsage(RPG.WT_LEGS)) {
				return false;
			}
		}
		if (wt == RPG.WT_TORSO) {
			if (!clearUsage(RPG.WT_FULLBODY)) {
				return false;
			}
		}
		if (wt == RPG.WT_LEGS) {
			if (!clearUsage(RPG.WT_FULLBODY)) {
				return false;
			}
		}

		// TODO check logic
		final int num = t.getNumber();
		if (num > 1 && wt != RPG.WT_MISSILE) {
			// remove excess items
			final Thing tt = t.separate(num - 1);
			addThing(tt); // don't stack
		}

		setUsage(t, wt);

		if (t.getFlag("IsArtifact")) {
			Item.identify(t);
		}

		return true;
	}

	// change usage, also change relevant modifiers
	public void setUsage(final Thing target, final int wt) {
		if (target.place != this) {
			throw new Error(target.getName(Game.hero()) + " not in inventory ");
		}
		if (target.y == wt) {
			return;
		}

		target.unApplyModifiers("WieldedModifiers", this);
		target.unApplyModifiers("EffectModifiers", this);
		target.y = wt;
		if (wt > 0 && wt != RPG.WT_EFFECT) {
			target.applyModifiers("WieldedModifiers", this);
		}
		if (wt == RPG.WT_EFFECT) {
			target.applyModifiers("EffectModifiers", this);
		}
	}

	private static final Modifier[] emptyModifiers = new Modifier[] {};

	/**
	 * Gets modifiers caused by this Thing with a given reason.
	 * 
	 * @param reason
	 * @return
	 */
	private Modifier[] getModifiers(final String reason) {
		final Modifier[] mods = (Modifier[]) get(reason);
		return mods == null ? emptyModifiers : mods;
	}

	/**
	 * Apply modifiers from this thing to a given target
	 * 
	 * @param reason
	 *            Modifier reason e.g. "WieldedModifiers"
	 * @param target
	 *            Target Thing
	 */
	public void applyModifiers(final String reason, final Thing target) {
		final Modifier[] mods = getModifiers(reason);
		for (final Modifier mod : mods) {
			target.applyModifier(Modifier.create(mod, this, reason));
		}
	}

	public void applyModifiers(final Modifier[] mods, final String reason,
			final Thing target) {
		for (final Modifier mod : mods) {
			target.applyModifier(Modifier.create(mod, this, reason));
		}
	}

	/**
	 * Unapply modifiers applied from this thing from a given target
	 * 
	 * @param reason
	 *            Modifier reason e.g. "WieldedModifiers"
	 * @param target
	 *            Target Thing
	 */
	public void unApplyModifiers(final String reason, final Thing target) {
		final Modifier[] mods = getModifiers(reason);
		for (final Modifier mod : mods) {
			final String stat = mod.getStat();
			target.removeModifiersWithSource(this, reason, stat);
		}
	}

	// remove modifiers for a given stat, source and reason
	private void removeModifiersWithSource(final Thing source,
			final String reason, final String stat) {
		final ArrayList al = getStatModifiers(stat);
		if (al != null) {
			for (int i = al.size() - 1; i >= 0; i--) {
				final Modifier m = (Modifier) al.get(i);
				if (m.getSource() == source && m.getReason().equals(reason)) {
					removeModifier(al, i);
				}
				if (al.size() == 0) {
					setStatModifiers(stat, null);
				}
			}
		}
	}

	private void removeModifier(final ArrayList al, final int i) {
		final Modifier m = (Modifier) al.remove(i);
		final String st = m.getString("RemoveMessage");
		if (st != null) {
			message(st);
		}
	}

	public void removeAllModifiers(final String reason) {
		final HashMap hm = getModifierList();
		if (hm == null) {
			return;
		}

		final Iterator it = hm.values().iterator();
		while (it.hasNext()) {
			final ArrayList al = (ArrayList) it.next();
			if (al != null) {
				for (int i = al.size() - 1; i >= 0; i--) {
					final Modifier m = (Modifier) al.get(i);
					if (m.getReason().equals(reason)) {
						removeModifier(al, i);
					}

				}
			}
			if (al.size() == 0) {
				it.remove();
			}
		}
	}

	public boolean isHostile(final Thing t) {
		return false;
	}

	public Thing cloneType() {
		return Lib.create(getString("Name"));
	}

	// public int notify(final int eventtype, final int ext, final Object o) {
	// return AI.notify(this, eventtype, ext, o);
	// }

	public void give(final Thing giver, final Thing gift) {
		if (handles("OnGift")) {
			final Event e = new Event("Gift");
			e.set("Gift", gift);
			e.set("Giver", giver);
			handle(e);
		} else {
			giver.message(getTheName() + " does not seem interested");
		}
	}

	public boolean isDead() {
		return getStat("HPS") <= 0;
	}

	private HashMap getModifierList() {
		return modifiers;
	}

	private void setModifierList(final HashMap hm) {
		modifiers = hm;
	}

	public boolean isModified() {
		return modifiers != null;
	}

	private ArrayList getStatModifiers(final String s) {
		final HashMap hm = getModifierList();
		if (hm == null) {
			return null;
		}
		return (ArrayList) hm.get(s);
	}

	private void setStatModifiers(final String s, final ArrayList al) {
		HashMap hm = getModifierList();

		if (hm == null) {
			if (al == null) {
				return;
			}
			hm = new HashMap();
			setModifierList(hm);
		}

		if (al != null) {
			hm.put(s, al);
		} else {
			hm.remove(s);
			if (hm.size() == 0) {
				setModifierList(null);
			}
		}
	}

	@Override
	public void add(final String reason, final Modifier mod) {
		super.add(reason, mod);

		if (reason.equals("CarriedModifiers") && place instanceof Thing) {
			getParent().applyModifier(Modifier.create(mod, this, reason));
		}

		if (reason.equals("SelfModifiers")) {
			applyModifier(Modifier.create(mod, this, reason));
		}
	}

	/*
	 * All additions of modifiers to this thing go through here
	 */
	private void applyModifier(final Modifier m) {
		final String s = m.getStat();
		ArrayList al = getStatModifiers(s);
		if (al == null) {
			al = new ArrayList();
			setStatModifiers(s, al);
		}
		al.add(m);
		final String st = m.getString("ApplyMessage");
		if (st != null) {
			message(st);
		}
		Collections.sort(al, Modifier.sorter);
	}

	public Object getModified(final String s, final int pos) {
		final ArrayList al = getStatModifiers(s);
		if (al != null && pos < al.size()) {

			// get the value from the next modifier
			final Modifier m = (Modifier) al.get(pos);
			return m.calculate(this, s, pos + 1);
		}
		// no modifiers left, so use properties
		return super.get(s);
	}

	@Override
	public int getStat(final String key) {
		final Integer i = (Integer) get(key);
		if (i == null) {
			return 0;
		}
		return i.intValue();
	}

	@Override
	public Object get(final String key) {
		if (modifiers != null) {
			return getModified(key, 0);
		}
		return super.get(key);
	}

	@Override
	public String report() {
		String s = super.report();
		if (modifiers != null) {
			s = s + "\n";
			s = s + "Modified Values:\n";
			final Iterator it = modifiers.keySet().iterator();
			while (it.hasNext()) {
				final String p = (String) it.next();
				s = s + p + " = " + get(p) + "\n";
			}
		}
		return s;
	}

	public List orthogonalExits(final int directionX, final int directionY) {
		final BattleMap map = getMap();
		if (map == null) {
			return Collections.EMPTY_LIST;
		}
		final List exits = new ArrayList();
		if (directionX == 1 || directionX == -1) {
			if (!map.isBlocked(x, y - 1) || isDoorVisible(x, y - 1)) {
				exits.add(new Point(x, y - 1));
			}
			if (!map.isBlocked(x, y + 1) || isDoorVisible(x, y + 1)) {
				exits.add(new Point(x, y + 1));
			}
		} else if (directionY == 1 || directionY == -1) {
			if (!map.isBlocked(x - 1, y) || isDoorVisible(x - 1, y)) {
				exits.add(new Point(x - 1, y));
			}
			if (!map.isBlocked(x + 1, y) || isDoorVisible(x + 1, y)) {
				exits.add(new Point(x + 1, y));
			}
		}
		return exits;
	}

	public boolean isDoorVisible(final int x, final int y) {
		final Thing[] things = getMap().getThings(x, y);
		for (final Thing thing : things) {
			if (thing.getFlag("IsSecretDoor")
					|| thing.getFlag("IsSecretPassage")) {
				continue;
			}
			if (thing.getFlag("IsDoor")) {
				return true;
			}
		}
		return false;
	}

	public List moreExits(final int directionX, final int directionY) {
		final BattleMap map = getMap();
		if (map == null) {
			return Collections.EMPTY_LIST;
		}
		final List exits = orthogonalExits(directionX, directionY);
		if (directionX == 1 || directionX == -1) {
			if (!map.isBlocked(x + directionX, y - 1)
					|| isDoorVisible(x + directionX, y - 1)) {
				exits.add(new Point(x + directionX, y - 1));
			}
			if (!map.isBlocked(x + directionX, y)
					|| isDoorVisible(x + directionX, y)) {
				exits.add(new Point(x + directionX, y));
			}
			if (!map.isBlocked(x + directionX, y + 1)
					|| isDoorVisible(x + directionX, y + 1)) {
				exits.add(new Point(x + directionX, y + 1));
			}
		} else if (directionY == 1 || directionY == -1) {
			if (!map.isBlocked(x + directionX, y - 1)
					|| isDoorVisible(x + directionX, y - 1)) {
				exits.add(new Point(x + directionX, y - 1));
			}
			if (!map.isBlocked(x + directionX, y)
					|| isDoorVisible(x + directionX, y)) {
				exits.add(new Point(x + directionX, y));
			}
			if (!map.isBlocked(x + directionX, y + 1)
					|| isDoorVisible(x + directionX, y + 1)) {
				exits.add(new Point(x + directionX, y + 1));
			}
		}
		return exits;
	}

	public boolean areSeveralDirectionsNotVisited() {
		final BattleMap map = getMap();
		final int[] xDelta = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
		final int[] yDelta = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };
		final List notVisited = new ArrayList();
		for (int i = 0; i < xDelta.length; i++) {
			if (map.isTileBlocked(x + xDelta[i], y + yDelta[i])) {
				continue;
			}
			final int pathValue = map.getPath(x + xDelta[i], y + yDelta[i]);
			if (pathValue == 0) {
				notVisited.add(new Point(xDelta[i], yDelta[i]));
			}
		}
		if (notVisited.size() < 2) {
			return false;
		}
		int signX = -2;
		int signY = -2;
		boolean xsMatch, ysMatch;
		for (final Iterator iter = notVisited.iterator(); iter.hasNext();) {
			final Point point = (Point) iter.next();
			if (signX == -2) {
				signX = point.x;
			}
			if (signY == -2) {
				signY = point.y;
			}
			xsMatch = point.x != 0 && point.x == signX;
			ysMatch = point.y != 0 && point.y == signY;
			if (!xsMatch && !ysMatch) {
				return true;
			}
		}
		return false;
	}

	/*
	 * These are the regions mentioned below:
	 */
	// AFFB
	// E..G
	// E..G
	// DHHC
	private List allExits(final boolean countAlreadyVisited) {
		int[] xDelta = {};
		int[] yDelta = {};
		final BattleMap map = getMap();
		if (map == null) {
			return Collections.EMPTY_LIST;
		}
		if (x == 0) {
			if (y == 0) {
				// region A
				xDelta = new int[] { 1, 1, 0 };
				yDelta = new int[] { 0, 1, 1 };
			} else if (y == map.getHeight() - 1) {
				// region D
				xDelta = new int[] { 0, 1, 1 };
				yDelta = new int[] { -1, -1, 0 };
			} else {
				// region E
				xDelta = new int[] { 0, 1, 1, 1, 0 };
				yDelta = new int[] { -1, -1, 0, 1, 1 };
			}
		} else if (y == 0) {
			if (x == map.getWidth() - 1) {
				// region B
				xDelta = new int[] { -1, -1, 0 };
				yDelta = new int[] { 0, 1, 1 };
			} else {
				// region F
				xDelta = new int[] { -1, -1, 0, 1, 1 };
				yDelta = new int[] { 0, 1, 1, 1, 0 };
			}
		} else if (x == map.getWidth() - 1) {
			if (y == map.getHeight() - 1) {
				// region C
				xDelta = new int[] { -1, -1, 0 };
				yDelta = new int[] { 0, -1, -1 };
			} else {
				// region G
				xDelta = new int[] { 0, -1, -1, -1, 0 };
				yDelta = new int[] { -1, -1, 0, 1, 1 };
			}
		} else if (y == map.getHeight() - 1) {
			// region H
			xDelta = new int[] { -1, -1, 0, 1, 1 };
			yDelta = new int[] { 0, -1, -1, -1, 0 };
		} else {
			// middle
			xDelta = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
			yDelta = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };
		}
		final List freedoms = new ArrayList();
		for (int i = 0; i < xDelta.length; i++) {
			if (!map.isBlocked(x + xDelta[i], y + yDelta[i])) {
				final int newX = x + xDelta[i];
				final int newY = y + yDelta[i];
				if (!countAlreadyVisited && map.getPath(newX, newY) == 1) {
					continue;
				}
				if (Movement.canMove(this, map, newX, newY)) {
					freedoms.add(new Point(x + xDelta[i], y + yDelta[i]));
				}
			}
		}
		return freedoms;
	}

	public boolean inARoom() {
		final BattleMap map = getMap();
		if (map == null) {
			return false;
		}
		return allExits(true).size() >= 3;
	}

	public void isRunning(final boolean isRunning) {
		set("IsRunMode", isRunning);
		if (isRunning) {
			incStat("RunCount", 1);
		} else {
			set("RunCount", 0);
			set("RunDirectionX", Integer.MIN_VALUE);
			set("RunDirectionY", Integer.MIN_VALUE);
		}
	}

	// MAINHAND == r
	public String inHandMessage() {
		final Thing bothHands = getWielded(RPG.WT_TWOHANDS);
		if (bothHands != null) {
			return bothHands.getName() + " in both hands";
		}
		final StringBuffer inHandMessage = new StringBuffer();
		final Thing inHand = getWielded(RPG.WT_MAINHAND);
		final Thing otherHand = getWielded(RPG.WT_SECONDHAND);
		inHandMessage.append(inHand == null ? ""
				: " " + inHand.getName() + " in right hand");
		if (otherHand != null) {
			if (inHandMessage.length() > 0) {
				inHandMessage.append(", ");
			}
			inHandMessage.append(otherHand.getName());
			inHandMessage.append(" in left hand");
		}
		if (inHand == null && otherHand == null) {
			return "nothing in hand";
		}
		return inHandMessage.toString().trim();
	}

	public void removeFromParent() {
		getMap().removeThing(this);
	}

	public boolean canReach(final Thing target, final int maxDistance) {
		return new AStarPathFinder(new TileBasedMap() {
			@Override
			public void pathFinderVisited(final int x, final int y) {

			}

			@Override
			public int getWidthInTiles() {
				return getMap().width;
			}

			@Override
			public int getHeightInTiles() {
				return getMap().height;
			}

			@Override
			public float getCost(final Mover mover, final int sx, final int sy,
					final int tx, final int ty) {
				return 1;
			}

			@Override
			public boolean blocked(final Mover mover, final int x,
					final int y) {
				return getMap().isTileBlocked(x, y);
			}
		}, maxDistance, true).findPath(null, x, y, target.x, target.y) != null;
	}
}