package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATFilesConstants;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DatFilesManager;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for landblock infos.
 * @author DAM
 */
public class LandblockInfoLoader
{
  private static final Logger LOGGER=Logger.getLogger(LandblockInfoLoader.class);

  // Flags marking the presence of optional data:
  private static final int HAS_CELLS = 1;
  private static final int HAS_LINKS = 2;
  private static final int HAS_INDICES = 4;

  // Flags indicating the presence of optional fields.
  private static final int DID = 1;
  private static final int TYPE = 2;
  private static final int INDEX = 4;
  private static final int PHYS_OBJ = 8;
  private static final int POS = 16;
  private static final int UNK_VECTOR = 32;
  private static final int UNK_HOOK = 64;
  private static final int PROPERTIES = 128;
  private static final int NAME = 256;
  private static final int PROPERTY_DIDS = 512;

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LandblockInfoLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load a landblock info.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   */
  private void loadLandblockInfo(int region, int blockX, int blockY)
  {
    long landblockInfoDID=0x80200000L+(region*0x10000)+(blockX*0x100)+blockY;

    DatFilesManager datFilesMgr=_facade.getDatFilesManager();
    DATArchive cell=datFilesMgr.getArchive(DATFilesConstants.CELL_SEED+region);
    byte[] data=cell.loadEntry(landblockInfoDID);
    if (data!=null)
    {
      //System.out.println("*** Landblock info: region="+region+", blockX="+blockX+", blockY="+blockY);
      ByteArrayInputStream bis=new ByteArrayInputStream(data);
      long did=BufferUtils.readUInt32AsLong(bis);
      if (did!=landblockInfoDID)
      {
        throw new IllegalArgumentException("Expected DID for block map: "+landblockInfoDID);
      }
      int flags=BufferUtils.readUInt32(bis);
      int sizeHint=BufferUtils.readUInt32(bis);
      // Cross links
      if ((flags & HAS_LINKS)!=0)
      {
        int count=BufferUtils.readTSize(bis);
        for(int i=0;i<count;i++)
        {
          loadCrossLink(bis);
        }
      }
      // Header Indices
      if ((flags & HAS_INDICES)!=0)
      {
        int count=BufferUtils.readTSize(bis);
        for(int i=0;i<count;i++)
        {
          loadHeaderIndices(bis);
        }
      }
      // Cells
      if ((flags & HAS_CELLS)!=0)
      {
        int count=BufferUtils.readTSize(bis);
        for(int i=0;i<count;i++)
        {
          loadCells(bis);
        }
      }
      int zero=BufferUtils.readUInt32(bis);
      if (zero!=0)
      {
        throw new IllegalArgumentException("Expected 0 here. Found: "+zero);
      }
      // Static entities
      {
        int count=BufferUtils.readUInt32(bis);
        for(int i=0;i<count;i++)
        {
          loadEntityDesc(bis);
        }
      }
      // Links
      {
        int count=BufferUtils.readUInt32(bis);
        for(int i=0;i<count;i++)
        {
          loadEntityLinkDesc(bis);
        }
      }
      // Properties
      DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
      PropertiesSet props=new PropertiesSet();
      propsLoader.decodeProperties(bis,props);

      zero=BufferUtils.readUInt32(bis);
      if (zero!=0)
      {
        throw new IllegalArgumentException("Expected 0 here. Found: "+zero);
      }

      // Weenies
      {
        int count=BufferUtils.readTSize(bis);
        for(int i=0;i<count;i++)
        {
          loadWeenie(bis);
        }
      }
      // End of data
      int bytesAvailable=bis.available();
      if (bytesAvailable>0)
      {
        int unknownFinal=BufferUtils.readUInt32(bis);
      }
      bytesAvailable=bis.available();
      if (bytesAvailable>0)
      {
        LOGGER.warn("Bytes lefts: "+bytesAvailable);
      }
    }
  }

  private void loadCrossLink(ByteArrayInputStream bis)
  {
    int fromIndex=BufferUtils.readUInt32(bis);
    long fromLbiDID=BufferUtils.readUInt32AsLong(bis);
    long toLbiDID=BufferUtils.readUInt32AsLong(bis);
    // See EnumMapper: EntityLinkType
    int type=BufferUtils.readUInt32(bis);
    int toIndex=BufferUtils.readUInt32(bis);
    int mask=BufferUtils.readUInt32(bis);
  }

  private void loadHeaderIndices(ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt16(bis);
    int[] array=readIntegerArray(bis);
    //System.out.println("Header: index="+index+", count="+array.length+" = >"+Arrays.toString(array));
  }

  private int[] readIntegerArray(ByteArrayInputStream bis)
  {
    int count=BufferUtils.readUInt32(bis);
    int[] ret=new int[count];
    for(int i=0;i<count;i++)
    {
      ret[i]=BufferUtils.readUInt16(bis);
    }
    return ret;
  }

  private int[] readShortIntegerArray(ByteArrayInputStream bis)
  {
    int count=BufferUtils.readUInt8(bis);
    int[] ret=new int[count];
    for(int i=0;i<count;i++)
    {
      ret[i]=BufferUtils.readUInt32(bis);
    }
    return ret;
  }

