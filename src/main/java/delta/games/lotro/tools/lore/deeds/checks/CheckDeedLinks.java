package delta.games.lotro.tools.lore.deeds.checks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxies;
import delta.games.lotro.lore.deeds.DeedProxy;

/**
 * Check/fix links in deeds.
 * @author DAM
 */
public class CheckDeedLinks
{
  /**
   * Do it!
   * @param deeds Deeds to use.
   */
  public void doIt(List<DeedDescription> deeds)
  {
    for(DeedDescription deed : deeds)
    {
      cleanChildDeeds(deed);
    }
    for(DeedDescription deed : deeds)
    {
      checkParentDeed(deed);
    }
  }

  private void checkParentDeed(DeedDescription deed)
  {
    List<DeedProxy> childProxies=deed.getChildDeedProxies().getDeedProxies();
    for(DeedProxy childProxy : childProxies)
    {
      DeedDescription childDeed=childProxy.getObject();
      DeedProxies parentProxies=childDeed.getParentDeedProxies();
      DeedProxy parentProxy=parentProxies.getByKey(deed.getKey());
      if (parentProxy==null)
      {
        parentProxy=new DeedProxy();
        parentProxy.setObject(deed);
        parentProxy.setKey(deed.getKey());
        parentProxy.setName(deed.getName());
        parentProxies.add(parentProxy);
        //System.out.println("Added link from "+childDeed.getKey()+" to "+deed.getKey());
      }
    }
  }

  private void cleanChildDeeds(DeedDescription deed)
  {
    Set<String> previousDeedsOfChildren=loadPreviousDeedsOfChildren(deed);
    List<DeedProxy> childrenToRemove=new ArrayList<DeedProxy>();
    List<DeedProxy> childProxies=deed.getChildDeedProxies().getDeedProxies();
    for(DeedProxy childProxy : childProxies)
    {
      String childKey=childProxy.getKey();
      if (previousDeedsOfChildren.contains(childKey))
      {
        childrenToRemove.add(childProxy);
      }
    }
    for(DeedProxy childToRemove : childrenToRemove)
    {
      childProxies.remove(childToRemove);
    }
  }

  private Set<String> loadPreviousDeedsOfChildren(DeedDescription deed)
  {
    Set<String> previousDeeds=new HashSet<String>();
    List<DeedProxy> childProxies=deed.getChildDeedProxies().getDeedProxies();
    for(DeedProxy childProxy : childProxies)
    {
      DeedDescription childDeed=childProxy.getObject();
      previousDeeds.addAll(loadPreviousDeeds(childDeed));
    }
    return previousDeeds;
  }

  private Set<String> loadPreviousDeeds(DeedDescription deed)
  {
    Set<String> previousDeeds=new HashSet<String>();
    DeedProxy previousProxy=deed.getPreviousDeedProxy();
    while (previousProxy!=null)
    {
      String key=previousProxy.getKey();
      previousDeeds.add(key);
      DeedDescription previousDeed=previousProxy.getObject();
      if (previousDeed!=null)
      {
        previousProxy=previousDeed.getPreviousDeedProxy();
      }
      else
      {
        previousProxy=null;
      }
    }
    return previousDeeds;
  }

}

