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
import delta.games.lotro.common.Title;
import delta.games.lotro.common.Virtue;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.lore.deeds.DeedDescription;
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

  /**
   * Parse the lotro wiki deed page for the given deed ID.
   * @param from Source page.
   * @return A deed or <code>null</code> if an error occurred.
   */
  public DeedDescription parseDeed(File from)
  {
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
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed page ["+from+"]",e);
    }
    return deed;
  }

  private DeedDescription buildDeed(String rawData)
  {
    DeedDescription deed=new DeedDescription();
    String[] lines=rawData.split("\n");
    Faction faction=null;
    Integer reputation=null;
    Title title=null;
    VirtueId virtueId=null;
    //Integer virtueCount=null;
    for(String line : lines)
    {
      //System.out.println(line);
      if (line.startsWith("| name"))
      {
        String name=getLineValue(line);
        deed.setName(name);
      }
      else if (line.startsWith("| Lore-text"))
      {
        String description=getLineValue(line);
        deed.setDescription(description);
      }
      else if (line.startsWith("| Faction"))
      {
        faction=extractFaction(line);
      }
      else if (line.startsWith("| Reputation"))
      {
        reputation=NumericTools.parseInteger(getLineValue(line));
      }
      else if (line.startsWith("| Title "))
      {
        title=extractTitle(line);
      }
      else if (line.startsWith("| Virtue "))
      {
        virtueId=extractVirtue(line);
      }
/*
| Virtue       = Determination
| Virtue-value = 1
| DP-reward    = 
| SM-reward    = 
| Item-reward  = Mark
| Item-amount  = 60
| Item-reward2 = Amroth Silver Piece
| Item-amount2 = 18
| Item-reward3 = 
| Item-amount3 = 
| Skill-reward = 
| Trait-reward = 
| Emote-reward = 
| TP-reward    = 10
| Deed-type    = Regional
| Deed-subtype = Western Gondor
| Regional-sub = Explorer
| Hidden       = 
| Deed-chain-1 = 
| Deed-chain-2 = 
| Deed-chain-3 = 
| Deed-chain-4 = 
| Deed-chain-5 = 
| Meta-deed     = 
| Parent-deed   = Explorer of West Gondor
| Extra         = 
       */
    }
    if ((faction!=null) && (reputation!=null))
    {
      ReputationItem item=new ReputationItem(faction);
      item.setAmount(reputation.intValue());
      deed.getRewards().getReputation().add(item);
    }
    if (title!=null)
    {
      deed.getRewards().addTitle(title);
    }
    if (virtueId!=null)
    {
      Virtue virtue=new Virtue(virtueId.name(),virtueId.getLabel());
      deed.getRewards().addVirtue(virtue);
    }
    return deed;
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
