package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;

/**
 * Loader for 'property modification' effects.
 * @param <T> Type of managed effects.
 * @author DAM
 */
public class PropertyModificationEffectLoader<T extends PropertyModificationEffect> extends AbstractEffectLoader<T>
{
  private DatStatUtils _statUtils;

  /**
   * Set the stat loading utils.
   * @param statUtils Utils to set.
   */
  public void setStatUtils(DatStatUtils statUtils)
  {
    _statUtils=statUtils;
  }

  @Override
  public void loadSpecifics(T effect, PropertiesSet effectProps)
  {
    // Effect PropertyModificationEffect (734) or MountEffect (2459)
    Object modArray=effectProps.getProperty("Mod_Array");
    if (modArray==null)
    {
      return;
    }
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(effectProps);
    effect.setStatsProvider(statsProvider);
  }
}
