package delta.games.lotro.tools.dat.maps.data;

/**
 * Cell data (restricted to usage in LotroCompanion tools).
 * @author DAM
 */
public class Cell
{
  private int _index;
  private int _dungeonId;

  /**
   * Constructor.
   * @param index Cell index.
   * @param dungeonId Dungeon identifier.
   */
  public Cell(int index, int dungeonId)
  {
    _index=index;
    _dungeonId=dungeonId;
  }

  /**
   * Get the cell index.
   * @return the cell index.
   */
  public int getIndex()
  {
    return _index;
  }

  /**
   * Get the dungeon identifier.
   * @return the dungeon identifier.
   */
  public int getDungeonId()
  {
    return _dungeonId;
  }
}
