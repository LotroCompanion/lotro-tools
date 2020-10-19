package delta.games.lotro.tools.dat.maps.landblocks.comparators;

import java.util.Comparator;

import delta.games.lotro.lore.geo.BlockReferenceComparator;
import delta.games.lotro.tools.dat.maps.landblocks.Landblock;

/**
 * Comparator for landblocks, using their identifier.
 * @author DAM
 */
public class LandblockIdComparator implements Comparator<Landblock>
{
  private BlockReferenceComparator _c=new BlockReferenceComparator();

  @Override
  public int compare(Landblock o1, Landblock o2)
  {
    return _c.compare(o1.getBlockId(),o2.getBlockId());
  }
}
