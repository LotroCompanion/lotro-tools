package delta.games.lotro.tools.lore.items.merges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Merges "legacy" items database and "tulkas index" database
 * into a single items database.
 * @author DAM
 */
public class MergeItemsLegacyAndTulkasIndex
{
  // Map tulkas subcategories int ids to list of legacy subcategories names
  //private HashMap<String,HashMap<String,IntegerHolder>> _tulkas2legacySubCategoriesMap;
  //private HashMap<String,Set<String>> _legacy2tulkasSubCategoriesMap;

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
  public MergeItemsLegacyAndTulkasIndex()
  {
    //_legacy2tulkasSubCategoriesMap=new HashMap<String,Set<String>>();
    //_tulkas2legacySubCategoriesMap=new HashMap<String,HashMap<String,IntegerHolder>>();
  }

  private void doIt()
  {
    File file1=new File("itemsLegacy.xml");
    HashMap<Integer,Item> legacyItems=loadItemsFile(file1);
    System.out.println(legacyItems.size());
    File file2=new File("itemsTulkasIndex.xml");
    HashMap<Integer,Item> tulkasItems=loadItemsFile(file2);
    System.out.println(tulkasItems.size());
    HashMap<Integer,Item> mergeResult=new HashMap<Integer,Item>();

    for(Integer id : tulkasItems.keySet())
    {
      Item tulkasItem=tulkasItems.get(id);
      Item result=tulkasItem;
      //inspectItemCategories(tulkasItem);
      Item legacyItem=legacyItems.get(id);
      if ((tulkasItem!=null) && (legacyItem!=null))
      {
        result=mergeItems(tulkasItem,legacyItem);
        legacyItems.remove(id);
      }
      String tulkasSubCategory=tulkasItem.getSubCategory();
      result.setProperty(ItemPropertyNames.TULKAS_CATEGORY,tulkasSubCategory);
      mergeResult.put(Integer.valueOf(result.getIdentifier()),result);
    }
    System.out.println(legacyItems.size());
    File toFile=new File("itemsLegacy+TulkasIndex.xml").getAbsoluteFile();
    List<Item> items=new ArrayList<Item>(mergeResult.values());
    ItemsManager.getInstance().writeItemsFile(toFile,items);
    //dumpCategoriesInfo();
  }

  // Map tulkas subcategories int ids to item names
  /*
  private HashMap<String,List<String>> _itemsByTulkasCategory=new HashMap<String,List<String>>();

  private void inspectItemCategories(Item tulkasItem)
  {
    String tulkasSubCategory=tulkasItem.getSubCategory();
    List<String> names=_itemsByTulkasCategory.get(tulkasSubCategory);
    if (names==null)
    {
      names=new ArrayList<String>();
      _itemsByTulkasCategory.put(tulkasSubCategory,names);
    }
    names.add(tulkasItem.getName());
  }

  private void dumpCategoriesInfo()
  {
    System.out.println(_tulkas2legacySubCategoriesMap);
    handleSubCategoriesMap();
    List<String> categories=new ArrayList<String>(_itemsByTulkasCategory.keySet());
    Collections.sort(categories);
    for(String category : categories)
    {
      System.out.println("=== " + category + " ===");
      List<String> names=_itemsByTulkasCategory.get(category);
      for(String name : names)
      {
        System.out.println("\t" + name);
      }
    }
    //System.out.println(_itemsByCategory);
  }
  */

