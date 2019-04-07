package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Emote;
import delta.games.lotro.common.ReputationItem;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Skill;
import delta.games.lotro.common.Title;
import delta.games.lotro.common.Trait;
import delta.games.lotro.common.Virtue;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.WellKnownItems;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Parse for lotro-wiki deed pages.
 * @author DAM
 */
public class LotroWikiDeedPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedPageParser.class);

  private static final String DEED_CHAIN_SEED="Deed-chain-";

  private File _currentFile;

  /**
   * Parse the lotro wiki deed page for the given deed ID.
   * @param from Source page.
   * @return A deed or <code>null</code> if an error occurred.
   */
  public List<DeedDescription> parseDeeds(File from)
  {
    _currentFile=from;
    List<DeedDescription> deeds=null;
    try
    {
      FileInputStream inputStream=new FileInputStream(from);
      Source source=new Source(inputStream);

      String name=null;
      Element backElement=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.DIV,"id","contentSub");
      if (backElement!=null)
      {
        Element a=JerichoHtmlUtils.findElementByTagName(backElement,HTMLElementName.A);
        if (a!=null)
        {
          name=a.getAttributeValue("title");
        }
      }

      Element deedSource=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.TEXTAREA,"id","wpTextbox1");
      if (deedSource!=null)
      {
        Segment content=deedSource.getContent();
        String text=content.toString();
        deeds=buildDeeds(text,name);
      }
      if (deeds!=null)
      {
        // Fixes
        for(DeedDescription deed : deeds)
        {
          handleFixes(deed);
        }
      }
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed page ["+from+"]",e);
    }
    return deeds;
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

  private Integer findDeedStartLine(String[] lines, int startIndex)
  {
    int endIndex=lines.length;
    if (endIndex<3) return null;
    int index=startIndex;
    while (index<endIndex)
    {
      if ((lines[index].contains("{{Deed")) && (!lines[index].contains("Deed Stub")) && (!lines[index].contains("{{Deeds-infobox}}")))
      {
        return Integer.valueOf(index);
      }
      index++;
    }
    return null;
  }

  private List<DeedDescription> buildDeeds(String rawData, String deedName)
  {
    String[] lines=rawData.split("\n");

    // Checks
    if (lines==null) return null;

    List<DeedDescription> ret=new ArrayList<DeedDescription>();
    int startIndex=0;
    while (true)
    {
      Integer deedLineIndex=findDeedStartLine(lines,startIndex);
      if (deedLineIndex==null)
      {
        break;
      }
      startIndex=deedLineIndex.intValue()+1;
      DeedDescription deed=parseDeed(deedLineIndex.intValue(),lines,deedName);
      if (deed!=null)
      {
        ret.add(deed);
      }
    }
    return ret;
  }

  private DeedDescription parseDeed(int startIndex, String[] lines, String deedName)
  {
    DeedDescription deed=new DeedDescription();
    deed.setName(deedName);
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
    // Deeds chain
    List<String> deedsChain=new ArrayList<String>();

    for(int index=startIndex+1;index<lines.length;index++)
    {
      String line=lines[index].trim();
      if (line.contains("{{Deed")) break;
      String lineKey=fetchLineKey(line);
      //System.out.println(line);
      if ("name".equals(lineKey))
      {
        //String name=getLineValue(line);
        //deed.setName(name);
      }
      else if ("Lore-text".equals(lineKey))
      {
        String description=getLineValue(line);
        description=normalize(description);
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
        String objectives=sb.toString().trim();
        objectives=normalize(objectives);
        deed.setObjectives(objectives);
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
      else if (("TP-reward".equals(lineKey)) || ("LP-reward".equals(lineKey)))
      {
        String tpStr=getLineValue(line);
        if ((!tpStr.isEmpty()) && (!"???".equals(tpStr)))
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
        // Skirmish Mark
        String smStr=getLineValue(line);
        if (!smStr.isEmpty())
        {
          if (smStr.endsWith("r")) smStr=smStr.substring(0,smStr.length()-1);
          Integer marks=NumericTools.parseInteger(smStr,false);
          if (marks!=null)
          {
            ObjectsSet objects=deed.getRewards().getObjects();
            Proxy<Item> item=new Proxy<Item>();
            item.setName("Mark");
            item.setId(WellKnownItems.MARK);
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
        // Class point
        String value=getLineValue(line);
        if (value.trim().length()>0)
        {
          deed.getRewards().setClassPoints(1);
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
      else if ("Emote-reward".equals(lineKey))
      {
        String emoteStr=getLineValue(line);
        if (!emoteStr.isEmpty())
        {
          handleEmoteReward(rewards,emoteStr);
        }
      }
      else if ("Skill-reward".equals(lineKey))
      {
        String skillStr=getLineValue(line);
        if (!skillStr.isEmpty())
        {
          handleSkillReward(rewards,skillStr);
        }
      }
      else if ("DP-reward".equals(lineKey))
      {
        String destinyPointsStr=getLineValue(line);
        if (!destinyPointsStr.isEmpty())
        {
          int destinyPoints;
          if ("?".equals(destinyPointsStr))
          {
            destinyPoints=1;
          }
          else
          {
            destinyPoints=NumericTools.parseInt(destinyPointsStr,0);
          }
          rewards.setDestinyPoints(destinyPoints);
        }
      }
      else if ("Level".equals(lineKey))
      {
        String levelStr=getLineValue(line);
        if ("?".equals(levelStr)) levelStr="";
        if ("???".equals(levelStr)) levelStr="";
        if (!levelStr.isEmpty())
        {
          levelStr=levelStr.replace("&lt;","<");
          if (levelStr.startsWith("<=")) levelStr=levelStr.substring(2);
          if (levelStr.equals("{{Level Cap}}")) levelStr="1000";
          if (levelStr.endsWith("+")) levelStr=levelStr.substring(0,levelStr.length()-1);
          levelStr=removeXmlComments(levelStr);
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
      else if ((lineKey!=null) && (lineKey.startsWith(DEED_CHAIN_SEED)))
      {
        Integer deedIndex=NumericTools.parseInteger(lineKey.substring(DEED_CHAIN_SEED.length()));
        if (deedIndex!=null)
        {
          // Add missing entries
          int nbMissingEntries=deedIndex.intValue()-deedsChain.size();
          for(int i=0;i<nbMissingEntries;i++)
          {
            deedsChain.add(null);
          }
          // Set deed name in chain
          String deedNameInChain=getLineValue(line);
          deedsChain.set(deedIndex.intValue()-1,deedNameInChain);
        }
      }
      else if ("Parent-deed".equals(lineKey))
      {
        String parentDeed=getLineValue(line);
        if (!parentDeed.isEmpty())
        {
          if ("Scourge-slayer of Mordor (Advanced)".equals(parentDeed))
          {
            DeedProxy nextProxy=new DeedProxy();
            nextProxy.setName(parentDeed);
            deed.setNextDeedProxy(nextProxy);
          }
          else
          {
            if (useParentDeedInfo(parentDeed,deedName))
            {
              parentDeed=fixParentDeedInfo(parentDeed,deedName);
              DeedProxy parentProxy=new DeedProxy();
              parentProxy.setName(parentDeed);
              deed.getParentDeedProxies().add(parentProxy);
            }
          }
        }
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
| Hidden       = 
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
    // Deeds chain
    handleDeedsChain(deed,deedsChain);
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

  private boolean useParentDeedInfo(String parentDeed, String deedName)
  {
    if (("Slayer of Dunland".equals(parentDeed)) && (deedName.contains("Advanced")))
    {
      return false;
    }
    return true;
  }

  private String fixParentDeedInfo(String parentDeed, String deedName)
  {
    if ("Foe-slayer of Lang Rhuven (Advanced)".equals(deedName))
    {
      return "Threats of the Wastes";
    }
    return parentDeed;
  }

  private void handleTraitReward(Rewards rewards, String traitStr)
  {
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
      Trait trait=new Trait(traitStr);
      rewards.addTrait(trait);
    }
  }

  private void handleEmoteReward(Rewards rewards, String emoteStr)
  {
    Emote emote=new Emote(emoteStr);
    rewards.addEmote(emote);
  }

  private void handleSkillReward(Rewards rewards, String skillStr)
  {
    Skill skill=new Skill(skillStr);
    rewards.addSkill(skill);
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
        Proxy<Item> item=new Proxy<Item>();
        item.setName(itemName);
        objects.addObject(item,count.intValue());
      }
    }
  }

  private static final String FACTION_SUFFIX=" (Faction)";
  private static final String REPUTATION_SUFFIX=" (Reputation)";

  private static final String TITLE_SUFFIX=" (Title)";

  private Faction extractFaction(String line)
  {
    String factionName=getLineValue(line);
    if (factionName.endsWith(FACTION_SUFFIX))
    {
      factionName=factionName.substring(0,factionName.length()-FACTION_SUFFIX.length());
    }
    if (factionName.endsWith(REPUTATION_SUFFIX))
    {
      factionName=factionName.substring(0,factionName.length()-REPUTATION_SUFFIX.length());
    }
    Faction faction=null;
    if (factionName.length()>0)
    {
      faction=FactionsRegistry.getInstance().getByName(factionName);
      if (faction==null)
      {
        System.err.println("Bad faction ["+factionName+"]");
      }
    }
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
      int index=titleName.indexOf("{{!}}");
      if (index!=-1)
      {
        titleName=titleName.substring(index+5);
      }
      index=titleName.indexOf("| display=");
      if (index!=-1)
      {
        titleName=titleName.substring(index+10);
      }
      
      index=titleName.indexOf('|');
      if (index!=-1)
      {
        titleName=titleName.substring(0,index);
      }
      if (titleName.endsWith(" (title)")) titleName=titleName.substring(0,titleName.length()-8);
      titleName=titleName.trim();
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
    CharacterClass requiredClass=null;
    if ("Class".equals(deedType))
    {
      type=DeedType.CLASS;
      category=deedSubType;
      requiredClass=extractClass(category);
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
    deed.setRequiredClass(requiredClass);
  }

  private CharacterClass extractClass(String category)
  {
    CharacterClass ret=null;
    if (category!=null)
    {
      ret=CharacterClass.getByName(category);
      if (ret==null)
      {
        System.out.println("Warn: not found: '"+category+"'");
      }
    }
    return ret;
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

  private boolean useDeedsChain(DeedDescription deed)
  {
    String name=deed.getName();
    // List deed pages where the deed chain is wrong
    if ("Aiding the Wold".equals(name)) return false;
    if ("Aiding the Norcrofts".equals(name)) return false;
    if ("Aiding the Sutcrofts".equals(name)) return false;
    if ("Aiding the Entwash Vale".equals(name)) return false;
    if ("Aiding the Eastemnet".equals(name)) return false;
    if ("Commanders of Isengard -- Tier 1".equals(name)) return false;
    if ("Commanders of Isengard -- Tier 2".equals(name)) return false;
    if ("Draigoch's Lair -- Tier 1".equals(name)) return false;
    if ("Draigoch's Lair -- Tier 2".equals(name)) return false;
    if ("Draigoch's Lair -- Challenge".equals(name)) return false;
    if ("Draigoch the Red".equals(name)) return false;
    if ("Commanders of the Foundry -- Tier 1".equals(name)) return false;
    if ("Commanders of the Foundry -- Tier 2".equals(name)) return false;
    if ("Saviour of the Roots of Fangorn".equals(name)) return false;
    if ("Roots of Fangorn: Defeat Gurthúl".equals(name)) return false;
    if ("Roots of Fangorn: Slaves of the Spider Queen".equals(name)) return false;
    if ("Discovery: Roots of Fangorn".equals(name)) return false;

    return true;
  }

  private void handleDeedsChain(DeedDescription deed, List<String> deedsChain)
  {
    if (!useDeedsChain(deed)) return;
    deedsChain=normalizeDeedsChain(deedsChain);
    if (deedsChain.size()==0) return;
    String name=deed.getName();
    int index=deedsChain.indexOf(name);
    if (index!=-1)
    {
      if (index>0)
      {
        DeedProxy previous=new DeedProxy();
        String previousDeedName=deedsChain.get(index-1);
        previous.setName(LotroWikiDeedCategoryPageParser.fixNames(previousDeedName));
        deed.setPreviousDeedProxy(previous);
      }
      if (index<deedsChain.size()-1)
      {
        DeedProxy next=new DeedProxy();
        String nextDeedName=deedsChain.get(index+1);
        next.setName(LotroWikiDeedCategoryPageParser.fixNames(nextDeedName));
        deed.setNextDeedProxy(next);
      }
    }
    else
    {
      // Try to solve Hybold links
      if ("Hytbold (Deed)".equals(deedsChain.get(0)))
      {
        String parent=deedsChain.get(deedsChain.size()-1);
        DeedProxy parentProxy=new DeedProxy();
        parentProxy.setName(parent);
        DeedProxy oldParentProxy=deed.getParentDeedProxies().getFirst();
        if (oldParentProxy!=null)
        {
          if (!oldParentProxy.getName().equals(parent))
          {
            System.out.println("Mismatch!");
          }
        }
        deed.getParentDeedProxies().add(parentProxy);
      }
      else
      {
        System.out.println("Name ["+name+"] not found in "+deedsChain);
      }
    }
  }

  private List<String> normalizeDeedsChain(List<String> deedsChain)
  {
    List<String> ret=new ArrayList<String>();
    for(String deed : deedsChain)
    {
      if ((deed!=null) && (deed.trim().length()>0))
      {
        int index=deed.indexOf("{{!}}");
        if (index!=-1) deed=deed.substring(0,index).trim();
        deed=deed.replace("Silent Slayer Slayer","Silent Slayer Stalker");
        if (deed.startsWith("Quest:"))
        {
          // Ignore
        }
        else
        {
          ret.add(deed);
        }
      }
    }
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

  private String normalize(String input)
  {
    input=normalizeHtml(input);
    input=removeXmlComments(input);
    input=cleanupLines(input);
    return input;
  }

  private String cleanupLines(String input)
  {
    input=input.replace("\r\n","\n");
    input="\n"+input+"\n";
    String oldInput=input;
    while(true)
    {
      input=input.replace(" \n","\n");
      input=input.replace("\n ","\n");
      input=input.replace("\n:","\n");
      input=input.replace("''\n","\n");
      input=input.replace("\n''","\n");
      input=input.replace("\n*","\n");
      if (oldInput.equals(input)) break;
      oldInput=input;
    }
    input=input.trim();
    return input;
  }

  private String removeXmlComments(String input)
  {
    while(true)
    {
      int index=input.indexOf("<!--");
      if (index==-1) break;
      int index2=input.indexOf("-->",index+4);
      if (index2==-1) break;
      input=input.substring(0,index)+input.substring(index2+3).trim();
    }
    return input;
  }

  private String normalizeHtml(String input)
  {
    input=input.replace("&lt;","<");
    input=input.replace("<br />","\n");
    input=input.replace("<br>","\n");
    input=input.replace("&amp;","&");
    return input;
  }
}
