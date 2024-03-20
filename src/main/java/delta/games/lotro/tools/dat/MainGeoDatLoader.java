package delta.games.lotro.tools.dat;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.dat.maps.MapConstants;
import delta.games.lotro.tools.dat.maps.MapsDataLoader;
import delta.games.lotro.tools.dat.maps.landblocks.MainLandblocksBuilder;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;

/**
 * Global procedure to load geographic data from DAT files.
 * @author DAM
 */
public class MainGeoDatLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainGeoDatLoader.class);

  private File _rootMapsDir;
  private File _categoriesDir;
  private File _indexesDir;
  private File _mapsDirs;
  private File _markersDir;
  private File _linksFile;
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainGeoDatLoader(DataFacade facade)
  {
    _facade=facade;
    _rootMapsDir=MapConstants.getRootDir();
    _categoriesDir=new File(_rootMapsDir,"categories");
    _indexesDir=new File(_rootMapsDir,"indexes");
    _mapsDirs=new File(_rootMapsDir,"maps");
    _markersDir=new File(_rootMapsDir,"markers");
    _linksFile=new File(_rootMapsDir,"links.xml");
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
    deleteDirectory(_categoriesDir,f);
    deleteDirectory(_indexesDir);
    deleteDirectory(_mapsDirs);
    deleteDirectory(_markersDir);
    deleteFile(_linksFile);
    deleteFile(GeneratedFiles.PARCHMENT_MAPS);
    deleteFile(GeneratedFiles.RESOURCES_MAPS);
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
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    new MainGeoDatLoader(facade).doIt();
    facade.dispose();
  }
}
