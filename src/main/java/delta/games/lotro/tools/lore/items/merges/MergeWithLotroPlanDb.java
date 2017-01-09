package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

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
          noIdItems.put(item.getName(),item);
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
    File file1=new File("itemsLegacy+Tulkas.xml");
    HashMap<Integer,Item> sourceItems=loadItemsFile(file1,null);
    System.out.println(sourceItems.size());
    File file2=new File("itemsdb.xml");
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
        lotroPlanItem=itemsWithNoId.get(sourceItem.getName());
        if (lotroPlanItem!=null)
        {
          itemsWithNoId.remove(sourceItem.getName());
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
    File toFile=new File("items-rc.xml").getAbsoluteFile();
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
      result.getStats().setStats(lotroplan.getStats());
    }
    // Essence slots
    result.setEssenceSlots(lotroplan.getEssenceSlots());

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
