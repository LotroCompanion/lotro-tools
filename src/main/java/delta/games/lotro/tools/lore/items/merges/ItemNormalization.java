package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Normalize items database "release candidate".
 * @author DAM
 */
public class ItemNormalization
{
  private HashMap<Integer,Item> loadItemsFile(File file)
  {
    ItemXMLParser parser=new ItemXMLParser();
    List<Item> items=parser.parseItemsFile(file);
    HashMap<Integer,Item> ret=new HashMap<Integer,Item>();
    for(Item item : items)
    {
      ret.put(Integer.valueOf(item.getIdentifier()),item);
    }
    return ret;
  }

  /**
   * Constructor.
   */
  public ItemNormalization()
  {
    // Nothing to do!
  }

  private void doIt()
  {
    File file1=new File("items-rc.xml");
    HashMap<Integer,Item> sourceItems=loadItemsFile(file1);
    System.out.println(sourceItems.size());

    List<Integer> ids=new ArrayList<Integer>(sourceItems.keySet());
    for(Integer id : ids)
    {
      Item sourceItem=sourceItems.get(id);
      sourceItem=normalizeItem(sourceItem);
      sourceItems.put(id,sourceItem);
    }
    File toFile=new File("items.xml").getAbsoluteFile();
    List<Item> items=new ArrayList<Item>(sourceItems.values());
    ItemsManager.getInstance().writeItemsFile(toFile,items);
  }

  private Item normalizeItem(Item source)
  {
    Item ret=normalizeCategory(source);
    return ret;
  }

  private Item normalizeCategory(Item item)
  {
    Item ret=normalizeArmours(item);
    ret=normalizeWeapons(ret);
    ret=normalizeCrafting(ret);
    ret=normalizeJewels(ret);
    // TODO weapons
    // TODO find out legendary items
    // Reshaped/Reforged/Crafted/Unearthed Class's XXX of the First/Second/Third Age
    // Captain's Emblem of the xxx Age
    // TODO recipes
    return ret;
  }

  private Item normalizeWeapons(Item item)
  {
    item=setWeaponTypeFromCategory(item,"10",WeaponType.DAGGER);
    item=setWeaponTypeFromCategory(item,"36",WeaponType.HALBERD);
    item=setWeaponTypeFromCategory(item,"110",WeaponType.JAVELIN);
    item=setWeaponTypeFromCategory(item,"29",WeaponType.CROSSBOW);
    item=setWeaponTypeFromCategory(item,"46",WeaponType.SPEAR);
    if (item instanceof Weapon) {
      Weapon weapon=(Weapon)item;
      WeaponType type=weapon.getWeaponType();
      if (type!=null)
      {
        EquipmentLocation loc=type.isRanged()?EquipmentLocation.RANGED_ITEM:EquipmentLocation.MAIN_HAND;
        weapon.setEquipmentLocation(loc);
        item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        item.setSubCategory(null);
      }
    }
    return item;
  }

  private Item setWeaponTypeFromCategory(Item item, String category, WeaponType type)
  {
    Item ret=item;
    if (category.equals(item.getSubCategory()))
    {
      if (item.getClass()==Item.class)
      {
        Weapon weapon=new Weapon();
        ret=weapon;
        weapon.copyFrom(item);
        weapon.setWeaponType(type);
      }
    }
    return ret;
  }
  /*
    ========= WEAPONS ==========
    1=Bow and some Javelin!
    104=Rune-stone
    187=Horn
    12 => {One-handed Axe=165, Two-handed Axe=84}
    => Axe
    24 => {Two-handed Hammer=21, One-handed Hammer=131}
    => Hammer
    34=Staff (legendary or not - loremaster only...)
    40 => {Two-handed Club=86, One-handed Club=148}
    => Club
    54=Thrown Weapon
    192=Satchel

    30 => {One-handed Mace=163, One-handed Hammer=28, One-handed Club=34}
    => Misc one-handed mace/hammer or club
    44 => {Two-handed Sword=127, One-handed Mace=1, Halberd=16, Dagger=7, Two-handed Hammer=21, Two-handed Club=13, One-handed Sword=238, One-handed Club=3, Two-handed Axe=26}
    => Misc main hand weapon (one or two handed)
    */

