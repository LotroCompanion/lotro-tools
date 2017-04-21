package delta.games.lotro.tools.lore.items;

import java.util.List;

import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;

/**
 * Performs consistency checks on a collection of items.
 * @author DAM
 */
public class ConsistencyChecks
{
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
      //int id=item.getIdentifier();
      //String name=item.getName();
      // Armours
      if (item instanceof Armour)
      {
        Armour armour=(Armour)item;
        int armourValue=armour.getArmourValue();
        if (armourValue==0)
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
  }
}
