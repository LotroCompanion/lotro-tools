package delta.games.lotro.tools.dat.maps.landblocks;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATFilesConstants;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DatFilesManager;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.EntityDescriptor;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.EntityDescLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.maps.data.Cell;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;
import delta.games.lotro.tools.dat.maps.data.LbiLink;
import delta.games.lotro.tools.dat.maps.data.Weenie;

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

  private DataFacade _facade;
  private GeneratorLoader _generatorLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LandblockInfoLoader(DataFacade facade)
  {
    _facade=facade;
    _generatorLoader=new GeneratorLoader(facade);
  }

  /**
   * Load a landblock info.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   * @return <code>true</code> if the landblock info exists, <code>false</code> otherwise.
   */
  @SuppressWarnings("unused")
  public LandBlockInfo loadLandblockInfo(int region, int blockX, int blockY)
  {
    long landblockInfoDID=0x80200000L+(region*0x10000)+(blockX*0x100)+blockY;

    DatFilesManager datFilesMgr=_facade.getDatFilesManager();
    DATArchive cellArchive=datFilesMgr.getArchive(DATFilesConstants.CELL_SEED+region);
    if (cellArchive==null)
    {
      return null;
    }
    byte[] data=cellArchive.loadEntry(landblockInfoDID);
    if (data==null)
    {
      return null;
    }
    //System.out.println("*** Landblock info: region="+region+", blockX="+blockX+", blockY="+blockY);
    //System.out.println("LBI data length: "+data.length);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    long did=BufferUtils.readUInt32AsLong(bis);
    if (did!=landblockInfoDID)
    {
      throw new IllegalArgumentException("Expected DID for landblock info: "+landblockInfoDID);
    }
    LandBlockInfo ret=new LandBlockInfo(landblockInfoDID);
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
      //System.out.println("Cells count: "+count);
      for(int i=0;i<count;i++)
      {
        Cell cell=loadCell(bis);
        if (cell!=null)
        {
          ret.addCell(cell);
        }
      }
    }
    LoaderUtils.readAssert(bis,0);
    // Static entities
    {
      int count=BufferUtils.readUInt32(bis);
      //System.out.println("Entity count: "+count);
      for(int i=0;i<count;i++)
      {
        //System.out.println("Entity desc index: "+i);
        EntityDescriptor entity=loadStaticEntity(bis);
        ret.addEntity(entity);
      }
    }
    // Links
    {
      int count=BufferUtils.readUInt32(bis);
      //System.out.println("Links count: "+count);
      for(int i=0;i<count;i++)
      {
        //System.out.println("Link index: "+i);
        LbiLink link=loadLink(bis);
        ret.addLink(link);
      }
    }
    // Properties
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet props=ret.getProps();
    propsLoader.decodeProperties(bis,props);
    // No area ID. It is in the LandBlockProperties

    LoaderUtils.readAssert(bis,0);

    // Weenies
    {
      int count=BufferUtils.readTSize(bis);
      //System.out.println("Weenies count: "+count);
      for(int i=0;i<count;i++)
      {
        //System.out.println("Weenie index: "+i);
        Weenie weenie=loadWeenie(bis);
        ret.addWeenie(weenie);
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
    return ret;
  }

  @SuppressWarnings("unused")
  private void loadCrossLink(ByteArrayInputStream bis)
  {
    int fromIndex=BufferUtils.readUInt32(bis);
    long fromLbiDID=BufferUtils.readUInt32AsLong(bis);
    long toLbiDID=BufferUtils.readUInt32AsLong(bis);
    // See EnumMapper: EntityLinkType
    int type=BufferUtils.readUInt32(bis);
    String typeStr=getLinkLabelFromCode(type);
    int toIndex=BufferUtils.readUInt32(bis);
    int mask=BufferUtils.readUInt32(bis);
    /*
    System.out.println("Cross link: "+typeStr+" - from: "+String.format("%04X",Long.valueOf(fromLbiDID))+"/"+fromIndex+
        " ; to:"+String.format("%04X",Long.valueOf(toLbiDID))+"/"+toIndex+", mask="+mask);
        */
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  private Cell loadCell(ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt16(bis);
    //System.out.println("Cell index: "+index);
    DatPosition position=GeoLoader.readPosition(bis); // position.cell=index
    //System.out.println("\tPosition: "+position);
    int flags=BufferUtils.readUInt32(bis);
    int cellMeshDID=BufferUtils.readUInt32(bis);
    //System.out.println("\tMesh ID: "+cellMeshDID);
    int neighboursCount=BufferUtils.readUInt32(bis);
    // Neighbours
    for(int i=0;i<neighboursCount;i++)
    {
      loadNeighbours(i,bis);
    }
    if ((flags&0x20)!=0)
    {
      int thisIndex=BufferUtils.readUInt16(bis); // always equal to this.index
      if (thisIndex!=index)
      {
        throw new IllegalStateException("thisIndex="+thisIndex+", expected="+index);
      }
      int[] cellIndices0=readIntegerArray(bis);
      int[] cellIndices1=readIntegerArray(bis);
      //System.out.println("\tCell indices: #0="+Arrays.toString(cellIndices0)+", #1="+Arrays.toString(cellIndices1));
      int unknown=BufferUtils.readUInt16(bis);
      //System.out.println("\tUnknown: "+unknown);
    }
    else
    {
      int[] cellIndices0=readIntegerArray(bis);
      int[] cellIndices1=readIntegerArray(bis);
      //System.out.println("\tCell indices: #0="+Arrays.toString(cellIndices0)+", #1="+Arrays.toString(cellIndices1));
    }
    Integer dungeonId=null;
    if ((flags&0x4)!=0)
    {
      DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
      PropertiesSet props=new PropertiesSet();
      propsLoader.decodeProperties(bis,props);
      if (props.getPropertyNames().size()>0)
      {
        dungeonId=(Integer)props.getProperty("Dungeon_DID");
        /*
        if ((dungeonDID!=null) && (dungeonDID.intValue()>0))
        {
          System.out.println("Found Dungeon ID: "+dungeonDID+" for cell "+index);
        }
        */
        //System.out.println("Cell props: "+props.dump());
        /* {
         * Physics_AdjustableScale=null (vector),
         * Render_AdjustableScale=null (vector),
         * Appearance_InstanceList=#1:
         *    Appearance_AprFile: 536870912
         *    Appearance_Key: 268438298 (Dng_DolGuldur_Isengard_Pristine)
         *    Appearance_Modifier: 0.0
         * }
         */
      }
    }
    Cell ret=new Cell(index,dungeonId);
    ret.setPosition(position);
    return ret;
  }

  @SuppressWarnings("unused")
  private void loadNeighbours(int expectedIndex, ByteArrayInputStream bis)
  {
    int index=BufferUtils.readUInt32(bis); // Neighbour index
    if (index!=expectedIndex)
    {
      throw new IllegalStateException("thisIndex="+index+", expected="+expectedIndex);
    }
    int cellIndex=BufferUtils.readUInt16(bis);
    int neighbourIndex=BufferUtils.readUInt16(bis);
    LoaderUtils.readAssert16(bis,0);
    LoaderUtils.readAssert16(bis,0);
    LoaderUtils.readAssert16(bis,0);
    boolean unknownBool=BufferUtils.readBoolean(bis);
    //System.out.println("\tNeighbour #"+index+" is cell #"+cellIndex+", and reverse neighbour index is "+neighbourIndex+", bool="+unknownBool);
  }

  private EntityDescriptor loadStaticEntity(ByteArrayInputStream bis)
  {
    EntityDescriptor entity=EntityDescLoader.decodeEntityDesc(_facade,bis,false);
    /*
    int physObjDid=entity.getPhysObjDid();
    if ((physObjDid>=0x47000000) && (physObjDid<=0x47FFFFFF))
    {
      byte[] data=_facade.loadData(physObjDid);
      ByteArrayInputStream bis2=new ByteArrayInputStream(data);
      loadStaticEntity(bis2);
    }
    if ((physObjDid>=0x1F000000) && (physObjDid<=0x1FFFFFFF))
    {
      // Visual descriptor
      byte[] data=_facade.loadData(physObjDid);
      ByteArrayInputStream bis2=new ByteArrayInputStream(data);
      VisualDescriptorLoader.readNetworkVisualDesc(bis2);
    }
    */
    return entity;
  }

  @SuppressWarnings("unused")
  private LbiLink loadLink(ByteArrayInputStream bis)
  {
    LbiLink link=new LbiLink();
    String worldBuilderName=BufferUtils.readPascalString(bis);
    link.setName(worldBuilderName);
    long iid=BufferUtils.readLong64(bis);
    link.setIid(iid);
    long toEntityId=BufferUtils.readLong64(bis);
    link.setToIid(toEntityId);
    long fromEntityId=BufferUtils.readLong64(bis);
    link.setFromIid(fromEntityId);
    int entityLinkType=BufferUtils.readUInt32(bis);
    String linkTypeStr=getLinkLabelFromCode(entityLinkType);
    link.setType(linkTypeStr);
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet props=new PropertiesSet();
    propsLoader.decodeProperties(bis,props);
    link.setProps(props);
    boolean isCrossLandblock=BufferUtils.readBoolean(bis);
    if (isCrossLandblock)
    {
      int codaIndex0=BufferUtils.readUInt32(bis);
      long codaLbiDID0=BufferUtils.readUInt32AsLong(bis);
      long codaLbiDID1=BufferUtils.readUInt32AsLong(bis);
      int codaIndex1=BufferUtils.readUInt32(bis);
      /*
      System.out.println("\tCrossLBI: index0="+codaIndex0+
          ", LBI0="+String.format("%04X",Long.valueOf(codaLbiDID0))+
          ", LBI1="+String.format("%04X",Long.valueOf(codaLbiDID1))+
          ", index1="+codaIndex1);
          */
    }
    return link;
  }

  private Weenie loadWeenie(ByteArrayInputStream bis)
  {
    Weenie weenie=new Weenie();
    long weenieIid=BufferUtils.readLong64(bis);
    weenie.setIid(weenieIid);
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet props=new PropertiesSet();
    propsLoader.decodeProperties(bis,props);
    weenie.setProps(props);
    Set<Integer> ids=_generatorLoader.handleGenerator(props);
    if ((ids!=null) && (ids.size()>0))
    {
      weenie.setGeneratorDids(ids);
    }
    return weenie;
  }

  private static String getLinkLabelFromCode(int code)
  {
    if (code==0) return "Undefined";
    if (code==0x01) return "PatrolOnce";
    if (code==0x02) return "Patrol";
    if (code==0x07) return "Generator_PositionSet";
    if (code==0x08) return "Generator";
    if (code==0x09) return "Segment";
    if (code==0x0B) return "FlyingPatrol";
    if (code==0x0C) return "Script";
    if (code==0x10) return "Path";
    if (code==0x13) return "AIRemoteDetector";
    if (code==0x17) return "SegmentBridge";
    if (code==0x1000000B) return "House_Permission";
    if (code==0x1000000C) return "House_Decoration";
    if (code==0x1000000D) return "House_Landmark";
    if (code==0x1000000F) return "House_ForSaleSign";
    if (code==0x10000010) return "House_Storage1";
    if (code==0x10000011) return "House_Storage2";
    if (code==0x10000012) return "House_Storage3";
    if (code==0x10000013) return "House_PropertyDriver";
    if (code==0x10000019) return "Phasing";

    LOGGER.warn("Unmanaged link code: "+code);
    return "? "+code+" ?";
  }
}
