package javelin.view.screen.town;

import javelin.model.item.Item;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;

/**
 * Option to be used with {@link ShoppingScreen}. Carries an {@link Item}.
 *
 * @author alex
 */
public class PurchaseOption extends Option{
	/** Item to be acquired. */
	public final Item i;

	/** Constructor. */
	public PurchaseOption(final Item i){
		super(i.name,i.price);
		this.i=i;
	}

	@Override
	public String toString(){
		return i.toString();
	}

	@Override
	public boolean equals(Object obj){
		return i.equals(obj);
	}

	@Override
	public int hashCode(){
		return i.hashCode();
	}
}