package javelin.model.world;

import java.io.Serializable;

public interface WorldActor extends Serializable {
	int getx();

	int gety();

	void remove();

	void place();

	String describe();
}
