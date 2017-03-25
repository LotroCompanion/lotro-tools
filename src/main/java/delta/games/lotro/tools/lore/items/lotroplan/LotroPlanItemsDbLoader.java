package delta.games.lotro.tools.lore.items.lotroplan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.StringSplitter;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class LotroPlanItemsDbLoader
{
  //private static final String[] NAMES={};
  private static final String[] NAMES={"jewels.txt", "hd_jewels.txt", "heavy.txt", "medium.txt", "light.txt", "weapons.txt"};

  private String _section;
  private HashMap<Integer,Item> _failedItems=new HashMap<Integer,Item>();

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LotroPlanItemsDbLoader().doIt();
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    List<Item> items=loadTable("itemsdb.txt");
    HashMap<String,List<Item>> map=asMap(items);
    for(String tableName : NAMES)
    {
      handleAdditionalTable(tableName,items,map);
    }
    ItemsManager mgr=ItemsManager.getInstance();
    File toFile=new File("data/items/tmp/itemsdb.xml").getAbsoluteFile();
    mgr.writeItemsFile(toFile,items);
    //List<Integer> ids=new ArrayList<Integer>(_failedItems.keySet());
    //new BuildItemsDbForIcons().buildDb(_failedItems,ids);
  }

  private void handleAdditionalTable(String tableName, List<Item> allItems, HashMap<String,List<Item>> map)
  {
    ArmourType armourType=null;
    if ("heavy.txt".equals(tableName)) armourType=ArmourType.HEAVY;
    else if ("medium.txt".equals(tableName)) armourType=ArmourType.MEDIUM;
    else if ("light.txt".equals(tableName)) armourType=ArmourType.LIGHT;
    List<Item> items=loadTable(tableName);
    for(Item item : items)
    {
      String name=item.getName();
      name=name.replace(' ',' ');
      updateArmourType(armourType,item);
      List<Item> selectedItems=map.get(name);
      if (selectedItems==null)
      {
        List<Item> newList=new ArrayList<Item>();
        newList.add(item);
        map.put(name,newList);
        allItems.add(item);
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
        }
        else
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

  private void updateArmourType(ArmourType type, Item selectedItem)
  {
    if (type!=null)
    {
      if (selectedItem instanceof Armour)
      {
        Armour armour=(Armour)selectedItem;
        if (selectedItem.getEquipmentLocation()==EquipmentLocation.OFF_HAND)
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

  private HashMap<String,List<Item>> asMap(List<Item> items)
  {
    HashMap<String,List<Item>> map=new HashMap<String,List<Item>>();
    for(Item item : items)
    {
      String name=item.getName();
      if (name==null) name="";
      name=name.replace(' ',' ');
      List<Item> itemsForName=map.get(name);
      if (itemsForName==null)
      {
        itemsForName=new ArrayList<Item>();
        map.put(name,itemsForName);
      }
      itemsForName.add(item);
    }
    return map;
  }

  private List<Item> loadTable(String filename)
  {
    _section=null;
    URL url=URLTools.getFromClassPath(filename,LotroPlanItemsDbLoader.class.getPackage());
    TextFileReader reader=new TextFileReader(url, EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(reader);
    List<Item> items=new ArrayList<Item>();
    //_fields=StringSplitter.split(lines.get(0),'\t');
    lines.remove(0);
    LotroPlanTable table=new LotroPlanTable();
    for(String line : lines)
    {
      Item item=buildItemFromLine(table, line);
      if (item!=null)
      {
        items.add(item);
      }
    }
    return items;
  }

  private Item buildItemFromLine(LotroPlanTable table, String line)
  {
    String[] fieldsTrimmed=StringSplitter.split(line.trim(),'\t');
    if (line.startsWith("#"))
    {
      System.out.println("Ignored: "+line);
      return null;
    }
    if (fieldsTrimmed.length<2)
    {
      _section=line.trim();
      System.out.println("Section: "+_section);
      return null;
    }
    String[] fields=StringSplitter.split(line,'\t');
    Integer armor=null;
    if (fields[LotroPlanTable.ARMOUR_INDEX].length()>0)
    {
      armor=NumericTools.parseInteger(fields[LotroPlanTable.ARMOUR_INDEX]);
    }
    Item item=null;
    Armour armour=null;
    if (armor!=null)
    {
      armour=new Armour();
      item=armour;
      armour.setArmourValue(armor.intValue());
    }
    else
    {
      item=new Item();
    }
    // ID
    String idStr="";
    if (fields.length>=LotroPlanTable.NOTES)
    {
      idStr=fields[LotroPlanTable.NOTES].trim();
    }
    int id=0;
    if (idStr.startsWith("ID:"))
    {
      idStr=idStr.substring(3).trim();
      id=NumericTools.parseInt(idStr,-1);
    }
    item.setIdentifier(id);
    // Name
    String name=fields[LotroPlanTable.NAME_INDEX];
    if (name.startsWith("("))
    {
      int index=name.indexOf(')');
      idStr=name.substring(1,index).trim();
      id=NumericTools.parseInt(idStr,-1);
      item.setIdentifier(id);
      name=name.substring(index+1).trim();
    }
    if (name.endsWith(":"))
    {
      name=name.substring(0,name.length()-1);
    }
    name=name.replace(' ',' ');
    if (name.endsWith("s)"))
    {
      name=name.substring(0,name.length()-2);
      int index=name.lastIndexOf('(');
      int nbSlots=NumericTools.parseInt(name.substring(index+1),0);
      name=name.substring(0,index).trim();
      item.setEssenceSlots(nbSlots);
    }
    if (name.endsWith(")"))
    {
      String newName=name.substring(0,name.length()-1);
      int index=newName.lastIndexOf('(');
      String valueStr=newName.substring(index+1);
      int minLevel;
      if ("TBD".equals(valueStr))
      {
        minLevel=-1;
      }
      else
      {
        minLevel=NumericTools.parseInt(valueStr,-1);
        name=newName.substring(0,index).trim();
      }
      if (minLevel>0)
      {
        item.setMinLevel(Integer.valueOf(minLevel));
      }
    }
    name=name.trim();
    item.setName(name);
    // Item level
    int itemLevel=NumericTools.parseInt(fields[LotroPlanTable.ITEM_LEVEL_INDEX],-1);
    if (itemLevel!=-1)
    {
      item.setItemLevel(Integer.valueOf(itemLevel));
    }
    // Stats
    BasicStatsSet itemStats=table.loadStats(fields);
    BasicStatsSet stats=item.getStats();
    stats.setStats(itemStats);
    // Slot
    EquipmentLocation slot=null;
    if ("Head".equals(_section)) slot=EquipmentLocation.HEAD;
    else if ("Shoulders".equals(_section)) slot=EquipmentLocation.SHOULDER;
    else if ("Chest".equals(_section)) slot=EquipmentLocation.CHEST;
    else if ("Hands".equals(_section)) slot=EquipmentLocation.HAND;
    else if ("Legs".equals(_section)) slot=EquipmentLocation.LEGS;
    else if ("Feet".equals(_section)) slot=EquipmentLocation.FEET;
    else if ("Shields".equals(_section)) slot=EquipmentLocation.OFF_HAND;
    else if ("Ears".equals(_section)) slot=EquipmentLocation.EAR;
    else if ("Neck".equals(_section)) slot=EquipmentLocation.NECK;
    else if ("Wrists".equals(_section)) slot=EquipmentLocation.WRIST;
    else if ("Fingers".equals(_section)) slot=EquipmentLocation.FINGER;
    else if ("Pockets".equals(_section)) slot=EquipmentLocation.POCKET;
    if (slot!=null)
    {
      item.setEquipmentLocation(slot);
    }
    return item;
  }
}
