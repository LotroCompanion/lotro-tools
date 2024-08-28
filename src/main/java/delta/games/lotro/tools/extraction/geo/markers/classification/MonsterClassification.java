package delta.games.lotro.tools.extraction.geo.markers.classification;

/**
 * Monster classification.
 * @author DAM
 */
public class MonsterClassification extends Classification
{
  private boolean _critter;

  /**
   * Constructor.
   * @param critter Critter or not.
   */
  public MonsterClassification(boolean critter)
  {
    _critter=critter;
  }

  /**
   * Indicates if this is a critter monster/NPC.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public boolean isCritter()
  {
    return _critter;
  }
}
