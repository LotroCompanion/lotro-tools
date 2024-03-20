package delta.games.lotro.tools.lore.deeds.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.geo.BlockReferenceComparator;
import delta.games.lotro.lore.quests.geo.AchievableGeoPoint;

/**
 * Manages the geo points for a single Achievable.
 * It helps in the process of building map descriptions for an achievable.
 * @author DAM
 */
public class AchievableGeoPointsManager
{
  // Points, sorted by map
  private Map<Integer,List<AchievableGeoPoint>> _pointsByMap;
  // Points, sorted by blocks
  private Map<BlockReference,List<AchievableGeoPoint>> _pointsByBlock;

  // TODO Keep link point <-> objective condition

  /**
   * Constructor.
   */
  public AchievableGeoPointsManager()
  {
    _pointsByMap=new HashMap<Integer,List<AchievableGeoPoint>>();
    _pointsByBlock=new HashMap<BlockReference,List<AchievableGeoPoint>>();
  }

  /**
   * Add a point for a known basemap.
   * @param mapId Basemap identifier.
   * @param point Point to add.
   */
  public void addPointForMap(int mapId, AchievableGeoPoint point)
  {
    Integer key=Integer.valueOf(mapId);
    List<AchievableGeoPoint> list=_pointsByMap.get(key);
    if (list==null)
    {
      list=new ArrayList<AchievableGeoPoint>();
      _pointsByMap.put(key,list);
    }
    list.add(point);
  }

  /**
   * Get the known basemap identifiers.
   * @return A sorted list of basemap identifiers.
   */
  public List<Integer> getMapIds()
  {
    List<Integer> ret=new ArrayList<Integer>(_pointsByMap.keySet());
    Collections.sort(ret);
    return ret;
  }

  /**
   * Get a list of all points for the given basemap identifier.
   * @param mapId Basemap identifier.
   * @return A possibly empty but never <code>null</code> list of points.
   */
  public List<AchievableGeoPoint> getPointsForMap(int mapId)
  {
    List<AchievableGeoPoint> points=_pointsByMap.get(Integer.valueOf(mapId));
    if (points==null)
    {
      points=new ArrayList<AchievableGeoPoint>();
    }
    return points;
  }

  /**
   * Add a point for a land block.
   * @param block Block identifier.
   * @param point Point to add.
   */
  public void addPointForBlock(BlockReference block, AchievableGeoPoint point)
  {
    List<AchievableGeoPoint> list=_pointsByBlock.get(block);
    if (list==null)
    {
      list=new ArrayList<AchievableGeoPoint>();
      _pointsByBlock.put(block,list);
    }
    list.add(point);

  }

  /**
   * Get the known land blocks.
   * @return A sorted list of land block identifiers.
   */
  public List<BlockReference> getBlocks()
  {
    List<BlockReference> ret=new ArrayList<BlockReference>(_pointsByBlock.keySet());
    Collections.sort(ret,new BlockReferenceComparator());
    return ret;
  }

  /**
   * Get a list of all points for the given land block.
   * @param block Block identifier.
   * @return A possibly empty but never <code>null</code> list of points.
   */
  public List<AchievableGeoPoint> getPointsForBlock(BlockReference block)
  {
    List<AchievableGeoPoint> points=_pointsByBlock.get(block);
    if (points==null)
    {
      points=new ArrayList<AchievableGeoPoint>();
    }
    return points;
  }
}
