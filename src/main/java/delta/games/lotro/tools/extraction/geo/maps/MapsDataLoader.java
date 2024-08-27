package delta.games.lotro.tools.extraction.geo.maps;

import java.io.File;
import java.io.FileFilter;

import delta.common.utils.NumericTools;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.geo.markers.MarkersDataLoader;
import delta.games.lotro.tools.extraction.utils.CleanupUtils;

/**
 * Loader for maps data.
 * @author DAM
 */
public class MapsDataLoader
{
  private DataFacade _facade;
  private MapsManager _mapsManager;
  private MarkersDataLoader _markersLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsDataLoader(DataFacade facade)
  {
    _facade=facade;
    File rootDir=MapConstants.getRootDir();
    _mapsManager=new MapsManager(rootDir,false);
    _markersLoader=new MarkersDataLoader(facade,_mapsManager);
  }

  /**
   * Cleanup managed data.
   */
  public void cleanup()
  {
    FileFilter f=new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        String name=pathname.getName();
        if (name.endsWith(".png"))
        {
          int id=NumericTools.parseInt(name.substring(0,name.length()-4),0);
          if (id>=70)
          {
            return false;
          }
        }
        return true;
      }
    };
    CleanupUtils.deleteDirectory(_mapsManager.getCategoriesDir(),f);
    CleanupUtils.deleteDirectory(_mapsManager.getIndexesDir());
    CleanupUtils.deleteDirectory(_mapsManager.getMapsDir());
    CleanupUtils.deleteDirectory(_mapsManager.getMarkersDir());
    CleanupUtils.deleteFile(_mapsManager.getLinksFile());
    CleanupUtils.deleteFile(GeneratedFiles.PARCHMENT_MAPS);
    CleanupUtils.deleteFile(GeneratedFiles.RESOURCES_MAPS);
  }

  /**
   * Load maps data.
   */
  public void doIt()
  {
    // Maps
    loadMaps(_mapsManager);
    // Markers
    _markersLoader.doIt();
    // Save links
    _mapsManager.getLinksManager().write();
  }

  private void loadMaps(MapsManager mapsManager)
  {
    // Basemaps loader
    GeoreferencedBasemapsLoader basemapsLoader=new GeoreferencedBasemapsLoader(_facade,mapsManager);
    // Parchment maps
    MapsSystemLoader mapsSystemLoader=new MapsSystemLoader(_facade,mapsManager,basemapsLoader);
    mapsSystemLoader.doIt();
    // Dungeon maps
    DungeonMapsLoader dungeonMapsLoader=new DungeonMapsLoader(_facade,basemapsLoader);
    dungeonMapsLoader.doIt();
    // Save data
    basemapsLoader.write();
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
