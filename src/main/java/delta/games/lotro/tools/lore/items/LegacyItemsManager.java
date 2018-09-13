package delta.games.lotro.tools.lore.items;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.io.xml.ItemSaxParser;

/**
 * Manager for legacy items (grabbed from R.I.P lotro lorebook).
 * @author DAM
 */
public class LegacyItemsManager
{
  private Map<String,List<Integer>> _mapByKey;
  private Map<String,Integer> _mapByIcon;

  /**
   * Constructor.
   */
  public LegacyItemsManager()
  {
    load();
  }

  private void load()
  {
    File file=new File("data/items/in/itemsLegacy.xml");
    List<Item> items=ItemSaxParser.parseItemsFile(file);
    _mapByKey=buildMapByKey(items);
    _mapByIcon=buildMapByIcon(items);
  }

  private Map<String,List<Integer>> buildMapByKey(List<Item> items)
  {
    Map<String,List<Integer>> map=new HashMap<String,List<Integer>>();
    for(Item item : items)
    {
      String key=item.getKey();
      key=key.replace("%27","'");
      key=key.replace("%C3%A2","â");
      key=key.replace("%C3%BA","ú");
      key=key.replace("%C3%B3","ó");
      key=key.replace("%C3%A9","é");
      key=key.replace("%C3%A4","ä");
      key=key.replace("%C3%BB","û");
      item.setKey(key);
      List<Integer> list=map.get(key);
      if (list==null)
      {
        list=new ArrayList<Integer>();
        map.put(key,list);
      }
      list.add(Integer.valueOf(item.getIdentifier()));
    }
    return map;
  }

  private Map<String,Integer> buildMapByIcon(List<Item> items)
  {
    Map<String,Integer> map=new HashMap<String,Integer>();
    for(Item item : items)
    {
      String iconURL=item.getIconURL();
      if (iconURL==null)
      {
        //System.out.println("No icon for: "+item.getName());
        continue;
      }
      Integer old=map.put(iconURL,Integer.valueOf(item.getIdentifier()));
      if (old!=null)
      {
        System.out.println("Duplicated icon URL: "+iconURL);
      }
    }
    return map;
  }

  /**
   * Get an item ID using its wiki key and its icon URL.
   * @param key Lorebook wiki key.
   * @param iconUrl Icon URL.
   * @return An item ID or <code>null</code> if not found.
   */
  public Integer getByKeyAndIconUrl(String key, String iconUrl)
  {
    Integer ret=null;
    List<Integer> byKey=_mapByKey.get(key);
    Integer byIcon=_mapByIcon.get(iconUrl);
    if (byIcon!=null)
    {
      if ((byKey==null) || (!byKey.contains(byIcon)))
      {
        //System.out.println("For key=["+key+"], icon=["+iconUrl+"], found "+byIcon+" by icon, but no key match!");
      }
      ret=byIcon;
    }
    else
    {
      if (byKey!=null)
      {
        ret=byKey.get(0);
      }
    }
    return ret;
  }
}
