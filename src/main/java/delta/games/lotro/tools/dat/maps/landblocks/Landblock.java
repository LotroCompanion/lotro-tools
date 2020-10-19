package delta.games.lotro.tools.dat.maps.landblocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.geo.BlockReference;

/**
 * Landblock.
 * <p>
 * Contains summary data for a single landblock, as used by tools.
 * @author DAM
 */
public class Landblock
{
  private BlockReference _id;
  private Map<Integer,Integer> _cell2Dungeon;
  private Integer _parentDungeon;
  private Integer _parentArea;
  private float _centerHeight;

  /**
   * Constructor.
   * @param id Block identifier.
   */
  public Landblock(BlockReference id)
  {
    _id=id;
    _cell2Dungeon=new HashMap<Integer,Integer>();
  }

  /**
   * Get the block identifier.
   * @return the block identifier.
   */
  public BlockReference getBlockId()
  {
    return _id;
  }

  /**
   * Add a cell/dungeon relation.
   * @param cellIndex Cell index.
   * @param dungeonId Dungeon identifier.
   */
  public void addCellDungeon(int cellIndex, int dungeonId)
  {
    _cell2Dungeon.put(Integer.valueOf(cellIndex),Integer.valueOf(dungeonId));
  }

  /**
   * Get the dungeon identifier for the given cell.
   * @param cellIndex Index of cell to use.
   * @return A dungeon identifier or <code>null</code>.
   */
  public Integer getCellDungeon(int cellIndex)
  {
    return _cell2Dungeon.get(Integer.valueOf(cellIndex));
  }

  /**
   * Get the indexes for the managed cells.
   * @return a list of cell indexes.
   */
  public List<Integer> getCellIndexes()
  {
    return new ArrayList<Integer>(_cell2Dungeon.keySet());
  }

  /**
   * Get a list of all the dungeons found in cells.
   * @return A possibly empty but never <code>null</code> list of dungeon IDs.
   */
  public List<Integer> getDungeonsFromCells()
  {
    List<Integer> ret=new ArrayList<Integer>();
    for(Integer dungeonId :_cell2Dungeon.values())
    {
      if (!ret.contains(dungeonId))
      {
        ret.add(dungeonId);
      }
    }
    return ret;
  }

  /**
   * Get the parent dungeon identifier.
   * @return A dungeon identifier or <code>null</code>.
   */
  public Integer getParentDungeon()
  {
    return _parentDungeon;
  }

  /**
   * Set the parent dungeon identifier.
   * @param dungeonId Dungeon identifier to set.
   */
  public void setParentDungeon(int dungeonId)
  {
    _parentDungeon=Integer.valueOf(dungeonId);
  }

  /**
   * Get the parent area identifier.
   * @return An area identifier or <code>null</code>.
   */
  public Integer getParentArea()
  {
    return _parentArea;
  }

  /**
   * Set the parent area identifier.
   * @param areaId Area identifier to set.
   */
  public void setParentArea(int areaId)
  {
    _parentArea=Integer.valueOf(areaId);
  }

  /**
   * Get the center height for this landblock.
   * @return a height.
   */
  public float getCenterHeight()
  {
    return _centerHeight;
  }

  /**
   * Set the center height.
   * @param centerHeight Center height (unknown unit).
   */
  public void setCenterHeight(float centerHeight)
  {
    _centerHeight=centerHeight;
  }

  /**
   * Get the parent zone identifier.
   * @param cell Cell index to use.
   * @param oz Height of point to check.
   * @return A parent zone identifier or <code>null</code>.
   */
  public Integer getParentData(int cell, float oz)
  {
    Integer ret=null;
    if (cell>0)
    {
      ret=getCellDungeon(cell);
    }
    if (ret==null)
    {
      Integer dungeonId=getParentDungeon();
      if (dungeonId!=null)
      {
        // Check height
        if (oz<_centerHeight)
        {
          ret=dungeonId;
        }
      }
    }
    if (ret==null)
    {
      ret=getParentArea();
    }
    return ret;
  }

  @Override
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    if (_parentArea!=null)
    {
      sb.append("Area=").append(_parentArea);
    }
    if (_parentDungeon!=null)
    {
      sb.append(", dungeon=").append(_parentDungeon);
    }
    if (_cell2Dungeon.size()>0)
    {
      sb.append(", cells=").append(_cell2Dungeon);
    }
    return sb.toString();
  }
}
