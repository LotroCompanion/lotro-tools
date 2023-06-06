package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Utility methods for effects found in DAT files.
 * @author DAM
 */
public class DatEffectUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatEffectUtils.class);

  /**
   * Load stats for an effect.
   * @param facade Data facade.
   * @param effectId Effect identifier.
   * @return A stats provider.
   */
  public static StatsProvider loadEffectStats(DataFacade facade, int effectId)
  {
    PropertiesSet effectProps=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,effectProps);
    return statsProvider;
  }

  /**
   * Load an effect.
   * @param facade Data facade.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  public static Effect loadEffect(DataFacade facade, int effectId)
  {
    return loadEffect(facade,effectId,null);
  }

  /**
   * Load an effect.
   * @param facade Data facade.
   * @param effectId Effect identifier.
   * @param i18nUtils I18N utilities.
   * @return An effect or <code>null</code> if not found.
   */
  public static Effect loadEffect(DataFacade facade, int effectId, I18nUtils i18nUtils)
  {
    Effect ret=null;
    PropertiesSet effectProps=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps!=null)
    {
      ret=new Effect();
      ret.setId(effectId);
      // Name
      String effectName;
      if (i18nUtils!=null)
      {
        effectName=i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
      }
      else
      {
        effectName=DatUtils.getStringProperty(effectProps,"Effect_Name");
      }
      ret.setName(effectName);
      // Duration
      Float duration=(Float)effectProps.getProperty("Effect_Duration_ConstantInterval");
      ret.setDuration(duration);
      // Icon
      Integer effectIconId=(Integer)effectProps.getProperty("Effect_Icon");
      if (effectIconId!=null)
      {
        ret.setIconId(effectIconId);
      }
      // Stats
      StatsProvider provider=DatStatUtils.buildStatProviders(facade,effectProps);
      ret.setStatsProvider(provider);
    }
    else
    {
      LOGGER.warn("Effect not found: "+effectId);
    }
    return ret;
  }
}
