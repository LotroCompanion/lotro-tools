package delta.games.lotro.tools.dat.maps.classification;

/**
 * Item classification.
 * @author DAM
 */
public class ItemClassification extends Classification
{
  // Mailbox
  // Reflecting Pool
  // Camp Site
  // Milestone

  private String _type;

  /**
   * Constructor.
   * @param type Item type.
   */
  public ItemClassification(String type)
  {
    _type=type;
  }

  /**
   * Get the item type.
   * @return an item type or <code>null</code>.
   */
  public String getType()
  {
    return _type;
  }

  @Override
  public String toString()
  {
    return "Item type="+_type;
  }
}
