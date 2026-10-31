package javelin.test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import javelin.Debug;

/// Allows tesitng units from {@link Debug#onworldhelp()}.
///
/// TASK proper testing
public class Tests{
  // TASK add other-existing tests
  List<Test> tests=List.of(new DungeonEncounters());

  /// Run all tests.
  public void run(){
    try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
      try{
        var completion=new ExecutorCompletionService<>(executor);
        for(var t:tests) completion.submit(t,null);
        for(var t:tests) completion.take().get();
      }catch(InterruptedException|ExecutionException e){
        throw new RuntimeException(e);
      }
    }
  }
}
