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
   * @param statUtils Stat utilities.
   * @param effectId Effect identifier.
   * @return A stats provider.
   */
  public static StatsProvider loadEffectStats(DatStatUtils statUtils, int effectId)
  {
    DataFacade facade=statUtils.getFacade();
    PropertiesSet effectProps=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    StatsProvider statsProvider=statUtils.buildStatProviders(effectProps);
    return statsProvider;
  }

  /**
   * Load an effect.
   * @param statUtils Stat utils.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  public static Effect loadEffect(DatStatUtils statUtils, int effectId)
  {
    return loadEffect(statUtils,effectId,null);
  }

  /**
   * Load an effect.
   * @param statUtils Stat utils.
   * @param effectId Effect identifier.
   * @param i18nUtils I18N utilities.
   * @return An effect or <code>null</code> if not found.
   */
  public static Effect loadEffect(DatStatUtils statUtils, int effectId, I18nUtils i18nUtils)
  {
    Effect ret=null;
    DataFacade facade=statUtils.getFacade();
    PropertiesSet effectProps=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps!=null)
    {
      //System.out.println(effectProps.dump());
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
      // Description
      String description;
      if (i18nUtils!=null)
      {
        description=i18nUtils.getStringProperty(effectProps,"Effect_Definition_Description");
      }
      else
      {
        description=DatUtils.getStringProperty(effectProps,"Effect_Definition_Description");
      }
      ret.setDescription(description);
      // TODO: use Effect_Applied_Description too!
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
      StatsProvider provider=statUtils.buildStatProviders(effectProps);
      ret.setStatsProvider(provider);
    }
    else
    {
      LOGGER.warn("Effect not found: "+effectId);
    }
    return ret;
  }
}
