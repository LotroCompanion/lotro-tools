package delta.games.lotro.tools.dat.maps.data;

/**
 * Storage for a block/cell reference.
 * @author DAM
 */
public class BlockReference
{
  private int _region;
  private int _blockX;
  private int _blockY;
  private int _cell;

  /**
   * Constructor.
   */
  public BlockReference()
  {
    _region=1; // Eriador
  }

  /**
   * Get the region code.
   * @return the region code.
   */
  public int getRegion()
  {
    return _region;
  }

  /**
   * Set the region code.
   * @param region Region code to set.
   */
  public void setRegion(int region)
  {
    _region=region;
  }

  /**
   * Get the cell.
   * @return the cell.
   */
  public int getCell()
  {
    return _cell;
  }

  /**
   * Set the cell.
   * @param cell Cell value.
   */
  public void setCell(int cell)
  {
    _cell=cell;
  }

  /**
   * Get the block X.
   * @return the block X.
   */
  public int getBlockX()
  {
    return _blockX;
  }

  /**
   * Get the block Y.
   * @return the block Y.
   */
  public int getBlockY()
  {
    return _blockY;
  }

  /**
   * Set block.
   * @param blockX Block X.
   * @param blockY Block Y.
   */
  public void setBlock(int blockX, int blockY)
  {
    _blockX=blockX;
    _blockY=blockY;
  }

  @Override
  public String toString()
  {
    return "R="+_region+",C="+_cell+",bx="+_blockX+",by="+_blockY;
  }
}
