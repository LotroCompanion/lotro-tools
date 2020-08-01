package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get private encounters (instances) from DAT files.
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

  private void doIt()
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
