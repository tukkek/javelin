package javelin.model.transport;

/** Flies overland. */
public class Airship extends Transport{
  /** Constructor. */
  public Airship(){
    super("Airship",100,100,16,50000);
    flies=true;
  }

  @Override
  public boolean battle(){
    return false;
  }
}
