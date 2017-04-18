package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Merges "legacy+tulkas" items database and "lotroplan" database
 * into a single items database.
 * @author DAM
 */
public class MergeWithLotroPlanDb
{
  private HashMap<Integer,Item> loadItemsFile(File file, Map<String,Item> noIdItems)
  {
    ItemXMLParser parser=new ItemXMLParser();
    List<Item> items=parser.parseItemsFile(file);
    HashMap<Integer,Item> ret=new HashMap<Integer,Item>();
    for(Item item : items)
    {
      int id=item.getIdentifier();
      if (id>0)
      {
        ret.put(Integer.valueOf(id),item);
      }
      else
      {
        if (noIdItems!=null)
        {
          String name=item.getName();
          name=name.replace(' ',' ');
          noIdItems.put(name,item);
        }
      }
    }
    return ret;
  }

  /**
   * Constructor.
   */
  public MergeWithLotroPlanDb()
  {
    // Nothing to do!
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    File file1=new File("data/items/tmp/itemsLegacy+Tulkas.xml");
    HashMap<Integer,Item> sourceItems=loadItemsFile(file1,null);
    System.out.println(sourceItems.size());
    File file2=new File("data/items/tmp/itemsdb.xml");
    Map<String,Item> itemsWithNoId=new HashMap<String,Item>();
    HashMap<Integer,Item> lotroPlanItems=loadItemsFile(file2,itemsWithNoId);
    System.out.println("LOTRO plan items (with ID): " + lotroPlanItems.size());
    System.out.println("LOTRO plan items (no ID): " + itemsWithNoId.size());
    HashMap<Integer,Item> mergeResult=new HashMap<Integer,Item>();

    for(Integer id : sourceItems.keySet())
    {
      Item sourceItem=sourceItems.get(id);
      Item result=sourceItem;
      Item lotroPlanItem=lotroPlanItems.get(id);
      if (lotroPlanItem==null)
      {
        String sourceName=sourceItem.getName();
        if (sourceName!=null)
        {
          sourceName=sourceName.replace(' ',' ');
        }
        lotroPlanItem=itemsWithNoId.get(sourceName);
        if (lotroPlanItem!=null)
        {
          itemsWithNoId.remove(sourceName);
        }
      }
      else
      {
        lotroPlanItems.remove(id);
      }
      if (lotroPlanItem!=null)
      {
        result=mergeItems(sourceItem,lotroPlanItem);
      }
      mergeResult.put(Integer.valueOf(result.getIdentifier()),result);
    }
    if (lotroPlanItems.size()>0)
    {
      System.out.println("LOTRO plan items (with ID), not used: " + lotroPlanItems.size() + ": " + lotroPlanItems);
    }
    if (itemsWithNoId.size()>0)
    {
      System.out.println("LOTRO plan items (no ID), not used: " + itemsWithNoId.size() + ": " + itemsWithNoId);
    }
    File toFile=new File("data/items/tmp/items-rc.xml").getAbsoluteFile();
    List<Item> items=new ArrayList<Item>(mergeResult.values());
    ItemsManager.getInstance().writeItemsFile(toFile,items);
  }

