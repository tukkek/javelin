package tyrant.mikera.tyrant.perf;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;



public class SizeOfLib implements IWork {
    private int size;

    public void run() {
        Lib.instance();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(Lib.instance().getAll());
            size = out.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUp() {
        RPG.setRandSeed(0);
        Lib.clear();
    }

    public String getMessage() {
        return "" + size + " bytes";
    }
}