package tyrant.mikera.tyrant.perf;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;


public class CreateLib implements IWork {
    public void run() {
        Lib.instance();
    }

    public void setUp() {
        RPG.setRandSeed(0);
        Lib.clear();
    }

    public String getMessage() {
        return "";
    }
}