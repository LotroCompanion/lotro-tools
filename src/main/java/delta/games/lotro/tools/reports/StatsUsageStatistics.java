package delta.games.lotro.tools.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.common.stats.StatDescription;

/**
 * Statistics about stats usage.
 * @author DAM
 */
public class StatsUsageStatistics
{
  private Map<StatDescription,IntegerHolder> _data;

  /**
   * Constructor.
   */
  public StatsUsageStatistics()
  {
    _data=new HashMap<StatDescription,IntegerHolder>();
  }

  /**
   * Reset statistics.
   */
  public void reset()
  {
    _data.clear();
  }

  /**
   * Register stat usage.
   * @param stat Stat to use.
   */
  public void registerStatUsage(StatDescription stat)
  {
    IntegerHolder counter=_data.get(stat);
    if (counter==null)
    {
      counter=new IntegerHolder();
      _data.put(stat,counter);
    }
    counter.increment();
  }

  /**
   * Get the list of used stats.
   * @return A list of stats, ordered by decreasing usage count.
   */
  private List<StatDescription> getUsedStats()
  {
    List<StatDescription> stats=new ArrayList<StatDescription>(_data.keySet());
    Comparator<StatDescription> c=new Comparator<StatDescription>()
    {
      @Override
      public int compare(StatDescription o1, StatDescription o2)
      {
        IntegerHolder count1=_data.get(o1);
        IntegerHolder count2=_data.get(o2);
        int diff=count2.getInt()-count1.getInt();
        if (diff==0)
        {
          return o1.getName().compareTo(o2.getName());
        }
        return diff;
      }
    };
    Collections.sort(stats,c);
    return stats;
  }

  /**
   * Display results.
   */
  public void showResults()
  {
    List<StatDescription> stats=getUsedStats();
    for(StatDescription stat : stats)
    {
      IntegerHolder counter=_data.get(stat);
      System.out.println(stat.getPersistenceKey()+"\t"+stat.getName()+"\t"+counter);
    }
  }
}
