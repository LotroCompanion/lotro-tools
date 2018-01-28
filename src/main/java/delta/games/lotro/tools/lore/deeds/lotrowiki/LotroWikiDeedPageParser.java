package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.games.lotro.common.ReputationItem;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Title;
import delta.games.lotro.common.Trait;
import delta.games.lotro.common.Virtue;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;

/**
 * Parse for lotro-wiki deed pages.
 * @author DAM
 */
public class LotroWikiDeedPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedPageParser.class);

  private File _currentFile;

  /**
   * Parse the lotro wiki deed page for the given deed ID.
   * @param from Source page.
   * @return A deed or <code>null</code> if an error occurred.
   */
  public DeedDescription parseDeed(File from)
  {
    _currentFile=from;
    DeedDescription deed=null;
    try
    {
      FileInputStream inputStream=new FileInputStream(from);
      Source source=new Source(inputStream);

      Element deedSource=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.TEXTAREA,"id","wpTextbox1");
      if (deedSource!=null)
      {
        Segment content=deedSource.getContent();
        String text=content.toString();
        deed=buildDeed(text);
      }
      Element backElement=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.DIV,"id","contentSub");
      if (backElement!=null)
      {
        Element a=JerichoHtmlUtils.findElementByTagName(backElement,HTMLElementName.A);
        if (a!=null)
        {
          String title=a.getAttributeValue("title");
          if (deed!=null)
          {
            deed.setName(title);
          }
        }
      }
      if (deed!=null)
      {
        // Fixes
        handleFixes(deed);
      }
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed page ["+from+"]",e);
    }
    return deed;
  }

  private void handleFixes(DeedDescription deed)
  {
    String name=deed.getName();
    if ("Deeds of Eriador".equals(name))
    {
      deed.setCategory("Region:Eriador");
    }
    if ("The Unwise (Deed)".equals(name))
    {
      deed.setCategory("Social:Food");
    }
  }

  private DeedDescription buildDeed(String rawData)
  {
    String[] lines=rawData.split("\n");

    // Checks
    if (lines==null) return null;
    if (lines.length<3) return null;
    if ((!lines[0].contains("{{Deed")) && (!lines[1].contains("{{Deed")) && (!lines[2].contains("{{Deed")))
    {
      return null;
    }

    DeedDescription deed=new DeedDescription();
    deed.setType(null);
    Rewards rewards=deed.getRewards();
    // Reputation
    Faction faction=null;
    Integer reputation=null;
    // Virtues
    VirtueId virtueId=null;
    Integer virtueCount=null;
    // Item rewards
    String[] itemRewards=null;
    Integer[] itemRewardCounts=null;
    // Categories
    String deedType=null;
    String deedSubtype=null;
    String regionalSub=null;

    for(int index=0;index<lines.length;index++)
    {
      String line=lines[index].trim();
      String lineKey=fetchLineKey(line);
      //System.out.println(line);
      if ("name".equals(lineKey))
      {
        String name=getLineValue(line);
        deed.setName(name);
      }
      else if ("Lore-text".equals(lineKey))
      {
        String description=getLineValue(line);
        deed.setDescription(description);
      }
      else if ("Objective".equals(lineKey))
      {
        StringBuilder sb=new StringBuilder();
        String firstLine=getLineValue(line);
        sb.append(firstLine);
        while(true)
        {
          String nextLine=lines[index+1].trim();
          if (nextLine.startsWith("|"))
          {
            break;
          }
          sb.append('\n').append(nextLine);
          index++;
        }
        deed.setObjectives(sb.toString().trim());
      }
      else if ("Faction".equals(lineKey))
      {
        faction=extractFaction(line);
      }
      else if ("Reputation".equals(lineKey))
      {
        String repValue=getLineValue(line).replace(",","");
        reputation=NumericTools.parseInteger(repValue);
      }
      else if ("Title".equals(lineKey))
      {
        Title title=extractTitle(line);
        if (title!=null)
        {
          rewards.addTitle(title);
        }
      }
      else if ("Virtue".equals(lineKey))
      {
        virtueId=extractVirtue(line);
      }
      else if ("Virtue-value".equals(lineKey))
      {
        virtueCount=NumericTools.parseInteger(getLineValue(line));
      }
      else if ("TP-reward".equals(lineKey))
      {
        String tpStr=getLineValue(line);
        if (!tpStr.isEmpty())
        {
          Integer tp=NumericTools.parseInteger(tpStr,false);
          if (tp!=null)
          {
            rewards.setLotroPoints(tp.intValue());
          }
          else
          {
            _logger.warn("Bad LOTRO points value in file "+_currentFile+": ["+tpStr+"]");
          }
        }
      }
      else if ("SM-reward".equals(lineKey))
      {
        String smStr=getLineValue(line);
        if (!smStr.isEmpty())
        {
          Integer marks=NumericTools.parseInteger(smStr,false);
          if (marks!=null)
          {
            ObjectsSet objects=deed.getRewards().getObjects();
            ObjectItem item=new ObjectItem("Mark");
            item.setItemId(1879224343);
            objects.addObject(item,marks.intValue());
          }
          else
          {
            _logger.warn("Bad SM value in file "+_currentFile+": ["+smStr+"]");
          }
        }
      }
      else if ("CTP-reward".equals(lineKey))
      {
        String value=getLineValue(line);
        if ("Y".equals(value))
        {
          // TODO Class Trait Point
        }
      }
      else if ("Trait-reward".equals(lineKey))
      {
        String traitStr=getLineValue(line);
        if (!traitStr.isEmpty())
        {
          handleTraitReward(rewards,traitStr);
        }
      }
      else if ("Level".equals(lineKey))
      {
        String levelStr=getLineValue(line);
        if ("?".equals(levelStr)) levelStr="";
        if ("???".equals(levelStr)) levelStr="";
        if (!levelStr.isEmpty())
        {
          if (levelStr.startsWith("&lt;=")) levelStr=levelStr.substring(5);
          Integer level=NumericTools.parseInteger(levelStr,false);
          if (level!=null)
          {
            deed.setMinLevel(level);
          }
          else
          {
            _logger.warn("Bad level value in file "+_currentFile+": ["+levelStr+"]");
          }
        }
      }
      else if ("Deed-type".equals(lineKey))
      {
        deedType=getLineValue(line);
      }
      else if ("Deed-subtype".equals(lineKey))
      {
        deedSubtype=getLineValue(line);
      }
      else if ("Regional-sub".equals(lineKey))
      {
        regionalSub=getLineValue(line);
      }
      else if ("Parent-deed".equals(lineKey))
      {
        //String parentDeed=getLineValue(line);
      }

      for(int i=1;i<=3;i++)
      {
        String suffix=(i!=1)?String.valueOf(i):"";
        if (("Item-reward"+suffix).equals(lineKey))
        {
          String itemName=getLineValue(line);
          if (!itemName.isEmpty())
          {
            if (itemRewards==null)
            {
              itemRewards=new String[3];
            }
            itemRewards[i-1]=itemName;
          }
        }
        if (("Item-amount"+suffix).equals(lineKey))
        {
          String itemCountStr=getLineValue(line);
          if (!itemCountStr.isEmpty())
          {
            if (itemRewardCounts==null)
            {
              itemRewardCounts=new Integer[3];
            }
            itemRewardCounts[i-1]=NumericTools.parseInteger(itemCountStr);
          }
        }
      }

/*
| DP-reward    = 
| Skill-reward = 
| Trait-reward = 
| Emote-reward = 
| Hidden       = 
| Deed-chain-1 = 
| Deed-chain-2 = 
| Deed-chain-3 = 
| Deed-chain-4 = 
| Deed-chain-5 = 
| Meta-deed     = 
| Extra         = 
       */
    }
    if ((faction!=null) && (reputation!=null))
    {
      ReputationItem item=new ReputationItem(faction);
      item.setAmount(reputation.intValue());
      rewards.getReputation().add(item);
    }
    if (virtueId!=null)
    {
      int count=(virtueCount!=null)?virtueCount.intValue():1;
      Virtue virtue=new Virtue(virtueId,count);
      rewards.addVirtue(virtue);
    }
    if (itemRewards!=null)
    {
      handleItemRewards(deed,itemRewards,itemRewardCounts);
    }
    // Categories
    handleCategories(deed,deedType,deedSubtype,regionalSub);
    return deed;
  }

  private String fetchLineKey(String line)
  {
    if (line.startsWith("|"))
    {
      line=line.substring(1).trim();
      int index=line.indexOf("=");
      if (index!=-1)
      {
        String key=line.substring(0,index).trim();
        return key;
      }
    }
    return null;
  }

  private void handleTraitReward(Rewards rewards, String traitStr)
  {
    System.out.println("Trait: "+traitStr);
    // Sometimes, a trait is in fact... a virtue!
    VirtueId virtueId=null;
    try
    {
      virtueId=VirtueId.valueOf(traitStr.toUpperCase());
    }
    catch(Exception e)
    {
      // Ignored
    }
    if (virtueId!=null)
    {
      Virtue virtue=new Virtue(virtueId,1);
      rewards.addVirtue(virtue);
    }
    else
    {
      if (traitStr.toLowerCase().endsWith(" (trait)")) traitStr=traitStr.substring(0,traitStr.length()-8);
      if (traitStr.toLowerCase().endsWith(" (beorning trait)")) traitStr=traitStr.substring(0,traitStr.length()-17);
      Trait trait=new Trait(null,traitStr);
      rewards.addTrait(trait);
    }
  }

  private void handleItemRewards(DeedDescription deed, String[] itemRewards, Integer[] itemRewardCounts)
  {
    ObjectsSet objects=deed.getRewards().getObjects();
    for(int i=0;i<itemRewards.length;i++)
    {
      String itemName=itemRewards[i];
      Integer count=null;
      if (itemRewardCounts!=null)
      {
        count=itemRewardCounts[i];
      }
      if (itemName!=null)
      {
        if (count==null)
        {
          count=Integer.valueOf(1);
        }
        ObjectItem item=new ObjectItem(itemName);
        objects.addObject(item,count.intValue());
      }
    }
  }

  private static final String FACTION_SUFFIX=" (Faction)";
  private static final String TITLE_SUFFIX=" (Title)";

  private Faction extractFaction(String line)
  {
    String factionName=getLineValue(line);
    if (factionName.endsWith(FACTION_SUFFIX))
    {
      factionName=factionName.substring(0,factionName.length()-FACTION_SUFFIX.length());
    }
    Faction faction=FactionsRegistry.getInstance().getByName(factionName);
    return faction;
  }

  private Title extractTitle(String line)
  {
    Title title=null;
    String titleName=getLineValue(line);
    if (!titleName.isEmpty())
    {
      if (titleName.endsWith(TITLE_SUFFIX))
      {
        titleName=titleName.substring(0,titleName.length()-TITLE_SUFFIX.length());
      }
      title=new Title(null,titleName);
    }
    return title;
  }

  private VirtueId extractVirtue(String line)
  {
    VirtueId virtue=null;
    String virtueName=getLineValue(line);
    if (!virtueName.isEmpty())
    {
      virtue=VirtueId.valueOf(virtueName.toUpperCase());
    }
    return virtue;
  }

  private void handleCategories(DeedDescription deed, String deedType, String deedSubType, String regionalSub)
  {
    DeedType type=null;
    String category=null;
    // Fixes
    if ("Halls of Crafting".equals(deedSubType)) deedType="Instances";
    if ("Instance".equals(deedType)) deedType="Instances";
    if ("the Wastes".equals(deedSubType)) deedSubType="The Wastes";
    if ("meta".equals(deedType)) deedType="Meta";
    if ("quest".equals(deedType)) deedType="Quest";
    if ("Racial".equals(deedType)) deedType="Race";
    if ("Tower of Orthanc".equals(regionalSub)) regionalSub="The Tower of Orthanc";
    if ("The Tower of Orthanc".equals(regionalSub)) deedType="Instances";
    if ("Skirmish".equals(deedSubType))
    {
      deedType="Skirmish";
      deedSubType="";
    }
    // Class deeds
    if ("Class".equals(deedType))
    {
      type=DeedType.CLASS;
      category=deedSubType;
    }
    // Type
    if ("Slayer".equals(regionalSub)) type=DeedType.SLAYER;
    else if ("Explorer".equals(regionalSub)) type=DeedType.EXPLORER;
    else if ("Lore".equals(regionalSub)) type=DeedType.LORE;
    else if ("Quest".equals(regionalSub)) type=DeedType.QUEST;
    else if ("Event".equals(regionalSub)) type=DeedType.EVENT;
    else if ("Reputation".equals(regionalSub)) type=DeedType.REPUTATION;
    else if ("Meta".equals(regionalSub)) type=null;

    if ("Exploration".equals(deedType)) type=DeedType.EXPLORER;
    else if ("Slayer".equals(deedType)) type=DeedType.SLAYER;
    else if ("Race".equals(deedType)) category="Racial";
    else if ("Lore".equals(deedType)) type=DeedType.LORE;
    else if ("Epic".equals(deedType)) category="Epic";
    else if ("Skirmish".equals(deedType)) category="Skirmish";
    else if ("Skirmish Instances".equals(deedType)) category="Skirmish";
    else if ("Summer Festival".equals(deedType)) category="Event:Summer Festival";
    else if ("Hidden".equals(deedType)) category="Hidden";

    if ("Epic".equals(deedSubType)) category="Epic";
    else if ("Event".equals(deedSubType)) type=DeedType.EVENT;
    else if ("LOTRO Anniversary".equals(deedSubType))
    {
      category="Event:LOTRO Anniversary";
      deedType="";
    }

    if ("Regional".equals(deedType))
    {
      if ((deedSubType!=null) && (deedSubType.length()>0))
      {
        category="Region:"+deedSubType;
      }
    }
    else if (("Meta".equals(deedType)) || ("Slayer".equals(deedType)) ||
             ("Social".equals(deedType)) || ("Quest".equals(deedType)) ||
             ("Quest".equals(deedType)))
    {
      // Special
      if ("Host of the West (Faction)".equals(deedSubType)) deedSubType="The Wastes";
      category=getPrefixForZone(deedSubType);
      if (category!=null)
      {
        category=category+":"+deedSubType;
      }
      if ("Western Gondor City Watch".equals(deedSubType))
      {
        type=DeedType.QUEST;
        category="Region:Dol Amroth:City Watch";
      }
    }
    else if ("Instances".equals(deedType))
    {
      category="Instances:"+deedSubType;
    }
    else if ("Hobby".equals(deedType))
    {
      type=DeedType.HOBBY;
      category=regionalSub;
    }
    else if ("Gorgoroth Meta".equals(deedType))
    {
      type=null;
      category="Region:Gorgoroth";
    }
    if ((("".equals(deedType)) && ("Reputation".equals(regionalSub))) || ("Reputation".equals(deedType)))
    {
      type=DeedType.REPUTATION;
    }

    if ((type!=null) || (category!=null))
    {
      deed.setType(type);
      deed.setCategory(category);
    }
  }

  private String getPrefixForZone(String zone)
  {
    String ret=null;
    // Instances
    if ("Dargnákh Unleashed".equals(zone)) ret="Instances";
    else if ("Dark Delvings".equals(zone)) ret="Instances";
    else if ("Fil Gashan".equals(zone)) ret="Instances";
    else if ("Forges of Khazad-dûm".equals(zone)) ret="Instances";
    else if ("Forgotten Treasury".equals(zone)) ret="Instances";
    else if ("Skûmfil".equals(zone)) ret="Instances";
    else if ("The Grand Stair".equals(zone)) ret="Instances";
    else if ("The Sixteenth Hall".equals(zone)) ret="Instances";
    else if ("The Vile Maw".equals(zone)) ret="Instances";
    else if ("The Tower of Orthanc".equals(zone)) ret="Instances";
    else if ("The Court of Seregost".equals(zone)) ret="Instances";
    else if ("Dungeons of Naerband".equals(zone)) ret="Instances";
    else if ("Shadows of Angmar".equals(zone)) ret="Cluster";

    // Regions
    else if ("Evendim".equals(zone)) ret="Region";
    else if ("Central Gondor".equals(zone)) ret="Region";
    else if ("Eastern Gondor".equals(zone)) ret="Region";
    else if ("Far Anórien".equals(zone)) ret="Region";
    else if ("Old Anórien".equals(zone)) ret="Region";
    else if ("The Wastes".equals(zone)) ret="Region";
    else if ("West Rohan".equals(zone)) ret="Region";
    else if ("Western Gondor".equals(zone)) ret="Region";
    return ret;
  }

  private String getLineValue(String line)
  {
    int index=line.indexOf('=');
    if (index!=-1)
    {
      return line.substring(index+1).trim();
    }
    return null;
  }
}
