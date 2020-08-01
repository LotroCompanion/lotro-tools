package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.maps.data.MapsManager;

/**
 * Loader for maps data.
 * @author DAM
 */
public class MapsDataLoader
{
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
  }

  private void initCategories(MapsManager mapsManager)
  {
    MapCategoriesBuilder builder=new MapCategoriesBuilder(_facade);
    builder.doIt(mapsManager);
    mapsManager.saveCategories();
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
