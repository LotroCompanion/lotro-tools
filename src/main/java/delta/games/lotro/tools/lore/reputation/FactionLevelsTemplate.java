package delta.games.lotro.tools.lore.reputation;

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
   * Get the faction levels for this template.
   * @return A list of faction levels.
   */
  public List<FactionLevel> getLevels()
  {
    return _levels;
  }
}
