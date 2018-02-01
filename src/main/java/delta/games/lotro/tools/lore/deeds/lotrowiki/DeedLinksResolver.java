package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;

/**
 * Resolver for deed links.
 * @author DAM
 */
public class DeedLinksResolver
{
  private List<DeedDescription> _deeds;
  private HashMap<String,DeedDescription> _mapByName;

  /**
   * Constructor.
   * @param deeds Deeds to process.
   */
  public DeedLinksResolver(List<DeedDescription> deeds)
  {
    _deeds=new ArrayList<DeedDescription>(deeds);
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
      System.out.println("Deed not found: "+deedName);
    }
  }
}
