package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.maps.GeoAreasManager;

/**
 * Get private encounters (instances) from DAT files.
 * @author DAM
 */
public class MainDatGeoAreasLoader
{
  private DataFacade _facade;
  private GeoAreasLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatGeoAreasLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new GeoAreasLoader(facade);
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==690)
        {
          _loader.getArea(id);
        }
        else if (classDefIndex==692)
        {
          _loader.getTerritory(id);
        }
        else if (classDefIndex==180)
        {
          _loader.getRegion(id);
        }
      }
    }
    GeoAreasManager geoManager=_loader.getGeoManager();
    geoManager.dump();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatGeoAreasLoader(facade).doIt();
    facade.dispose();
  }
}
