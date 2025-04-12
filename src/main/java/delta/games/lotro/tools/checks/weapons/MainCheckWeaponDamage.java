package delta.games.lotro.tools.checks.weapons;

import delta.common.utils.io.Console;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;

/**
 * Tool to check weapon damage computations.
 * @author DAM
 */
public class MainCheckWeaponDamage
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    int nbWeapons=0;
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      Integer level=item.getItemLevel();
      if (item instanceof Weapon)
      {
        Weapon weapon=(Weapon)item;
        int id=item.getIdentifier();
        String name=item.getName();
        // Min damage
        float computedMinDamage=weapon.computeMinDamage(level.intValue());
        int minDamage=weapon.getMinDamage();
        float diffMinDamage=Math.abs(minDamage-Math.round(computedMinDamage));
        if (diffMinDamage>0.01)
        {
          Console.println("ID="+id+", name="+name+", level="+level);
          Console.println("\tMin damage="+minDamage+", computedMinDamage="+Math.round(computedMinDamage));
        }
        float computedMaxDamage=weapon.computeMaxDamage(level.intValue());
        int maxDamage=weapon.getMaxDamage();
        float diffMaxDamage=Math.abs(maxDamage-Math.round(computedMaxDamage));
        if (diffMaxDamage>0.01)
        {
          Console.println("ID="+id+", name="+name+", level="+level);
          Console.println("\tMax damage="+maxDamage+", computedMaxDamage="+Math.round(computedMaxDamage));
        }
        nbWeapons++;
      }
    }
    Console.println("Checked "+nbWeapons+" weapons!");
  }
}
