package javelin.controller.ai.cache;

/**
 * Node in a tree, used for storing a {@link #payload}.
 * 
 * @author alex
 */
class Link {
	/**
	 * Linked to {@link Cache#CACHESIZE} {@link Link}s.
	 */
	final Link[] cache = new Link[Cache.CACHESIZE];
	/**
	 * Cached value for this node.
	 */
	Object payload = null;
}