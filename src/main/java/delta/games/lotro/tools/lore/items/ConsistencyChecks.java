package delta.games.lotro.tools.lore.items;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Performs consistency checks on a collection of items.
 * @author DAM
 */
public class ConsistencyChecks
{
  private List<Item> _missingStats;
  private int _nbMissingStats;
  private int _nbStats;
  private int _nbLegendaryItems;

  /**
   * Constructor.
   */
  public ConsistencyChecks()
  {
    _missingStats=new ArrayList<Item>();
  }

  /**
   * Perform consistency checks.
   * @param items Items to use.
   */
  public void consistencyChecks(List<Item> items)
  {
    int nbMissingArmourValues=0;
    int nbMissingArmourTypes=0;
    int nbMissingWeaponTypes=0;
    for(Item item : items)
    {
      checkItemStats(item);
      //int id=item.getIdentifier();
      //String name=item.getName();
      // Armours
      if (item instanceof Armour)
      {
        Armour armour=(Armour)item;
        FixedDecimalsInteger armourValue=armour.getStats().getStat(STAT.ARMOUR);
        if (armourValue==null)
        {
          nbMissingArmourValues++;
          //System.out.println("No armour value for: " + name + " (" + id + ')');
        }
        ArmourType type=armour.getArmourType();
        if (type==null)
        {
          nbMissingArmourTypes++;
          //System.out.println("No armour type for: " + name + " (" + id + ')');
        }
      }
      // Weapons
      if (item instanceof Weapon)
      {
        Weapon weapon=(Weapon)item;
        WeaponType type=weapon.getWeaponType();
        if (type==null)
        {
          nbMissingWeaponTypes++;
          //System.out.println("No weapon type for: " + name + " (" + id + ')');
        }
      }
    }
    System.out.println("Nb armours with missing armour type: " + nbMissingArmourTypes);
    System.out.println("Nb armours with missing armour value: " + nbMissingArmourValues);
    System.out.println("Nb weapons with missing type: " + nbMissingWeaponTypes);
    System.out.println("Nb legendary items: " + _nbLegendaryItems);
    System.out.println("Nb items with stats: " + _nbStats);
    System.out.println("Nb items with missing stats: " + _nbMissingStats);
  }

  private void checkItemStats(Item item) 
  {
    EquipmentLocation location=item.getEquipmentLocation();
    if (location!=null)
    {
      boolean isLegendary = ((item instanceof Legendary) || (location==EquipmentLocation.BRIDLE));
      if (isLegendary)
      {
        _nbLegendaryItems++;
      }
      else
      {
        boolean ok=true;
        BasicStatsSet stats=item.getStats();
        if (stats.getStatsCount()==0)
        {
          ok=false;
        }
        if (!ok)
        {
          _missingStats.add(item);
          _nbMissingStats++;
          //if ((location==EquipmentLocation.BACK) || (location==EquipmentLocation.LEGS))
          {
            //System.out.println("No stat for item: " + item + " at " + location);
          }
        }
        else
        {
          _nbStats++;
        }
      }
    }
  }
}
