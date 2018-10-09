package delta.games.lotro.tools.lore.recipes.lotrowiki;

import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.finder.ItemSelector;

/**
 * Item selector for legendary items using their crafting tier and their category (to guess their item level).
 * @author DAM
 */
public class LegendaryItemSelector implements ItemSelector
{
  private int _expectedLevel;

  /**
   * Constructor.
   * @param tier Quality to select.
   * @param category Category of recipe.
   */
  public LegendaryItemSelector(int tier, String category)
  {
    if (tier==6)
    {
      if (category.contains("Level 65")) _expectedLevel=65;
      else if (category.contains("Level 60")) _expectedLevel=60;
      else
      {
        System.out.println("Unmanaged category for tier 6: "+category);
      }
    }
    else if (tier==7) _expectedLevel=75;
    else if (tier==8) _expectedLevel=85;
    else if (tier==9)
    {
      if (category.contains("Level 100")) _expectedLevel=100;
      else if (category.contains("Level 95")) _expectedLevel=95;
      else
      {
        System.out.println("Unmanaged category for tier 9: "+category);
      }
    }
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
      Integer itemLevel=item.getItemLevel();
      if ((itemLevel!=null) && (itemLevel.intValue()==_expectedLevel))
      {
        ret=item;
        break;
      }
    }
    return ret;
  }
}
