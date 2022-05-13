package javelin.model.item;

/**
 * A command-activated item requires nothing but saying words or intent to use
 * while still taking an action to do so. Doesn't {@link Item#provokesaoo}.
 *
 * @author alex
 */
public class CommandItem extends Item{
  /** Constructor. */
  public CommandItem(String name,double price,boolean register){
    super(name,price,register);
    provokesaoo=false;
    usedinbattle=true;
  }
}
