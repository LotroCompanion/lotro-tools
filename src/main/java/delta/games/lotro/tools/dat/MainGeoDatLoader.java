package delta.games.lotro.tools.dat;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.maps.MainDatDungeonsLoader;
import delta.games.lotro.tools.dat.maps.MainDatGeoAreasLoader;
import delta.games.lotro.tools.dat.maps.MapsDataLoader;
import delta.games.lotro.tools.dat.maps.landblocks.MainLandblocksBuilder;
import delta.games.lotro.tools.dat.utils.VersionFinder;

/**
 * Global procedure to load geographic data from DAT files.
 * @author DAM
 */
public class MainGeoDatLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainGeoDatLoader.class);

  private static final File ROOT_MAPS_DIR=new File("../lotro-maps-db");
  private static final File CATEGORIES_DIR=new File(ROOT_MAPS_DIR,"categories");
  private static final File INDEXES_DIR=new File(ROOT_MAPS_DIR,"indexes");
  private static final File MAPS_DIR=new File(ROOT_MAPS_DIR,"maps");
  private static final File MARKERS_DIR=new File(ROOT_MAPS_DIR,"markers");
  private static final File LINKS=new File(ROOT_MAPS_DIR,"links.xml");
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainGeoDatLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void doIt()
  {
    cleanup();
    load();
  }

  private void load()
  {
    // Landblocks
    new MainLandblocksBuilder(_facade).doIt();
    // Dungeons
    MainDatDungeonsLoader dungeonsLoader=new MainDatDungeonsLoader(_facade);
    dungeonsLoader.doIt();
    // Geographics areas
    new MainDatGeoAreasLoader(_facade).doIt();
    // Load dungeon positions
    dungeonsLoader.loadPositions();
    // Maps data (basemaps, markers)
    new MapsDataLoader(_facade).doIt();
  }

  private void cleanup()
  {
    // Maps
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
    deleteDirectory(CATEGORIES_DIR,f);
    deleteDirectory(INDEXES_DIR);
    deleteDirectory(MAPS_DIR);
    deleteDirectory(MARKERS_DIR);
    deleteFile(LINKS);
    deleteFile(GeneratedFiles.PARCHMENT_MAPS);
    deleteFile(GeneratedFiles.RESOURCES_MAPS);
    // Dungeons
    deleteFile(GeneratedFiles.DUNGEONS);
    // Areas
    deleteFile(GeneratedFiles.GEO_AREAS);
    deleteFile(GeneratedFiles.AREA_ICONS);
    // Landblocks
    deleteFile(GeneratedFiles.LANDBLOCKS);
  }

  private void deleteFile(File toDelete)
  {
    if (toDelete.exists())
    {
      boolean ok=toDelete.delete();
      if (!ok)
      {
        LOGGER.warn("Could not delete file: "+toDelete);
      }
    }
  }

  private void deleteDirectory(File toDelete)
  {
    deleteDirectory(toDelete,null);
  }

  private void deleteDirectory(File toDelete, FileFilter filter)
  {
    FilesDeleter deleter=new FilesDeleter(toDelete,filter,true);
    deleter.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    VersionFinder.initVersion(args);
    DataFacade facade=new DataFacade();
    new MainGeoDatLoader(facade).doIt();
    facade.dispose();
  }
}
