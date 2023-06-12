package delta.games.lotro.tools.dat.characters;

import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.tomes.StatTome;
import delta.games.lotro.character.stats.tomes.StatTomesManager;
import delta.games.lotro.character.stats.tomes.io.xml.StatTomesXMLWriter;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

/**
 * Get stat tomes from DAT files.
 * @author DAM
 */
public class MainStatTomesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainStatTomesLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private StatTomesManager _tomesManager;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainStatTomesLoader(DataFacade facade)
  {
    _facade=facade;
    _statUtils=new DatStatUtils(facade);
    _tomesManager=new StatTomesManager();
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    PropertiesSet statDirectoryProps=_facade.loadProperties(0x79000229);
    Object[] subDirectories=(Object[])statDirectoryProps.getProperty("Stat_StatList");
    for(Object subDirectoryObj : subDirectories)
    {
      int subDirectoryId=((Integer)subDirectoryObj).intValue();
      handleStat(subDirectoryId);
    }
    //showRegistry();
    StatTomesXMLWriter.write(GeneratedFiles.STAT_TOMES,_tomesManager);
  }

  private void handleStat(int directoryId)
  {
    PropertiesSet statProps=_facade.loadProperties(directoryId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] tomeTraitIds=(Object[])statProps.getProperty("WebStore_Stat_Trait_List");
    if (tomeTraitIds!=null)
    {
      int rank=1;
      for(Object tomeTraitObj : tomeTraitIds)
      {
        int tomeTraitId=((Integer)tomeTraitObj).intValue();
        handleStatTome(tomeTraitId,rank);
        rank++;
      }
    }
  }

  private void handleStatTome(int tomeTraitId, int rank)
  {
    PropertiesSet traitTomeProps=_facade.loadProperties(tomeTraitId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] traitEffects=(Object[])traitTomeProps.getProperty("EffectGenerator_TraitEffectList");
    if ((traitEffects!=null) && (traitEffects.length==1))
    {
      PropertiesSet traitEffectProps=(PropertiesSet)traitEffects[0];
      int effectId=((Integer)traitEffectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Float spellcraft=(Float)traitEffectProps.getProperty("EffectGenerator_EffectSpellcraft");
      StatsProvider statsProvider=DatEffectUtils.loadEffectStats(_statUtils,effectId);
      int level=spellcraft.intValue();
      BasicStatsSet stats=statsProvider.getStats(1,level);
      registerStatTome(tomeTraitId,rank,stats);
    }
    else
    {
      LOGGER.warn("No effect or more than 1 effect!");
    }
  }

  private void registerStatTome(int traitId, int rank, BasicStatsSet stats)
  {
    StatDescription stat=stats.getStats().iterator().next();
    StatTome tome=new StatTome(stat,rank,traitId,stats);
    _tomesManager.registerStatTome(tome);
  }

  void showRegistry()
  {
    List<StatDescription> stats=_tomesManager.getStats();
    for(StatDescription stat : stats)
    {
      System.out.println(stat.getName());
      int nbRanks=_tomesManager.getNbOfRanks(stat);
      for(int i=1;i<=nbRanks;i++)
      {
        StatTome tome=_tomesManager.getStatTome(stat,i);
        System.out.println("\t"+tome);
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
    new MainStatTomesLoader(facade).doIt();
    facade.dispose();
  }
}
