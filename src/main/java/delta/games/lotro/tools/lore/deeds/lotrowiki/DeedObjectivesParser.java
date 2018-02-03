package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.util.HashMap;
import java.util.List;

import delta.common.utils.text.TextTools;
import delta.games.lotro.lore.deeds.DeedDescription;

/**
 * Parser to find child deeds in deed objectives.
 * @author DAM
 */
public class DeedObjectivesParser
{
  private HashMap<String,DeedDescription> _mapByName;

  /**
   * Constructor.
   * @param mapByName Map of known deeds (name to deed).
   */
  public DeedObjectivesParser(HashMap<String,DeedDescription> mapByName)
  {
    _mapByName=mapByName;
  }

  /**
   * Find child deed using objectives.
   * @param deed Deed to use.
   */
  public void doIt(DeedDescription deed)
  {
    String objectives=deed.getObjectives();
    String[] lines=objectives.split("\n");
    for(String line : lines)
    {
      int index=line.indexOf("Complete ");
      if (index!=-1)
      {
        DeedDescription linkedDeed=findDeedInObjectivesLine(deed,line);
        if (linkedDeed!=null)
        {
          DeedLinksResolver.addChildDeed(deed,linkedDeed);
        }
      }
    }
  }

  private DeedDescription findDeedInObjectivesLine(DeedDescription parentDeed, String line)
  {
    DeedDescription ret=null;
    List<String> links=TextTools.findAllBetween(line,"[[","]]");
    boolean foundLinkToBeIgnored=false;
    for(String link : links)
    {
      int index=link.indexOf('|');
      String linkId;
      String linkText;
      if (index!=-1)
      {
        linkId=link.substring(0,index).trim();
        linkText=link.substring(index+1).trim();
      }
      else
      {
        linkId=null;
        linkText=link.trim();
      }
      if (linkId!=null)
      {
        ret=resolveDeed(linkId);
        if (ret==null)
        {
          if (ignoreLink(linkId)) foundLinkToBeIgnored=true;
        }
      }
      if (ret==null)
      {
        ret=resolveDeed(linkText);
        if (ret==null)
        {
          if (ignoreLink(linkText)) foundLinkToBeIgnored=true;
        }
      }
    }
    boolean ignoreLine=ignoreLine(line);
    if ((ret==null) && (!foundLinkToBeIgnored) && (!ignoreLine))
    {
      System.out.println(parentDeed.getName()+"\t"+line);
    }
    return ret;
  }

  private boolean ignoreLink(String link)
  {
    if (link.startsWith("Quest:")) return true;
    if (link.toLowerCase().contains("complete quests")) return true;
    return false;
  }

  private boolean ignoreLine(String line)
  {
    if (line.toLowerCase().contains("complete quests")) return true;
    if (line.toLowerCase().contains("complete tasks")) return true;
    return false;
  }

  private DeedDescription resolveDeed(String text)
  {
    if (text==null) return null;
    //if ("Dargnákh Unleashed".equals(text)) text="Isengard: Dargnákh Unleashed";
    //if ("The Foundry".equals(text)) text="Isengard: The Foundry";
    DeedDescription deed=_mapByName.get(text);
    return deed;
  }
}
