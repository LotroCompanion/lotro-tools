package delta.games.lotro.tools.lore.deeds.keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
  private static final Logger LOGGER=Logger.getLogger(DeedsBundle.class);

  private List<DeedDescription> _deeds;
  private Map<String,List<DeedDescription>> _mapByName;
  private Map<String,DeedDescription> _mapByKey;
  private Map<Integer,DeedDescription> _mapById;

  /**
   * Constructor.
   */
  public DeedsBundle()
  {
    _deeds=new ArrayList<DeedDescription>();
    _mapByName=new HashMap<String,List<DeedDescription>>();
    _mapByKey=new HashMap<String,DeedDescription>();
    _mapById=new HashMap<Integer,DeedDescription>();
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
    String name=deed.getName().trim();
    registerByName(name,deed);
    String lowerCase=name.toLowerCase();
    if (!name.equals(lowerCase))
    {
      registerByName(name.toLowerCase(),deed);
    }
    String key=deed.getKey();
    if (key!=null)
    {
      DeedDescription old=_mapByKey.put(key,deed);
      if (old!=null)
      {
        LOGGER.warn("Duplicate deed key: "+key);
      }
    }
    int id=deed.getIdentifier();
    if (id!=0)
    {
      Integer idKey=Integer.valueOf(id);
      DeedDescription old=_mapById.put(idKey,deed);
      if (old!=null)
      {
        LOGGER.warn("Duplicate deed ID: "+id);
      }
    }
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
   * Get a deed using its key.
   * @param key Key to search.
   * @return A deed or <code>null</code> if not found.
   */
  public DeedDescription getDeedByKey(String key)
  {
    return _mapByKey.get(key);
  }

  /**
   * Get a deed using its identifier.
   * @param id Identifier to search.
   * @return A deed or <code>null</code> if not found.
   */
  public DeedDescription getDeedById(int id)
  {
    return _mapById.get(Integer.valueOf(id));
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
