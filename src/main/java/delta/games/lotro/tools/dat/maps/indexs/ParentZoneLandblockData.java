package delta.games.lotro.tools.dat.maps.indexs;

import java.util.HashMap;
import java.util.Map;

/**
 * 'Parent zone' data for a single landblock.
 * @author DAM
 */
public class ParentZoneLandblockData
{
  private Map<Integer,Integer> _cell2Dungeon;
  private Integer _parentDungeon;
  private Integer _parentArea;

  /**
   * Constructor.
   */
  public ParentZoneLandblockData()
  {
    _cell2Dungeon=new HashMap<Integer,Integer>();
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