  private Item mergeItems(Item source, Item lotroplan)
  {
    int id=source.getIdentifier();
    Item result=source;

    // Check types
    if (source.getClass()!=lotroplan.getClass())
    {
      //System.out.println("ID: " + id+": type conflict: lotroplan=" + lotroplan.getClass() + ", source=" + source.getClass());
      if (source.getClass()==Item.class)
      {
        if (lotroplan instanceof Armour)
        {
          Armour armour=new Armour();
          Armour lotroplanArmour=(Armour)lotroplan;
          result=armour;
          armour.copyFrom(source);
          armour.setArmourValue(lotroplanArmour.getArmourValue());
          armour.setArmourType(lotroplanArmour.getArmourType());
        }
        else
        {
          System.out.println("ID: " + id+": type conflict: lotroplan=" + lotroplan.getClass() + ", source=" + source.getClass());
        }
      }
      if (source.getClass()==Weapon.class)
      {
        if (lotroplan.getClass()==Item.class)
        {
          result=source;
        }
        else
        {
          System.out.println("ID: " + id+": type conflict: lotroplan=" + lotroplan.getClass() + ", source=" + source.getClass());
        }
      }
    }
    else
    {
      result=source;
    }
    // Name
    {
      String sourceName=source.getName();
      /*
      String lotroplanName=lotroplan.getName();
      if (!lotroplanName.equals(sourceName))
      {
        System.out.println("ID: " + id+": name conflict: lotroplan=" + lotroplanName + ", source=" + sourceName);
        if (lotroplanName!=null)
        {
          result.setProperty(ItemPropertyNames.LOTRO_PLAN_NAME, lotroplanName);
        }
      }
      */
      result.setName(sourceName);
    }
    // Armour
    {
      if (lotroplan instanceof Armour)
      {
        Armour lotroplanArmour=(Armour)lotroplan;
        Armour resultArmour=(Armour)result;
        // Check values
        {
          int lotroplanArmourValue=lotroplanArmour.getArmourValue();
          /*
          int resultArmourValue=resultArmour.getArmourValue();
          if (lotroplanArmourValue!=resultArmourValue)
          {
            System.out.println("ID: " + id+": armour value conflict: lotroplan=" + lotroplanArmourValue + ", source=" + resultArmourValue);
          }
          */
          resultArmour.setArmourValue(lotroplanArmourValue);
        }
      }
    }
    // Item level
    {
      Integer sourceItemLevel=source.getItemLevel();
      Integer lotroPlanItemLevel=lotroplan.getItemLevel();
      boolean conflict=false;
      if (lotroPlanItemLevel!=null)
      {
        if (sourceItemLevel==null)
        {
          conflict=true;
          //result.setMinLevel(tulkasMinLevel);
        }
        else
        {
          if (lotroPlanItemLevel.intValue()!=sourceItemLevel.intValue())
          {
            conflict=true;
            //result.setMinLevel(tulkasMinLevel);
          }
        }
      }
      else
      {
        if (sourceItemLevel!=null)
        {
          conflict=true;
          //result.setMinLevel(sourceMinLevel);
        }
      }
      if (conflict)
      {
        //System.out.println("ID: " + id+": item level conflict: lotroplan=" + lotroPlanItemLevel + ", source=" + sourceItemLevel);
      }
      result.setItemLevel(lotroplan.getItemLevel());
    }
    // Stats
    {
      BasicStatsSet resultStats=result.getStats();
      FixedDecimalsInteger stealth=resultStats.getStat(STAT.STEALTH_LEVEL);
      FixedDecimalsInteger tacticalCritMultiplier=resultStats.getStat(STAT.TACTICAL_CRITICAL_MULTIPLIER);
      resultStats.clear();
      resultStats.setStats(lotroplan.getStats());
      if (stealth!=null)
      {
        resultStats.setStat(STAT.STEALTH_LEVEL,stealth);
      }
      if (tacticalCritMultiplier!=null)
      {
        resultStats.setStat(STAT.TACTICAL_CRITICAL_MULTIPLIER,tacticalCritMultiplier);
      }
    }
    // Essence slots
    result.setEssenceSlots(lotroplan.getEssenceSlots());
    // Slot
    EquipmentLocation lpLocation=lotroplan.getEquipmentLocation();
    if (lpLocation!=null)
    {
      EquipmentLocation location=result.getEquipmentLocation();
      boolean conflict=false;
      if ((location!=null) && (location!=lpLocation))
      {
        conflict=true;
      }
      if (conflict)
      {
        System.out.println("ID: " + id+": slot conflict: lotroplan=" + lpLocation + ", source=" + location);
      }
      result.setEquipmentLocation(lpLocation);
    }
    // Sub-category
    String lpSubCategory=lotroplan.getSubCategory();
    if ((lpSubCategory!=null) && (lpSubCategory.length()>0))
    {
      /*
      String subCategory=result.getSubCategory();
      boolean conflict=false;
      if ((subCategory!=null) && (!subCategory.equals(lpSubCategory)))
      {
        conflict=true;
      }
      if (conflict)
      {
        System.out.println("ID: " + id+": category conflict: lotroplan=" + lpSubCategory + ", source=" + subCategory);
      }
      */
      result.setSubCategory("LP:"+lpSubCategory);
    }
    // Class requirement
    CharacterClass lpClass=lotroplan.getRequiredClass();
    if (lpClass!=null)
    {
      CharacterClass cClass=result.getRequiredClass();
      boolean conflict=false;
      if ((cClass!=null) && (cClass!=lpClass))
      {
        conflict=true;
      }
      if (conflict)
      {
        System.out.println("ID: " + id+": class conflict: lotroplan=" + lpClass + ", source=" + cClass);
      }
      result.setRequiredClass(lpClass);
    }
    // Properties
    result.getProperties().putAll(lotroplan.getProperties());

    return result;
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MergeWithLotroPlanDb().doIt();
  }
}
