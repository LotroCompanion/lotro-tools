package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.utils.Proxy;

/**
 * Proxy builder.
 * @author DAM
 */
public class ProxyBuilder
{
  private static final Logger LOGGER=Logger.getLogger(ProxyBuilder.class);

  /**
   * Build an item proxy.
   * @param itemId Item identifier.
   * @return the newly built proxy or <code>null</code> if item was not found.
   */
  public static Proxy<Item> buildItemProxy(int itemId)
  {
    Proxy<Item> itemProxy=null;
    // Item ID
    Item item=ItemsManager.getInstance().getItem(itemId);
    if (item!=null)
    {
      String itemName=item.getName();
      itemProxy=new Proxy<Item>();
      itemProxy.setId(itemId);
      itemProxy.setName(itemName);
    }
    else
    {
      LOGGER.warn("Could not find item with ID="+itemId);
    }
    return itemProxy;
  }
}
