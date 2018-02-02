package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;
import delta.games.lotro.lore.deeds.DeedType;

/**
 * Resolver for deed links.
 * @author DAM
 */
public class DeedLinksResolver
{
  private List<DeedDescription> _deeds;
  private List<DeedDescription> _toAdd;
  private HashMap<String,DeedDescription> _mapByName;

  /**
   * Constructor.
   * @param deeds Deeds to process.
   */
  public DeedLinksResolver(List<DeedDescription> deeds)
  {
    _deeds=deeds;
    _toAdd=new ArrayList<DeedDescription>();
    loadMap();
  }

  private void loadMap()
  {
    _mapByName=new HashMap<String,DeedDescription>();
    for(DeedDescription deed : _deeds)
    {
      String name=deed.getName();
      DeedDescription old=_mapByName.put(name,deed);
      if (old!=null)
      {
        System.out.println("Multiple instances of deed name: "+name);
      }
    }
  }

  /**
   * Do resolve links.
   */
  public void doIt()
  {
    for(DeedDescription deed : _deeds)
    {
      resolveDeed(deed);
    }
    _deeds.addAll(_toAdd);
  }

  private void resolveDeed(DeedDescription deed)
  {
    resolveDeedProxy(deed.getParentDeedProxy());
    resolveDeedProxy(deed.getNextDeedProxy());
    resolveDeedProxy(deed.getPreviousDeedProxy());
    for(DeedProxy childProxy : deed.getChildDeeds())
    {
      resolveDeedProxy(childProxy);
    }
  }

  private void resolveDeedProxy(DeedProxy proxy)
  {
    if (proxy==null) return;
    String deedName=proxy.getName();
    deedName=deedName.replace("  "," ");
    DeedDescription proxiedDeed=_mapByName.get(deedName);
    if (proxiedDeed!=null)
    {
      proxy.setName(deedName);
      proxy.setDeed(proxiedDeed);
      proxy.setKey(proxiedDeed.getKey());
    }
    else
    {
      boolean ok=false;
      if (deedName.endsWith(" Slayer (Advanced)"))
      {
        String baseDeedName=deedName.substring(0, deedName.length() - 10).trim();
        DeedDescription baseDeed=_mapByName.get(baseDeedName);
        if (baseDeed != null)
        {
          DeedDescription advancedDeed=buildAdvancedSkirmishLieutenantDeed(baseDeed);
          _toAdd.add(advancedDeed);
          ok=true;
        }
      }
      if (!ok)
      {
        System.out.println("Deed not found: "+deedName);
      }
    }
  }

  private DeedDescription buildAdvancedSkirmishLieutenantDeed(DeedDescription baseDeed) {
    DeedDescription deed = new DeedDescription();
    deed.setKey(baseDeed.getKey()+"_(Advanced)");
    deed.setName(baseDeed.getName()+" (Advanced)");
    deed.setType(DeedType.SLAYER);
    deed.setCategory(baseDeed.getCategory());
    deed.setDescription(baseDeed.getDescription());
    // Objectives
    // First line of base deed, replace (5) by (50)
    {
      String newObjectives="";
      String objectives=baseDeed.getObjectives();
      int index=objectives.indexOf('\n');
      if (index!=-1)
      {
        newObjectives=objectives.substring(0,index);
      }
      else
      {
        newObjectives=objectives;
      }
      newObjectives=newObjectives.replace("(5)","(50)");
      deed.setObjectives(newObjectives);
    }
    // Previous
    DeedProxy previous=new DeedProxy();
    previous.setDeed(baseDeed);
    previous.setKey(previous.getKey());
    previous.setName(previous.getName());
    deed.setPreviousDeedProxy(previous);
    // Rewards
    ObjectItem item=new ObjectItem("Mark");
    item.setItemId(1879224343);
    deed.getRewards().getObjects().addObject(item,500);
    return deed;
  }
}
