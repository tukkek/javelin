package javelin.test;

import java.util.List;

import javelin.Debug;

/// A test that can {@link #run()} and {@link #fail(String)}.
///
/// Can {@link #show()} individually or run all {@link Tests}.
///
/// TASK For now can be run from {@link Debug#onworldhelp()}.
public abstract class Test implements Runnable{
  boolean verbose=false;

  void fail(String s){
    throw new RuntimeException(s);
  }

  /// Runs test and shows messages.
  public void show(){
    verbose=true;
    run();
  }

  void say(String text){
    if(verbose) IO.println(text);
  }

  void say(List<String> texts){
    say(String.join(" ",texts));
  }
}