  private void loadCells(ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt16(bis);
    DatPosition position=GeoLoader.readPosition(bis);
    //System.out.println("Position: "+position);
    int flags=BufferUtils.readUInt32(bis);
    int cellMeshDID=BufferUtils.readUInt32(bis);
    int count=BufferUtils.readUInt32(bis);
    // Neighbours
    for(int i=0;i<count;i++)
    {
      loadNeighbours(bis);
    }
    if ((flags&0x20)!=0)
    {
      int thisIndex=BufferUtils.readUInt16(bis); // always equal to this.index
      int[] cellIndices0=readIntegerArray(bis);
      int[] cellIndices1=readIntegerArray(bis);
      int unknown=BufferUtils.readUInt16(bis);
    }
    else
    {
      int[] cellIndices0=readIntegerArray(bis);
      int[] cellIndices1=readIntegerArray(bis);
    }
    if ((flags&0x4)!=0)
    {
      DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
      PropertiesSet props=new PropertiesSet();
      propsLoader.decodeProperties(bis,props);
      /*
      if (props.getPropertyNames().size()>0)
      {
        System.out.println("Cell props: "+props.dump());
      }
      */
    }
  }

  private void loadNeighbours(ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt32(bis);
    int cellIndex=BufferUtils.readUInt16(bis);
    int neighbourIndex=BufferUtils.readUInt32(bis);
    int[] unknown=readIntegerArray(bis);
    boolean unknownBool=BufferUtils.readBoolean(bis);
  }

  private void loadEntityDesc(ByteArrayInputStream bis)
  {
    int flags=BufferUtils.readUInt32(bis);
    if ((flags & DID) == DID)
    {
      int did=BufferUtils.readUInt32(bis);
    }
    if ((flags & TYPE) == TYPE)
    {
      int type=BufferUtils.readUInt32(bis);
    }
    if ((flags & INDEX) == INDEX)
    {
      int index=BufferUtils.readUInt32(bis);
      int blockAndType=BufferUtils.readUInt32(bis);
    }
    if ((flags & PHYS_OBJ) == PHYS_OBJ)
    {
      int physObjDID=BufferUtils.readUInt32(bis);
    }
    if ((flags & POS) == POS)
    {
      float x=BufferUtils.readFloat(bis);
      float y=BufferUtils.readFloat(bis);
      float z=BufferUtils.readFloat(bis);
      /*float w=*/BufferUtils.readFloat(bis);
      /*float x=*/BufferUtils.readFloat(bis);
      /*float y=*/BufferUtils.readFloat(bis);
      /*float z=*/BufferUtils.readFloat(bis);
      //if (verbose) System.out.println("\tRot: w="+w+",x="+x+",y="+y+",z="+z);
    }
    if ((flags & UNK_VECTOR) == UNK_VECTOR) {
      // Unknown vector
      float x=BufferUtils.readFloat(bis);
      float y=BufferUtils.readFloat(bis);
      float z=BufferUtils.readFloat(bis);
    }
    if ((flags & PROPERTIES) == PROPERTIES)
    {
      DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
      PropertiesSet props=new PropertiesSet();
      propsLoader.decodeProperties(bis,props);
    }
    if ((flags & PROPERTY_DIDS) == PROPERTY_DIDS)
    {
      int[] dids=readShortIntegerArray(bis);
      System.out.println("Property DIDs: "+Arrays.toString(dids));
    }
    if ((flags & UNK_HOOK) == UNK_HOOK)
    {
      int unkHook=BufferUtils.readUInt32(bis);
    }
    if ((flags & NAME) == NAME)
    {
      String name=BufferUtils.readPascalString(bis);
      //System.out.println(name);
    }
  }

  private void loadEntityLinkDesc(ByteArrayInputStream bis)
  {
    String worldBuilderName=BufferUtils.readPascalString(bis);
    int id=BufferUtils.readUInt32(bis);
    int blockAndTypeMask=BufferUtils.readUInt32(bis);
    int toIndex=BufferUtils.readUInt32(bis);
    int toBlockAndType=BufferUtils.readUInt32(bis);
    int fromIndex=BufferUtils.readUInt32(bis);
    int fromBlockAndType=BufferUtils.readUInt32(bis);
    int type=BufferUtils.readUInt32(bis);
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet props=new PropertiesSet();
    propsLoader.decodeProperties(bis,props);
    boolean isCrossLandblock=BufferUtils.readBoolean(bis);
    if (isCrossLandblock)
    {
      int codaIndex0=BufferUtils.readUInt32(bis);
      long codaLbiDID0=BufferUtils.readUInt32AsLong(bis);
      long codaLbiDID1=BufferUtils.readUInt32AsLong(bis);
      int codaIndex1=BufferUtils.readUInt32(bis);
    }
  }

  private void loadWeenie(ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt32(bis);
    int blockAndType=BufferUtils.readUInt32(bis);
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet props=new PropertiesSet();
    propsLoader.decodeProperties(bis,props);
  }

  private void doIt()
  {
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          loadLandblockInfo(region,blockX,blockY);
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
    LandblockInfoLoader loader=new LandblockInfoLoader(facade);
    loader.doIt();
  }
}
