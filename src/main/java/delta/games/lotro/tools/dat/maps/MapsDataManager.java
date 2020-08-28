package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.index.MarkersIndex;
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
    File rootDir=new File("../lotro-maps-db");
    _mapsManager=new MapsManager(rootDir);
    _index=new MarkersIndexesManager(rootDir);
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
   * @param marker Marker to register.
   * @param region Region.
   * @param blockX Block X.
   * @param blockY Block Y.
   */
  public void registerMarker(Marker marker, int region, int blockX, int blockY)
  {
    _mapsManager.getMarkersManager().registerMarker(marker,region,blockX,blockY);
  }

  /**
   * Register a marker.
   * @param did Associated DID (dungeon, area, NPC...).
   * @param marker Marker to add.
   */
  public void registerDidMarker(int did, Marker marker)
  {
    MarkersIndex index=_index.getDidIndex(did,true);
    index.addMarker(marker.getId());
  }

  /**
   * Register a content layer marker.
   * @param layerId Identifier of the layer to use.
   * @param marker Marker to register.
   */
  public void registerContentLayerMarker(int layerId, Marker marker)
  {
    MarkersIndex index=_index.getContentLayerIndex(layerId,true);
    index.addMarker(marker.getId());
  }

  /**
   * Write data to files.
   */
  public void write()
  {
    _mapsManager.getMarkersManager().write();
    _index.writeIndexes();
  }
}
