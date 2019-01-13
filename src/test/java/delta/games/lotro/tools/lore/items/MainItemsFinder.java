package delta.games.lotro.tools.lore.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Tool to find items in the items database.
 * @author DAM
 */
public class MainItemsFinder
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Set<EquipmentLocation> locs=new HashSet<EquipmentLocation>();
    locs.add(EquipmentLocation.HEAD);
    locs.add(EquipmentLocation.SHOULDER);
    locs.add(EquipmentLocation.CHEST);
    locs.add(EquipmentLocation.HAND);
    locs.add(EquipmentLocation.LEGS);
    locs.add(EquipmentLocation.FEET);
    ItemsManager itemsMgr=ItemsManager.getInstance();
    List<Item> items=itemsMgr.getAllItems();
    for(Item item : items)
    {
      if (item.getQuality()==ItemQuality.RARE)
      {
        String name=item.getName();
        if ((name!=null) && (name.endsWith("of the Abyss")))
        {
          if (item instanceof Armour)
          {
            FixedDecimalsInteger stat=item.getStats().getStat(WellKnownStat.MIGHT);
            if (stat!=null)
            {
              if (locs.contains(item.getEquipmentLocation()))
              {
                System.out.println(item.getIdentifier()+" "+item.getName());
              }
            }
          }
        }
      }
    }
  }
}
