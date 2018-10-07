package delta.games.lotro.tools.lore.recipes.lotrowiki;

import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.finder.ItemSelector;

/**
 * Item selector that chooses items that have a given quality.
 * @author DAM
 */
public class QualityBasedItemSelector implements ItemSelector
{
  private ItemQuality _quality;

  /**
   * Constructor.
   * @param quality Quality to select.
   */
  public QualityBasedItemSelector(ItemQuality quality)
  {
    _quality=quality;
  }

  /**
   * Choose an item.
   * @param items Items to choose from.
   * @return the selected item, or <code>null</code>.
   */
  public Item chooseItem(List<Item> items)
  {
    Item ret=null;
    for(Item item : items)
    {
      if (item.getQuality()==_quality)
      {
        ret=item;
        break;
      }
    }
    return ret;
  }
}
