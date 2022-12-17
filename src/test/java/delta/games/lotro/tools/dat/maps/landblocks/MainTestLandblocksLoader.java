package delta.games.lotro.tools.dat.maps.landblocks;

import delta.games.lotro.dat.data.DataFacade;

/**
 * Simple test class for the landblocks loader.
 * @author DAM
 */
public class MainTestLandblocksLoader
{
  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    LandblockLoader loader=new LandblockLoader(facade);
    /*Landblock landblock=*/loader.buildLandblock(2,254,252);
  }
}
