package tyrant.mikera.tyrant;

import java.awt.Color;

public interface IMessageHandler {
    public void clear();
    public void add(String s);
    public void add(String s, Color c);
    
    /**
     * If headless this may answer null.
     */
    public TPanel getPanel();
}
