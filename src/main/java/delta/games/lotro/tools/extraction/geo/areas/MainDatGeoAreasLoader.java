package delta.games.lotro.tools.extraction.geo.areas;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Get geographic areas from DAT files.
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

  /**
   * Load all geographic areas.
   */
  public void doIt()
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
    _loader.addMissingRegions();
    // Save geo areas
    _loader.save();
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
