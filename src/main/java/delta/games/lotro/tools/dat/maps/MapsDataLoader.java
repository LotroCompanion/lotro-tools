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
  }

  /**
   * Load maps data.
   */
  public void doIt()
  {
    // Categories
    MapsManager mapsManager=_mapsDataMgr.getMapsManager();
    initCategories(mapsManager);
    // Map notes
    MapNotesLoader mapNotesLoader=new MapNotesLoader(_facade,_mapsDataMgr,_dungeonLoader,_geoAreasLoader);
    mapNotesLoader.doIt();
    // Quest Event Target Locations
    GeoData data=QuestEventTargetLocationLoader.loadGeoData(_facade);
    loadPositions(data);
    // Save markers
    _mapsDataMgr.writeMarkers();
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
        if (marker!=null)
        {
          int region=position.getRegion();
          if ((region>=1) && (region<=4))
          {
            if (layerId==0)
            {
              _mapsDataMgr.registerWorldMarker(region,marker);
            }
            else
            {
              _mapsDataMgr.registerContentLayerMarker(layerId,marker);
            }
          }
          else
          {
            LOGGER.warn("Found unsupported region: "+region);
          }
        }
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
