package delta.games.lotro.tools.dat;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.dat.maps.MainDatDungeonsLoader;
import delta.games.lotro.tools.dat.maps.MainDatGeoAreasLoader;
import delta.games.lotro.tools.dat.maps.MapConstants;
import delta.games.lotro.tools.dat.maps.MapsDataLoader;
import delta.games.lotro.tools.dat.maps.landblocks.MainLandblocksBuilder;

/**
 * Global procedure to load geographic data from DAT files.
 * @author DAM
 */
public class MainGeoDatLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainGeoDatLoader.class);

  private static File ROOT_MAPS_DIR;
  private static File CATEGORIES_DIR;
  private static File INDEXES_DIR;
  private static File MAPS_DIR;
  private static File MARKERS_DIR;
  private static File LINKS;
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainGeoDatLoader(DataFacade facade)
  {
    _facade=facade;
    ROOT_MAPS_DIR=MapConstants.getRootDir();
    CATEGORIES_DIR=new File(ROOT_MAPS_DIR,"categories");
    INDEXES_DIR=new File(ROOT_MAPS_DIR,"indexes");
    MAPS_DIR=new File(ROOT_MAPS_DIR,"maps");
    MARKERS_DIR=new File(ROOT_MAPS_DIR,"markers");
    LINKS=new File(ROOT_MAPS_DIR,"links.xml");
  }

  private void doIt()
  {
    cleanup();
    load();
  }

  private void load()
  {
    // Land-blocks
    new MainLandblocksBuilder(_facade).doIt();
    // Dungeons
    MainDatDungeonsLoader dungeonsLoader=new MainDatDungeonsLoader(_facade);
    dungeonsLoader.doIt();
    // Geographic areas
    new MainDatGeoAreasLoader(_facade).doIt();
    // Load dungeon positions
    dungeonsLoader.loadPositions();
    // Maps data (base-maps, markers)
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
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null file!");
      return;
    }
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
    if (toDelete==null)
    {
      LOGGER.warn("Cannot delete null directory!");
      return;
    }
    FilesDeleter deleter=new FilesDeleter(toDelete,filter,true);
    deleter.doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=new DataFacade();
    new MainGeoDatLoader(facade).doIt();
    facade.dispose();
  }
}
