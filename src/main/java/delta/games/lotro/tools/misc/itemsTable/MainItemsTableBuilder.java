package delta.games.lotro.tools.misc.itemsTable;

import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.utils.maths.Progression;

/**
 * @author dm
 */
public class MainItemsTableBuilder
{
  private static final int CHAR_LEVEL=150;
  private static final int ITEM_LEVEL=500;

  private void handleItem(Item item)
  {
    // Filter on slot
    EquipmentLocation location=item.getEquipmentLocation();
    if (location==null)
    {
      return;
    }
    boolean isScalable=item.isScalable();
    if (isScalable)
    {
      handleScalableItem(item);
    }
    else
    {
      if (useNonScalableItem(item))
      {
        Integer itemLevel=item.getItemLevel();
        showItem(item,item.getMinLevel(),itemLevel.intValue());
      }
    }
  }

  private boolean useNonScalableItem(Item item)
  {
    Integer minLevel=item.getMinLevel();
    if ((minLevel==null) || (minLevel.intValue()>CHAR_LEVEL))
    {
      return false;
    }
    Integer maxLevel=item.getMaxLevel();
    if ((maxLevel!=null) && (maxLevel.intValue()<CHAR_LEVEL))
    {
      return false;
    }
    Integer itemLevel=item.getItemLevel();
    if ((itemLevel==null) || (itemLevel.intValue()<ITEM_LEVEL))
    {
      return false;
    }
    return true;
  }

  private void handleScalableItem(Item item)
  {
    // For scaled items, the "min level" is set to the scaling level
    // So let's find items that are scalable at CHAR_LEVEL
    Munging scaling=item.getMunging();
    Integer min=scaling.getMin();
    Integer max=scaling.getMax();
    Progression progression=scaling.getProgression();
    if ((min!=null) && (min.intValue()>CHAR_LEVEL))
    {
      return;
    }
    if ((max!=null) && (max.intValue()<CHAR_LEVEL))
    {
      return;
    }
    Integer minLevel=item.getMinLevel();
    if ((minLevel==null) || (minLevel.intValue()>CHAR_LEVEL))
    {
      return;
    }
    Integer maxLevel=item.getMaxLevel();
    if ((maxLevel!=null) && (maxLevel.intValue()<CHAR_LEVEL))
    {
      return;
    }
    if (progression!=null)
    {
      int itemLevel=progression.getValue(CHAR_LEVEL).intValue();
      showItem(item,Integer.valueOf(CHAR_LEVEL),itemLevel);
      return;
    }
    //System.out.println("No progression:");
    showItem(item,Integer.valueOf(CHAR_LEVEL),item.getItemLevel().intValue());
  }

  private void showItem(Item item, Integer level, int itemLevel)
  {
    System.out.println(item.getIdentifier()+"\t"+item.getName()+"\t"+level+"\t"+itemLevel+"\t"+item.getMinLevel()+"\t"+item.getMunging());
  }

  private void doIt()
  {
    /*
    Item item=ItemsManager.getInstance().getItem(1879444233);
    useItem(item);
    */
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      handleItem(item);
    }
  }
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new MainItemsTableBuilder().doIt();
  }
}
