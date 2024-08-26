package delta.games.lotro.tools.extraction.geo.markers.classification;

import delta.games.lotro.lore.agents.AgentClassification;

/**
 * Monster classification.
 * @author DAM
 */
public class MonsterClassification extends Classification
{
  private AgentClassification _classification;
  private boolean _isCritter;

  /**
   * Constructor.
   * @param classification Agent classification.
   */
  public MonsterClassification(AgentClassification classification)
  {
    _classification=classification;
    if (classification!=null)
    {
      _isCritter=(classification.getEntityClassification().getSpecies().getCode()==27);
    }
    else
    {
      _isCritter=true;
    }
  }

  /**
   * Indicates if this is a critter monster/NPC.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public boolean isCritter()
  {
    return _isCritter;
  }

  @Override
  public String toString()
  {
    return _classification.toString();
  }
}
