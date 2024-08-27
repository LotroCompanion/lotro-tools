package delta.games.lotro.tools.extraction.geo;

import java.util.Locale;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.geo.areas.MainDatGeoAreasLoader;
import delta.games.lotro.tools.extraction.geo.dungeons.MainDatDungeonsLoader;
import delta.games.lotro.tools.extraction.geo.landblocks.MainLandblocksBuilder;
import delta.games.lotro.tools.extraction.geo.maps.MapsDataLoader;
import delta.games.lotro.tools.extraction.utils.CleanupUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Global procedure to load geographic data from DAT files.
 * @author DAM
 */
public class MainGeoDatLoader
{
  private DataFacade _facade;
  private MapsDataLoader _mapsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainGeoDatLoader(DataFacade facade)
  {
    _facade=facade;
    _mapsLoader=new MapsDataLoader(facade);
  }

  private void doIt()
  {
    cleanup();
    load();
  }

  private void load()
  {
    // Geographic areas
    new MainDatGeoAreasLoader(_facade).doIt();
    // Dungeons
    MainDatDungeonsLoader dungeonsLoader=new MainDatDungeonsLoader(_facade);
    dungeonsLoader.doIt();
    dungeonsLoader.loadPositions();
    // Landmarks
    new MainDatLandmarksLoader(_facade).doIt();
    // Land-blocks
    new MainLandblocksBuilder(_facade).doIt();
    // Maps data (base-maps, markers)
    _mapsLoader.doIt();
  }

  private void cleanup()
  {
    // Maps
    _mapsLoader.cleanup();
    // Landblocks
    CleanupUtils.deleteFile(GeneratedFiles.LANDBLOCKS);
    // Landmarks
    CleanupUtils.deleteFile(GeneratedFiles.LANDMARKS);
    // Dungeons
    CleanupUtils.deleteFile(GeneratedFiles.DUNGEONS);
    // Areas
    CleanupUtils.deleteFile(GeneratedFiles.GEO_AREAS);
    CleanupUtils.deleteDirectory(GeneratedFiles.AREA_ICONS);
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
