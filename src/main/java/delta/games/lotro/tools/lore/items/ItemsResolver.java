package delta.games.lotro.tools.lore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Resolve items using their name/key.
 * @author DAM
 */
public class ItemsResolver
{
  private HashMap<String,List<Item>> _ids;

  /**
   * Constructor.
   */
  public ItemsResolver()
  {
    _ids=loadFileIds();
  }

  /**
   * Get an item using a 'key' (name, lorebook key, icon path).
   * @param key Key to use.
   * @return An item or <code>null</code> if not found.
   */
  public Item getItem(String key)
  {
    List<Item> items=_ids.get(key);
    if (items==null)
    {
      return null;
    }
    if (items.size()>1)
    {
      System.out.println("Warn: "+key+" : "+items.size()+" : "+items);
      return null;
    }
    return items.get(0);
  }

  /**
   * Load map (keys/names)->list of item ids
   * @return a map.
   */
  private HashMap<String,List<Item>> loadFileIds()
  {
    HashMap<String,List<Item>> idStr2Id=new HashMap<String,List<Item>>(); 
    ItemsManager mgr=ItemsManager.getInstance();
    List<Item> items=mgr.getAllItems();
    for(Item item : items)
    {
      String name=item.getName();
      registerMapping(idStr2Id,name,item);
    }
    // Dump keys
    /*
    List<String> keys=new ArrayList<String>(idStr2Id.keySet());
    Collections.sort(keys);
    for(String key : keys)
    {
      List<Item> itemsForId=idStr2Id.get(key);
      //if (ids.size()>1)
      {
        System.out.println("*************** "+key+" ******************");
        Collections.sort(items,new ItemIdComparator());
        for(Item item : itemsForId)
        {
          System.out.println("\t"+item);
        }
      }
    }
    */
    return idStr2Id;
  }

  private void registerMapping(HashMap<String,List<Item>> idStr2Id, String key, Item item)
  {
    if (key!=null)
    {
      List<Item> ids=idStr2Id.get(key);
      if (ids==null)
      {
        ids=new ArrayList<Item>();
        idStr2Id.put(key,ids);
      }
      if (!ids.contains(item))
      {
        ids.add(item);
      }
    }
  }
}
