package delta.games.lotro.tools.dat.maps.indexs;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.maps.BlockMapLoader;
import delta.games.lotro.tools.dat.maps.LandblockDataLoader;
import delta.games.lotro.tools.dat.maps.LandblockInfoLoader;
import delta.games.lotro.tools.dat.maps.data.Cell;
import delta.games.lotro.tools.dat.maps.data.HeightMap;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;

/**
 * Loader for parent zone data.
 * @author DAM
 */
public class ParentZonesLoader
{
  private LandblockInfoLoader _lbiLoader;
  private BlockMapLoader _blockMapLoader;
  private LandblockDataLoader _lbdLoader;

  /**
   * Constructor.
   * @param facade
   */
  public ParentZonesLoader(DataFacade facade)
  {
    _lbiLoader=new LandblockInfoLoader(facade);
    _blockMapLoader=new BlockMapLoader(facade);
    _lbdLoader=new LandblockDataLoader(facade);
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
      HeightMap heightmap=_lbdLoader.loadLandblockData(region,blockX,blockY);
      if (heightmap!=null)
      {
        float centerHeight=heightmap.getCenterHeight();
        ret.setCenterHeight(centerHeight);
        //Dungeon dungeon=DungeonsManager.getInstance().getDungeonById(dungeonDID.intValue());
        //System.out.println("Dungeon "+dungeonDID+" ("+dungeon.getName()+"): center height "+centerHeight+" for R"+region+", BX="+blockX+",BY="+blockY);
      }
    }
    // Cells
    for(Cell cell : lbi.getCells())
    {
      Integer cellDungeonId=cell.getDungeonId();
      if (cellDungeonId!=null)
      {
        ret.addCellDungeon(cell.getIndex(),cellDungeonId.intValue());
      }
    }
    return ret;
  }
}
