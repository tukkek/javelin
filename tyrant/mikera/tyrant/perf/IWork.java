package tyrant.mikera.tyrant.perf;


public interface IWork extends Runnable {
    void setUp();
    String getMessage();
}
