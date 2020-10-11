package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.index.MarkersIndex;
import delta.games.lotro.maps.data.markers.index.MarkersIndexesManager;
import delta.games.lotro.tools.dat.maps.classification.Classification;
import delta.games.lotro.tools.dat.maps.classification.MarkerClassifier;
import delta.games.lotro.tools.dat.maps.classification.ResourceClassification;

/**
 * Manager for all maps data.
 * @author DAM
 */
public class MapsDataManager
{
  private MapsManager _mapsManager;
  private MarkersIndexesManager _index;
  private MarkerClassifier _classifier;
  private ResourcesMapsBuilder _resourcesMapsBuilder;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsDataManager(DataFacade facade)
  {
    File rootDir=new File("../lotro-maps-db");
    _mapsManager=new MapsManager(rootDir);
    _index=new MarkersIndexesManager(rootDir);
    _classifier=new MarkerClassifier(facade);
    _resourcesMapsBuilder=new ResourcesMapsBuilder();
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
   * @param parentZoneId Parent zone id.
   */
  public void registerMarker(Marker marker, int region, int blockX, int blockY, int parentZoneId)
  {
    _mapsManager.getMarkersManager().registerMarker(marker,region,blockX,blockY);
    // Register in parent zone inde
    registerDidMarker(parentZoneId,marker);
    int did=marker.getDid();
    Classification classification=_classifier.classifyDid(did);
    if (classification instanceof ResourceClassification)
    {
      registerDidMarker(did,marker);
      ResourceClassification resourceClassification=(ResourceClassification)classification;
      CraftingLevel level=resourceClassification.getCraftingLevel();
      _resourcesMapsBuilder.registerResource(did,level,parentZoneId);
    }
  }

  /**
   * Register a marker.
   * @param did Associated DID (dungeon, area, NPC...).
   * @param marker Marker to add.
   */
  public void registerDidMarker(int did, Marker marker)
  {
    MarkersIndex index=_index.getDidIndex(did);
    index.addMarker(marker.getId());
  }

  /**
   * Register a content layer marker.
   * @param layerId Identifier of the layer to use.
   * @param marker Marker to register.
   */
  public void registerContentLayerMarker(int layerId, Marker marker)
  {
    // Merge layer 1 "InstanceZero" with world
    if (layerId==1)
    {
      layerId=0;
    }
    MarkersIndex index=_index.getContentLayerIndex(layerId);
    index.addMarker(marker.getId());
  }

  /**
   * Write data to files.
   */
  public void write()
  {
    _mapsManager.getBasemapsManager().write();
    _mapsManager.getMarkersManager().write();
    _index.writeIndexes();
    _mapsManager.getLinksManager().write();
    _resourcesMapsBuilder.write();
  }
}
