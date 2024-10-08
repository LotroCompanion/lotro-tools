package delta.games.lotro.tools.extraction.items;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.common.enums.comparator.LotroEnumEntryCodeComparator;
import delta.games.lotro.common.enums.comparator.LotroEnumEntryNameComparator;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.comparators.ArmourTypeComparator;
import delta.games.lotro.lore.items.comparators.ItemQualityComparator;

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
  private HashMap<EquipmentLocation,IntegerHolder> _itemsBySlot;

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
    _itemsBySlot=new HashMap<EquipmentLocation,IntegerHolder>();
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
    showStatistics(System.out);
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
    // By slot
    {
      EquipmentLocation location=item.getEquipmentLocation();
      IntegerHolder counter=_itemsBySlot.get(location);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _itemsBySlot.put(location,counter);
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

  private void showStatistics(PrintStream out)
  {
    // Items
    out.println("Items: " + _itemsCount);
    // - qualities
    out.println("- by quality:");
    List<ItemQuality> itemQualities=new ArrayList<ItemQuality>(_itemsByQuality.keySet());
    Collections.sort(itemQualities,new ItemQualityComparator());
    for(ItemQuality itemQuality : itemQualities)
    {
      IntegerHolder count=_itemsByQuality.get(itemQuality);
      out.println("\t"+itemQuality+": "+count);
    }
    // - sub-categories
    out.println("- by sub-category:");
    List<String> itemSubCategories=new ArrayList<String>(_itemsBySubCategory.keySet());
    Collections.sort(itemSubCategories);
    for(String itemSubCategory : itemSubCategories)
    {
      IntegerHolder count=_itemsBySubCategory.get(itemSubCategory);
      out.println("\t"+itemSubCategory+": "+count);
    }
    // - slot
    out.println("- by slot:");
    List<EquipmentLocation> locations=new ArrayList<EquipmentLocation>(_itemsBySlot.keySet());
    Collections.sort(locations,new LotroEnumEntryCodeComparator<EquipmentLocation>());
    for(EquipmentLocation location : locations)
    {
      IntegerHolder count=_itemsBySlot.get(location);
      out.println("\t"+location+": "+count);
    }
    // Armours
    out.println("Armours: " + _armoursCount);
    List<ArmourType> armourTypes=new ArrayList<ArmourType>(_armoursByType.keySet());
    Collections.sort(armourTypes,new ArmourTypeComparator());
    for(ArmourType armourType : armourTypes)
    {
      IntegerHolder count=_armoursByType.get(armourType);
      out.println("\t"+armourType+": "+count);
    }
    // Weapons
    out.println("Weapons: " + _weaponsCount);
    List<WeaponType> weaponTypes=new ArrayList<WeaponType>(_weaponsByType.keySet());
    Collections.sort(weaponTypes,new LotroEnumEntryNameComparator<WeaponType>());
    for(WeaponType weaponType : weaponTypes)
    {
      IntegerHolder count=_weaponsByType.get(weaponType);
      out.println("\t"+weaponType+": "+count);
    }
  }
}
