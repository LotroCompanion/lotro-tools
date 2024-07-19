package delta.games.lotro.tools.lore.maps;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;

/**
 * Detailed map.
 * It gathers a "georeferenced basemap" and a "parchment map".
 * @author DAM
 */
public class DetailedMap implements Identifiable
{
  private GeoreferencedBasemap _baseMap;
  private ParchmentMap _parchmentMap;

  /**
   * Constructor.
   * @param baseMap Georeferenced basemap.
   * @param parchmentMap Parchment map.
   */
  public DetailedMap(GeoreferencedBasemap baseMap, ParchmentMap parchmentMap)
  {
    _baseMap=baseMap;
    _parchmentMap=parchmentMap;
  }

  @Override
  public int getIdentifier()
  {
    return _parchmentMap.getIdentifier();
  }

  /**
   * Get the region.
   * @return A region code.
   */
  public int getRegion()
  {
    return _parchmentMap.getRegion();
  }

  /**
   * Get the georeferenced basemap.
   * @return the georeferenced basemap.
   */
  public GeoreferencedBasemap getBasemap()
  {
    return _baseMap;
  }

  /**
   * Indicates if this map contains the given point.
   * @param lon Longitude.
   * @param lat Latitude.
   * @return <code>true</code> if it does, <code>false</code> otherwise.
   */
  public boolean contains(float lon, float lat)
  {
    return _baseMap.getBoundingBox().isInBox(new GeoPoint(lon,lat));
  }
}
