package delta.games.lotro.tools.lore.items.lotroplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Merge items from different tables of LotroPlan.
 * @author DAM
 */
public class ItemsMerger
{
  private List<Item> _items;
  private HashMap<String,List<Item>> _mapByName;
  private HashMap<Integer,Item> _mapById;
  private HashMap<Integer,Item> _failedItems;

  /**
   * Constructor.
   */
  public ItemsMerger()
  {
    _items=new ArrayList<Item>();
    _mapByName=new HashMap<String,List<Item>>();
    _mapById=new HashMap<Integer,Item>();
    _failedItems=new HashMap<Integer,Item>();
  }

  /**
   * Get the result items.
   * @return the result items.
   */
  public List<Item> getItems()
  {
    return _items;
  }

  /**
   * Register some items.
   * @param items Items to register.
   */
  public void registerItems(List<Item> items)
  {
    for(Item item : items)
    {
      register(item);
    }
  }

  private void register(Item item)
  {
    registerByName(item);
    registerById(item);
    _items.add(item);
  }

  private void registerByName(Item item)
  {
    String name=item.getName();
    if (name==null) name="";
    name=name.replace(' ',' ');
    List<Item> itemsForName=_mapByName.get(name);
    if (itemsForName==null)
    {
      itemsForName=new ArrayList<Item>();
      _mapByName.put(name,itemsForName);
    }
    itemsForName.add(item);
  }

  private void registerById(Item item)
  {
    int id=item.getIdentifier();
    if (id!=0)
    {
      _mapById.put(Integer.valueOf(id),item);
    }
  }

  /**
   * Handle a new item.
   * @param item Item to add.
   * @param armourType Armour type for this item (or <code>null</code>)
   */
  public void newItem(Item item, ArmourType armourType)
  {
    String name=item.getName();
    name=name.replace(' ',' ');
    updateArmourType(armourType,item);

    // Check for known item...
    List<Item> selectedItems=_mapByName.get(name);
    if (selectedItems==null)
    {
      // Item not known
      List<Item> newList=new ArrayList<Item>();
      newList.add(item);
      _mapByName.put(name,newList);
      _items.add(item);
      return;
    }

    // Find item
    Item selectedItem=findItem(selectedItems,item);
    if (selectedItem!=null)
    {
      // Found...
      mergeStats(item,selectedItem);
      mergeItems(armourType,item,selectedItem);
      return;
    }

    // Item with ID...
    int id=item.getIdentifier();
    if (id!=0)
    {
      Item itemsdbItem=_mapById.get(Integer.valueOf(id));
      if (itemsdbItem==null)
      {
        _items.add(item);
        return;
      }
    }

    // Look for different versions of a scalable item
    StringBuilder log=new StringBuilder();
    selectedItem=inspectScalableItems(item,selectedItems,log);
    if (selectedItem!=null)
    {
      Integer itemLevel=item.getItemLevel();
      Integer selectedItemLevel=selectedItem.getItemLevel();
      if (compareItemLevels(itemLevel,selectedItemLevel)>0)
      {
        mergeStats(item,selectedItem);
      }
      mergeItems(armourType,item,selectedItem);
      return;
    }

    // Could not handle item correctly, complain!
    Integer itemLevel=item.getItemLevel();
    System.out.println("Name: "+name+" ("+itemLevel+") not found. Selection is:"+selectedItems);
    System.out.println(log);
    if (selectedItems!=null)
    {
      for(Item currentItem : selectedItems)
      {
        _failedItems.put(Integer.valueOf(currentItem.getIdentifier()),item);
      }
    }
  }

  private Item inspectScalableItems(Item item, List<Item> selectedItems, StringBuilder sb)
  {
    List<Item> matchingItems=new ArrayList<Item>();
    String slices=item.getProperty(ItemPropertyNames.SLICED_STATS);
    if (slices!=null)
    {
      SlicesBasedItemStatsProvider provider=SlicesBasedItemStatsProvider.fromPersistedString(slices);
      if (provider!=null)
      {
        for(Item selectedItem : selectedItems)
        {
          Integer selectedItemLevel=selectedItem.getItemLevel();
          if ((selectedItemLevel!=null) && (provider!=null))
          {
            BasicStatsSet scaledStats=provider.getStats(selectedItemLevel.intValue());
            BasicStatsSet itemStats=selectedItem.getStats();
            boolean same=compareStats(itemStats,scaledStats);
            if (same)
            {
              //System.out.println("Item " + item + " and " + selectedItem + " are versions of the same scalable item.");
              String itemLevels=buildItemLevelProperty(selectedItemLevel,item.getItemLevel());
              selectedItem.setProperty("itemLevels", itemLevels);
              matchingItems.add(selectedItem);
            }
            else
            {
              sb.append("Stats are different: " + item + " != " + selectedItem + "\n");
              sb.append("Scaled: " + scaledStats + "\n");
              sb.append("Expected: " + itemStats + "\n");
            }
          }
        }
      }
    }
    Item matchingItem=null;
    if (matchingItems.size()>0)
    {
      if (matchingItems.size()>1)
      {
        sb.append("Several matches for " + item + ": " + matchingItems + "\n");
      }
      else
      {
        matchingItem=matchingItems.get(0);
        sb.setLength(0);
      }
    }
    return matchingItem;
  }

  private String buildItemLevelProperty(Integer... itemLevels)
  {
    List<Integer> itemLevelsList=new ArrayList<Integer>();
    for(Integer itemLevel :  itemLevels)
    {
      if (itemLevel!=null)
      {
        itemLevelsList.add(itemLevel);
      }
    }
    Collections.sort(itemLevelsList);
    return itemLevelsList.toString();
  }

