package delta.games.lotro.tools.dat.maps.indexs;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DatPosition;

/**
 * Index of parent zones.
 * Gives the parent zone for positions.
 * @author DAM
 */
public class ParentZoneIndex
{
  private ParentZonesLoader _loader;
  private Map<String,ParentZoneLandblockData> _index;

  /**
   * Constructor.
   * @param loader Optional loader.
   */
  public ParentZoneIndex(ParentZonesLoader loader)
  {
    _loader=loader;
    _index=new HashMap<String,ParentZoneLandblockData>();
  }

  private String getKey(int region, int blockX, int blockY)
  {
    return region+"#"+blockX+"#"+blockY;
  }

  /**
   * Get the parent zone data for a single landblock.
   * @param region Region.
   * @param blockX Block X.
   * @param blockY Block Y.
   * @return A data storage or <code>null</code>.
   */
  public ParentZoneLandblockData getLandblockData(int region, int blockX, int blockY)
  {
    String key=getKey(region,blockX,blockY);
    ParentZoneLandblockData data=_index.get(key);
    if (data==null)
    {
      if (_loader!=null)
      {
        data=_loader.buildLandblockData(region,blockX,blockY);
        if (data!=null)
        {
          _index.put(key,data);
        }
      }
    }
    return data;
  }

  /**
   * Register a landblock data.
   * @param region Region.
   * @param blockX Block X.
   * @param blockY Block Y.
   * @param data Data to add.
   */
  public void registerLandblockData(int region, int blockX, int blockY, ParentZoneLandblockData data)
  {
    String key=getKey(region,blockX,blockY);
    _index.put(key,data);
  }

  /**
   * Get the parent zone (area or dungeon) for a position.
   * @param position Position to use.
   * @return A parent zone identifier or <code>null</code>.
   */
  public Integer getParentZone(DatPosition position)
  {
    int region=position.getRegion();
    int blockX=position.getBlockX();
    int blockY=position.getBlockY();
    ParentZoneLandblockData data=getLandblockData(region,blockX,blockY);
    Integer ret=null;
    if (data!=null)
    {
      ret=data.getParentData(position.getCell());
    }
    return ret;
  }
}
