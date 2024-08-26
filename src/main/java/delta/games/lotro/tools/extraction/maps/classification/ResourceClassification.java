package delta.games.lotro.tools.extraction.maps.classification;

import delta.games.lotro.lore.crafting.CraftingLevel;

/**
 * Resource node classification.
 * @author DAM
 */
public class ResourceClassification extends Classification
{
  private CraftingLevel _level;

  /**
   * Constructor.
   * @param level Crafting level.
   */
  public ResourceClassification(CraftingLevel level)
  {
    _level=level;
  }

  /**
   * Get the crafting level.
   * @return the crafting level.
   */
  public CraftingLevel getCraftingLevel()
  {
    return _level;
  }

  @Override
  public String toString()
  {
    return "Node for="+_level;
  }
}
