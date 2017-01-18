package delta.games.lotro.tools.lore.relics;

import java.io.File;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicType;
import delta.games.lotro.lore.items.legendary.relics.RelicsCategory;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.utils.FixedDecimalsInteger;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Parser for Lotro-Wiki relics page.
 * @author DAM
 */
public class RelicsIndexPageParser
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

  private RelicsManager _relicsMgr;

  private void handleTable(String categoryName, Segment source, String id, Integer level, RelicType defaultType)
  {
    Element table=findRelicsTable(source,id);
    if (table!=null)
    {
      RelicsCategory category=_relicsMgr.getRelicCategory(categoryName,true);
      List<Element> trs=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
      for(Element tr : trs)
      {
        StartTag tag=tr.getStartTag();
        String value=tag.getAttributeValue("class");
        if (("odd".equals(value)) || ("even".equals(value)))
        {
          Relic relic=handTableRow(tr,level,defaultType);
          if (relic!=null)
          {
            category.addRelic(relic);
          }
        }
      }
    }
    else
    {
      System.err.println("Cannot find table: "+id);
    }
  }

  private Relic handTableRow(Element tr, Integer level, RelicType defaultType)
  {
    Relic relic=null;
    List<Element> tds=JerichoHtmlUtils.findElementsByTagName(tr,HTMLElementName.TD);
    if (tds.size()==3)
    {
      // Icon
      Element iconElement=tds.get(0);
      String iconPath=extractIcon(iconElement);
      // Name
      Element nameElement=tds.get(1);
      String name=JerichoHtmlUtils.getTagContents(nameElement,HTMLElementName.A);
      // Type
      RelicType type=getTypeFromName(name);

      relic=new Relic(name,type,level);
      relic.setIconFilename(iconPath);
      // Stats
      Element statsElement=tds.get(2);
      String statsStr=JerichoHtmlUtils.getTextFromTag(statsElement);
      BasicStatsSet stats=parseStats(statsStr);
      relic.getStats().setStats(stats);
    }
    else if (tds.size()==4) // Retired relics
    {
      // Icon
      Element iconElement=tds.get(0);
      String iconPath=extractIcon(iconElement);
      // Name
      Element nameElement=tds.get(1);
      String name=JerichoHtmlUtils.getTagContents(nameElement,HTMLElementName.A);
      if (name==null)
      {
        name=JerichoHtmlUtils.getTagContents(nameElement,HTMLElementName.TD);
      }
      if (name.startsWith("Relic:"))
      {
        name=name.substring(6).trim();
      }
      // Level
      Element tierElement=tds.get(2);
      String tierStr=JerichoHtmlUtils.getTextFromTag(tierElement);
      Integer tier=null;
      if (tierStr.length()>0)
      {
        tier=NumericTools.parseInteger(tierStr.trim());
      }
      else
      {
        tier=null;
      }

      relic=new Relic(name,defaultType,level);
      relic.setIconFilename(iconPath);
      // Stats
      Element statsElements=tds.get(3);
      String statsStr=JerichoHtmlUtils.getTextFromTag(statsElements);
      statsStr=statsStr.replace(", ","\n");
      BasicStatsSet stats=parseStats(statsStr);
      relic.getStats().setStats(stats);
    }
    else if (tds.size()==5) // Crafted relics
    {
      // Icon
      Element iconElement=tds.get(0);
      String iconPath=extractIcon(iconElement);
      // Name
      Element nameElement=tds.get(1);
      String name=JerichoHtmlUtils.getTagContents(nameElement,HTMLElementName.A);
      // Type
      RelicType type=RelicType.CRAFTED_RELIC;
      // Level
      Element levelElement=tds.get(2);
      String levelStr=JerichoHtmlUtils.getTextFromTag(levelElement);
      if (levelStr.length()>0)
      {
        level=NumericTools.parseInteger(levelStr.trim());
      }
      else
      {
        level=null;
      }

      relic=new Relic(name,type,level);
      relic.setIconFilename(iconPath);
      // Stats
      Element statsElements=tds.get(3);
      String statsStr=JerichoHtmlUtils.getTextFromTag(statsElements);
      BasicStatsSet stats=parseStats(statsStr);
      relic.getStats().setStats(stats);
    }
    //System.out.println(relic);
    return relic;
  }

  private String extractIcon(Element td)
  {
    Element src=JerichoHtmlUtils.findElementByTagName(td,HTMLElementName.IMG);
    String iconPath=src.getAttributeValue("src");
    int index=iconPath.indexOf("/");
    if (index!=-1)
    {
      iconPath=iconPath.substring(index+1).trim();
    }
    return iconPath;
  }

  private BasicStatsSet parseStats(String statsStr)
  {
    //System.out.println(statsStr);
    BasicStatsSet statsSet=new BasicStatsSet();
    String[] lines=statsStr.split("\n");
    for(String line : lines)
    {
      line=line.trim();
      int firstSpaceIndex=line.indexOf(" ");
      if (firstSpaceIndex!=-1)
      {
        String valueStr=line.substring(0,firstSpaceIndex);
        String statName=line.substring(firstSpaceIndex+1).trim();
        //System.out.println("Value: "+valueStr);
        //System.out.println("Stat: "+statName);
        Float value=parseValue(valueStr);
        STAT[] stats=parseStat(statName);
        if ((value!=null) && (stats!=null))
        {
          for(STAT stat : stats)
          {
            statsSet.addStat(stat,new FixedDecimalsInteger(value.floatValue()));
          }
        }
      }
      else
      {
        System.err.println("Cannot parse: "+line);
      }
    }
    return statsSet;
  }

  private Float parseValue(String valueStr)
  {
    boolean negative=false;
    if (valueStr.startsWith("-"))
    {
      negative=true;
      valueStr=valueStr.substring(1).trim();
    }
    if (valueStr.endsWith("%"))
    {
      valueStr=valueStr.substring(0,valueStr.length()-1).trim();
    }
    Float value=NumericTools.parseFloat(valueStr);
    if (value!=null)
    {
      if (negative)
      {
        value=Float.valueOf(-value.floatValue());
      }
    }
    return value;
  }

  private STAT[] parseStat(String statName)
  {
    STAT[] ret=null;
    int index=statName.indexOf("(Requires level");
    if (index!=-1)
    {
      statName=statName.substring(0,index).trim();
    }
    if ((statName.contains("Block")) && (statName.contains("Parry")) &&
        (statName.contains("Evade")))
    {
      ret=new STAT[]{STAT.BLOCK,STAT.PARRY,STAT.EVADE};
    }
    else if (statName.contains("Critical Rating"))
    {
      ret=new STAT[]{STAT.CRITICAL_RATING};
    }
    else if (statName.contains("Tactical Offence Rating"))
    {
      ret=new STAT[]{STAT.TACTICAL_MASTERY};
    }
    else if (statName.contains("Melee Critical Defence"))
    {
      ret=new STAT[]{STAT.MELEE_CRITICAL_DEFENCE};
    }
    else if (statName.contains("Ranged Critical Defence"))
    {
      ret=new STAT[]{STAT.RANGED_CRITICAL_DEFENCE};
    }
    else
    {
      STAT stat=STAT.getByName(statName);
      if (stat==null)
      {
        System.err.println("Cannot parse stat: "+statName);
      }
      else
      {
        ret=new STAT[]{stat};
      }
    }
    return ret;
  }

  private RelicType getTypeFromName(String name)
  {
    if (name.indexOf("Setting")!=-1) return RelicType.SETTING;
    if (name.indexOf("Gem")!=-1) return RelicType.GEM;
    if (name.indexOf("Rune")!=-1) return RelicType.RUNE;
    if (name.indexOf("Device")!=-1) return RelicType.CRAFTED_RELIC;
    System.out.println("Unmanaged name: " + name);
    return null;
  }

  private Element findRelicsTable(Segment segment, String id)
  {
    Element span=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(segment,HTMLElementName.SPAN,"id",id);
    Element h2=span.getParentElement();
    Element table=getNextSibling(h2);
    return table;
  }

  private Element getNextSibling(Element element)
  {
    Element ret=null;
    Element parent=element.getParentElement();
    List<Element> allChildren=parent.getChildElements();
    int index=allChildren.indexOf(element);
    if (index!=-1)
    {
      if (index<allChildren.size())
      {
        ret=allChildren.get(index+1);
      }
    }
    return ret;
  }

  /**
   * Parse a page file.
   * @return A list of relics or <code>null</code> if an error occurred.
   */
  public RelicsManager parseRelicsIndex()
  {
    _relicsMgr=new RelicsManager();
    try
    {
      // Relics
      URL url=URLTools.getFromClassPath("relics.txt",RelicsIndexPageParser.class.getPackage());
      Source source=new Source(url);
      source.fullSequentialParse();

      handleTable("Tier 1",source,"Tier_1_Relics",null,null);
      handleTable("Tier 2",source,"Tier_2_Relics",null,null);
      handleTable("Tier 3",source,"Tier_3_Relics",null,null);
      handleTable("Tier 4",source,"Tier_4_Relics",null,null);
      handleTable("Tier 5",source,"Tier_5_Relics",null,null);
      handleTable("Tier 6",source,"Tier_6_Relics",null,null);
      handleTable("Tier 7",source,"Tier_7_Relics",null,null);
      handleTable("Tier 8",source,"Tier_8_Relics",null,null);
      handleTable("Tier 9",source,"Tier_9_Relics",null,null);
      handleTable("Tier 10",source,"Tier_10_Relics",null,null);
      // Unique relics (55)
      handleTable("Unique relics (55)",source,"Unique_Relics_.28Level_55.29",Integer.valueOf(55),null);
      // Singular relics (60)
      handleTable("Singular relics (60)",source,"Singular_Relics_.28Level_60.29",Integer.valueOf(60),null);
      // Extraordinary Relics (Level 65)
      handleTable("Extraordinary Relics (Level 65)",source,"Extraordinary_Relics_.28Level_65.29",Integer.valueOf(65),null);
      // Westfold Relics (Level 70)
      handleTable("Westfold Relics (Level 70)",source,"Westfold_Relics_.28Level_70.29",Integer.valueOf(70),null);
      // Great River Relics (Level 75)
      handleTable("Great River Relics (Level 75)",source,"Great_River_Relics_.28Level_75.29",Integer.valueOf(75),null);
      // Eastemnet Relics (Level 80)
      handleTable("Eastemnet Relics (Level 80)",source,"Eastemnet_Relics_.28Level_80.29",Integer.valueOf(80),null);
      // Wildermore Relics (Level 85)
      handleTable("Wildermore Relics (Level 85)",source,"Wildermore_Relics_.28Level_85.29",Integer.valueOf(85),null);
      // Westemnet Relics (Level 90)
      handleTable("Westemnet Relics (Level 90)",source,"Westemnet_Relics_.28Level_90.29",Integer.valueOf(90),null);

      // Ignore bridles
      /*
      // Basic Mounted (Level 75)
      handleTable(relics,source,"Basic_Mounted_.28Level_75.29",Integer.valueOf(75));
      // Unique Mounted (Level 75)
      handleTable(relics,source,"Unique_Mounted_.28Level_75.29",Integer.valueOf(75));
      // Eastemnet Mounted (Level 80)
      handleTable(relics,source,"Eastemnet_Mounted_.28Level_80.29",Integer.valueOf(80));
      // Riddermark Mounted (Level 85)
      handleTable(relics,source,"Riddermark_Mounted_.28Level_85.29",Integer.valueOf(85));
      // Westemnet Mounted (Level 90)
      handleTable(relics,source,"Westemnet_Mounted_.28Level_90.29",Integer.valueOf(90));
      */

      // Crafted_Relics_Index
      handleTable("Crafted",source,"Crafted_Relics_Index",null,null);

      // Retired relics
      URL urlRetired=URLTools.getFromClassPath("retired_relics.txt",RelicsIndexPageParser.class.getPackage());
      Source sourceRetired=new Source(urlRetired);
      sourceRetired.fullSequentialParse();

      handleTable("Retired Settings",sourceRetired,"Retired_Settings_Index",null,RelicType.SETTING);
      handleTable("Retired Gems",sourceRetired,"Retired_Gems_Index",null,RelicType.GEM);
      handleTable("Retired Runes",sourceRetired,"Retired_Runes_Index",null,RelicType.RUNE);
    }
    catch(Exception e)
    {
      _relicsMgr=null;
      _logger.error("Cannot parse relics page!",e);
    }
    return _relicsMgr;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    RelicsIndexPageParser parser=new RelicsIndexPageParser();
    RelicsManager relicsMgr=parser.parseRelicsIndex();
    File toFile=new File("relics.xml").getAbsoluteFile();
    relicsMgr.writeRelicsFile(toFile);
    System.out.println("Wrote file: "+toFile);
  }
}
