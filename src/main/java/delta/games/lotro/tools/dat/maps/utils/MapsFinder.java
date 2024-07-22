package delta.games.lotro.tools.dat.maps.utils;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.lore.maps.AbstractMap;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.tools.dat.maps.GeoUtils;
import delta.games.lotro.tools.dat.maps.MapConstants;
import delta.games.lotro.tools.dat.maps.MapUtils;
import delta.games.lotro.tools.lore.maps.AdvancedMapsManager;
import delta.games.lotro.tools.lore.maps.DetailedMap;

/**
 * Finds map for points.
 * @author DAM
 */
public class MapsFinder
{
  private static final Logger LOGGER=Logger.getLogger(MapsFinder.class);

  private AdvancedMapsManager _mapsManager;
  private GeoreferencedBasemapsManager _basemapsManager;

  /**
   * Constructor.
   */
  public MapsFinder()
  {
    File rootDir=MapConstants.getRootDir();
    MapsManager mapsManager=new MapsManager(rootDir);
    _basemapsManager=mapsManager.getBasemapsManager();
    _mapsManager=new AdvancedMapsManager(_basemapsManager,ParchmentMapsManager.getInstance());
  }

  /**
   * Get the map for the given position.
   * @param position Position to use.
   * @return A map identifier or <code>null</code> if not found.
   */
  public Integer getMap(DatPosition position)
  {
    Integer mapId=getMapUsingParentZone(position);
    if (mapId!=null)
    {
      mapId=checkMap(mapId,position);
    }
    if (mapId==null)
    {
      float[] lonLat=position.getLonLat();
      DetailedMap map=_mapsManager.getBestMapForPoint(position.getRegion(),lonLat[0],lonLat[1]);
      if (map!=null)
      {
        int mapIdentifier=map.getIdentifier();
        mapId=Integer.valueOf(mapIdentifier);
      }
    }
    return mapId;
  }

  private Integer checkMap(Integer mapId, DatPosition position)
  {
    // Check bounds
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    GeoreferencedBasemap basemap=_basemapsManager.getMapById(mapId.intValue());
    GeoBox box=basemap.getBoundingBox();
    boolean isInBox=box.isInBox(new GeoPoint(lonLat[0],lonLat[1]));
    if (!isInBox)
    {
      LOGGER.warn("Point not in map box: "+position+", map="+basemap.getName());
      mapId=null;
    }
    return mapId;
  }

  private Integer getMapUsingParentZone(DatPosition position)
  {
    Integer ret=null;
    Integer parentZoneId=GeoUtils.getZoneID(position);
    if (parentZoneId!=null)
    {
      AbstractMap map=MapUtils.findMapForZone(parentZoneId.intValue());
      if (map!=null)
      {
        ret=Integer.valueOf(map.getIdentifier());
      }
      else
      {
        Area area=GeoAreasManager.getInstance().getAreaById(parentZoneId.intValue());
        LOGGER.warn("No map for zone: "+parentZoneId+" => "+area);
      }
    }
    else
    {
      LOGGER.warn("No parent zone ID for "+position);
    }
    return ret;
  }
}
