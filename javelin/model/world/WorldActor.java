package javelin.model.world;

import java.io.Serializable;

import javelin.view.screen.world.WorldScreen;

/**
 * An independent overworld feature.
 * 
 * If you're creating a new actor type don't forget to update
 * {@link WorldScreen#getallmapactors()}!
 * 
 * @author alex
 */
public interface WorldActor extends Serializable {
	int getx();

	int gety();

	void remove();

	void place();

	void move(int tox, int toy);

	/**
	 * Called when an incursion reaches this actor's location.
	 * 
	 * @see Incursion#ignoreincursion(Incursion)
	 * @see Incursion#fight(int, int)
	 * 
	 * @param incursion
	 *            Attacking incursion.
	 * @return <code>true</code> if this place gets destroyed,
	 *         <code>false</code> if the Incursion is destroyed or
	 *         <code>null</code> if neither.
	 */
	Boolean destroy(Incursion attacker);

	/**
	 * Some actors should not be attacked by {@link Incursion}s for any reason.
	 * 
	 * Note that even though they will ignore a target an incursion path can
	 * still take them to it in some cases. You can use
	 * {@link Incursion#ignoreincursion(Incursion)} to prevent such a case from
	 * destroying the target.
	 * 
	 * @param attacker
	 *            Attacking squad.
	 * @return <code>true</code> if the attacking squad has no interest in this
	 *         target.
	 */
	boolean ignore(Incursion attacker);
}
