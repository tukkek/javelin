package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Thing;


public interface IThingFilter {
    boolean accept(Thing thing, String query);
}
