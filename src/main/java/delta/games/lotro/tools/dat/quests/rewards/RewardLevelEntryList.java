package delta.games.lotro.tools.dat.quests.rewards;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry list for a reward level.
 * @author DAM
 * @param <T> Type of values.
 */
public class RewardLevelEntryList<T extends Number>
{
  private Map<Integer,T> _entries;

  /**
   * Constructor.
   */
  public RewardLevelEntryList()
  {
    _entries=new HashMap<Integer,T>();
  }

  /**
   * Add an entry.
   * @param tier Tier.
   * @param value Value.
   */
  public void addEntry(int tier, T value)
  {
    _entries.put(Integer.valueOf(tier),value);
  }

  /**
   * Get the value for a tier.
   * @param tier Tier to use.
   * @return A value or <code>null</code>.
   */
  public T getValue(int tier)
  {
    return _entries.get(Integer.valueOf(tier));
  }

  @Override
  public String toString()
  {
    return _entries.toString();
  }
}
