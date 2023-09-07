package delta.games.lotro.tools.checks;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.weapons.WeaponSpeedEntry;

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
      boolean scalable=item.isScalable();
      if (scalable)
      {
        continue;
      }
      if (item instanceof Weapon)
      {
        Weapon weapon=(Weapon)item;
        int id=item.getIdentifier();
        String name=item.getName();
        float dps=weapon.getDPS();
        int minDamage=weapon.getMinDamage();
        int maxDamage=weapon.getMaxDamage();
        WeaponType type=weapon.getWeaponType();
        float speed=0;
        float mod=0;
        ItemQuality quality=weapon.getQuality();
        WeaponSpeedEntry speedEntry=weapon.getSpeed();
        if (speedEntry!=null)
        {
          speed=speedEntry.getBaseActionDuration();
          mod=speedEntry.getBaseAnimationDurationMultiplierModifier();
        }
        String dpsStr=Float.toString(dps).replace('.',',');
        String speedStr=Float.toString(speed).replace('.',',');
        String modStr=Float.toString(mod).replace('.',',');
        System.out.println(id+"\t"+name+"\t"+dpsStr+"\t"+minDamage+"\t"+maxDamage+"\t"+type.getKey()+"\t"+speedStr+"\t"+modStr+"\t"+quality.getKey());
        nbWeapons++;
      }
    }
    System.out.println("Got "+nbWeapons+" weapons!");
  }
}
