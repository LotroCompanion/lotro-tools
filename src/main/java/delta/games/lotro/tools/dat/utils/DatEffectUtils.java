package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.legendary.passives.Passive;

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
   * Load a passive.
   * @param statUtils Stat utils.
   * @param effectId Effect identifier.
   * @return A passive or <code>null</code> if not found.
   */
  public static Passive loadPassive(DatStatUtils statUtils, int effectId)
  {
    Passive ret=null;
    DataFacade facade=statUtils.getFacade();
    PropertiesSet effectProps=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps!=null)
    {
      ret=new Passive();
      ret.setId(effectId);
      // Stats
      StatsProvider provider=statUtils.buildStatProviders(effectProps);
      ret.setStatsProvider(provider);
    }
    else
    {
      LOGGER.warn("Passive not found: "+effectId);
    }
    return ret;
  }
}
