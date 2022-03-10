package delta.games.lotro.tools.dat.quests;

import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.RequirementsLoadingUtils;

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
  public static void loadUsageRequirements(PropertiesSet permissions, UsageRequirement storage)
  {
    // - level
    RequirementsLoadingUtils.loadLevelRequirements(permissions,storage);
    // - races
    RequirementsLoadingUtils.loadRequiredRaces(permissions,storage);
    // - classes
    RequirementsLoadingUtils.loadRequiredClasses(permissions,storage);
    // - faction
    RequirementsLoadingUtils.loadRequiredFaction(permissions,storage);
    // TODO: Usage_WorldEvent_AllConditionList, Usage_WorldEvent_AnyConditionList
    // TODO: Usage_RequiredAccountToken
  }
}
