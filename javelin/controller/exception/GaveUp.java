package javelin.controller.exception;

/**
 * Used when any sort of procedural generation has reached it's limit so it can
 * be given a chance to restart the process in the hopes of not being locked-up
 * again.
 * 
 * Most of the time a stuck procedural generation will eventually find a
 * solution but this prevents it from taking too long and consuming resources
 * unnecessarily.
 * 
 * @author alex
 */
public class GaveUp extends Exception {

}
