package delta.games.lotro.tools.dat.maps.landblocks;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;
import delta.games.lotro.lore.maps.landblocks.io.xml.LandblocksXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Tool class to load all the landblocks.
 * @author DAM
 */
public class MainLandblocksBuilder
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainLandblocksBuilder(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Build a landblocks manager with all known landblocks.
   * @return a landblocks manager.
   */
  private LandblocksManager build()
  {
    LandblockLoader landblockLoader=new LandblockLoader(_facade);
    LandblocksManager index=new LandblocksManager();
    int nbBlocks=0;
    boolean isLive=Context.isLive();
    int[] regions=isLive?new int[]{1,2,3,4,14}:new int[]{1};
    for(int region : regions)
    {
      System.out.println("Region "+region);
      for(int blockX=0;blockX<=0xFF;blockX++)
      {
        System.out.println("X="+blockX);
        for(int blockY=0;blockY<=0xFF;blockY++)
        {
          Landblock data=landblockLoader.buildLandblock(region,blockX,blockY);
          if (data!=null)
          {
            index.addLandblock(data);
            nbBlocks++;
          }
        }
      }
    }
    System.out.println("Loaded "+nbBlocks+" blocks!");
    return index;
  }

  /**
   * Load landblock data.
   */
  public void doIt()
  {
    LandblocksManager index=build();
    // Save landblocks.
    boolean ok=LandblocksXMLWriter.writeLandblocksFile(GeneratedFiles.LANDBLOCKS,index);
    if (ok)
    {
      System.out.println("Wrote landblocks file: "+GeneratedFiles.LANDBLOCKS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainLandblocksBuilder(facade).doIt();
  }
}
