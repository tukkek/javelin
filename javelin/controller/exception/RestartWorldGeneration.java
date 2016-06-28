package javelin.controller.exception;

import javelin.model.world.World;

/**
 * Instead of making sure {@link World} is being properly generated, it's easier
 * to just give up when too many tries are reached, and just try a best-effort
 * algorhitm. It's OK to try to generate the world a few times every time a game
 * is started if the process doesn't take more than a few seconds.
 * 
 * @author alex
 */
public class RestartWorldGeneration extends RuntimeException {

}
