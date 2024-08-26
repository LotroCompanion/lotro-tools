package delta.games.lotro.tools.extraction.requirements;

import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for usage requirements.
 * @author DAM
 */
public class UsageRequirementsLoader
{
  /**
   * Load usage requirements.
   * @param permissions Input properties.
   * @param storage Storage for loaded data.
   */
  public void loadUsageRequirements(PropertiesSet permissions, UsageRequirement storage)
  {
    // - level
    RequirementsLoadingUtils.loadLevelRequirements(permissions,storage);
    // - races
    RequirementsLoadingUtils.loadRequiredRaces(permissions,storage);
    // - classes
    RequirementsLoadingUtils.loadRequiredClasses(permissions,storage);
    // - faction
    RequirementsLoadingUtils.loadRequiredFaction(permissions,storage);
    // - profession
    RequirementsLoadingUtils.loadRequiredProfession(permissions,storage);
    // TODO: Usage_RequiredAccountToken
  }
}
