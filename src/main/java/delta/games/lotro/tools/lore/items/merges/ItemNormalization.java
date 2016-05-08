package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemCategory;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;
import delta.games.lotro.utils.FixedDecimalsInteger;

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

  /**
   * Do the job.
   */
  public void doIt()
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

  private Item normalizeItem(Item item)
  {
    Item ret=normalizeArmours(item);
    ret=normalizeWeapons(ret);
    ret=normalizeCrafting(ret);
    ret=normalizeJewels(ret);
    ret=normalizeRecipes(ret);
    ret=normalizeLegendaryItem(ret);
    ret=normalizeCraftingTool(ret);
    ret=normalizeInstrument(ret);
    return ret;
  }

  private Item normalizeCraftingTool(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("32".equals(category))
    {
      String oldCategory=item.getSubCategory();
      if ((!"32".equals(oldCategory)) && (!"Craft Tool".equals(oldCategory)))
      {
        System.out.println("Warn: expected 'Craft Tool', got '"+oldCategory+"'");
      }
      item.setSubCategory("Crafting Tool");
      item.setEquipmentLocation(EquipmentLocation.TOOL);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeInstrument(Item item)
  {
    String category=item.getProperty(ItemPropertyNames.TULKAS_CATEGORY);
    if ("11".equals(category))
    {
      String oldCategory=item.getSubCategory();
      if ((oldCategory!=null) && (!"11".equals(oldCategory)) && (!oldCategory.startsWith("Instrument")))
      {
        System.out.println("Warn: expected 'Instrument', got '"+oldCategory+"'");
      }
      item.setSubCategory("Instrument");
      item.setEquipmentLocation(EquipmentLocation.RANGED_ITEM);
      item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
    }
    return item;
  }

  private Item normalizeWeapons(Item item)
  {
    item=setWeaponTypeFromCategory(item,"10",WeaponType.DAGGER);
    item=setWeaponTypeFromCategory(item,"36",WeaponType.HALBERD);
    item=setWeaponTypeFromCategory(item,"110",WeaponType.JAVELIN);
    item=setWeaponTypeFromCategory(item,"29",WeaponType.CROSSBOW);
    item=setWeaponTypeFromCategory(item,"46",WeaponType.SPEAR);
    item=setWeaponTypeFromCategory(item,"One-handed Axe",WeaponType.ONE_HANDED_AXE);
    item=setWeaponTypeFromCategory(item,"Two-handed Axe",WeaponType.TWO_HANDED_AXE);
    item=setWeaponTypeFromCategory(item,"One-handed Sword",WeaponType.ONE_HANDED_SWORD);
    item=setWeaponTypeFromCategory(item,"Two-handed Sword",WeaponType.TWO_HANDED_SWORD);
    item=setWeaponTypeFromCategory(item,"One-handed Club",WeaponType.ONE_HANDED_CLUB);
    item=setWeaponTypeFromCategory(item,"Rune-stone",WeaponType.RUNE_STONE);
    item=setWeaponTypeFromCategory(item,"Staff",WeaponType.STAFF);
    item=setWeaponTypeFromCategory(item,"Dagger",WeaponType.DAGGER);
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
    // TODO: these categories
    /*
        104=Rune-stone
        1=Bow and some Javelin!
        12 => {One-handed Axe=165, Two-handed Axe=84}
        => Axe
        24 => {Two-handed Hammer=21, One-handed Hammer=131}
        => Hammer
        34=Staff (legendary or not - loremaster only...)
        40 => {Two-handed Club=86, One-handed Club=148}
        => Club
        30 => {One-handed Mace=163, One-handed Hammer=28, One-handed Club=34}
        => Misc one-handed mace/hammer or club
        44 => {Two-handed Sword=127, One-handed Mace=1, Halberd=16, Dagger=7, Two-handed Hammer=21, Two-handed Club=13, One-handed Sword=238, One-handed Club=3, Two-handed Axe=26}
        => Misc main hand weapon (one or two handed)
     */


    
    return item;
  }

  private Item setWeaponTypeFromCategory(Item item, String category, WeaponType type)
  {
    Weapon ret=null;
    if (category.equals(item.getSubCategory()))
    {
      if (item.getClass()==Item.class)
      {
        ret=new Weapon();
        ret.copyFrom(item);
      }
      else
      {
        ret=(Weapon)item;
      }
      ret.setWeaponType(type);
    }
    return (ret!=null)?ret:item;
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
      else if ("Shield".equals(category))
      {
        if ((previous==ArmourType.LIGHT) || (previous==null))
        {
          armour.setArmourType(ArmourType.SHIELD);
          armour.setSubCategory(null);
          armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
          armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
          armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        }
      }
      else if ("Heavy Shield".equals(category))
      {
        armour.setArmourType(ArmourType.HEAVY_SHIELD);
        armour.setSubCategory(null);
        armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
        armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      }
      else if ("Warden's Shield".equals(category))
      {
        armour.setArmourType(ArmourType.WARDEN_SHIELD);
        armour.setSubCategory(null);
        armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
        armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
      }
      else if ("33".equals(category))
      {
        ArmourType type=armour.getArmourType();
        if (type==null)
        {
          String name=armour.getName();
          if (name.toLowerCase().contains("warden"))
          {
            armour.setArmourType(ArmourType.WARDEN_SHIELD);
          }
          else if ((name.indexOf("Heavy Shield")!=-1) || (name.indexOf("Bulwark")!=-1)
              || (name.indexOf("Battle-shield")!=-1))
          {
            armour.setArmourType(ArmourType.HEAVY_SHIELD);
          }
          else
          {
            // Default as shield
            armour.setArmourType(ArmourType.SHIELD);
          }
        }
        type=armour.getArmourType();
        if (type!=null)
        {
          armour.setSubCategory(null);
          armour.setEquipmentLocation(EquipmentLocation.OFF_HAND);
          armour.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
          armour.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
          if (type==ArmourType.WARDEN_SHIELD)
          {
            armour.setRequiredClass(CharacterClass.WARDEN);
          }
        }
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
      if (ret instanceof Armour)
      {
        Armour armour=(Armour)ret;
        int armourValue=armour.getArmourValue();
        ret.getStats().setStat(STAT.ARMOUR,new FixedDecimalsInteger(armourValue));
        ret.setCategory(ItemCategory.ITEM);
      }
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

  private static final String[] PROFESSIONS = {"Prospector", "Farmer", "Forester", "Woodworker", "Cook", "Jeweller", "Metalsmith", "Weaponsmith", "Tailor", "Scholar" };

  private static int[][] CRAFTING_CATEGORIES = {
    { 60, 61, 63, 67, 65, 59, 64, 66, 58, 62},
    { 134, 162, 122, 147, 113, 143, 141, 148, 155, 139},
    { 135, 121, 127, 152, 114, 146, 144, 153, 160, 140},
    { 136, 126, 130, 157, 115, 151, 149, 158, 120, 142},
    { 137, 129, 132, 118, 116, 156, 154, 119, 125, 145},
    { 138, 131, 133, 123, 117, 161, 159, 124, 128, 150},
    { 202, 200, 201, 197, 203, 196, 195, 198, 199, 204},
    { 215, 213, 214, 210, 216, 209, 208, 211, 212, 217},
    { 228, 226, 227, 223, 229, 222, 221, 224, 225, 220}
  };

  private Item normalizeRecipes(Item item)
  {
    boolean found=false;
    for(int i=0;i<CRAFTING_CATEGORIES.length;i++)
    {
      for(int j=0;j<CRAFTING_CATEGORIES[0].length;j++)
      {
        String craftingCategory=String.valueOf(CRAFTING_CATEGORIES[i][j]);
        String subCategory=item.getSubCategory();
        if (craftingCategory.equals(subCategory))
        {
          String newCategory="Recipe:"+PROFESSIONS[j]+":Tier"+(i+1);
          item.setSubCategory(newCategory);
          item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
          item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
          found=true;
          break;
        }
      }
      if (found)
      {
        break;
      }
    }
    return item;
  }

  private Item normalizeLegendaryItem(Item item)
  {
    String name=item.getName();
    ItemQuality quality=null;
    if (name==null) return item;
    if (name.endsWith("of the First Age")) quality=ItemQuality.LEGENDARY;
    else if (name.endsWith("of the Second Age")) quality=ItemQuality.INCOMPARABLE;
    else if (name.endsWith("of the Third Age")) quality=ItemQuality.RARE;
    if (quality!=null)
    {
      CharacterClass cClass=null;
      if (name.indexOf("Champion")!=-1) cClass=CharacterClass.CHAMPION;
      else if (name.indexOf("Captain")!=-1) cClass=CharacterClass.CAPTAIN;
      else if (name.indexOf("Beorning")!=-1) cClass=CharacterClass.BEORNING;
      else if (name.indexOf("Burglar")!=-1) cClass=CharacterClass.BURGLAR;
      else if (name.indexOf("Guardian")!=-1) cClass=CharacterClass.GUARDIAN;
      else if (name.indexOf("Hunter")!=-1) cClass=CharacterClass.HUNTER;
      else if (name.indexOf("Lore-master")!=-1) cClass=CharacterClass.LORE_MASTER;
      else if (name.indexOf("Minstrel")!=-1) cClass=CharacterClass.MINSTREL;
      else if (name.indexOf("Rune-keeper")!=-1) cClass=CharacterClass.RUNE_KEEPER;
      else if (name.indexOf("Warden")!=-1) cClass=CharacterClass.WARDEN;

      WeaponType weaponType=null;
      String classItemType=null;
      boolean bridle=false;
      if (name.indexOf("Great Club")!=-1) weaponType=WeaponType.TWO_HANDED_CLUB;
      else if (name.indexOf("Great Axe")!=-1) weaponType=WeaponType.TWO_HANDED_AXE;
      else if (name.indexOf("Great Sword")!=-1) weaponType=WeaponType.TWO_HANDED_SWORD;
      else if (name.indexOf("Greatsword")!=-1) weaponType=WeaponType.TWO_HANDED_SWORD;
      else if (name.indexOf("Great Hammer")!=-1) weaponType=WeaponType.TWO_HANDED_HAMMER;
      else if (name.indexOf("Halberd")!=-1) weaponType=WeaponType.HALBERD;
      else if (name.indexOf("Club")!=-1) weaponType=WeaponType.ONE_HANDED_CLUB;
      else if (name.indexOf("Axe")!=-1) weaponType=WeaponType.ONE_HANDED_AXE;
      else if (name.indexOf("Sword")!=-1) weaponType=WeaponType.ONE_HANDED_SWORD;
      else if (name.indexOf("Hammer")!=-1) weaponType=WeaponType.ONE_HANDED_HAMMER;
      else if (name.indexOf("Dagger")!=-1) weaponType=WeaponType.DAGGER;
      else if (name.indexOf("Spear")!=-1) weaponType=WeaponType.SPEAR;
      else if (name.indexOf("Mace")!=-1) weaponType=WeaponType.ONE_HANDED_MACE;
      else if (name.indexOf("Rune-stone")!=-1)
      {
        weaponType=WeaponType.RUNE_STONE;
        cClass=CharacterClass.RUNE_KEEPER;
      }
      else if (name.indexOf("Stone")!=-1)
      {
        weaponType=WeaponType.RUNE_STONE;
        cClass=CharacterClass.RUNE_KEEPER;
      }
      else if (name.indexOf("Staff")!=-1) weaponType=WeaponType.STAFF;
      else if (name.indexOf("Carving")!=-1) classItemType="Carving";
      else if (name.indexOf("Tools")!=-1) classItemType="Tools";
      else if (name.indexOf("Emblem")!=-1) classItemType="Emblem";
      else if (name.indexOf("Rune-satchel")!=-1) classItemType="Rune-satchel";
      else if (name.indexOf("Rune")!=-1) classItemType="Rune";
      else if (name.indexOf("Belt")!=-1) classItemType="Belt";
      else if (name.indexOf("Crossbow")!=-1) weaponType=WeaponType.CROSSBOW;
      else if (name.indexOf("Bow")!=-1) weaponType=WeaponType.BOW;
      else if (name.indexOf("Book")!=-1) classItemType="Book";
      else if (name.indexOf("Songbook")!=-1) classItemType="Songbook";
      else if (name.indexOf("Javelin")!=-1) weaponType=WeaponType.JAVELIN;
      else if (name.indexOf("Bridle")!=-1) bridle=true;

      if ((weaponType!=null) || (classItemType!=null) || (bridle))
      {
        item.setQuality(quality);
        item.setRequiredClass(cClass);
        String category=null;
        if (bridle)
        {
          category="Legendary Bridle";
          item.setCategory(ItemCategory.ITEM);
        }
        else if (classItemType!=null)
        {
          category="Legendary Class Item:"+classItemType;
          item.setCategory(ItemCategory.ITEM);
        }
        else if (weaponType!=null)
        {
          category="Legendary Weapon";
          Weapon weapon;
          if (item instanceof Weapon)
          {
            weapon=(Weapon)item;
          }
          else
          {
            weapon=new Weapon();
            weapon.copyFrom(item);
            item=weapon;
          }
          weapon.setWeaponType(weaponType);
        }
        item.setSubCategory(category);
        item.removeProperty(ItemPropertyNames.TULKAS_CATEGORY);
        item.removeProperty(ItemPropertyNames.LEGACY_CATEGORY);
        //System.out.println(name+", class="+cClass+", category="+category+", weapon type="+type);
      }
    }
    return item;
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
