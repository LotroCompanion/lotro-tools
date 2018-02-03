package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private HashMap<String,DeedDescription> _mapByKey;

  /**
   * Constructor.
   * @param deeds Deeds to process.
   */
  public DeedLinksResolver(List<DeedDescription> deeds)
  {
    _deeds=deeds;
    _toAdd=new ArrayList<DeedDescription>();
    loadMapByName();
  }

  private void loadMapByName()
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
    // Resolve deed links
    for(DeedDescription deed : _deeds)
    {
      resolveDeed(deed);
    }
    // Add all missing deeds
    _deeds.addAll(_toAdd);
    // Load a map by key
    loadMapByKey();
    DeedObjectivesParser objectivesParser=new DeedObjectivesParser(_mapByName,_mapByKey);
    // Find additional links in objectives
    for(DeedDescription deed : _deeds)
    {
      objectivesParser.doIt(deed);
    }
    // Remove useless children
    for(DeedDescription deed : _deeds)
    {
      removeUnwantedChildren(deed);
      checkForUnwantedChildren(deed);
    }
    // Check link symetry
    for(DeedDescription deed : _deeds)
    {
      checkDeedSymetry(deed);
    }
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
    if (proxy.getKey()!=null) return; // Already resolved
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
        if (baseDeed!=null)
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

  private void loadMapByKey()
  {
    _mapByKey=new HashMap<String,DeedDescription>();
    for(DeedDescription deed : _deeds)
    {
      String deedKey=deed.getKey();
      DeedDescription old=_mapByKey.put(deedKey,deed);
      if (old!=null)
      {
        System.out.println("Multiple instances of deed key: "+deedKey);
      }
    }
  }

  private void checkDeedSymetry(DeedDescription deed)
  {
    checkNext2PreviousSymetry(deed);
    checkPrevious2NextSymetry(deed);
    checkParent2ChildSymetry(deed);
  }

  private void checkNext2PreviousSymetry(DeedDescription deed) 
  {
    DeedProxy nextProxy=deed.getNextDeedProxy();
    if (nextProxy!=null)
    {
      String nextKey=nextProxy.getKey();
      DeedDescription nextDeed=_mapByKey.get(nextKey);
      if (nextDeed!=null)
      {
        DeedProxy previousProxy=nextDeed.getPreviousDeedProxy();
        if (previousProxy==null)
        {
          previousProxy=new DeedProxy();
          previousProxy.setDeed(deed);
          previousProxy.setKey(deed.getKey());
          previousProxy.setName(deed.getName());
          nextDeed.setPreviousDeedProxy(previousProxy);
        }
      }
    }
  }

  private void checkPrevious2NextSymetry(DeedDescription deed) 
  {
    DeedProxy previousProxy=deed.getPreviousDeedProxy();
    if (previousProxy!=null)
    {
      String previousKey=previousProxy.getKey();
      DeedDescription previousDeed=_mapByKey.get(previousKey);
      if (previousDeed!=null)
      {
        DeedProxy nextProxy=previousDeed.getPreviousDeedProxy();
        if (nextProxy==null)
        {
          nextProxy=new DeedProxy();
          nextProxy.setDeed(deed);
          nextProxy.setKey(deed.getKey());
          nextProxy.setName(deed.getName());
          previousDeed.setNextDeedProxy(nextProxy);
        }
      }
    }
  }

  private void checkParent2ChildSymetry(DeedDescription deed)
  {
    DeedProxy parentProxy=deed.getParentDeedProxy();
    if (parentProxy!=null)
    {
      String parentKey=parentProxy.getKey();
      DeedDescription parentDeed=_mapByKey.get(parentKey);
      if (parentDeed!=null)
      {
        addChildDeed(parentDeed,deed);
      }
    }
  }

  /**
   * Add child deed, if it does not exist.
   * @param parentDeed Parent deed.
   * @param childDeed Child deed.
   */
  public static void addChildDeed(DeedDescription parentDeed, DeedDescription childDeed)
  {
    // Ignore links to self!
    if (parentDeed.getName().equals(childDeed.getName()))
    {
      return;
    }
    // Find child
    boolean found=false;
    for(DeedProxy currentChildDeed : parentDeed.getChildDeeds())
    {
      if (currentChildDeed.getKey().equals(childDeed.getKey()))
      {
        found=true;
        break;
      }
    }
    // Add it if it is not found!
    if (!found)
    {
      DeedProxy childProxy=new DeedProxy();
      childProxy.setDeed(childDeed);
      childProxy.setKey(childDeed.getKey());
      childProxy.setName(childDeed.getName());
      parentDeed.getChildDeeds().add(childProxy);
    }
  }

  private void checkForUnwantedChildren(DeedDescription deed)
  {
    List<DeedProxy> children=deed.getChildDeeds();
    // Grab child deed names
    Map<String,DeedProxy> childNames=new HashMap<String,DeedProxy>();
    for(DeedProxy child : children)
    {
      childNames.put(child.getName(),child);
    }
    // For each child, check if one of its previous is the children list
    List<DeedProxy> toRemove=new ArrayList<DeedProxy>();
    for(DeedProxy child : children)
    {
      DeedDescription childDeed=child.getDeed();
      DeedProxy previous=childDeed.getPreviousDeedProxy();
      while(previous!=null)
      {
        DeedProxy previousInChildren=childNames.get(previous.getName());
        if (previousInChildren!=null)
        {
          toRemove.add(previousInChildren);
          break;
        }
        DeedDescription previousDeed=previous.getDeed();
        previous=(previousDeed!=null)?previousDeed.getPreviousDeedProxy():null;
      }
    }
    children.removeAll(toRemove);
  }

  private void removeUnwantedChildren(DeedDescription deed)
  {
    // Enemies Beneath the Hills
    if ("Enemies Beneath the Hills".equals(deed.getName()))
    {
      removeChildByName(deed,"Leaders Beneath the Hills");
      removeChildByName(deed,"Skoironk: Enemies Beneath");
      removeChildByName(deed,"Towers of the Teeth: Enemies Beneath");
    }
  }

  private void removeChildByName(DeedDescription deed, String name)
  {
    DeedProxy toRemove=null;
    for(DeedProxy child : deed.getChildDeeds())
    {
      if (name.equals(child.getName()))
      {
        toRemove=child;
        break;
      }
    }
    if (toRemove!=null)
    {
      deed.getChildDeeds().remove(toRemove);
      toRemove.getDeed().setParentDeedProxy(null);
    }
  }
}
