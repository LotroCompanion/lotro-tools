package delta.games.lotro.tools.reports;

/**
 * Builds all the available reports.
 * @author DAM
 */
public class ReportsBuilder
{
  private void doIt()
  {
    // Items
    ItemsReport itemsReport=new ItemsReport();
    itemsReport.doIt();
    // Deeds
    DeedsReport deedsReport=new DeedsReport();
    deedsReport.doIt();
    // Sets
    SetsReport setsReport=new SetsReport();
    setsReport.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ReportsBuilder().doIt();
  }
}
