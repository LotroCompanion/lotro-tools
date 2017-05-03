package delta.games.lotro.tools.lore.items.scalables;

import java.util.List;

import delta.games.lotro.lore.items.Item;

/**
 * Check scalable items stats from collected samples.
 * @author DAM
 */
public class MainCheckScalableItems
{
  private void doIt()
  {
    ScalableItemsSamplesLoader loader=new ScalableItemsSamplesLoader();
    List<Item> items=loader.loadTable("samples.txt");
    for(Item item : items)
    {
      System.out.println(item.dump());
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCheckScalableItems().doIt();
  }
}
