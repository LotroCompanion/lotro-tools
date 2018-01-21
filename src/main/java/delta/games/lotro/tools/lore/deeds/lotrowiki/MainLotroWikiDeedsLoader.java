package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;

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
    String url=LotroWikiConstants.BASE_URL+"index.php/Category:Western_Gondor_Deeds";
    File westernGondorDeeds=lotroWiki.download(url,"western_gondor_deeds");
    LotroWikiDeedCategoryPageParser parser=new LotroWikiDeedCategoryPageParser();
    parser.parseDeedCategoryPage(westernGondorDeeds);
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
