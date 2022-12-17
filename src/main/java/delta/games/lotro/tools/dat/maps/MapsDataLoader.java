package delta.games.lotro.tools.dat.maps;

import java.util.List;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.geo.AchievableGeoData;
import delta.games.lotro.dat.data.geo.AchievableGeoDataItem;
import delta.games.lotro.dat.data.geo.ContentLayerGeoData;
import delta.games.lotro.dat.data.geo.DidGeoData;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.categories.CategoriesManager;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;
import delta.games.lotro.tools.dat.maps.landblocks.LandblockGeneratorsAnalyzer;
import delta.games.lotro.tools.dat.maps.landblocks.LandblockInfoLoader;

/**
 * Loader for maps data.
 * @author DAM
 */
public class MapsDataLoader
{
  private DataFacade _facade;
  private MapsDataManager _mapsDataMgr;
  private MarkersLoadingUtils _markerUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsDataLoader(DataFacade facade)
  {
    _facade=facade;
    _mapsDataMgr=new MapsDataManager(facade);
  }

  /**
   * Load maps data.
   */
  public void doIt()
  {
    // Categories
    MapsManager mapsManager=_mapsDataMgr.getMapsManager();
    initCategories(mapsManager.getCategories());
    _markerUtils=new MarkersLoadingUtils(_facade,_mapsDataMgr);
    // Parchment maps
    MapsSystemLoader mapsSystemLoader=new MapsSystemLoader(_facade,mapsManager);
    mapsSystemLoader.doIt();
    // Map notes
    {
      long now1=System.currentTimeMillis();
      System.out.println("Loading map notes...");
      MapNotesLoader mapNotesLoader=new MapNotesLoader(_facade,_markerUtils);
      mapNotesLoader.doIt();
      long now2=System.currentTimeMillis();
      System.out.println("Map notes took: "+(now2-now1)+"ms");
    }
    // Quest map notes
    {
      long now1=System.currentTimeMillis();
      System.out.println("Loading quest map notes...");
      QuestMapNotesLoader questMapNotesLoader=new QuestMapNotesLoader(_facade,_markerUtils);
      questMapNotesLoader.doIt();
      long now2=System.currentTimeMillis();
      System.out.println("Quest map notes took: "+(now2-now1)+"ms");
    }
    // Quest Event Target Locations
    if (Context.isLive())
    {
      long now1=System.currentTimeMillis();
      System.out.println("Loading quest event target locations...");
      GeoData data=QuestEventTargetLocationLoader.loadGeoData(_facade);
      loadPositions(data);
      long now2=System.currentTimeMillis();
      System.out.println("QETL took: "+(now2-now1)+"ms");
    }
    // Landblock analyser
    {
      System.out.println("Loading landblock locations...");
      long now1=System.currentTimeMillis();
      analyzeLandblocks();
      long now2=System.currentTimeMillis();
      System.out.println("Landblocks took: "+(now2-now1)+"ms");
    }
    // Prune categories
    MainCategoriesPruner pruner=new MainCategoriesPruner(_mapsDataMgr.getMapsManager());
    pruner.doIt();

    // Save markers
    _mapsDataMgr.write();
  }

  private void initCategories(CategoriesManager categoriesManager)
  {
    MapCategoriesBuilder builder=new MapCategoriesBuilder(_facade);
    builder.doIt(categoriesManager);
    categoriesManager.save();
  }

  private void analyzeLandblocks()
  {
    LandblockInfoLoader lbiLoader=new LandblockInfoLoader(_facade);
    LandblockGeneratorsAnalyzer analyzer=new LandblockGeneratorsAnalyzer(_facade,_markerUtils);
    boolean isLive=Context.isLive();
    int[] regions=isLive?new int[]{1,2,3,4,14}:new int[]{1};
    for(int region : regions)
    {
      System.out.println("Region "+region);
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        //System.out.println("X="+blockX);
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          LandBlockInfo lbi=lbiLoader.loadLandblockInfo(region,blockX,blockY);
          if (lbi!=null)
          {
            analyzer.handleLandblock(lbi);
          }
        }
      }
    }
  }

  private void loadPositions(GeoData data)
  {
    // Word geo data
    System.out.println("\tWorld geo data");
    ContentLayerGeoData worldGeoData=data.getWorldGeoData();
    loadPositions(worldGeoData);
    // Content layers geo data
    System.out.println("\tContent layers geo data");
    List<Integer> contentLayers=data.getContentLayers();
    for(Integer contentLayer : contentLayers)
    {
      ContentLayerGeoData contentLayerGeoData=data.getContentLayerGeoData(contentLayer.intValue());
      loadPositions(contentLayerGeoData);
    }
    // Achievables geo data
    System.out.println("\tAchievables geo data");
    List<AchievableGeoData> achievableGeoDatas=data.getAllAchievableGeoData();
    for(AchievableGeoData achievableGeoData : achievableGeoDatas)
    {
      loadAchievableGeoData(achievableGeoData);
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
        _markerUtils.buildMarker(position,dataId,layerId);
      }
    }
  }

  private void loadAchievableGeoData(AchievableGeoData achievableGeoData)
  {
    for(AchievableGeoDataItem dataItem : achievableGeoData.getAllItems())
    {
      int did=dataItem.getDid();
      if (did==0)
      {
        continue;
      }
      DataIdentification dataId=DataIdentificationTools.identify(_facade,did);
      DatPosition position=dataItem.getPosition();
      _markerUtils.buildMarker(position,dataId,0); // Assume world marker!
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
