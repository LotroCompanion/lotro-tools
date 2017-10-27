package delta.games.lotro.tools.lore.deeds;

import java.util.Arrays;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.deeds.index.DeedCategory;
import delta.games.lotro.lore.deeds.index.DeedSummary;
import delta.games.lotro.lore.deeds.index.DeedsIndex;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.lore.items.ItemsResolver;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Loader for the reward items of deeds.
 * @author DAM
 */
public class DeedsItemsLoader
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

  private ItemsResolver _resolver;

  private int _resolved=0;
  private int _missed=0;

  private static final String WIKI_SEED="/wiki/";

  /**
   * Constructor.
   */
  public DeedsItemsLoader()
  {
    _resolver=new ItemsResolver();
  }

  private void handleSet(ObjectsSet set)
  {
    if (set!=null)
    {
      int nb=set.getNbObjectItems();
      for(int i=0;i<nb;i++)
      {
        ObjectItem item=set.getItem(i);
        handleItem(item);
      }
    }
  }

  private void handleItem(ObjectItem itemReference)
  {
    // Use name
    String name=itemReference.getName();
    Item item=_resolver.getItem(name);
    if (item==null)
    {
      // Use item 'key' (Wiki key)
      String url=itemReference.getObjectURL();
      if ((url!=null) && (url.startsWith(WIKI_SEED)))
      {
        String itemKey=url.substring(WIKI_SEED.length());
        item=_resolver.getItem(itemKey);
      }
    }
    if (item==null)
    {
      // Use icon path
      String iconUrl=itemReference.getIconURL();
      if (iconUrl!=null)
      {
        iconUrl=ItemsResolver.normalizeIconUrl(iconUrl);
        item=_resolver.getItem(iconUrl);
      }
    }
    if (item!=null)
    {
      // Found!
      itemReference.setItemId(item.getIdentifier());
      itemReference.setIconURL(null);
      itemReference.setObjectURL(null);
      _resolved++;
    }
    else if ("Rotten Fruit".equals(name))
    {
      // Found!
      itemReference.setItemId(1879199969);
      itemReference.setIconURL(null);
      itemReference.setObjectURL(null);
      _resolved++;
    }
    else if ("Flower Petals".equals(name))
    {
      // Found!
      itemReference.setItemId(1879199971);
      itemReference.setIconURL(null);
      itemReference.setObjectURL(null);
      _resolved++;
    }
    else
    {
      // Not found!
      System.out.println("Not found: "+itemReference);
      _missed++;
    }
  }

  private void handleDeed(int index, DeedDescription q)
  {
    try
    {
      String key=q.getKey();
      System.out.println("#"+index+", deed: "+key);
      Rewards r=q.getRewards();
      ObjectsSet set=r.getObjects();
      handleSet(set);
      ObjectsSet set2=r.getSelectObjects();
      handleSet(set2);
    }
    catch(Throwable t)
    {
      t.printStackTrace();
    }
  }
  
  private void doIt()
  {
    DeedsManager qm=DeedsManager.getInstance();
    DeedsIndex index=qm.getIndex();
    if (index!=null)
    {
      String[] categories=index.getCategories();
      System.out.println(Arrays.deepToString(categories));
      for(String category : categories)
      {
        DeedCategory c=index.getCategory(category);
        DeedSummary[] deeds=c.getDeeds();
        int indexDeed=0;
        for(DeedSummary deedsSummary : deeds)
        {
          int id=deedsSummary.getIdentifier();
          DeedDescription deed=qm.getDeed(id);
          handleDeed(indexDeed,deed);
          indexDeed++;
        }
      }
      System.out.println("Resolved: "+_resolved+", missed: "+_missed);
    }
    else
    {
      _logger.error("Cannot gets deeds index file!");
    }
  }

  /**
   * Basic main method for test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DeedsItemsLoader().doIt();
  }
}
