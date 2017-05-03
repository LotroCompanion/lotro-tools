package delta.games.lotro.tools.lore.items.scalables;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.stats.ItemStatSliceData;
import delta.games.lotro.lore.items.stats.SlicesBasedItemStatsProvider;

/**
 * Finds scaling parameters from raw stat values.
 * @author DAM
 */
public class SliceCountFinder
{
  private Map<STAT,HashMap<Integer,Float>> _map;

  /**
   * Constructor.
   * @param itemLevel Item level.
   */
  public SliceCountFinder(int itemLevel)
  {
    _map=new HashMap<STAT,HashMap<Integer,Float>>();
    for(STAT stat : STAT.values())
    {
      if (stat==STAT.ARMOUR) continue;
      HashMap<Integer,Float> map=new HashMap<Integer,Float>();
      for(int i=1;i<50;i++)
      {
        Float sliceCount=Float.valueOf(((float)i)/10);
        ItemStatSliceData slice=new ItemStatSliceData(stat,sliceCount,null);
        int value=(int)SlicesBasedItemStatsProvider.getStatValue(slice,itemLevel);
        map.put(Integer.valueOf(value*100),sliceCount);
      }
      _map.put(stat,map);
    }
  }

  /**
   * Get the slice count value for a give stat and stat value.
   * @param stat Stat to use.
   * @param statValue Value to use.
   * @return A slice count or <code>null</code> if not found.
   */
  public Float getSliceCount(STAT stat, int statValue)
  {
    Float ret=null;
    HashMap<Integer,Float> conversionMap=_map.get(stat);
    if (conversionMap!=null)
    {
      ret=conversionMap.get(Integer.valueOf(statValue));
    }
    return ret;
  }
}
