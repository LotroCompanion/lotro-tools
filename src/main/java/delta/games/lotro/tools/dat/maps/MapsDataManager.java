package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.index.DIDMarkersIndex;
import delta.games.lotro.maps.data.markers.index.MarkersIndexesManager;

/**
 * Manager for all maps data.
 * @author DAM
 */
public class MapsDataManager
{
  private MapsManager _mapsManager;
  private MarkersIndexesManager _index;

  /**
   * Constructor.
   */
  public MapsDataManager()
  {
    _mapsManager=new MapsManager(new File("../lotro-maps-db"));
    _index=new MarkersIndexesManager();
  }

  /**
   * Get the maps manager.
   * @return the maps manager.
   */
  public MapsManager getMapsManager()
  {
    return _mapsManager;
  }

  /**
   * Register a marker.
   * @param did Associated DID (dungeon, area, NPC...).
   * @param marker Marker to add.
   */
  public void registerMarker(int did, Marker marker)
  {
    DIDMarkersIndex index=_index.getDidIndex(did,true);
    index.addMarker(marker);
  }
}
