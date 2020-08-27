package delta.games.lotro.tools.dat.maps;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.geo.ContentLayerGeoData;
import delta.games.lotro.dat.data.geo.DidGeoData;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

/**
 * Loader for maps data.
 * @author DAM
 */
public class MapsDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapsDataLoader.class);

  private DataFacade _facade;
  private MapsDataManager _mapsDataMgr;
  // Loaders
  private DungeonLoader _dungeonLoader;
  private GeoAreasLoader _geoAreasLoader;
  private ParentZoneIndex _parentZonesIndex;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsDataLoader(DataFacade facade)
  {
    _facade=facade;
    _mapsDataMgr=new MapsDataManager();
    _dungeonLoader=new DungeonLoader(facade);
    _geoAreasLoader=new GeoAreasLoader(facade);
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    _parentZonesIndex=new ParentZoneIndex(parentZoneLoader);
  }

  /**
   * Load maps data.
   */
  public void doIt()
  {
    // Categories
    MapsManager mapsManager=_mapsDataMgr.getMapsManager();
    initCategories(mapsManager);
    MarkersLoadingUtils markersUtils=new MarkersLoadingUtils(_facade,_mapsDataMgr,_dungeonLoader,_geoAreasLoader);
    // Map notes
    MapNotesLoader mapNotesLoader=new MapNotesLoader(_facade,markersUtils);
    mapNotesLoader.doIt();
    // Quest Event Target Locations
    GeoData data=QuestEventTargetLocationLoader.loadGeoData(_facade);
    loadPositions(data);
    // Quest map notes
    QuestMapNotesLoader questMapNotesLoader=new QuestMapNotesLoader(_facade,markersUtils);
    questMapNotesLoader.doIt();
    // Save markers
    _mapsDataMgr.write();
  }

  private void initCategories(MapsManager mapsManager)
  {
    MapCategoriesBuilder builder=new MapCategoriesBuilder(_facade);
    builder.doIt(mapsManager);
    mapsManager.saveCategories();
  }

  private void loadPositions(GeoData data)
  {
    ContentLayerGeoData worldGeoData=data.getWorldGeoData();
    loadPositions(worldGeoData);
    List<Integer> contentLayers=data.getContentLayers();
    for(Integer contentLayer : contentLayers)
    {
      ContentLayerGeoData contentLayerGeoData=data.getContentLayerGeoData(contentLayer.intValue());
      loadPositions(contentLayerGeoData);
    }
  }

  private void loadPositions(ContentLayerGeoData data)
  {
    int layerId=data.getContentLayer();
    for(Integer did : data.getDids())
    {
      DataIdentification dataId=DataIdentificationTools.identify(_facade,did.intValue());
      if (!MarkerUtils.accept(dataId))
      {
        continue;
      }
      DidGeoData didData=data.getGeoData(did.intValue());
      List<DatPosition> positions=didData.getPositions();
      for(DatPosition position : positions)
      {
        Marker marker=MarkerUtils.buildMarker(position,dataId);
        if (marker==null)
        {
          continue;
        }
        int region=position.getRegion();
        if ((region<1) || (region>4))
        {
          LOGGER.warn("Found unsupported region: "+region);
          continue;
        }
        ParentZoneLandblockData parentData=_parentZonesIndex.getLandblockData(position.getRegion(),position.getBlockX(),position.getBlockY());
        if (parentData==null)
        {
          LOGGER.warn("No parent data for: "+position);
          continue;
        }
        _mapsDataMgr.registerMarker(marker);
        // Indexs
        // - parent zone
        int cell=position.getCell();
        Integer parentArea=(parentData!=null)?parentData.getParentData(cell):null;
        if (parentArea!=null)
        {
          _mapsDataMgr.registerDidMarker(parentArea.intValue(),marker);
        }
        // - content layer
        _mapsDataMgr.registerContentLayerMarker(layerId,marker);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapsDataLoader loader=new MapsDataLoader(facade);
    loader.doIt();
  }
}
