package delta.games.lotro.tools.lore.deeds.geo.migration;

/**
 * Old marker specification.
 * @author DAM
 */
public class OldMarkerSpec
{
  private int _deedId;
  private int _pointId;
  private String _mapKey;
  private float _latitude;
  private float _longitude;

  /**
   * Constructor.
   * @param deedId Deed identifier.
   * @param pointId Old point identifier.
   * @param mapKey Old map key.
   * @param latitude Latitude.
   * @param longitude Longitude.
   */
  public OldMarkerSpec(int deedId, int pointId, String mapKey, float latitude, float longitude)
  {
    _deedId=deedId;
    _pointId=pointId;
    _mapKey=mapKey;
    _latitude=latitude;
    _longitude=longitude;
  }

  /**
   * Get the deed identifier.
   * @return the deed identifier.
   */
  public int getDeedId()
  {
    return _deedId;
  }

  /**
   * Get the point identifier.
   * @return the point identifier.
   */
  public int getPointId()
  {
    return _pointId;
  }

  /**
   * Get the map key.
   * @return the map key.
   */
  public String getMapKey()
  {
    return _mapKey;
  }

  /**
   * Get the latitude.
   * @return the latitude.
   */
  public float getLatitude()
  {
    return _latitude;
  }

  /**
   * Get the longitude.
   * @return the longitude.
   */
  public float getLongitude()
  {
    return _longitude;
  }

  /**
   * Set the position.
   * @param longitude Longitude to set.
   * @param latitude Latitude to set.
   */
  public void setPosition(float longitude, float latitude)
  {
    _longitude=longitude;
    _latitude=latitude;
  }

  @Override
  public String toString()
  {
    return "Deed: "+_deedId+", point: "+_pointId+", map: "+_mapKey+", lat: "+_latitude+", lon: "+_longitude;
  }
}
