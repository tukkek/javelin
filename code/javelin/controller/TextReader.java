package javelin.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javelin.Javelin;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Screen;

/**
 * Outputs the content of a text file to the screen.
 *
 * @author alex
 */
public class TextReader{
  static public String read(File text){
    try{
      var out="";
      var reader=new BufferedReader(new FileReader(text));
      var line=reader.readLine();
      while(line!=null){
        out+=line+"\n";
        line=reader.readLine();
      }
      reader.close();
      return out;
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  public static Character show(File f,String footer){
    Screen.change(new InfoScreen(TextReader.read(f)+footer));
    return Javelin.input();
  }
}
