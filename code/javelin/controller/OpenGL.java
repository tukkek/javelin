package javelin.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.opengl.awt.GLData;

import javelin.view.screen.BattleScreen;

public class OpenGL{
  public static final boolean DEBUG=false;
  public static final Object LOCK=new Object();

  static final int FREQUENCY=DEBUG?10:20;
  static final int TICK=1_000/FREQUENCY;

  static class Render extends TimerTask{
    @Override
    public void run(){
      var screen=BattleScreen.active;
      if(screen==null) return;
      var panel=screen.mappanel;
      if(panel==null) return;
      var canvas=panel.canvas;
      if(DEBUG)
        IO.println(List.of("tick",canvas.isDisplayable(),canvas.isShowing()));
      if(canvas.isDisplayable()&&canvas.isShowing()) synchronized(LOCK){
        canvas.render();
      }
    }
  }

  static Render thread=new Render();
  static Timer timer=null;

  static public GLData spawn(){
    var data=new GLData();
    data.majorVersion=3;
    data.minorVersion=3;
    data.profile=GLData.Profile.CORE;
    data.versionPolicy=GLData.VersionPolicy.AT_LEAST;
    data.samples=4;
    return data;
  }

  public static void render(){
    if(timer!=null) return;
    timer=new Timer();
    timer.scheduleAtFixedRate(new Render(),0,TICK);
  }

  public static void configure(){}
}
