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
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Loads starter stats from a raw data file.
 * @author DAM
 */
public class LotroPlanItemsDbLoader
{
  private static final String[] NAMES={};
  //private static final String[] NAMES={"jewels.txt", "hd_jewels.txt", "heavy.txt", "medium.txt", "light.txt", "weapons.txt"};

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
      handleAdditionalTable(tableName,map);
    }
    ItemsManager mgr=ItemsManager.getInstance();
    File toFile=new File("itemsdb.xml").getAbsoluteFile();
    mgr.writeItemsFile(toFile,items);
    //List<Integer> ids=new ArrayList<Integer>(_failedItems.keySet());
    //new BuildItemsDbForIcons().buildDb(_failedItems,ids);
  }

  private void handleAdditionalTable(String tableName, HashMap<String,List<Item>> map)
  {
    List<Item> items=loadTable(tableName);
    for(Item item : items)
    {
      String name=item.getName();
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
        System.out.println("Name: "+name+": "+nbSlots+" slots");
      }
      List<Item> selectedItems=map.get(name);
      Item selectedItem=findItem(selectedItems,item);
      if (selectedItem!=null)
      {
        BasicStatsSet itemStats=selectedItem.getStats();
        itemStats.clear();
        itemStats.setStats(item.getStats());
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

  private Item findItem(List<Item> selectedItems, Item item)
  {
    Item selectedItem=null;
    if (selectedItems!=null)
    {
      for(Item currentItem : selectedItems)
      {
        if (areEqual(currentItem.getItemLevel(),item.getItemLevel()))
        {
          selectedItem=currentItem;
          break;
        }
      }
    }
    return selectedItem;
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
    if (fieldsTrimmed.length<2)
    {
      System.out.println("Ignored: "+line);
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
    String idStr=fields[LotroPlanTable.NOTES].trim();
    int id=0;
    if (idStr.startsWith("ID:"))
    {
      idStr=idStr.substring(3).trim();
      id=NumericTools.parseInt(idStr,-1);
    }
    item.setIdentifier(id);
    // Name
    String name=fields[LotroPlanTable.NAME_INDEX];
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
    return item;
  }
}
