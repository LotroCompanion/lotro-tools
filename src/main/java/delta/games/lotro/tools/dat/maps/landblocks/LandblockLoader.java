package delta.games.lotro.tools.dat.maps.landblocks;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.geo.HeightMap;
import delta.games.lotro.dat.loaders.HeightMapDataLoader;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.maps.landblocks.Cell;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;

/**
 * Loader for landblocks.
 * @author DAM
 */
public class LandblockLoader
{
  private LandblockInfoLoader _lbiLoader;
  private BlockMapLoader _blockMapLoader;
  private HeightMapDataLoader _heightDataLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LandblockLoader(DataFacade facade)
  {
    _lbiLoader=new LandblockInfoLoader(facade);
    _blockMapLoader=new BlockMapLoader(facade);
    _heightDataLoader=new HeightMapDataLoader(facade);
  }

  /**
   * Build a landblock.
   * @param region Region identifier.
   * @param blockX Block coordinate (horizontal).
   * @param blockY Block coordinate (vertical).
   * @return the loaded data or <code>null</code>.
   */
  public Landblock buildLandblock(int region, int blockX, int blockY)
  {
    // Landblock Info
    LandBlockInfo lbi=_lbiLoader.loadLandblockInfo(region,blockX,blockY);
    if (lbi==null)
    {
      return null;
    }
    BlockReference blockId=new BlockReference(region,blockX,blockY);
    Landblock ret=new Landblock(blockId);
    // Block map
    PropertiesSet props=_blockMapLoader.loadPropertiesForMapBlock(region,blockX,blockY);
    if (props!=null)
    {
      Integer areaId=(Integer)props.getProperty("Area_DID"); // Always set?
      if (areaId!=null)
      {
        ret.setParentArea(areaId.intValue());
      }
      /*
      {
        Integer value=(Integer)props.getProperty("Ambient_MusicRegion");
        if ((value!=null) && (value.intValue()>1))
        {
          System.out.println("R="+region+",BX="+blockX+",BY="+blockY+" => Ambient_MusicRegion="+value);
        }
      }
      {
        Integer value=(Integer)props.getProperty("Ambient_MusicType");
        if ((value!=null) && (value.intValue()>1))
        {
          System.out.println("R="+region+",BX="+blockX+",BY="+blockY+" => Ambient_MusicType="+value);
        }
      }
      {
        Integer value=(Integer)props.getProperty("Ambient_SoundEnum_BlockMapOverride");
        if ((value!=null) && (value.intValue()>1))
        {
          System.out.println("R="+region+",BX="+blockX+",BY="+blockY+" => Ambient_SoundEnum_BlockMapOverride="+value);
        }
      }
      */
    }
    // Dungeon ID
    PropertiesSet lbiProps=lbi.getProps();
    Integer dungeonDID=(Integer)lbiProps.getProperty("Dungeon_DID");
    if (dungeonDID!=null)
    {
      ret.setParentDungeon(dungeonDID.intValue());
    }
    // Heightmap
    HeightMap heightmap=_heightDataLoader.loadHeightMapData(region,blockX,blockY);
    if (heightmap!=null)
    {
      float centerHeight=heightmap.getCenterHeight();
      ret.setCenterHeight(centerHeight);
    }
    // Cells
    for(Cell cell : lbi.getCells())
    {
      ret.addCell(cell);
    }
    return ret;
  }
}
