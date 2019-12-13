package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for block maps.
 * @author DAM
 */
public class BlockMapLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public BlockMapLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load a block map.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   */
  private void loadBlockMap(int region, int blockX, int blockY)
  {
    int blockMapDID=0x80100000+(region*0x10000)+(blockX*0x100)+blockY;

    byte[] data=_facade.loadData(blockMapDID);
    if (data!=null)
    {
      System.out.println("*** Block map: region="+region+", blockX="+blockX+", blockY="+blockY);
      ByteArrayInputStream bis=new ByteArrayInputStream(data);
      int did=BufferUtils.readUInt32(bis);
      if (did!=blockMapDID)
      {
        throw new IllegalArgumentException("Expected DID for block map: "+blockMapDID);
      }
      int count=BufferUtils.readTSize(bis);
      System.out.println(count+" entries in this block map!");
      for(int i=0;i<count;i++)
      {
        int key=BufferUtils.readUInt32(bis);
        int index=BufferUtils.readUInt32(bis);
        System.out.println("Mapping key="+key+" to index="+index);
      }
    }
  }

  private void doIt()
  {
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          loadBlockMap(region,blockX,blockY);
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
    BlockMapLoader loader=new BlockMapLoader(facade);
    loader.doIt();
  }
}
