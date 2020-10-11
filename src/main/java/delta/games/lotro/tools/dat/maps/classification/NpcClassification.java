package delta.games.lotro.tools.dat.maps.classification;

/**
 * NPC classification.
 * @author DAM
 */
public class NpcClassification extends Classification
{
  // Item-master
  // Crafting vendor
  // Town Crier
  // Vendor
  // Trainer
  // Stable-master
  // Trader
  // Barber
  // Faction Representative

  private String _type;

  /**
   * Constructor.
   * @param type NPC type (may be <code>null</code>).
   */
  public NpcClassification(String type)
  {
    _type=type;
  }

  /**
   * Get the NPC type.
   * @return the NPC type.
   */
  public String getType()
  {
    return _type;
  }

  @Override
  public String toString()
  {
    return "NPC type="+_type;
  }
}
