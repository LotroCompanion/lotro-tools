package delta.games.lotro.tools.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;

/**
 * Resolution stats.
 * @author DAM
 */
public class ResolutionStats
{
  private Map<String,IntegerHolder> _missingKeys;
  private int _totalMisses;
  private int _totalHits;
  private int _recipeMisses;
  private int _resolvedItems;
  private int _uncompleteItems;

  /**
   * Constructor.
   */
  public ResolutionStats()
  {
    _missingKeys=new HashMap<String,IntegerHolder>();
  }

  /**
   * Start new item.
   */
  public void startItem()
  {
    _recipeMisses=0;
  }

  /**
   * Register a resolution result.
   * @param ok Indicates if the resolution was ok or not.
   * @param name Name of the item to resolve.
   */
  public void registerResolution(boolean ok, String name)
  {
    if (ok)
    {
      _totalHits++;
    }
    else
    {
      IntegerHolder holder=_missingKeys.get(name);
      if (holder==null)
      {
        holder=new IntegerHolder();
        _missingKeys.put(name,holder);
      }
      holder.increment();
      _totalMisses++;
      _recipeMisses++;
    }
  }

  /**
   * End current item.
   */
  public void endItem()
  {
    if (_recipeMisses==0)
    {
      _resolvedItems++;
    }
    else
    {
      _uncompleteItems++;
    }
  }

  /**
   * Show stats.
   */
  public void show()
  {
    int total=0;
    int nbMissingKeys=_missingKeys.size();
    int nbFound=0;
    int size=0;
    while (nbFound<nbMissingKeys)
    {
      List<String> keys=getMissingKeys(size);
      int nbKeys=keys.size();
      if (nbKeys>0)
      {
        String label=keys.toString();
        //int length=label.length();
        //if (length>200) label=label.substring(0,200)+"...";
        System.out.println(size+": "+keys.size()+" keys: "+label);
        total+=(size*nbKeys);
        System.out.println("Total: "+total);
        nbFound+=nbKeys;
      }
      size++;
    }
    System.out.println("Missing: "+nbMissingKeys);
    System.out.println("Hits/misses/total: "+_totalHits+"/"+_totalMisses+"/"+(_totalHits+_totalMisses));
    System.out.println("Resolved/uncomplete/total: "+_resolvedItems+"/"+_uncompleteItems+"/"+(_resolvedItems+_uncompleteItems));
  }

  private List<String> getMissingKeys(int occurrence)
  {
    List<String> keys=new ArrayList<String>();
    for(Map.Entry<String,IntegerHolder> entry : _missingKeys.entrySet())
    {
      if (entry.getValue().getInt()==occurrence)
      {
        keys.add(entry.getKey());
      }
    }
    Collections.sort(keys);
    return keys;
  }
}
