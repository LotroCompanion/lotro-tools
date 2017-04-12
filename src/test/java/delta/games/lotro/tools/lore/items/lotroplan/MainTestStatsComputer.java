package delta.games.lotro.tools.lore.items.lotroplan;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.lore.items.stats.ItemStatSliceData;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;

/**
 * Simple test for the stats computer.
 * @author DAM
 */
public class MainTestStatsComputer
{
  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainTestStatsComputer().doIt();
  }

  private void doIt()
  {
    doStat(192,"=CALCSLICE(\"Might\";B237;3)");
    doStat(195,"=CALCSLICE(\"Agility\";B225;2)");
    doStat(192,"=CALCSLICE(\"Will\";B233;3)");
    doStat(176,"=CALCSLICE(\"Vitality\";B310;2,8)");
    doStat(201,"=CALCSLICE(\"Will\";B167;1,4)");
    doStat(195,"=CALCSLICE(\"Morale\";B226;2)");
  }

  private void doStat(int itemLevel, String formula)
  {
    ItemStatSliceData slice=SliceFormulaParser.parse(formula);
    SlicesBasedItemStatsProvider provider=new SlicesBasedItemStatsProvider();
    provider.addSlice(slice);
    BasicStatsSet stats=provider.getStats(itemLevel);
    System.out.println(stats);
  }
}
