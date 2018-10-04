package delta.games.lotro.tools.lore.recipes.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.text.TextTools;
import delta.games.lotro.common.Duration;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.tools.utils.lotrowiki.LotroWikiConstants;
import delta.games.lotro.tools.utils.lotrowiki.LotroWikiSiteInterface;
import delta.games.lotro.utils.Escapes;

/**
 * Parser for lotro-wiki recipes index pages.
 * @author DAM
 */
public class LotroWikiRecipeIndexPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiRecipeIndexPageParser.class);

  private static final String INDEX="/index.php/";
  private static final String INDEX_MISSING_PAGE_START="/index.php?title=";
  private static final String INDEX_MISSING_PAGE_END="&action=edit";

  private LotroWikiSiteInterface _lotroWiki;

  public int _recipesCount=0;

  /**
   * Constructor.
   * @param lotroWiki Lotro-wiki interface.
   */
  public LotroWikiRecipeIndexPageParser(LotroWikiSiteInterface lotroWiki)
  {
    _lotroWiki=lotroWiki;
  }

  /**
   * Handle a recipe index category.
   * @param indexId Index identifier.
   * @return a list of loaded recipes.
   */
  public List<Recipe> doRecipesIndex(String indexId)
  {
    String url=LotroWikiConstants.BASE_URL+"/index.php/"+indexId;
    String filename=Escapes.escapeFile(indexId)+"/main.html";
    File recipesIndexFile=_lotroWiki.download(url,filename);
    System.out.println(recipesIndexFile);
    List<Recipe> recipes=parseRecipesIndexPage(recipesIndexFile);
    return recipes;
  }

  /**
   * Parse a lotro-wiki recipes index page.
   * @param from Source page.
   * @return loaded recipes.
   */
  private List<Recipe> parseRecipesIndexPage(File from)
  {
    List<Recipe> recipes=new ArrayList<Recipe>();
    try
    {
      FileInputStream inputStream=new FileInputStream(from);
      Source source=new Source(inputStream);
      parseTables(source);
      //parseIndex(source,deedIds);
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse recipes index page ["+from+"]",e);
    }
    return recipes;
  }

  private void parseTables(Source source)
  {
    Element root=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.DIV,"class","mw-parser-output");
    if (root!=null)
    {
      List<Element> children=root.getChildElements();
      for(Element child : children)
      {
        String tagName=child.getStartTag().getName();
        if (HTMLElementName.H2.equals(tagName))
        {
          String category=JerichoHtmlUtils.getTextFromTag(child);
          System.out.println("=== Category: "+category+" ===");
        }
        else if (HTMLElementName.TABLE.equals(tagName))
        {
          if (checkTable(child))
          {
            parseTable(child);
          }
        }
      }
    }
  }

  private void parseTable(Element table)
  {
    List<Element> rows=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
    // Remove header
    Element headerRow=rows.remove(0);
    Integer xpColumnIndex=findColumnByName(headerRow,"XP");
    Integer resultIndex=findColumnByName(headerRow,"Craft Item");
    Integer componentsIndex=findColumnByName(headerRow,"Components");
    Integer recipeIndex=findColumnByName(headerRow,"Recipe");
    for(Element row : rows)
    {
      System.out.println("-- Recipe --");
      /*Recipe recipe=*/handleRow(row,xpColumnIndex,resultIndex,componentsIndex,recipeIndex);
      _recipesCount++;
    }
  }

  private boolean checkTable(Element table)
  {
    String tableClass=table.getStartTag().getAttributeValue("class");
    return "altRowsMed topTable".equals(tableClass);
  }

  private Integer findColumnByName(Element headerRow, String headerPart)
  {
    Integer ret=null;
    List<Element> headerCells=JerichoHtmlUtils.findElementsByTagName(headerRow,HTMLElementName.TH);
    int index=0;
    for(Element headerCell : headerCells)
    {
      String headerText=JerichoHtmlUtils.getTextFromTag(headerCell);
      if (headerText.contains(headerPart))
      {
        ret=Integer.valueOf(index);
      }
      index++;
    }
    return ret;
  }

  private Recipe handleRow(Element row, Integer xpIndex, Integer resultIndex, Integer componentsIndex, Integer recipeIndex)
  {
    if (resultIndex==null)
    {
      return null;
    }
    Recipe recipe=null;
    List<Element> cells=JerichoHtmlUtils.findElementsByTagName(row,HTMLElementName.TD);
    if (cells.size()>=4)
    {
      System.out.println("Results");
      Element resultsCell=cells.get(resultIndex.intValue());
      parseItems(resultsCell,false);
      Element typeCell=cells.get(recipeIndex.intValue());
      parseRecipeCell(typeCell);
      System.out.println("Ingredients");
      Element ingredientsCell=cells.get(componentsIndex.intValue());
      parseItems(ingredientsCell,false);
      if (xpIndex!=null)
      {
        Element xpCell=cells.get(xpIndex.intValue());
        String xpStr=JerichoHtmlUtils.getTextFromTag(xpCell).trim();
        if ((xpStr.length()>0) && (!"?".equals(xpStr)))
        {
          Integer xp=NumericTools.parseInteger(xpStr);
          System.out.println("xp="+xp);
        }
        else
        {
          System.out.println("No XP");
        }
      }
    }
    return recipe;
  }

  private void parseRecipeCell(Element recipeCell)
  {
    String contents=JerichoHtmlUtils.getTextFromTag(recipeCell).trim();
    //System.out.println("{"+contents+"}");
    String[] lines=contents.split("\n");
    for(String line : lines)
    {
      line=line.trim();
      if (line.length()==0) continue;
      if (line.endsWith("Recipe")) line=line.substring(0,line.length()-6).trim();
      if (line.length()==0) continue;
      // Types
      if (line.contains("Basic")) System.out.println("Basic");
      else if (line.equals("Vendor")) System.out.println("Vendor");
      else if (line.equals("Reputation")) System.out.println("Reputation");
      else if (line.equals("Reputation Vendor")) System.out.println("Reputation");
      else if (line.equals("Barter")) System.out.println("Barter");
      else if (line.equals("Festival")) System.out.println("Festival Vendor");
      else if ((line.startsWith("former Spring Festival drop")) || (line.equals("obsolete Spring Festival recipe")) || (line.equals("obsolete Spring Festival item")))
      {
        System.out.println("Festival Drop");
        System.out.println("Obsolete");
      }
      else if (line.equals("Guild")) System.out.println("Guild");
      else if (line.equals("Drop")) System.out.println("Drop");
      else if (line.equals("Quest")) System.out.println("Quest");
      else if (line.equals("Obsolete")) System.out.println("Obsolete");
      // Single use?
      else if ((line.contains("Single Use")) || (line.contains("Single use")))
      {
        System.out.println("One time use");
      }
      // Cooldown
      else if ((line.contains("d ")) || (line.endsWith("h")) || (line.endsWith("d")))
      {
        Integer cooldown=Duration.parseDurationString(line);
        System.out.println("Cooldown: "+Duration.getDurationString(cooldown.intValue()));
      }
      else if (line.contains("day"))
      {
        int spaceIndex=line.indexOf(" ");
        Integer nbDays=NumericTools.parseInteger(line.substring(0,spaceIndex));
        if (nbDays!=null)
        {
          int cooldown=nbDays.intValue()*Duration.DAY;
          System.out.println("Cooldown: "+Duration.getDurationString(cooldown));
        }
      }
      else if (line.contains("week"))
      {
        int spaceIndex=line.indexOf(" ");
        Integer nbWeeks=NumericTools.parseInteger(line.substring(0,spaceIndex));
        if (nbWeeks!=null)
        {
          int cooldown=nbWeeks.intValue()*Duration.DAY*7;
          System.out.println("Cooldown: "+Duration.getDurationString(cooldown));
        }
      }
      else
      {
        System.out.println("Unmanaged line ["+line+"]");
      }
    }
  }

  private Integer _count;
  private ItemProxy _itemId;

  private void parseItems(Element cell, boolean critical)
  {
    // Extract initial count, if there is one
    {
      Integer count=parseItemCountFromItemText(cell);
      if (count!=null)
      {
        _count=count;
      }
    }
    List<Element> children=cell.getChildElements();
    for(Element child : children)
    {
      //<span style="font-size: 1em;">
      String tagName=child.getStartTag().getName();
      if (HTMLElementName.SPAN.equals(tagName))
      {
        // Count:
        // <span style="position: absolute">
        Element countTag=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(child,HTMLElementName.SPAN,"style","position: absolute");
        if (countTag!=null)
        {
          _count=parseItemCount(countTag);
        }
        parseItems(child,false);
      }
      else if (HTMLElementName.I.equals(tagName))
      {
        _count=null;
        _itemId=null;
        parseItems(child,true);
      }
      else if (HTMLElementName.A.equals(tagName))
      {
        // Check if it is an icon link
        Element img=JerichoHtmlUtils.findElementByTagName(child,HTMLElementName.IMG);
        if (img==null)
        {
          _itemId=parseItemIdFromLink(child);
          Integer count=parseItemCountFromItemText(child);
          if (count!=null)
          {
            _count=count;
          }
          showItem(_itemId,_count,critical);
          _count=null;
          _itemId=null;
        }
      }
    }
  }

  private void showItem(ItemProxy itemId, Integer count, boolean critical)
  {
    if (itemId==null)
    {
      return;
    }
    System.out.print('\t');
    if (critical)
    {
      System.out.print("Critical: ");
    }
    if ((count!=null) && (count.intValue()>1))
    {
      System.out.print(count+" ");
    }
    String itemName=itemId.getName();
    String itemKey=itemId.getItemKey();
    System.out.println(itemName+" ("+itemKey+")");
  }

  private ItemProxy parseItemIdFromLink(Element aTag)
  {
    ItemProxy ret=null;
    // Item
    String itemKey=null;
    String itemName=null;
    // Regular page link
    // <a href="/index.php/Item:Rowan_Campfire_Kit" title="Item:Rowan Campfire Kit">
    // Missing page link:
    // <a href="/index.php?title=Item:Ploughman%27s_Loaf&amp;action=edit&amp;redlink=1" class="new" title="Item:Ploughman&#39;s Loaf (page does not exist)">Item:Ploughman's Loaf</a>
    String link=aTag.getAttributeValue("href");
    if (link!=null)
    {
      String missingLink=TextTools.findBetween(link,INDEX_MISSING_PAGE_START,INDEX_MISSING_PAGE_END);
      if (missingLink!=null)
      {
        itemKey=missingLink;
        itemName=JerichoHtmlUtils.getTextFromTag(aTag);
        if (itemName.startsWith("Item:")) itemName=itemName.substring(5);
        ret=new ItemProxy();
        ret.setItemKey(itemKey);
        ret.setName(itemName);
      }
      else
      {
        if (link.startsWith(INDEX))
        {
          itemKey=link.substring(INDEX.length());
          itemName=aTag.getAttributeValue("title");
          if (itemName.startsWith("Item:")) itemName=itemName.substring(5);
        }
      }
    }
    if ((itemKey!=null) && (!itemKey.startsWith("Item:")))
    {
      if ((itemKey.endsWith("_Recipe_Index")) || ("Fish".equals(itemKey)))
      {
        // Ignore
      }
      else
      {
        System.out.println("Warn: bad item key: "+itemKey);
      }
    }
    else
    {
      ret=new ItemProxy();
      ret.setItemKey(itemKey);
      ret.setName(itemName);
    }
    return ret;
  }

  private Integer parseItemCount(Element countTag)
  {
    // Count
    Integer count=null;
    if (countTag!=null)
    {
      String countStr=JerichoHtmlUtils.getTextFromTag(countTag).trim();
      if (countStr.length()>0)
      {
        count=NumericTools.parseInteger(countStr);
      }
    }
    return count;
  }

  private Integer parseItemCountFromItemText(Element child)
  {
    Integer count=null;
    String text=JerichoHtmlUtils.getTextFromTag(child).trim();
    int firstSpaceIndex=text.indexOf(' ');
    if (firstSpaceIndex!=-1)
    {
      String countStr=text.substring(0,firstSpaceIndex);
      count=NumericTools.parseInteger(countStr,false);
    }
    return count;
  }
}
