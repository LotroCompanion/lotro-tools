package delta.games.lotro.tools.extraction.geo.landblocks;

import java.util.Collections;
import java.util.List;

import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;
import delta.games.lotro.lore.maps.landblocks.comparators.LandblockIdComparator;

/**
 * Evaluate assumptions about landblocks.
 * @author DAM
 */
public class MainLandblocksManagerAssumptions
{
  private void doIt()
  {
    LandblocksManager landblocksMgr=LandblocksManager.getInstance();
    List<Landblock> landblocks=landblocksMgr.getLandblocks();
    Collections.sort(landblocks,new LandblockIdComparator());
    for(Landblock landblock : landblocks)
    {
      checkCellsButNoDungeon(landblock);
    }
  }

  private void checkCellsButNoDungeon(Landblock landblock)
  {
    Integer parentDungeon=landblock.getParentDungeon();
    if (parentDungeon==null)
    {
      int nbCells=landblock.getCellIndexes().size();
      if (nbCells>0)
      {
        List<Integer> dungeonsFromCells=landblock.getDungeonsFromCells();
        if (dungeonsFromCells.size()==0)
        {
          Integer parentArea=landblock.getParentArea();
          System.out.println("Landblock "+landblock.getBlockId()+": has cells ("+nbCells+") but no dungeon! Parent area="+parentArea);
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainLandblocksManagerAssumptions().doIt();
  }
}
