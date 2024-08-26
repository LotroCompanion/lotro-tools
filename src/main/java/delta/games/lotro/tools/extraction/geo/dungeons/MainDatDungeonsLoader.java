package delta.games.lotro.tools.extraction.geo.dungeons;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get dungeons definitions from DAT files.
 * @author DAM
 */
public class MainDatDungeonsLoader
{
  private DataFacade _facade;
  private DungeonLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDungeonsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new DungeonLoader(facade);
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
          _loader.loadDungeon(id);
        }
      }
    }
    _loader.save();
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
