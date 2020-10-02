package delta.games.lotro.tools.dat.maps.index;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

/**
 * Simple test class for the parent zones loader.
 * @author DAM
 */
public class MainTestParentZonesLoader
{
  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    ParentZonesLoader loader=new ParentZonesLoader(facade);
    /*ParentZoneLandblockData data=*/loader.buildLandblockData(2,254,252);
  }
}
