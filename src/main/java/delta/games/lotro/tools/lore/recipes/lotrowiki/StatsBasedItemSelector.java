package delta.games.lotro.tools.lore.recipes.lotrowiki;

import java.util.List;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.finder.ItemSelector;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Item selector that chooses items that have a given stat.
 * @author DAM
 */
public class StatsBasedItemSelector implements ItemSelector
{
  private STAT _stat;

  /**
   * Constructor.
   * @param stat
   */
  public StatsBasedItemSelector(STAT stat)
  {
    _stat=stat;
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
      FixedDecimalsInteger stat=item.getStats().getStat(_stat);
      if (stat!=null)
      {
        ret=item;
        break;
      }
    }
    return ret;
  }
}
