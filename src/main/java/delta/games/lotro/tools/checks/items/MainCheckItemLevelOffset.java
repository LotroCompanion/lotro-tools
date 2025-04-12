package delta.games.lotro.tools.checks.items;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.lore.items.legendary2.Legendary2;
import delta.games.lotro.lore.items.scaling.Munging;

/**
 * Tool to check usage of item level offsets.
 * @author DAM
 */
public class MainCheckItemLevelOffset
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      Integer offset=item.getItemLevelOffset();
      if (offset==null)
      {
        continue;
      }
      int id=item.getIdentifier();
      String name=item.getName();
      if (item instanceof Legendary)
      {
        System.out.println("ID="+id+", name="+name+", LEGENDARY, offset="+offset); // NOSONAR
        //=> there are some!
      }
      if (item instanceof Legendary2)
      {
        System.out.println("ID="+id+", name="+name+", LEGENDARY2, offset="+offset); // NOSONAR
        // => None!
      }
      Munging munging=item.getMunging();
      if (munging!=null)
      {
        System.out.println("ID="+id+", name="+name+", munging="+munging+", offset="+offset); // NOSONAR
        // => there are some!
      }
    }
  }
}
