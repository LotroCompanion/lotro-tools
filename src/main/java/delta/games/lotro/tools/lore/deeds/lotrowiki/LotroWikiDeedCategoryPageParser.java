package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;

/**
 * Parse for lotro-wiki deed category pages.
 * @author DAM
 */
public class LotroWikiDeedCategoryPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedCategoryPageParser.class);

  private static final String INDEX="/index.php/";

  private LotroWikiSiteInterface _lotroWiki;

  /**
   * Constructor.
   * @param lotroWiki Lotro-wiki interface.
   */
  public LotroWikiDeedCategoryPageParser(LotroWikiSiteInterface lotroWiki)
  {
    _lotroWiki=lotroWiki;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   */
  public void doCategory(String categoryId)
  {
    String url=LotroWikiConstants.BASE_URL+"/index.php/Category:"+categoryId;
    File deedsCategoryFile=_lotroWiki.download(url,categoryId+"/main.html");
    List<String> deedIds=parseDeedCategoryPage(deedsCategoryFile);
    List<DeedDescription> deeds=loadDeeds(categoryId,deedIds);
    File to=new File("deeds-"+categoryId+".xml").getAbsoluteFile();
    DeedXMLWriter.writeDeedsFile(to,deeds);
  }

  /**
   * Parse a lotro-wiki deed category page.
   * @param from Source page.
   * @return loaded deed IDs.
   */
  public List<String> parseDeedCategoryPage(File from)
  {
    List<String> deedIds=new ArrayList<String>();
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
          String deedId=handleRow(row);
          if (deedId!=null)
          {
            deedIds.add(deedId);
          }
        }
      }
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed category page ["+from+"]",e);
    }
    return deedIds;
  }

  private List<DeedDescription> loadDeeds(String categoryId, List<String> deedIds)
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    LotroWikiDeedPageParser parser=new LotroWikiDeedPageParser();
    int index=0;
    for(String deedId : deedIds)
    {
      String url=LotroWikiConstants.BASE_URL+"/index.php?title="+deedId+"&action=edit";
      String name=categoryId+"/deed"+index+".html";
      File deedFile=_lotroWiki.download(url,name);
      DeedDescription deed=parser.parseDeed(deedFile);
      deeds.add(deed);
      System.out.println(deed);
      index++;
    }
    return deeds;
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
        return text.contains("Deed");
      }
    }
    return false;
  }

  private String handleRow(Element row)
  {
    String deedId=null;
    List<Element> cells=JerichoHtmlUtils.findElementsByTagName(row,HTMLElementName.TD);
    if (cells.size()>=1)
    {
      Element deedCell=cells.get(0);
      Element anchor=JerichoHtmlUtils.findElementByTagName(deedCell,HTMLElementName.A);
      if (anchor!=null)
      {
        String title=anchor.getAttributeValue("title");
        String href=anchor.getAttributeValue("href");
        System.out.println(href + "  ==>  "+title);
        if (href.startsWith(INDEX))
        {
          deedId=href.substring(INDEX.length());
        }
      }
    }
    return deedId;
  }
}
