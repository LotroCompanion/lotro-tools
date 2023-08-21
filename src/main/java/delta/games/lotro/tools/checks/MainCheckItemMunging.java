package delta.games.lotro.tools.checks;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.scaling.Munging;

/**
 * Tool to check item munging.
 * @author DAM
 */
public class MainCheckItemMunging
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      Munging munging=item.getMunging();
      if (munging==null)
      {
        continue;
      }
      Integer min=munging.getMin();
      Integer level=item.getItemLevel();
      if ((min!=null) && (level!=null) && (min.intValue()>level.intValue()))
      {
        int id=item.getIdentifier();
        String name=item.getName();
        System.out.println("ID="+id+", name="+name+", min="+min+", level="+level);
      }
    }
  }
}
