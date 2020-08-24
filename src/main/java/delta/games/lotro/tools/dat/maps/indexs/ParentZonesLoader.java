package delta.games.lotro.tools.dat.maps.indexs;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.maps.BlockMapLoader;
import delta.games.lotro.tools.dat.maps.LandblockInfoLoader;
import delta.games.lotro.tools.dat.maps.data.Cell;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;

/**
 * Loader for parent zone data.
 * @author DAM
 */
public class ParentZonesLoader
{
  private LandblockInfoLoader _lbiLoader;
  private BlockMapLoader _blockMapLoader;

  /**
   * Constructor.
   * @param facade
   */
  public ParentZonesLoader(DataFacade facade)
  {
    _lbiLoader=new LandblockInfoLoader(facade);
    _blockMapLoader=new BlockMapLoader(facade);
  }

  /**
   * Build the whole index.
   * @return the built index.
   */
  public ParentZoneIndex buildIndex()
  {
    ParentZoneIndex index=new ParentZoneIndex();
    int nbBlocks=0;
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          ParentZoneLandblockData data=buildLandblockData(region,blockX,blockY);
          if (data!=null)
          {
            index.registerLandblockData(region,blockX,blockY,data);
            //System.out.println("R="+region+", bx="+blockX+", by="+blockY+" => "+zoneData);
            nbBlocks++;
          }
        }
      }
    }
    System.out.println("Loaded "+nbBlocks+" blocks!");
    return index;
  }

  /**
   * Build landblock data.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   * @return the loaded data or <code>null</code>.
   */
  public ParentZoneLandblockData buildLandblockData(int region, int blockX, int blockY)
  {
    // Landblock Info
    LandBlockInfo lbi=_lbiLoader.loadLandblockInfo(region,blockX,blockY);
    if (lbi==null)
    {
      return null;
    }
    ParentZoneLandblockData ret=new ParentZoneLandblockData();
    // Block map
    PropertiesSet props=_blockMapLoader.loadPropertiesForMapBlock(region,blockX,blockY);
    if (props!=null)
    {
      int areaId=((Integer)props.getProperty("Area_DID")).intValue(); // Always set
      ret.setParentArea(areaId);
    }
    // Dungeon ID
    PropertiesSet lbiProps=lbi.getProps();
    Integer dungeonDID=(Integer)lbiProps.getProperty("Dungeon_DID");
    if (dungeonDID!=null)
    {
      ret.setParentDungeon(dungeonDID.intValue());
    }
    // Cells
    for(Cell cell : lbi.getCells())
    {
      int cellDungeonId=cell.getDungeonId();
      ret.addCellDungeon(cell.getIndex(),cellDungeonId);
    }
    return ret;
  }

  /**
   * Main method to build the whole index.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    ParentZonesLoader loader=new ParentZonesLoader(facade);
    /*ParentZoneIndex index=*/loader.buildIndex();
  }
}
