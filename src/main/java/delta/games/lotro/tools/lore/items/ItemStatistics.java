package delta.games.lotro.tools.lore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.comparators.ArmourTypeComparator;
import delta.games.lotro.lore.items.comparators.ItemQualityComparator;
import delta.games.lotro.lore.items.comparators.WeaponTypeComparator;

/**
 * Computes and display statistics on a collection of items.
 * @author DAM
 */
public class ItemStatistics
{
  // General
  int _itemsCount;
  private HashMap<ItemQuality,IntegerHolder> _itemsByQuality;
  private HashMap<String,IntegerHolder> _itemsBySubCategory;

  // Armour
  private int _armoursCount;
  private HashMap<ArmourType,IntegerHolder> _armoursByType;

  // Weapons
  private int _weaponsCount;
  private HashMap<WeaponType,IntegerHolder> _weaponsByType;

  /**
   * Constructor.
   */
  public ItemStatistics() 
  {
    _itemsByQuality=new HashMap<ItemQuality,IntegerHolder>();
    _armoursByType=new HashMap<ArmourType,IntegerHolder>();
    _weaponsByType=new HashMap<WeaponType,IntegerHolder>();
    _itemsBySubCategory=new HashMap<String,IntegerHolder>();
  }

  /**
   * Compute and show statistics on items.
   * @param items Items to use.
   */
  public void showStatistics(List<Item> items)
  {
    for(Item item : items)
    {
      handleItem(item);
      if (item instanceof Armour)
      {
        handleArmour((Armour)item);
      }
      if (item instanceof Weapon)
      {
        handleWeapon((Weapon)item);
      }
    }
    showStatistics();
  }

  private void handleItem(Item item) 
  {
    // Global count
    _itemsCount++;
    // By quality
    {
      ItemQuality quality=item.getQuality();
      IntegerHolder counter=_itemsByQuality.get(quality);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _itemsByQuality.put(quality,counter);
      }
      counter.increment();
    }
    // By sub-category
    {
      String subCategory=item.getSubCategory();
      if (subCategory==null) subCategory="";
      IntegerHolder counter=_itemsBySubCategory.get(subCategory);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _itemsBySubCategory.put(subCategory,counter);
      }
      counter.increment();
    }
  }

  private void handleArmour(Armour armour) 
  {
    // Global count
    _armoursCount++;
    // By type
    {
      ArmourType type=armour.getArmourType();
      IntegerHolder counter=_armoursByType.get(type);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _armoursByType.put(type,counter);
      }
      counter.increment();
    }
  }

  private void handleWeapon(Weapon weapon) 
  {
    // Global count
    _weaponsCount++;
    // By type
    {
      WeaponType type=weapon.getWeaponType();
      IntegerHolder counter=_weaponsByType.get(type);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _weaponsByType.put(type,counter);
      }
      counter.increment();
    }
  }

  private void showStatistics()
  {
    // Items
    System.out.println("Items: " + _itemsCount);
    // - qualities
    System.out.println("\tBy quality:");
    List<ItemQuality> itemQualities=new ArrayList<ItemQuality>(_itemsByQuality.keySet());
    Collections.sort(itemQualities,new ItemQualityComparator());
    for(ItemQuality itemQuality : itemQualities)
    {
      IntegerHolder count=_itemsByQuality.get(itemQuality);
      System.out.println("\t"+itemQuality+": "+count);
    }
    // - sub-categories
    System.out.println("\tBy sub-category:");
    List<String> itemSubCategories=new ArrayList<String>(_itemsBySubCategory.keySet());
    Collections.sort(itemSubCategories);
    for(String itemSubCategory : itemSubCategories)
    {
      IntegerHolder count=_itemsBySubCategory.get(itemSubCategory);
      System.out.println("\t"+itemSubCategory+": "+count);
    }
    // Armours
    System.out.println("Armours: " + _armoursCount);
    List<ArmourType> armourTypes=new ArrayList<ArmourType>(_armoursByType.keySet());
    Collections.sort(armourTypes,new ArmourTypeComparator());
    for(ArmourType armourType : armourTypes)
    {
      IntegerHolder count=_armoursByType.get(armourType);
      System.out.println("\t"+armourType+": "+count);
    }
    // Weapons
    System.out.println("Weapons: " + _weaponsCount);
    List<WeaponType> weaponTypes=new ArrayList<WeaponType>(_weaponsByType.keySet());
    Collections.sort(weaponTypes,new WeaponTypeComparator());
    for(WeaponType weaponType : weaponTypes)
    {
      IntegerHolder count=_weaponsByType.get(weaponType);
      System.out.println("\t"+weaponType+": "+count);
    }
  }
}
