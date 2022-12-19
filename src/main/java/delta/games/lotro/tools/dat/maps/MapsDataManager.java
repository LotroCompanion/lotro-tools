package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.categories.CategoriesConstants;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.maps.data.markers.LandblockMarkersManager;
import delta.games.lotro.maps.data.markers.index.MarkersIndex;
import delta.games.lotro.maps.data.markers.index.MarkersIndexesManager;
import delta.games.lotro.tools.dat.maps.classification.Classification;
import delta.games.lotro.tools.dat.maps.classification.CropClassification;
import delta.games.lotro.tools.dat.maps.classification.MarkerClassifier;
import delta.games.lotro.tools.dat.maps.classification.MonsterClassification;
import delta.games.lotro.tools.dat.maps.classification.ResourceClassification;

/**
 * Manager for all maps data.
 * @author DAM
 */
public class MapsDataManager
{
  private MapsManager _mapsManager;
  private MarkersIndexesManager _index;
  private Map<Integer,MarkersStore> _didStore;
  private Map<Integer,MarkersStore> _clStore;
  private MarkerClassifier _classifier;
  private ResourcesMapsBuilder _resourcesMapsBuilder;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsDataManager(DataFacade facade)
  {
    File rootDir=MapConstants.getRootDir();
    _mapsManager=new MapsManager(rootDir);
    _index=new MarkersIndexesManager(rootDir);
    _didStore=new HashMap<Integer,MarkersStore>();
    _clStore=new HashMap<Integer,MarkersStore>();
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
    // Register in parent zone index
    marker.setParentZoneId(parentZoneId);
    registerDidMarker(parentZoneId,marker);
    // Classify DID
    int did=marker.getDid();
    Classification classification=_classifier.classifyDid(did);
    if (classification instanceof ResourceClassification)
    {
      registerDidMarker(did,marker);
      ResourceClassification resourceClassification=(ResourceClassification)classification;
      CraftingLevel level=resourceClassification.getCraftingLevel();
      _resourcesMapsBuilder.registerResource(did,level,parentZoneId);
      if (classification instanceof CropClassification)
      {
        marker.setCategoryCode(CategoriesConstants.CROP);
      }
      else
      {
        marker.setCategoryCode(CategoriesConstants.RESOURCE_NODE);
      }
    }
    if (classification instanceof MonsterClassification)
    {
      boolean isCritter=((MonsterClassification)classification).isCritter();
      if (isCritter)
      {
        marker.setCategoryCode(CategoriesConstants.CRITTER);
      }
    }
  }

  /**
   * Register a marker.
   * @param did Associated DID (dungeon, area, NPC...).
   * @param marker Marker to add.
   */
  public void registerDidMarker(int did, Marker marker)
  {
    Integer key=Integer.valueOf(did);
    MarkersStore store=_didStore.get(key);
    if (store==null)
    {
      store=new MarkersStore(did);
      _didStore.put(key,store);
    }
    store.addMarker(marker);
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
    Integer key=Integer.valueOf(layerId);
    MarkersStore store=_clStore.get(key);
    if (store==null)
    {
      store=new MarkersStore(layerId);
      _clStore.put(key,store);
    }
    store.addMarker(marker);
  }

  private void buildIndexes()
  {
    // DID
    for(Integer did : _didStore.keySet())
    {
      MarkersStore store=_didStore.get(did);
      MarkersIndex index=new MarkersIndex(did.intValue(),store.getMarkers());
      _index.setDidIndex(index);
    }
    // CL
    for(Integer contentLayerId : _clStore.keySet())
    {
      MarkersStore store=_clStore.get(contentLayerId);
      MarkersIndex index=new MarkersIndex(contentLayerId.intValue(),store.getMarkers());
      _index.setContentLayerIndex(index);
    }
  }

  private void cleanupMarkers()
  {
    MarkerDuplicatesRemover remover=new MarkerDuplicatesRemover();
    GlobalMarkersManager markersMgr=_mapsManager.getMarkersManager();
    for(LandblockMarkersManager landblockMarkersMgr : markersMgr.getAllManagers())
    {
      remover.handleLandblock(landblockMarkersMgr);
    }
    int nbRemovedMarkers=remover.getRemovedMarkers();
    int nbTotalMarkers=remover.getTotalMarkers();
    System.out.println("Removed "+nbRemovedMarkers+" markers / "+nbTotalMarkers);
  }

  /**
   * Write data to files.
   */
  public void write()
  {
    cleanupMarkers();
    _mapsManager.getBasemapsManager().write();
    _mapsManager.getMarkersManager().write();
    buildIndexes();
    _index.writeIndexes();
    _mapsManager.getLinksManager().write();
    _resourcesMapsBuilder.write();
  }
}
