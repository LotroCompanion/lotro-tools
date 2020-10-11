package delta.games.lotro.tools.dat.maps.classification;

import delta.games.lotro.lore.agents.AgentClassification;

/**
 * Monster classification.
 * @author DAM
 */
public class MonsterClassification extends Classification
{
  private AgentClassification _classification;

  /**
   * Constructor.
   * @param classification Agent classification.
   */
  public MonsterClassification(AgentClassification classification)
  {
    _classification=classification;
  }

  @Override
  public String toString()
  {
    return _classification.toString();
  }
}
