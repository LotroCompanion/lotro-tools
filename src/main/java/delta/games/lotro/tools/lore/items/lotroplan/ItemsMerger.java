package delta.games.lotro.tools.lore.items.lotroplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
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
    List<Item> selectedItems=_mapByName.get(name);
    if (selectedItems==null)
    {
      List<Item> newList=new ArrayList<Item>();
      newList.add(item);
      _mapByName.put(name,newList);
      _items.add(item);
    }
    else
    {
      Item selectedItem=findItem(selectedItems,item);
      if (selectedItem!=null)
      {
        BasicStatsSet itemStats=selectedItem.getStats();
        itemStats.clear();
        itemStats.setStats(item.getStats());
        updateArmourType(armourType,selectedItem);
        selectedItem.setEssenceSlots(item.getEssenceSlots());
        selectedItem.setEquipmentLocation(item.getEquipmentLocation());
        selectedItem.setSubCategory(item.getSubCategory());
        selectedItem.setRequiredClass(item.getRequiredClass());
      }
      else
      {
        boolean doComplain=true;
        int id=item.getIdentifier();
        if (id!=0)
        {
          Item itemsdbItem=_mapById.get(Integer.valueOf(id));
          if (itemsdbItem==null)
          {
            _items.add(item);
            doComplain=false;
          }
        }
        if (doComplain)
        {
          Integer itemLevel=item.getItemLevel();
          System.out.println("Name: "+name+" ("+itemLevel+") not found. Selection is:"+selectedItems);
          if (selectedItems!=null)
          {
            for(Item currentItem : selectedItems)
            {
              _failedItems.put(Integer.valueOf(currentItem.getIdentifier()),item);
            }
          }
        }
      }
    }
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
