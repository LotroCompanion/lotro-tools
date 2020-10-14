package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.PositionDecoder;

/**
 * Simple test class for the landblock data loader.
 * @author DAM
 */
public class MainTestLandblockDataLoader
{
  private DataFacade _facade;
  private LandblockDataLoader _loader;

  private MainTestLandblockDataLoader()
  {
    _facade=new DataFacade();
    _loader=new LandblockDataLoader(_facade);
  }

  private void doIt()
  {
    DatPosition position=PositionDecoder.fromLatLon(-62.5f,-14.7f);
    System.out.println("Position: "+position);
    int region=1; int blockX=position.getBlockX(); int blockY=position.getBlockY();
    //int region=2; int blockX=252; int blockY=83;
    //int region=2; int blockX=0x71; int blockY=0xB5;
    _loader.loadLandblockData(region,blockX,blockY);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    MainTestLandblockDataLoader loader=new MainTestLandblockDataLoader();
    loader.doIt();
  }
}
