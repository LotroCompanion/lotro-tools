package delta.games.lotro.tools.lore.deeds.lotrowiki;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.utils.DownloadService;

/**
 * Parse for lotro-wiki deed pages.
 * @author DAM
 */
public class LotroWikiDeedPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedPageParser.class);

  /**
   * Parse the lotro wiki deed page for the given deed ID.
   * @param deedId Id of the targeted deed.
   * @return A deed or <code>null</code> if an error occurred.
   */
  public DeedDescription parseDeed(String deedId)
  {
    DeedDescription deed=null;
    try
    {
      String url="http://lotro-wiki.com/index.php?title="+deedId+"&action=edit";
      DownloadService downloader=DownloadService.getInstance();
      String page=downloader.getPage(url);
      Source source=new Source(page);

      Element deedSource=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.TEXTAREA,"id","wpTextbox1");
      if (deedSource!=null)
      {
        Segment content=deedSource.getContent();
        String text=content.toString();
        System.out.println(text);
      }
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed page ["+deedId+"]",e);
    }
    return deed;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    LotroWikiDeedPageParser parser=new LotroWikiDeedPageParser();
    String[] ids={
        "A_Full_Belly_and_a_Nap_in_the_Dirt",
        "Warg-slayer_(Advanced)_(Southern_Mirkwood)",
        "Bastions_of_Hope"
    };
    for(String id : ids)
    {
      parser.parseDeed(id);
    }
  }
}
