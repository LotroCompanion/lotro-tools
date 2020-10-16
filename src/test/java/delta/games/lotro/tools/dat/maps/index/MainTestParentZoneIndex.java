package delta.games.lotro.tools.dat.maps.index;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

/**
 * Test class for the parent zone index.
 * @author DAM
 */
public class MainTestParentZoneIndex
{
  /**
   * Build the whole index.
   * @return the built index.
   */
  private static ParentZoneIndex buildIndex()
  {
    DataFacade facade=new DataFacade();
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    ParentZoneIndex index=new ParentZoneIndex(parentZoneLoader);
    int nbBlocks=0;
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          ParentZoneLandblockData data=index.getLandblockData(region,blockX,blockY);
          if (data!=null)
          {
            nbBlocks++;
          }
        }
      }
    }
    System.out.println("Loaded "+nbBlocks+" blocks!");
    return index;
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    /*ParentZoneIndex index=*/buildIndex();
  }
}
