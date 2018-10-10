package delta.games.lotro.tools.lore.items.merges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.items.Item;

/**
 * Tries to set the missing item level for crafted legendary items.
 * @author DAM
 */
public class LegendaryItemLevelFinder
{
  private Map<String,List<Item>> _liItemsByName;

  /**
   * Constructor.
   */
  public LegendaryItemLevelFinder()
  {
    _liItemsByName=new HashMap<String,List<Item>>();
  }

  /**
   * Do it.
   * @param items Items to use.
   */
  public void doIt(List<Item> items)
  {
    selectItems(items);
    updateItems();
  }

  private void selectItems(List<Item> items)
  {
    for(Item item : items)
    {
      boolean eligible=isEligible(item);
      if (eligible)
      {
        String name=item.getName();
        List<Item> itemsOfName=_liItemsByName.get(name);
        if (itemsOfName==null)
        {
          itemsOfName=new ArrayList<Item>();
          _liItemsByName.put(name,itemsOfName);
        }
        itemsOfName.add(item);
      }
    }
  }

  private boolean isEligible(Item item)
  {
    String name=item.getName();
    if (name==null)
    {
      return false;
    }
    // 3rd age
    if ((name.startsWith("Crafted")) && (name.endsWith("of the Third Age")))
    {
      return true;
    }
    // 2nd age
    if ((name.startsWith("Reforged")) && (name.endsWith("Second Age")))
    {
      return true;
    }
    // 1st age
    if ((name.startsWith("Reshaped")) && (name.endsWith("First Age")))
    {
      return true;
    }
    return false;
  }

  private void updateItems()
  {
    for(Map.Entry<String,List<Item>> entry : _liItemsByName.entrySet())
    {
      String name=entry.getKey();
      List<Item> items=entry.getValue();
      handleItems(name,items);
    }
  }

  private void handleItems(String name,List<Item> items)
  {
    int nbItems=items.size();
    if (nbItems==6)
    {
      // Expect to find level 60, 65, 75, 85, 95, 100
      int[] levels={60, 65, 75, 85, 95, 100};
      resolve(name,items,levels);
    }
    else if (nbItems==5)
    {
      if (name.endsWith("First Age"))
      {
        // Expect to find level 65, 75, 85, 95, 100
        int[] levels={ 65, 75, 85, 95, 100};
        resolve(name,items,levels);
      }
    }
    else
    {
      System.out.println("Nb items: "+nbItems+" for "+name);
    }
  }

  private void resolve(String name, List<Item> items, int[] levels)
  {
    List<Item> itemsWithNoLevel=new ArrayList<Item>();
    for(Item item : items)
    {
      Integer itemLevel=item.getItemLevel();
      if (itemLevel!=null)
      {
        for(int i=0;i<levels.length;i++)
        {
          if (itemLevel.intValue()==levels[i])
          {
            levels[i]=-1;
            break;
          }
        }
      }
      else
      {
        itemsWithNoLevel.add(item);
      }
    }
    if (itemsWithNoLevel.size()==1)
    {
      Item toPatch=itemsWithNoLevel.get(0);
      for(int i=0;i<levels.length;i++)
      {
        if (levels[i]!=-1)
        {
          if (levels[i]==100)
          {
            toPatch.setItemLevel(Integer.valueOf(levels[i]));
            System.out.println("Set level "+levels[i]+" to item: "+name);
            break;
          }
        }
      }
    }
  }
}
