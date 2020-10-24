package delta.games.lotro.tools.dat.instances;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.geo.BlockReference;

/**
 * Builds groups of (land)blocks.
 * @author DAM
 */
public class BlockGroupsBuilder
{
  private List<List<BlockReference>> _groups;

  /**
   * Constructor.
   */
  public BlockGroupsBuilder()
  {
    _groups=new ArrayList<List<BlockReference>>();
  }

  /**
   * Build groups of blocks from a raw blocks list.
   * @param blocks Blocks to use.
   * @return A list of block groups.
   */
  public List<List<BlockReference>> buildGroups(List<BlockReference> blocks)
  {
    _groups.clear();
    List<BlockReference> blocksToUse=new ArrayList<BlockReference>(blocks);
    while(blocksToUse.size()>0)
    {
      BlockReference block=findGroupForABlock(blocksToUse);
      if (block==null)
      {
        block=blocksToUse.get(0);
        List<BlockReference> newGroup=new ArrayList<BlockReference>();
        newGroup.add(block);
        _groups.add(newGroup);
      }
      blocksToUse.remove(block);
    }
    List<List<BlockReference>> ret=new ArrayList<List<BlockReference>>(_groups);
    _groups.clear();
    return ret;
  }

  private BlockReference findGroupForABlock(List<BlockReference> blocks)
  {
    for(BlockReference block : blocks)
    {
      List<BlockReference> foundGroup=findGroupForBlock(block);
      if (foundGroup!=null)
      {
        // Attach
        foundGroup.add(block);
        return block;
      }
    }
    return null;
  }

  private List<BlockReference> findGroupForBlock(BlockReference block)
  {
    for(List<BlockReference> group : _groups)
    {
      for(BlockReference refBlock : group)
      {
        boolean areNeighbours=areNeighbours(refBlock,block);
        if (areNeighbours)
        {
          return group;
        }
      }
    }
    return null;
  }

  private boolean areNeighbours(BlockReference ref, BlockReference toTest)
  {
    // Test region
    int refRegion=ref.getRegion();
    int testRegion=toTest.getRegion();
    if (refRegion!=testRegion)
    {
      return false;
    }
    int deltaX=ref.getBlockX()-toTest.getBlockX();
    int deltaY=ref.getBlockY()-toTest.getBlockY();
    if (((deltaX<=1) && (deltaX>=-1)) && ((deltaY<=1) && (deltaY>=-1)))
    {
      return true;
    }
    return false;
  }
}
