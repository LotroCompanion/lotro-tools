package delta.games.lotro.tools.dat.maps.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.data.EntityDescriptor;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.maps.landblocks.Cell;

/**
 * Land-block info.
 * @author DAM
 */
public class LandBlockInfo
{
  private long _id;
  private PropertiesSet _props;
  private List<Cell> _cells;
  private Map<Long,EntityDescriptor> _entities;
  private Map<Long,Weenie> _weenies;
  private Map<Long,LbiLink> _links;

  /**
   * Constructor.
   * @param id
   */
  public LandBlockInfo(long id)
  {
    _id=id;
    _props=new PropertiesSet();
    _cells=new ArrayList<Cell>();
    _entities=new HashMap<Long,EntityDescriptor>();
    _weenies=new HashMap<Long,Weenie>();
    _links=new HashMap<Long,LbiLink>();
  }

  /**
   * Get the identifier of this block.
   * @return an identifier.
   */
  public long getId()
  {
    return _id;
  }

  /**
   * Get the region of this landblock.
   * @return a region code.
   */
  public int getRegion()
  {
    return (int)(_id&0xF0000)>>16;
  }

  /**
   * Get the X-block for this landblock.
   * @return a X block value.
   */
  public int getBlockX()
  {
    return (int)(_id&0xFF00)>>8;
  }

  /**
   * Get the Y-block for this landblock.
   * @return a Y block value.
   */
  public int getBlockY()
  {
    return (int)(_id&0xFF);
  }

  /**
   * Get the landblock properties.
   * @return the landblock properties.
   */
  public PropertiesSet getProps()
  {
    return _props;
  }

  /**
   * Add a cell.
   * @param cell Cell to add.
   */
  public void addCell(Cell cell)
  {
    _cells.add(cell);
  }

  /**
   * Get all cells.
   * @return A list of managed cells.
   */
  public List<Cell> getCells()
  {
    return new ArrayList<Cell>(_cells);
  }

  /**
   * Add an entity.
   * @param entity Entity to add.
   */
  public void addEntity(EntityDescriptor entity)
  {
    Long key=Long.valueOf(entity.getIid());
    _entities.put(key,entity);
  }

  /**
   * Get all the managed entities, sorted by IID.
   * @return a list of entities.
   */
  public List<EntityDescriptor> getEntities()
  {
    List<EntityDescriptor> ret=new ArrayList<EntityDescriptor>();
    for(Long iid : getEntityIids())
    {
      ret.add(_entities.get(iid));
    }
    return ret;
  }

  /**
   * Get the IIDs of the managed entities.
   * @return A sorted list of entity IIDs.
   */
  public List<Long> getEntityIids()
  {
    List<Long> ret=new ArrayList<Long>(_entities.keySet());
    Collections.sort(ret);
    return ret;
  }

  /**
   * Get an entity using its identifier.
   * @param iid Entity identifier.
   * @return An entity or <code>null</code> if not found.
   */
  public EntityDescriptor getEntityByIid(long iid)
  {
    return _entities.get(Long.valueOf(iid));
  }

  /**
   * Add a link.
   * @param link Link to add.
   */
  public void addLink(LbiLink link)
  {
    Long key=Long.valueOf(link.getIid());
    _links.put(key,link);
  }

  /**
   * Get all the managed links, sorted by IID.
   * @return a list of links.
   */
  public List<LbiLink> getLinks()
  {
    List<LbiLink> ret=new ArrayList<LbiLink>();
    for(Long iid : getLinkIids())
    {
      ret.add(_links.get(iid));
    }
    return ret;
  }

  /**
   * Get the IIDs of the managed links.
   * @return A sorted list of link IIDs.
   */
  public List<Long> getLinkIids()
  {
    List<Long> ret=new ArrayList<Long>(_links.keySet());
    Collections.sort(ret);
    return ret;
  }

  /**
   * Get a link using its identifier.
   * @param iid Link identifier.
   * @return A link or <code>null</code> if not found.
   */
  public LbiLink getLinkByIid(long iid)
  {
    return _links.get(Long.valueOf(iid));
  }

  /**
   * Add a weenie.
   * @param weenie Weenie to add.
   */
  public void addWeenie(Weenie weenie)
  {
    Long key=Long.valueOf(weenie.getIid());
    _weenies.put(key,weenie);
  }

  /**
   * Get all the managed weenies, sorted by IID.
   * @return a list of weenies.
   */
  public List<Weenie> getWeenies()
  {
    List<Weenie> ret=new ArrayList<Weenie>();
    for(Long iid : getWeenieIids())
    {
      ret.add(_weenies.get(iid));
    }
    return ret;
  }

  /**
   * Get the IIDs of the managed weenies.
   * @return A sorted list of weenie IIDs.
   */
  public List<Long> getWeenieIids()
  {
    List<Long> ret=new ArrayList<Long>(_weenies.keySet());
    Collections.sort(ret);
    return ret;
  }

  /**
   * Get a weenie using its identifier.
   * @param iid Weenie identifier.
   * @return A weenie or <code>null</code> if not found.
   */
  public Weenie getWeenieByIid(long iid)
  {
    return _weenies.get(Long.valueOf(iid));
  }
}
