package javelin.controller;

import java.util.prefs.Preferences;

import javelin.Javelin;
import javelin.view.screen.WorldScreen;

public class Highscore{

  public static final Preferences RECORD=Preferences
      .userNodeForPackage(Javelin.class);

  public Highscore(){
    // TODO Auto-generated constructor stub
  }

  /**
   * Sets the highscore and...
   *
   * @return a message with previous and current score.
   */
  public static String record(){
    final var stored=Highscore.gethighscore();
    final var current=WorldScreen.currentday();
    var message="Previous record: "+stored;
    if(stored<current){
      message+="\nNew record: "+current+"!";
      Highscore.sethighscore(current);
    }else message+="\nCurrent game: "+current;
    return message;
  }

  /**
   * @param score Updates the highscore record with this value.
   */
  public static void sethighscore(final long score){
    Highscore.RECORD.putLong("record",score);
  }

  /**
   * @return The current highscore value.
   */
  public static long gethighscore(){
    return Highscore.RECORD.getLong("record",0);
  }

}
