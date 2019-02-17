package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.common.Effect;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Utility methods for effects found in DAT files.
 * @author DAM
 */
public class DatEffectUtils
{
  /**
   * Load stats for an effect.
   * @param facade Data facade.
   * @param effectId Effect identifier.
   * @return A stats provider.
   */
  public static StatsProvider loadEffectStats(DataFacade facade, int effectId)
  {
    PropertiesSet effectProps=facade.loadProperties(effectId+0x9000000);
    //System.out.println(effectProps.dump());
    StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,effectProps);
    /*
    int nbStats=statsProvider.getNumberOfStatProviders();
    for(int i=0;i<nbStats;i++)
    {
      StatProvider statProvider=statsProvider.getStatProvider(i);
      StatDescription stat=statProvider.getStat();
      System.out.println("\t\t"+stat);
    }
    */
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
    Effect ret=null;
    PropertiesSet effectProps=facade.loadProperties(effectId+0x9000000);
    if (effectProps!=null)
    {
      ret=new Effect();
      ret.setId(effectId);
      StatsProvider provider=DatStatUtils.buildStatProviders(facade,effectProps);
      ret.setStatsProvider(provider);
    }
    else
    {
      System.out.println("Effect not found: "+effectId);
    }
    return ret;
  }
}
