package delta.games.lotro.tools.lore.items.scalables;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.stats.ScaledArmourComputer;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;

/**
 * Finds armour descriptions from armour attributes (value, location, quality).
 * @author DAM
 */
public class ArmourDescriptionFinder
{
  private static EquipmentLocation[] LOCATIONS={
    EquipmentLocation.HEAD, EquipmentLocation.SHOULDER, EquipmentLocation.CHEST,
    EquipmentLocation.HAND, EquipmentLocation.BACK, EquipmentLocation.LEGS, EquipmentLocation.FEET,
    EquipmentLocation.OFF_HAND
  };
  private static ArmourType[] TYPES={
    ArmourType.HEAVY, ArmourType.MEDIUM, ArmourType.LIGHT
  };
  private static ItemQuality[] QUALITIES={
    ItemQuality.LEGENDARY, ItemQuality.INCOMPARABLE, ItemQuality.RARE, ItemQuality.UNCOMMON
  };

  private Map<EquipmentLocation,Map<ItemQuality,Map<Integer,String>>> _map;

  /**
   * Constructor.
   * @param itemLevel Item level to use.
   * @param verbose Verbose or not.
   */
  public ArmourDescriptionFinder(int itemLevel, boolean verbose)
  {
    if (verbose)
    {
      System.out.println("Armour level:"+itemLevel);
    }
    _map=new HashMap<EquipmentLocation,Map<ItemQuality,Map<Integer,String>>>();
    ScaledArmourComputer computer=new ScaledArmourComputer();
    for(EquipmentLocation location : LOCATIONS)
    {
      Map<ItemQuality,Map<Integer,String>> map=new HashMap<ItemQuality,Map<Integer,String>>();
      _map.put(location,map);
      for(ItemQuality quality : QUALITIES)
      {
        Map<Integer,String> valuesMap=new HashMap<Integer,String>();
        map.put(quality,valuesMap);
        for(ArmourType armourType : TYPES)
        {
          if (location==EquipmentLocation.BACK)
          {
            if (armourType!=ArmourType.LIGHT) continue;
          }
          double armorValue=computer.getArmour(itemLevel,armourType,location,quality,1);
          if (verbose)
          {
            System.out.println("location="+location+", quality="+quality+",type="+armourType+": "+armorValue);
          }
          String label=SlicesBasedItemStatsProvider.getArmorLabel(location,quality,armourType);
          Integer intArmourValue=Integer.valueOf(((int)armorValue)*100);
          String old=valuesMap.put(intArmourValue,label);
          if (old!=null)
          {
            System.err.println("Multiple: "+old+": value="+intArmourValue);
          }
        }
      }
    }
  }

  /**
   * Get the armor description from armour value, location and quality.
   * @param armorValue Armour value.
   * @param armourType Armour type.
   * @param location Location.
   * @param quality Quality.
   * @return An armor description or <code>null</code> if not found.
   */
  public String getArmourDescription(int armorValue, ArmourType armourType, EquipmentLocation location, ItemQuality quality)
  {
    String label=null;
    Map<ItemQuality,Map<Integer,String>> qualityMap=_map.get(location);
    if (quality!=null)
    {
      Map<Integer,String> valuesMap=qualityMap.get(quality);
      if (valuesMap!=null)
      {
        label=valuesMap.get(Integer.valueOf(armorValue));
      }
    }
    if (label==null)
    {
      label=SlicesBasedItemStatsProvider.getArmorLabel(location,quality,armourType);
    }
    return label;
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ArmourDescriptionFinder(95,true);
  }
}
