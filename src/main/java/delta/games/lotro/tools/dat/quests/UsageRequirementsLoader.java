package delta.games.lotro.tools.dat.quests;

import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.RequirementsLoadingUtils;
import delta.games.lotro.tools.dat.utils.WorldEventConditionsLoader;

/**
 * Loader for usage requirements.
 * @author DAM
 */
public class UsageRequirementsLoader
{
  private WorldEventConditionsLoader _weConditionsLoader;

  /**
   * Constructor.
   * @param weConditionsLoader
   */
  public UsageRequirementsLoader(WorldEventConditionsLoader weConditionsLoader)
  {
    _weConditionsLoader=weConditionsLoader;
  }

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
    // - world events
    _weConditionsLoader.loadWorldEventsUsageRequirements(permissions,storage);
    // TODO: Usage_RequiredAccountToken
  }
}
