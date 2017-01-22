package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Merges "legacy+tulkas index" items database and "tulkas new" database
 * into a single items database.
 * @author DAM
 */
public class MergeWithTulkasNew
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
  public MergeWithTulkasNew()
  {
    // Nothing to do!
  }

  /**
   * Do the job.
   */
  public void doIt()
  {
    File file1=new File("data/items/tmp/itemsLegacy+TulkasIndex.xml");
    HashMap<Integer,Item> sourceItems=loadItemsFile(file1);
    System.out.println(sourceItems.size());
    File file2=new File("data/items/tmp/itemsTulkas13.1.xml");
    HashMap<Integer,Item> tulkasItems=loadItemsFile(file2);
    System.out.println(tulkasItems.size());
    HashMap<Integer,Item> mergeResult=new HashMap<Integer,Item>();

    for(Integer id : sourceItems.keySet())
    {
      Item sourceItem=sourceItems.get(id);
      Item result=sourceItem;
      Item tulkasItem=tulkasItems.get(id);
      if (tulkasItem!=null)
      {
        result=mergeItems(sourceItem,tulkasItem);
        tulkasItems.remove(id);
      }
      mergeResult.put(Integer.valueOf(result.getIdentifier()),result);
    }
    System.out.println(tulkasItems.size() + ": " + tulkasItems);
    File toFile=new File("data/items/tmp/itemsLegacy+Tulkas.xml").getAbsoluteFile();
    List<Item> items=new ArrayList<Item>(mergeResult.values());
    ItemsManager.getInstance().writeItemsFile(toFile,items);
  }

  private Item mergeItems(Item source, Item tulkas)
  {
    int id=source.getIdentifier();
    Item result=source;

    // Check types
    if (source.getClass()!=tulkas.getClass())
    {
      //System.out.println("ID: " + id+": type conflict: tulkas=" + tulkas.getClass() + ", source=" + source.getClass());
      if (source.getClass()==Item.class)
      {
        if (tulkas instanceof Weapon)
        {
          Weapon weapon=new Weapon();
          Weapon tulkasWeapon=(Weapon)tulkas;
          result=weapon;
          weapon.copyFrom(source);
          weapon.setMinDamage(tulkasWeapon.getMinDamage());
          weapon.setMaxDamage(tulkasWeapon.getMaxDamage());
          weapon.setDPS(tulkasWeapon.getDPS());
          weapon.setDamageType(tulkasWeapon.getDamageType());
          weapon.setWeaponType(tulkasWeapon.getWeaponType());
        }
        else if (tulkas instanceof Armour)
        {
          Armour armour=new Armour();
          Armour tulkasArmour=(Armour)tulkas;
          result=armour;
          armour.copyFrom(source);
          armour.setArmourValue(tulkasArmour.getArmourValue());
          armour.setArmourType(tulkasArmour.getArmourType());
        }
      }
      else
      {
        System.out.println("Unmanaged cast");
      }
    }
    else
    {
      result=source;
    }
    String sourceName=source.getName();
    // Name
    {
      String tulkasName=tulkas.getName();
      if (!tulkasName.equals(sourceName))
      {
        //System.out.println("ID: " + id+": name conflict: tulkas=" + tulkasName + ", source=" + sourceName);
        if (tulkasName!=null)
        {
          result.setProperty(ItemPropertyNames.OLD_TULKAS_NAME, tulkasName);
        }
      }
    }
    // Required level
    {
      Integer tulkasMinLevel=tulkas.getMinLevel();
      Integer sourceMinLevel=result.getMinLevel();
      boolean conflict=false;
      if (tulkasMinLevel!=null)
      {
        if (sourceMinLevel==null)
        {
          //conflict=true;
          result.setMinLevel(tulkasMinLevel);
        }
        else
        {
          if (tulkasMinLevel.intValue()!=sourceMinLevel.intValue())
          {
            conflict=true;
            result.setMinLevel(tulkasMinLevel);
          }
        }
      }
      else
      {
        if (sourceMinLevel!=null)
        {
          conflict=true;
          result.setMinLevel(sourceMinLevel);
        }
      }
      if (conflict)
      {
        //System.out.println("ID: " + id+": min level conflict: tulkas=" + tulkasMinLevel + ", source=" + sourceMinLevel);
      }
    }
    // Item level
    {
      result.setItemLevel(tulkas.getItemLevel());
    }
    // Required class
    {
      CharacterClass tulkasClass=tulkas.getRequiredClass();
      CharacterClass sourceClass=source.getRequiredClass();
      if (tulkasClass!=sourceClass)
      {
        if ((tulkasClass!=null) && (sourceClass==null))
        {
          result.setRequiredClass(tulkasClass);
        }
        else
        {
          System.out.println("ID: " + id+": required class conflict: tulkas=" + tulkasClass + ", source=" + sourceClass);
        }
      }
    }
    // Quality
    {
      ItemQuality tulkasQuality=tulkas.getQuality();
      ItemQuality sourceQuality=source.getQuality();
      if (tulkasQuality!=sourceQuality)
      {
        // Use source quality since it comes from tulkas index (17.1), which is more recent that tulkas new (13.1)
        result.setQuality(sourceQuality);
        //System.out.println("ID: " + id+": quality conflict: tulkas=" + tulkasQuality + ", source=" + sourceQuality);
      }
    }
    // Bonus
    if (result!=source)
    {
      result.getBonus().clear();
      result.getBonus().addAll(source.getBonus());
    }
    // Stats
    result.getStats().setStats(tulkas.getStats());
    // Slot
    {
      EquipmentLocation tulkasSlot=tulkas.getEquipmentLocation();
      EquipmentLocation sourceSlot=source.getEquipmentLocation();
      if (tulkasSlot!=sourceSlot)
      {
        // Use tulkas slot since it is not set in source
        result.setEquipmentLocation(tulkasSlot);
        //System.out.println("ID: " + id+": slot conflict: tulkas=" + tulkasSlot + ", source=" + sourceSlot);
      }
    }

    return result;
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MergeWithTulkasNew().doIt();
  }
}
