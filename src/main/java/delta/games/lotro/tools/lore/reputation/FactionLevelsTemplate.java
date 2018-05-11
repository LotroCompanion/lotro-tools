package delta.games.lotro.tools.lore.reputation;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.reputation.FactionLevel;

/**
 * Template for faction levels.
 * @author DAM
 */
public class FactionLevelsTemplate
{
  private String _key;
  private List<FactionLevel> _levels;

  /**
   * Constructor.
   * @param key Identifying key.
   * @param levels A list of faction levels.
   */
  public FactionLevelsTemplate(String key, List<FactionLevel> levels)
  {
    _key=key;
    _levels=levels;
  }

  /**
   * Get the identifying key for this template.
   * @return A key.
   */
  public String getKey()
  {
    return _key;
  }

  /**
   * Build the faction levels using this template.
   * @return A list of new faction levels.
   */
  public List<FactionLevel> buildLevels()
  {
    List<FactionLevel> ret=new ArrayList<FactionLevel>();
    for(FactionLevel level : _levels)
    {
      FactionLevel newLevel=new FactionLevel(level.getKey(),level.getName(),level.getValue(),level.getLotroPoints(),level.getRequiredXp());
      ret.add(newLevel);
    }
    return ret;
  }
}
