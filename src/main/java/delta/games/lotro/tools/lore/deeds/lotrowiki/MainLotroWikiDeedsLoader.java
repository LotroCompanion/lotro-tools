package delta.games.lotro.tools.lore.deeds.lotrowiki;

/**
 * Download deeds data from the site lotro-wiki.
 * @author DAM
 */
public class MainLotroWikiDeedsLoader
{
  //private File _outDir;

  /**
   * Constructor.
   */
  public MainLotroWikiDeedsLoader()
  {
    //_outDir=new File("data/lore/deeds").getAbsoluteFile();
  }

  private void doIt()
  {
    LotroWikiSiteInterface lotroWiki=new LotroWikiSiteInterface();
    // Load a sample "deed category" URL
    LotroWikiDeedCategoryPageParser parser=new LotroWikiDeedCategoryPageParser(lotroWiki);
    parser.doCategory("Western_Gondor_Deeds");
    parser.doCategory("Central_Gondor_Deeds");
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainLotroWikiDeedsLoader().doIt();
  }
}
