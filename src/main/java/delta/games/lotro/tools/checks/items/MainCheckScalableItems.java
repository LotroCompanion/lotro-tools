package delta.games.lotro.tools.checks.items;

import delta.common.utils.io.Console;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.lore.items.legendary2.Legendary2;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.utils.maths.Progression;

/**
 * Tool to checks scalable items.
 * @author DAM
 */
public class MainCheckScalableItems
{
  static void findRegularItemWithScalingNoProgression()
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      if (item instanceof Legendary) continue;
      if (item instanceof Legendary2) continue;
      Munging munging=item.getMunging();
      if (munging==null) continue;
      Progression progression=munging.getProgression();
      if (progression==null)
      {
        int id=item.getIdentifier();
        String name=item.getName();
        Console.println("ID="+id+", name="+name+", munging="+munging);
      }
    }
  }

  static void findScalableWithNoStats()
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      if (item instanceof Legendary) continue;
      if (item instanceof Legendary2) continue;
      Munging munging=item.getMunging();
      if (munging==null) continue;
      Progression progression=munging.getProgression();
      if (progression==null) continue;
      StatsProvider statsProvider=item.getStatsProvider();
      if (statsProvider!=null) continue;
      int id=item.getIdentifier();
      String name=item.getName();
      Console.println("ID="+id+", name="+name+", munging="+munging);
      // => no such weapon
      // => some armour chests
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    findScalableWithNoStats();
  }
}
