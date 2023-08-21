package delta.games.lotro.tools.checks;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;

/**
 * Tool to check weapon DPS computations.
 * @author DAM
 */
public class MainCheckWeaponDPS
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
        float dps=weapon.getDPS();
        float computedDPS=weapon.computeDPS();
        float diff=Math.abs(dps-computedDPS);
        if (diff>0.01)
        {
          System.out.println("ID="+id+", name="+name+", level="+level);
          System.out.println("\tDPS="+dps+", computedDPS="+computedDPS);
        }
        nbWeapons++;
      }
    }
    System.out.println("Checked "+nbWeapons+" weapons!");
  }
}
