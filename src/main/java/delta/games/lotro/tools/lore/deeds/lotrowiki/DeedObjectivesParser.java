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
  private HashMap<String,DeedDescription> _mapByKey;

  /**
   * Constructor.
   * @param mapByName Map of known deeds (name to deed).
   * @param mapByKey Map of known deeds (key to deed).
   */
  public DeedObjectivesParser(HashMap<String,DeedDescription> mapByName,HashMap<String,DeedDescription> mapByKey)
  {
    _mapByName=mapByName;
    _mapByKey=mapByKey;
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
      //int index=line.indexOf("Complete ");
      //if (index!=-1)
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
    if (links.size()>0)
    {
      boolean ignore=false;
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
          boolean ignoreLink=ignoreLink(linkId);
          if (ignoreLink)
          {
            ignore=true;
            continue;
          }
          ret=resolveDeed(linkId);
        }
        if (ret==null)
        {
          boolean ignoreLink=ignoreLink(linkText);
          if (ignoreLink)
          {
            ignore=true;
            continue;
          }
          ret=resolveDeed(linkText);
        }
      }
      if ((ret==null) && (!ignore))
      {
        boolean ignoreLine=ignoreLine(line);
        if (!ignoreLine)
        {
          //System.out.println(parentDeed.getName()+" => "+line);
        }
      }
    }
    return ret;
  }

  private boolean ignoreLink(String link)
  {
    if (link.startsWith("Quest:")) return true;
    if (link.startsWith("Item:")) return true;
    //if (link.toLowerCase().contains("complete quests")) return true;
    return false;
  }

  private boolean ignoreLine(String line)
  {
    if (line.toLowerCase().contains("complete quests")) return true;
    if (line.toLowerCase().contains("complete tasks")) return true;
    if (line.toLowerCase().contains("discover")) return true;
    if (line.toLowerCase().contains("you have aided")) return true;
    if (line.toLowerCase().contains("find ")) return true;
    if (line.toLowerCase().contains("defeat ")) return true;
    if (line.toLowerCase().contains("meet ")) return true;
    if (line.toLowerCase().contains("use ")) return true;
    if (line.toLowerCase().contains("explore ")) return true;
    if (line.toLowerCase().contains("mount ")) return true;
    if (line.toLowerCase().contains("must earn ")) return true;
    if (line.toLowerCase().contains("speak with ")) return true;
    if (line.toLowerCase().contains("journey to the ")) return true;
    return false;
  }

  private DeedDescription resolveDeed(String text)
  {
    if (text==null) return null;
    if ("the Ruins of Bree-land".equals(text)) text="The Ruins of Breeland";

    if (text.startsWith("the ")) text="The "+text.substring(4);
    //if ("Dargnákh Unleashed".equals(text)) text="Isengard: Dargnákh Unleashed";
    //if ("The Foundry".equals(text)) text="Isengard: The Foundry";
    DeedDescription deed=_mapByName.get(text);
    if (deed==null)
    {
      deed=_mapByKey.get(text);
    }
    return deed;
  }
}
