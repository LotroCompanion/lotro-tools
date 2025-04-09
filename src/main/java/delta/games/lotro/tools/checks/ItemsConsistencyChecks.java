package delta.games.lotro.tools.checks;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.EquipmentLocations;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.legendary.Legendary;

/**
 * Performs consistency checks on a collection of items.
 * @author DAM
 */
public class ItemsConsistencyChecks
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ItemsConsistencyChecks.class);

  private List<Item> _missingStats;
  private int _nbMissingStats;
  private int _nbStats;
  private int _nbLegendaryItems;

  /**
   * Constructor.
   */
  public ItemsConsistencyChecks()
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
      item.setStatsFromStatsProvider();
      checkItemStats(item);
      // Armours
      if (item instanceof Armour)
      {
        Armour armour=(Armour)item;
        Number armourValue=armour.getStats().getStat(WellKnownStat.ARMOUR);
        if (armourValue==null)
        {
          nbMissingArmourValues++;
        }
        ArmourType type=armour.getArmourType();
        if (type==null)
        {
          nbMissingArmourTypes++;
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
        }
      }
    }
    LOGGER.info("Items statistics:");
    LOGGER.info("Nb armours with missing armour type: {}",Integer.valueOf(nbMissingArmourTypes));
    LOGGER.info("Nb armours with missing armour value: {}",Integer.valueOf(nbMissingArmourValues));
    LOGGER.info("Nb weapons with missing type: {}",Integer.valueOf(nbMissingWeaponTypes));
    LOGGER.info("Nb legendary items: {}",Integer.valueOf(_nbLegendaryItems));
    LOGGER.info("Nb items with stats: {}",Integer.valueOf(_nbStats));
    LOGGER.info("Nb items with missing stats: {}",Integer.valueOf(_nbMissingStats));
  }

  private void checkItemStats(Item item)
  {
    EquipmentLocation location=item.getEquipmentLocation();
    if (location!=null)
    {
      boolean isLegendary = ((item instanceof Legendary) || (location==EquipmentLocations.BRIDLE));
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
        }
        else
        {
          _nbStats++;
        }
      }
    }
  }
}
