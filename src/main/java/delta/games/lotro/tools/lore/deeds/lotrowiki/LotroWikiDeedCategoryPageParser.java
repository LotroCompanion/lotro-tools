package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.games.lotro.tools.utils.JerichoHtmlUtils;

/**
 * Parse for lotro-wiki deed category pages.
 * @author DAM
 */
public class LotroWikiDeedCategoryPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedCategoryPageParser.class);

  private static final String INDEX="/index.php/";

  /**
   * Parse a lotro-wiki deed category page.
   * @param from Page date.
   */
  public void parseDeedCategoryPage(File from)
  {
    try
    {
      FileInputStream inputStream=new FileInputStream(from);
      Source source=new Source(inputStream);

      List<Element> tables=JerichoHtmlUtils.findElementsByTagName(source,HTMLElementName.TABLE);
      for(Element table : tables)
      {
        boolean ok=checkTable(table);
        if (!ok)
        {
          continue;
        }
        List<Element> rows=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
        rows.remove(0);
        for(Element row : rows)
        {
          handleRow(row);
        }
      }
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed category page ["+from+"]",e);
    }
  }

  private boolean checkTable(Element table)
  {
    List<Element> rows=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
    if (rows.size()>=1)
    {
      Element header=rows.get(0);
      List<Element> cells=JerichoHtmlUtils.findElementsByTagName(header,HTMLElementName.TH);
      if (cells.size()>=1)
      {
        String text=JerichoHtmlUtils.getTextFromTag(cells.get(0));
        return "Deed".equals(text);
      }
    }
    return false;
  }

  private void handleRow(Element row)
  {
    List<Element> cells=JerichoHtmlUtils.findElementsByTagName(row,HTMLElementName.TD);
    if (cells.size()>=1)
    {
      Element deedCell=cells.get(0);
      Element anchor=JerichoHtmlUtils.findElementByTagName(deedCell,HTMLElementName.A);
      String title=anchor.getAttributeValue("title");
      String href=anchor.getAttributeValue("href");
      System.out.println(href + "  ==>  "+title);
      if (href.startsWith(INDEX))
      {
        String deedId=href.substring(INDEX.length());
        LotroWikiDeedPageParser parser=new LotroWikiDeedPageParser();
        parser.parseDeed(deedId);
      }
    }
  }
}
