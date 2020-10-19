package delta.games.lotro.tools.dat.maps.landblocks;

import java.io.ByteArrayInputStream;

import delta.games.lotro.dat.DATFilesConstants;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DatFilesManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.maps.data.HeightMap;

/**
 * Loader for landblock data.
 * @author DAM
 */
public class LandblockDataLoader
{
  //private static final Logger LOGGER=Logger.getLogger(LandblockDataLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LandblockDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load a landblock data.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   * @return a height map.
   */
  @SuppressWarnings("unused")
  public HeightMap loadLandblockData(int region, int blockX, int blockY)
  {
    long landblockDataDID=0x80000000L+(region*0x10000)+(blockX*0x100)+blockY;

    DatFilesManager datFilesMgr=_facade.getDatFilesManager();
    DATArchive cellArchive=datFilesMgr.getArchive(DATFilesConstants.CELL_SEED+region);
    if (cellArchive==null)
    {
      return null;
    }
    byte[] data=cellArchive.loadEntry(landblockDataDID);
    if (data==null)
    {
      return null;
    }
    //System.out.println("*** Landblock data: region="+region+", blockX="+blockX+", blockY="+blockY);
    //System.out.println("LBD data length: "+data.length);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    long did=BufferUtils.readUInt32AsLong(bis);
    if (did!=landblockDataDID)
    {
      throw new IllegalArgumentException("Expected DID for landblock data: "+landblockDataDID);
    }
    int entitiesDid=BufferUtils.readUInt32(bis);
    LoaderUtils.readAssert(bis,0);

    // Divide by 65535 to get 0..1 range then double to get offset z
    int[] floorHeightMap=BufferUtils.readPrefixedArrayUInt16(bis);
    //System.out.println("Floor height map size: "+floorHeightMap.length);

    /*
    boolean hasCeiling=BufferUtils.readBoolean(bis);
    if (hasCeiling)
    {
      int[] ceilingHeightMap=BufferUtils.readPrefixedArrayUInt16(bis);
      System.out.println("Ceiling height map size: "+ceilingHeightMap.length);
    }
    */
    return new HeightMap(floorHeightMap);
  }
}
