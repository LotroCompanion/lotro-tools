package delta.games.lotro.tools.checks.cosmetics;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.items.Item;

/**
 * Group of cosmetically related items.
 * @author DAM
 */
public class CosmeticGroup
{
  private int _id;
  private List<Item> _items;

  /**
   * Constructor.
   * @param id Group identifier.
   */
  public CosmeticGroup(int id)
  {
    _id=id;
    _items=new ArrayList<Item>();
  }

  /**
   * Add an item.
   * @param item Item to add.
   */
  public void addItem(Item item)
  {
    _items.add(item);
  }

  /**
   * Get the group identifier.
   * @return the group identifier.
   */
  public int getId()
  {
    return _id;
  }

  /**
   * Get the managed items.
   * @return the managed items.
   */
  public List<Item> getItems()
  {
    return _items;
  }
}
