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

	String describe();

	void move(int tox, int toy);
}
