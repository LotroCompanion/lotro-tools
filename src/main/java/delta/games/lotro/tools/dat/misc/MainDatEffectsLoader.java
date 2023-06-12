package delta.games.lotro.tools.dat.misc;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

/**
 * Get effects from DAT files.
 * @author DAM
 */
public class MainDatEffectsLoader
{
  private DataFacade _facade;
  private DatStatUtils _statUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _statUtils=new DatStatUtils(facade);
  }

  private Effect load(int effectId)
  {
    Effect ret=DatEffectUtils.loadEffect(_statUtils,effectId);
    if (ret!=null)
    {
      String name=ret.getName();
      StatsProvider statsProvider=ret.getStatsProvider();
      StatProvider hopeProvider=statsProvider.getStat(WellKnownStat.HOPE);
      if (hopeProvider!=null)
      {
        System.out.println("Effect: id="+effectId+", name="+name+" => "+statsProvider+" => "+statsProvider.getStats(1,120));
      }
    }
    return ret;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return ((classDefIndex==WStateClass.EFFECT) || (classDefIndex==WStateClass.EFFECT2)
          || (classDefIndex==WStateClass.EFFECT3) || (classDefIndex==WStateClass.EFFECT4)
          || (classDefIndex==WStateClass.EFFECT5));
    }
    return false;
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        load(id);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatEffectsLoader(facade).doIt();
    facade.dispose();
  }
}