  private boolean compareStats(BasicStatsSet expected, BasicStatsSet actual)
  {
    int expectedCount=expected.getStatsCount();
    int actualCount=actual.getStatsCount();
    if (expectedCount!=actualCount)
    {
      return false;
    }
    for(STAT stat : expected.getStats())
    {
      FixedDecimalsInteger expectedValue=expected.getStat(stat);
      FixedDecimalsInteger actualValue=actual.getStat(stat);
      if (actualValue==null)
      {
        return false;
      }
      int expectedInternalValue=expectedValue.getInternalValue();
      int actualInternalValue=actualValue.getInternalValue();
      if (Math.abs(expectedInternalValue-actualInternalValue)>100)
      {
        return false;
      }
    }
    return true;
  }

  private void mergeItems(ArmourType armourType, Item item, Item selectedItem)
  {
    selectedItem.setEssenceSlots(item.getEssenceSlots());
    selectedItem.setEquipmentLocation(item.getEquipmentLocation());
    updateArmourType(armourType,selectedItem);
    selectedItem.setSubCategory(item.getSubCategory());
    selectedItem.setRequiredClass(item.getRequiredClass());
    selectedItem.getProperties().putAll(item.getProperties());
  }

  private void mergeStats(Item item, Item selectedItem)
  {
    BasicStatsSet itemStats=selectedItem.getStats();
    Integer itemLevel=item.getItemLevel();
    Integer selectedItemLevel=selectedItem.getItemLevel();
    int compareLevels=compareItemLevels(itemLevel,selectedItemLevel);
    if (compareLevels!=0)
    {
      String itemLevels=buildItemLevelProperty(itemLevel,selectedItemLevel);
      selectedItem.setProperty("itemLevels", itemLevels);
    }
    selectedItem.setItemLevel(item.getItemLevel());
    itemStats.clear();
    itemStats.setStats(item.getStats());
  }

  /**
   * Compare item levels of two items.
   * @param value1 Item level 1.
   * @param value2 Item level 2.
   * @return <code>1</code> if the item level of item 1 is defined and strictly superior
   * to the one in item2, <code>0</code> if equal, <code>-1</code> otherwise.
   */
  private int compareItemLevels(Integer value1, Integer value2)
  {
    if (value1!=null)
    {
      if (value2==null) return 1;
      if (value1.intValue()>value2.intValue()) return 1;
      if (value1.intValue()==value2.intValue()) return 0;
      return -1;
    }
    if (value2==null) return 0;
    return -1;
  }

  private void updateArmourType(ArmourType type, Item item)
  {
    if (type!=null)
    {
      if (item instanceof Armour)
      {
        Armour armour=(Armour)item;
        if (item.getEquipmentLocation()==EquipmentLocation.OFF_HAND)
        {
          if (type==ArmourType.HEAVY) type=ArmourType.HEAVY_SHIELD;
          else if (type==ArmourType.MEDIUM) type=ArmourType.WARDEN_SHIELD;
          else if (type==ArmourType.LIGHT) type=ArmourType.SHIELD;
        }
        armour.setArmourType(type);
      }
    }
  }

  private Item findItem(List<Item> selectedItems, Item item)
  {
    if (selectedItems!=null)
    {
      // Use identifier if we can
      if (item.getIdentifier()!=0)
      {
        for(Item currentItem : selectedItems)
        {
          if (currentItem.getIdentifier()==item.getIdentifier())
          {
            return currentItem;
          }
        }
        return null;
      }
      List<Item> goodItems=null;
      // Use item level
      for(Item currentItem : selectedItems)
      {
        if (areEqual(currentItem.getItemLevel(),item.getItemLevel()))
        {
          if (goodItems==null)
          {
            goodItems=new ArrayList<Item>();
          }
          goodItems.add(currentItem);
        }
      }
      if (goodItems!=null)
      {
        if (goodItems.size()==1)
        {
          return goodItems.get(0);
        }
        // Several items do match...
        Item usingMight=findItemUsingStat(STAT.MIGHT,selectedItems,item);
        if (usingMight!=null)
        {
          return usingMight;
        }
        Item usingAgility=findItemUsingStat(STAT.AGILITY,selectedItems,item);
        if (usingAgility!=null)
        {
          return usingAgility;
        }
        Item usingWill=findItemUsingStat(STAT.WILL,selectedItems,item);
        if (usingWill!=null)
        {
          return usingWill;
        }
        System.out.println("Several items do match " + item + ": " + goodItems);
      }
    }
    return null;
  }

  private Item findItemUsingStat(STAT stat, List<Item> selectedItems, Item item)
  {
    List<Item> goodItems=null;
    for(Item currentItem : selectedItems)
    {
      if (areEqualUsingStat(stat,currentItem,item))
      {
        if (goodItems==null)
        {
          goodItems=new ArrayList<Item>();
        }
        goodItems.add(currentItem);
      }
    }
    if (goodItems!=null)
    {
      if (goodItems.size()==1)
      {
        return goodItems.get(0);
      }
    }
    return null;
  }

  private boolean areEqualUsingStat(STAT stat, Item item1, Item item2)
  {
    FixedDecimalsInteger value1=item1.getStats().getStat(stat);
    FixedDecimalsInteger value2=item2.getStats().getStat(stat);
    if (value1!=null)
    {
      if (value2==null) return false;
      return value1.getInternalValue()==value2.getInternalValue();
    }
    return value2==null;
  }

  private boolean areEqual(Integer value1, Integer value2)
  {
    if (value1!=null)
    {
      return value1.equals(value2);
    }
    return false;
  }

}
