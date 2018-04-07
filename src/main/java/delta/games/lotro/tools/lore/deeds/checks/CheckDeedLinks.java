package delta.games.lotro.tools.lore.deeds.checks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.lore.deeds.DeedDescription;
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
      handleDeed(deed);
    }
  }

  private void handleDeed(DeedDescription deed)
  {
    Set<String> previousDeedsOfChildren=loadPreviousDeedsOfChildren(deed);
    List<DeedProxy> childrenToRemove=new ArrayList<DeedProxy>();
    List<DeedProxy> childProxies=deed.getChildDeeds();
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
    List<DeedProxy> children=deed.getChildDeeds();
    for(DeedProxy childProxy : children)
    {
      DeedDescription childDeed=childProxy.getDeed();
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
      DeedDescription previousDeed=previousProxy.getDeed();
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