  private Item mergeItems(Item tulkas, Item legacy)
  {
    int id=tulkas.getIdentifier();
    String tulkasName=tulkas.getName();
    // Name
    {
      String legacyName=legacy.getName();
      if (!tulkasName.equals(legacyName))
      {
        //System.out.println("ID: " + id+": name conflict: tulkas=" + tulkasName + ", legacy=" + legacyName);
        if (legacyName!=null)
        {
          legacy.setProperty(ItemPropertyNames.LEGACY_NAME, legacyName);
          legacy.setName(tulkasName);
        }
      }
    }
    // Category: use legacy
    // Subcategory
    {
      String legacySubCategory=legacy.getSubCategory();
      if ((legacySubCategory!=null) && (legacySubCategory.length()>0))
      {
        legacy.setProperty(ItemPropertyNames.LEGACY_CATEGORY, legacySubCategory);
      }

      /*
      // Fill tulkas to legacy map
      {
        HashMap<String,IntegerHolder> map=_tulkas2legacySubCategoriesMap.get(tulkasSubCategory);
        if (map==null)
        {
          map=new HashMap<String,IntegerHolder>();
          _tulkas2legacySubCategoriesMap.put(tulkasSubCategory,map);
        }
        IntegerHolder holder=map.get(legacySubCategory);
        if (holder==null)
        {
          holder=new IntegerHolder();
          map.put(legacySubCategory,holder);
        }
        holder.increment();
      }
      // Fill legacy to tulkas map
      {
        Set<String> set=_legacy2tulkasSubCategoriesMap.get(legacySubCategory);
        if (set==null)
        {
          set=new HashSet<String>();
          _legacy2tulkasSubCategoriesMap.put(legacySubCategory,set);
        }
        set.add(tulkasSubCategory);
      }
      */
    }
    // Unicity
    {
      boolean legacyUnique=legacy.isUnique();
      boolean tulkasUnique=tulkas.isUnique();
      if ((legacyUnique==true) && (tulkasUnique==false) &&
          ((tulkasName.contains("Hytbold")) || (tulkasName.contains("Guide to")) || (tulkasName.contains("Map to"))))
      {
        tulkasUnique=true;
      }
      if (legacyUnique!=tulkasUnique)
      {
        System.out.println("ID: " + id + "(name=" + tulkasName + "): unicity conflict: tulkas=" + tulkasUnique + ", legacy=" + legacyUnique);
      }
      legacy.setUnique(tulkasUnique);
    }
    // Sturdiness
    {
      ItemSturdiness legacySturdiness=legacy.getSturdiness();
      ItemSturdiness tulkasSturdiness=tulkas.getSturdiness();
      if ((tulkasSturdiness!=null) && (legacySturdiness!=tulkasSturdiness))
      {
        System.out.println("ID: " + id + "(name=" + tulkasName + "): sturdiness conflict: tulkas=" + tulkasSturdiness + ", legacy=" + legacySturdiness);
      }
      if (tulkasSturdiness!=null)
      {
        legacy.setSturdiness(tulkasSturdiness);
      }
    }
    // Quality
    {
      ItemQuality quality=tulkas.getQuality();
      legacy.setQuality(quality);
    }
    // Description
    {
      String tulkasDescription=tulkas.getDescription();
      /*
      String legacyDescription=legacy.getDescription();
      if ((legacyDescription!=null) && (legacyDescription.length()>0))
      {
        if ((tulkasDescription!=null) && (tulkasDescription.length()>0))
        {
          if (!tulkasDescription.equals(legacyDescription))
          {
            System.out.println("ID: " + id + "(name=" + tulkasName + "): description conflict:\ntulkas=" + tulkasDescription + ",\nlegacy=" + legacyDescription);
          }
        }
      }
      */
      legacy.setDescription(tulkasDescription);
    }
    // Properties
    {
      Map<String,String> props=tulkas.getProperties();
      for(String propertyName : props.keySet())
      {
        legacy.setProperty(propertyName,props.get(propertyName));
      }
    }
    return legacy;
  }

  /*
  private void handleSubCategoriesMap()
  {
    // Tulkas to legacy
    HashMap<String,String> mapping=new HashMap<String,String>();
    for(Map.Entry<String,HashMap<String,IntegerHolder>> entry : _tulkas2legacySubCategoriesMap.entrySet())
    {
      HashMap<String,IntegerHolder> map=entry.getValue();
      if (map.size()==1)
      {
        mapping.put(entry.getKey(),map.keySet().iterator().next());
      }
      else
      {
        System.out.println(entry.getKey() + " => " + map);
      }
    }
    System.out.println("Mapping: " + mapping);
    // Legacy to tulkas
    System.out.println("Legacy to tulkas: " + _legacy2tulkasSubCategoriesMap);
    for(Map.Entry<String,Set<String>> entry : _legacy2tulkasSubCategoriesMap.entrySet())
    {
      Set<String> tulkasSubCategoryIds=entry.getValue();
      if (tulkasSubCategoryIds.size()>1)
      {
        System.out.println(entry.getKey() + " => " + tulkasSubCategoryIds);
      }
    }
  }
  */

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MergeItemsLegacyAndTulkasIndex().doIt();
  }
}
