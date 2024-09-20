package delta.games.lotro.tools.extraction.characters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;

/**
 * Utility methods related to traits.
 * @author DAM
 */
public class TraitUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(TraitUtils.class);

  /**
   * Get a trait.
   * @param traitId Trait identifier.
   * @return the trait description or <code>null</code> if not found/loaded.
   */
  public static TraitDescription getTrait(int traitId)
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    TraitDescription trait=traitsMgr.getTrait(traitId);
    if (trait==null)
    {
      LOGGER.warn("Could not find trait ID="+traitId);
    }
    return trait;
  }
}
