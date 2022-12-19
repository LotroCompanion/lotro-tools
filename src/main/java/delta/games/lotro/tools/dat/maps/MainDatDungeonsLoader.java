package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;

/**
 * Get private encounters (instances) from DAT files.
 * @author DAM
 */
public class MainDatDungeonsLoader
{
  private DataFacade _facade;
  private DungeonLoader _loader;
  private GeoreferencedBasemapsManager _basemapsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDungeonsLoader(DataFacade facade)
  {
    _facade=facade;
    File rootDir=MapConstants.getMapsDir();
    _basemapsManager=new GeoreferencedBasemapsManager(rootDir);
    _loader=new DungeonLoader(facade,_basemapsManager);
  }

  /**
   * Load all dungeons.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==691)
        {
          _loader.getDungeon(id);
        }
      }
    }
    _loader.save();
    _basemapsManager.write();
  }

  /**
   * Load positions.
   */
  public void loadPositions()
  {
    // TODO Handle dungeon positions for SoA Book 11, if possible
    boolean live=Context.isLive();
    if (live)
    {
      _loader.loadPositions();
      _loader.save();
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatDungeonsLoader(facade).doIt();
    facade.dispose();
  }
}
