package delta.games.lotro.tools.lore.deeds.keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.deeds.DeedDescription;

/**
 * Bundle of deeds.
 * 
 * <p>Provides simplified access to deeds:
 * <ul>
 * <li>by name
 * <li>...
 * </ul>
 * @author DAM
 */
public class DeedsBundle
{
  private List<DeedDescription> _deeds;
  private Map<String,List<DeedDescription>> _mapByName;

  /**
   * Constructor.
   */
  public DeedsBundle()
  {
    _deeds=new ArrayList<DeedDescription>();
    _mapByName=new HashMap<String,List<DeedDescription>>();
  }

  /**
   * Load some deeds in this bundle.
   * @param deeds
   */
  public void setDeeds(List<DeedDescription> deeds)
  {
    _mapByName.clear();
    for(DeedDescription deed : deeds)
    {
      registerDeed(deed);
    }
  }

  /**
   * Get all managed deeds.
   * @return A list of deeds.
   */
  public List<DeedDescription> getAll()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>(_deeds);
    return deeds;
  }

  private void registerDeed(DeedDescription deed)
  {
    _deeds.add(deed);
    String name=deed.getName();
    registerByName(name,deed);
    registerByName(name.toLowerCase(),deed);
  }

  private void registerByName(String name, DeedDescription deed)
  {
    List<DeedDescription> deeds=_mapByName.get(name);
    if (deeds==null)
    {
      deeds=new ArrayList<DeedDescription>();
      _mapByName.put(name,deeds);
    }
    deeds.add(deed);
  }

  /**
   * Get deeds by name.
   * @param name Name of deed to get.
   * @return A possibly empty but not <code>null</code> list of deeds.
   */
  public List<DeedDescription> getDeedsByName(String name)
  {
    List<DeedDescription> ret=_mapByName.get(name);
    if (ret==null)
    {
      ret=new ArrayList<DeedDescription>();
    }
    return ret;
  }
}
