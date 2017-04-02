package delta.games.lotro.tools.lore.items.lotroplan;

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
    Double might=StatsComputer.getValue(192,"=CALCSLICE(\"Might\";B237;3)");
    System.out.println(might);
    Double agility=StatsComputer.getValue(195,"=CALCSLICE(\"Agility\";B225;2)");
    System.out.println(agility);
    Double will=StatsComputer.getValue(192,"=CALCSLICE(\"Will\";B233;3)");
    System.out.println(will);
    Double vitality=StatsComputer.getValue(176,"=CALCSLICE(\"Vitality\";B310;2,8)");
    System.out.println(vitality);
    Double will2=StatsComputer.getValue(201,"=CALCSLICE(\"Will\";B167;1,4)");
    System.out.println(will2);
    Double morale=StatsComputer.getValue(195,"=CALCSLICE(\"Morale\";B226;2)");
    System.out.println(morale);
  }
}
