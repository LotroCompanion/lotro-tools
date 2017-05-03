package delta.games.lotro.tools.lore.items.scalables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;

/**
 * Find some scalable items using their name.
 * @author DAM
 */
public class ScalableItemsFinder
{
  private static final String[] PREFIXES = { "Lesser", "Potent", "Greater" };
  private static final ItemQuality[] PREFIX_QUALITY = {
    ItemQuality.UNCOMMON, ItemQuality.RARE, ItemQuality.INCOMPARABLE
  };
  private static final String[] ADJECTIVES = {
    "Resolute","Valourous", "Dextrous", "Enduring","Steadfast"
  };

  private static final String[] JEWELS = {
    "Bauble", "Pocket",
    "Necklace", "Bracelet", "Ring", "Earring"
  };
  private static final EquipmentLocation[] JEWEL_LOCATIONS = {
    EquipmentLocation.POCKET, EquipmentLocation.POCKET,
    EquipmentLocation.NECK, EquipmentLocation.WRIST, EquipmentLocation.FINGER, EquipmentLocation.EAR
  };
  private static final String[] ARMOURS = {
    "Hat", "Helm", "Helmet",
    "Shoulderpads", "Shoulderguards",
    "Robe", "Jacket", "Breastplate",
    "Cloak",
    "Gloves", "Gauntlets",
    "Leggings",
    "Boots"
  };
  
  private static final EquipmentLocation[] ARMOUR_LOCATIONS = {
    EquipmentLocation.HEAD, EquipmentLocation.HEAD, EquipmentLocation.HEAD,
    EquipmentLocation.SHOULDER, EquipmentLocation.SHOULDER,
    EquipmentLocation.CHEST, EquipmentLocation.CHEST, EquipmentLocation.CHEST,
    EquipmentLocation.BACK,
    EquipmentLocation.HAND, EquipmentLocation.HAND,
    EquipmentLocation.LEGS,
    EquipmentLocation.FEET
  };
  private static final ArmourType[] ARMOUR_TYPES = {
    ArmourType.LIGHT, null, null,
    null, ArmourType.HEAVY,
    ArmourType.LIGHT, ArmourType.MEDIUM, ArmourType.HEAVY,
    ArmourType.LIGHT,
    null, ArmourType.HEAVY,
    null,
    null
  };
  private static final String[] WEAPONS = {
    "Axe", "Club", "Dagger", "Hammer", "Mace", "Sword", "Bow", "Crossbow"
  };
  private static final WeaponType[] WEAPON_TYPES = {
    WeaponType.ONE_HANDED_AXE, WeaponType.ONE_HANDED_CLUB, WeaponType.DAGGER,
    WeaponType.ONE_HANDED_HAMMER, WeaponType.ONE_HANDED_MACE,
    WeaponType.ONE_HANDED_SWORD, WeaponType.BOW, WeaponType.CROSSBOW
  };
  private static final String[] SHIELDS = {
    "Warden's Shield", "Shield", "Heavy Shield"
  };
  private static final ArmourType[] SHIELD_TYPES = {
    ArmourType.WARDEN_SHIELD, ArmourType.SHIELD, ArmourType.HEAVY_SHIELD
  };
  private static final String[][] ITEMS = {
    JEWELS, ARMOURS, WEAPONS, SHIELDS
  };

  private static final String[] SUFFIXES = {
    "of Finesse", "of Fate", "of Tactics", "of Penetration", "of Prowess",
    "of Evasion", "of Protection", "of Perseverance", "of Parrying", "of Blocking"
  };

  /**
   * Find some scalable items.
   * @param items Items to search.
   * @return A list of selected items.
   */
  public List<Item> findScalableItems(List<Item> items)
  {
    List<Item> ret=new ArrayList<Item>();
    Map<String,Item> generatedItems=generateItems();
    for(Item item : items)
    {
      Item generated=generatedItems.get(item.getName());
      if (generated!=null)
      {
        improveArmourType(generated,item);
        ret.add(item);
      }
    }
    return ret;
  }

  private void improveArmourType(Item generated, Item item)
  {
    if (generated instanceof Armour)
    {
      ArmourType type=((Armour)generated).getArmourType();
      if (type!=null)
      {
        if (item instanceof Armour)
        {
          Armour armour=(Armour)item;
          if (armour.getArmourType()==null)
          {
            armour.setArmourType(type);
          }
        }
      }
    }
  }

  private Map<String,Item> generateItems()
  {
    Map<String,Item> ret=new HashMap<String,Item>();
    for(int i=0;i<PREFIXES.length;i++)
    {
      ItemQuality quality=PREFIX_QUALITY[i];
      String prefix=PREFIXES[i];
      for(String[] items : ITEMS)
      {
        for(int j=0;j<items.length;j++)
        {
          String itemName=items[j];
          EquipmentLocation location=null;
          ArmourType armourType=null;
          WeaponType weaponType=null;
          if (items==JEWELS)
          {
            location=JEWEL_LOCATIONS[j];
          }
          else if (items==ARMOURS)
          {
            location=ARMOUR_LOCATIONS[j];
            armourType=ARMOUR_TYPES[j];
          }
          else if (items==SHIELDS)
          {
            location=EquipmentLocation.OFF_HAND;
            armourType=SHIELD_TYPES[j];
          }
          else if (items==WEAPONS)
          {
            weaponType=WEAPON_TYPES[j];
            location=weaponType.isRanged()?EquipmentLocation.RANGED_ITEM:EquipmentLocation.MAIN_HAND;
          }
          for(String adjective : ADJECTIVES)
          {
            for(String suffix : SUFFIXES)
            {
              String name=generateName(prefix,adjective,itemName,suffix);
              Item item=null;
              if (armourType!=null)
              {
                Armour armour=new Armour();
                armour.setArmourType(armourType);
                item=armour;
              }
              else if (weaponType!=null)
              {
                Weapon weapon=new Weapon();
                weapon.setWeaponType(weaponType);
                item=weapon;
              }
              else
              {
                item=new Item();
              }
              item.setName(name);
              item.setQuality(quality);
              item.setEquipmentLocation(location);
              ret.put(name,item);
            }
          }
        }
      }
    }
    return ret;
  }

  private String generateName(String prefix, String adjective, String item, String suffix)
  {
    return prefix+" "+adjective+" "+item+" "+suffix;
  }
}
