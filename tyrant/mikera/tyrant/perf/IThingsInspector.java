package tyrant.mikera.tyrant.perf;

import tyrant.mikera.engine.Thing;


public interface IThingsInspector {
    void inspect(Thing thing);
    void printResults();
    void setup(String[] args);
}
