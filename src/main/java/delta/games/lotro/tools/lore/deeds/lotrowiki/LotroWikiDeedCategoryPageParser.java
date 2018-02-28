package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.lore.deeds.DeedsContainer;
import delta.games.lotro.tools.lore.items.ItemsResolver;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.utils.Escapes;

/**
 * Parse for lotro-wiki deed category pages.
 * @author DAM
 */
public class LotroWikiDeedCategoryPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedCategoryPageParser.class);

  private static final String INDEX="/index.php/";

  private LotroWikiSiteInterface _lotroWiki;

  private ItemsResolver _resolver;
  private HashSet<String> _deedIds;

  /**
   * Constructor.
   * @param lotroWiki Lotro-wiki interface.
   */
  public LotroWikiDeedCategoryPageParser(LotroWikiSiteInterface lotroWiki)
  {
    _lotroWiki=lotroWiki;
    _deedIds=new HashSet<String>();
    _resolver=new ItemsResolver();
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param category Category to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, String category)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    if (category!=null)
    {
      if (category.endsWith(":")) category=category.substring(0,category.length()-1);
    }
    for(DeedDescription deed : deeds)
    {
      if (type!=null) deed.setType(type);
      if (category!=null) deed.setCategory(category);
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param characterClass Character class to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, CharacterClass characterClass)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    for(DeedDescription deed : deeds)
    {
      if (type!=null)
      {
        deed.setType(type);
      }
      if (characterClass!=null)
      {
        deed.setRequiredClass(characterClass);
        deed.setCategory(null);
      }
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param race Race to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, Race race)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    for(DeedDescription deed : deeds)
    {
      if (race!=null) deed.setCategory("Race:"+race.getLabel());
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId)
  {
    String url=LotroWikiConstants.BASE_URL+"/index.php/Category:"+Escapes.escapeUrl(categoryId);
    String file=categoryId+"/main.html";
    File deedsCategoryFile=_lotroWiki.download(url,Escapes.escapeFile(file));
    List<String> deedIds=parseDeedCategoryPage(deedsCategoryFile);
    List<DeedDescription> deeds=loadDeeds(categoryId,deedIds);
    writeFile(categoryId,deeds);
    return deeds;
  }

  private void writeFile(String categoryId,List<DeedDescription> deeds)
  {
    File to=new File("deeds-"+Escapes.escapeFile(categoryId)+".xml").getAbsoluteFile();
    DeedsContainer.writeSortedDeeds(deeds,to);
    //DeedXMLWriter.writeDeedsFile(to,deeds);
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
      parseTables(source,deedIds);
      parseIndex(source,deedIds);
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed category page ["+from+"]",e);
    }
    return deedIds;
  }

  private void parseTables(Source source, List<String> deedIds)
  {
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

  private void parseIndex(Source source, List<String> deedIds)
  {
    Element indexSection=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.DIV,"id","mw-pages");
    if (indexSection!=null)
    {
      List<Element> anchors=JerichoHtmlUtils.findElementsByTagName(indexSection,HTMLElementName.A);
      for(Element anchor : anchors)
      {
        //String title=anchor.getAttributeValue("title");
        String href=anchor.getAttributeValue("href");
        //System.out.println(href + "  ==>  "+title);
        if (href.startsWith(INDEX))
        {
          String deedId=href.substring(INDEX.length());
          deedIds.add(deedId);
        }
      }
    }
  }

  private List<DeedDescription> loadDeeds(String categoryId, List<String> deedIds)
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    Set<String> deedKeys=new HashSet<String>();
    LotroWikiDeedPageParser parser=new LotroWikiDeedPageParser();
    int index=0;
    for(String deedId : deedIds)
    {
      String url=LotroWikiConstants.BASE_URL+"/index.php?title="+deedId+"&action=edit";
      String name=Escapes.escapeFile(categoryId)+"/deed"+index+".html";
      File deedFile=_lotroWiki.download(url,name);
      DeedDescription deed=parser.parseDeed(deedFile);
      if (deed!=null)
      {
        boolean alreadyKnown=_deedIds.contains(deedId);
        if (!alreadyKnown)
        {
          deed.setKey(deedId);
          if (!deedKeys.contains(deedId))
          {
            deeds.add(deed);
            deedKeys.add(deedId);
          }
          _deedIds.add(deedId);
          //System.out.println(deed);
          resolveItemRewards(deed);
        }
      }
      index++;
    }
    return deeds;
  }

  private void resolveItemRewards(DeedDescription deed)
  {
    Rewards rewards=deed.getRewards();
    ObjectsSet objects=rewards.getObjects();
    int nbItems=objects.getNbObjectItems();
    for(int i=0;i<nbItems;i++)
    {
      ObjectItem objectItem=objects.getItem(i);
      resolveItem(objectItem);
    }
  }

  private void resolveItem(ObjectItem objectItem)
  {
    String name=objectItem.getName();
    int itemId=resolveByName(name);
    if (itemId==0)
    {
      Item item=_resolver.getItem(name);
      if (item!=null)
      {
        itemId=item.getIdentifier();
      }
    }
    if (itemId!=0)
    {
      objectItem.setItemId(itemId);
      objectItem.setObjectURL(null);
      objectItem.setIconURL(null);
    }
    else
    {
      System.out.println("Item not found [" + name + "]");
    }
  }

  private int resolveByName(String name)
  {
    int itemId=0;
    if ("Armour (Wastes)".equals(name)) itemId=1879341924;
    else if ("Bag of Flower Petals".equals(name)) itemId=1879199971;
    else if ("Black Steel Key".equals(name)) itemId=1879356039;
    else if ("Broken Blade (Wastes)".equals(name)) itemId=1879342063;
    else if ("Flower Petals (Multi-use)".equals(name)) itemId=1879200102;
    else if ("Gold-bound_Lootbox".equals(name)) itemId=1879225083;
    else if ("Golden Token of the Riddermark".equals(name)) itemId=1879237278;
    else if ("Grant Golf Chip Emote".equals(name)) itemId=1879187356;
    else if ("Ivar's Helm".equals(name)) itemId=1879197561; //(Cosmetic)
    else if ("Letter (Rohan Awaits)".equals(name)) itemId=1879249134; //("Letter")
    else if ("Major Essence of Critical Rating".equals(name)) itemId=1879313417; // (assuming Tier7)
    else if ("Major Essence of Physical Mitigation".equals(name)) itemId=1879313525; // (assuming Tier7)
    else if ("Map of Eriador".equals(name)) itemId=1879205541;
    else if ("Metal Scrap (Wastes)".equals(name)) itemId=1879342064;
    else if ("Prized Ost Dunhoth War-steed".equals(name)) itemId=1879206179;
    else if ("Provisions (Wastes)".equals(name)) itemId=1879341934;
    else if ("Rotten Fruit (Multi-use)".equals(name)) itemId=1879200100;
    else if ("Rotten Fruit".equals(name)) itemId=1879199969;
    else if ("Salt (Wastes)".equals(name)) itemId=1879342065;
    else if ("Steed of Elessar's Host".equals(name)) itemId=1879345100;
    else if ("Sturdy Steel Key".equals(name)) itemId=1879227487; //1879223825 (fond bleu) or 1879227487 (fond jaune)(different icons)
    else if ("Universal Healing Potion".equals(name)) itemId=1879248609; //(Rejuvenation Potion)
    else if ("Upgrade Task Limit (+1)".equals(name)) itemId=1879201943; // or 1879201944, 1879201945, 1879201946
    else if ("Weapons (Wastes)".equals(name)) itemId=1879341942;
    return itemId;
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
        //String title=anchor.getAttributeValue("title");
        String href=anchor.getAttributeValue("href");
        //System.out.println(href + "  ==>  "+title);
        if (href.startsWith(INDEX))
        {
          deedId=href.substring(INDEX.length());
        }
      }
    }
    return deedId;
  }
}