  private Item normalizeArmours(Item item)
  {
    int id=item.getIdentifier();
    if (item instanceof Armour)
    {
      Armour armour=(Armour)item;
      String category=armour.getSubCategory();
      ArmourType previous=armour.getArmourType();
      if ("Heavy Armour".equals(category))
      {
        if ((previous!=null) && (previous!=ArmourType.HEAVY))
        {
          System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.HEAVY);
        }
        armour.setArmourType(ArmourType.HEAVY);
        armour.setSubCategory(null);
      }
      else if ("Medium Armour".equals(category))
      {
        if ((previous!=null) && (previous!=ArmourType.MEDIUM))
        {
          System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.MEDIUM);
        }
        armour.setArmourType(ArmourType.MEDIUM);
        armour.setSubCategory(null);
      }
      else if ("Light Armour".equals(category))
      {
        if ((previous!=null) && (previous!=ArmourType.LIGHT))
        {
          System.out.println("ID: " + id+": armour type conflict: was=" + previous + ", should be=" + ArmourType.LIGHT);
        }
        armour.setArmourType(ArmourType.LIGHT);
        armour.setSubCategory(null);
      }
    }
    Item ret=normalizeArmour(item, "3", "Chest", EquipmentLocation.CHEST);
    ret=normalizeArmour(ret, "5", "Hands", EquipmentLocation.HAND);
    ret=normalizeArmour(ret, "6", "Shoulders", EquipmentLocation.SHOULDER);
    ret=normalizeArmour(ret, "7", "Head", EquipmentLocation.HEAD);
    ret=normalizeArmour(ret, "15", "Leggings", EquipmentLocation.LEGS);
    ret=normalizeArmour(ret, "23", "Boots", EquipmentLocation.FEET);
    ret=normalizeArmour(ret, "45", "Cloak", EquipmentLocation.BACK);

    // Check shields
    //33 => {Warden's Shield=99, Shield=107, Heavy Shield=75}
    //=> Shield (any type)
    return ret;
  }

  private Item normalizeArmour(Item item, String categoryInt, String expectedCategoryStr, EquipmentLocation loc)
  {
    Item ret=item;
    int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (categoryInt.equals(categoryProp)) {
      Armour armour=null;
      if (!(item instanceof Armour))
      {
        armour=new Armour();
        armour.copyFrom(item);
      }
      else
      {
        armour=(Armour)item;
      }
      ret=armour;
      // Location
      {
        EquipmentLocation previousLoc=ret.getEquipmentLocation();
        if ((previousLoc!=null) && (previousLoc!=loc))
        {
          System.out.println("ID: " + id+": loc conflict: was=" + previousLoc + ", should be=" + loc);
        }
        armour.setEquipmentLocation(loc);
      }
      // Sub category
      {
        String previousSubCategory=ret.getSubCategory();
        if ((previousSubCategory!=null) && (previousSubCategory.length()>0) && 
            (!previousSubCategory.equals(categoryInt)) && (!previousSubCategory.equalsIgnoreCase(expectedCategoryStr)))
        {
          System.out.println("ID: " + id+": armour category conflict: was=" + previousSubCategory + ", should be=" + expectedCategoryStr);
        }
        armour.setSubCategory(null);
      }
      armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return ret;
  }

  private Item normalizeCrafting(Item item)
  {
    Item ret=normalizeCrafting(item, "37");
    ret=normalizeCrafting(ret, "38");
    ret=normalizeCrafting(ret, "56");
    ret=normalizeCrafting(ret, "188");
    return ret;
  }

  private Item normalizeCrafting(Item item, String categoryInt)
  {
    Item ret=item;
    //int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if (categoryInt.equals(categoryProp)) {
      String previousSubCategory=ret.getSubCategory();
      if ((previousSubCategory!=null) && (previousSubCategory.length()>0) && 
          (!previousSubCategory.equals(categoryInt)))
      {
        //System.out.println("ID: " + id+": crafting category conflict: was=" + previousSubCategory);
      }
      ret.setSubCategory("Crafting Item");
      ret.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      ret.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return ret;
  }

  private Item normalizeJewels(Item item)
  {
    Item ret=item;
    //int id=item.getIdentifier();
    String categoryProp=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    String subCategory=item.getSubCategory();
    if (("49".equals(categoryProp)) || ("49".equals(subCategory))) {
      String previousSubCategory=ret.getSubCategory();
      //Pocket=103, Wrist=211, Ear=211, Neck=170, Finger=198
      if ("Pocket".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.POCKET);
        ret.setSubCategory(null);
      }
      else if ("Wrist".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.WRIST);
        ret.setSubCategory(null);
      }
      else if ("Ear".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.EAR);
        ret.setSubCategory(null);
      }
      else if ("Neck".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.NECK);
        ret.setSubCategory(null);
      }
      else if ("Finger".equals(previousSubCategory))
      {
        ret.setEquipmentLocation(EquipmentLocation.FINGER);
        ret.setSubCategory(null);
      }
      else
      {
        EquipmentLocation loc=ret.getEquipmentLocation();
        if (loc!=null)
        {
          ret.setSubCategory(null);
        }
        else
        {
          ret.setSubCategory("Jewelry");
          normalizeJewelByName(ret,"Token",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Earring",EquipmentLocation.EAR);
          normalizeJewelByName(ret,"Bauble",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Necklace",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Ring",EquipmentLocation.FINGER);
          normalizeJewelByName(ret,"Bracelet",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Cuff",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Barrow-brie",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Stone",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Pendant",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Choker",EquipmentLocation.NECK);
          normalizeJewelByName(ret,"Band ",EquipmentLocation.FINGER);
          normalizeJewelByName(ret,"Armlet",EquipmentLocation.WRIST);
          normalizeJewelByName(ret,"Mountain-stone",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Scroll",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Phial",EquipmentLocation.POCKET);
          normalizeJewelByName(ret,"Pocket-square",EquipmentLocation.POCKET);
        }
      }
      ret.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      ret.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return ret;
  }

  private void normalizeJewelByName(Item item, String pattern, EquipmentLocation loc)
  {
    String name=item.getName();
    if (name.contains(pattern))
    {
      item.setEquipmentLocation(loc);
      item.setSubCategory(null);
    }
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemNormalization().doIt();
  }
}
