package delta.games.lotro.tools.lore.deeds.checks;

import java.util.List;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;

/**
 * Check/fix deed names.
 * @author DAM
 */
public class NormalizeDeedNames
{
  /**
   * Do it!
   * @param deeds Deeds to use.
   */
  public void doIt(List<DeedDescription> deeds)
  {
    for(DeedDescription deed : deeds)
    {
      normalizeDeedName(deed);
    }
    for(DeedDescription deed : deeds)
    {
      fixProxyNames(deed);
    }
  }

  private void normalizeDeedName(DeedDescription deed)
  {
    String deedName=deed.getName();
    deedName=deedName.replace("(Deed)","").trim();
    deedName=deedName.replace("(deed)","").trim();
    deedName=deedName.replace("(Beorning Deed)","").trim();
    deedName=deedName.replace("(Faction)","").trim();
    deedName=deedName.replace("(Trait Point Deed)","").trim();
    deedName=deedName.replace("  "," ");
    deed.setName(deedName);
  }

  private void fixProxyNames(DeedDescription deed)
  {
    // Parents
    for(DeedProxy proxy : deed.getParentDeedProxies().getDeedProxies())
    {
      String expectedName=proxy.getDeed().getName();
      proxy.setName(expectedName);
    }
    // Childrens
    for(DeedProxy proxy : deed.getChildDeedProxies().getDeedProxies())
    {
      String expectedName=proxy.getDeed().getName();
      proxy.setName(expectedName);
    }
    // Previous
    DeedProxy previous=deed.getPreviousDeedProxy();
    if (previous!=null)
    {
      String expectedName=previous.getDeed().getName();
      previous.setName(expectedName);
    }
    // Next
    DeedProxy next=deed.getNextDeedProxy();
    if (next!=null)
    {
      String expectedName=next.getDeed().getName();
      next.setName(expectedName);
    }
  }
}
