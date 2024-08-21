package delta.games.lotro.tools.dat.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.lore.achievables.geo.MainGeoDataInjector;

/**
 * Get quests/deeds definitions from DAT files.
 * @author DAM
 */
public class MainDatAchievablesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatAchievablesLoader.class);

  private DataFacade _facade;
  private AchievablesLogger _logger;
  private AchievablesLoadingUtils _utils;
  private QuestsLoader _questsLoader;
  private DeedsLoader _deedsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param rewardsLoader Rewards loader.
   */
  public MainDatAchievablesLoader(DataFacade facade, DatRewardsLoader rewardsLoader)
  {
    _facade=facade;
    _logger=new AchievablesLogger(true,true,"achievables.txt");
    _utils=new AchievablesLoadingUtils(facade,rewardsLoader);
    _questsLoader=new QuestsLoader(facade,_utils);
    _deedsLoader=new DeedsLoader(facade,_utils);
  }

  private void load(int indexDataId)
  {
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      // Route: quest or deed?
      boolean isQuest=DatQuestDeedsUtils.isQuest(properties);
      if (isQuest)
      {
        _questsLoader.loadQuest(indexDataId,properties);
      }
      else
      {
        _deedsLoader.loadDeed(indexDataId,properties);
      }
      _logger.handleAchievable(indexDataId,isQuest,properties);
    }
    else
    {
      LOGGER.warn("Could not handle achievable ID="+indexDataId);
    }
  }

  /**
   * Load quests and deeds.
   */
  public void doIt()
  {
    // Scan quests and deeds
    doScan();
    // Use deeds index
    doIndex();
    // Add quest arcs
    _questsLoader.loadQuestArcs();
    // Add race/class requirements for deed
    _deedsLoader.loadRaceRequirements();
    _deedsLoader.loadClassRequirements();
    // Resolve proxies
    resolveProxies();

    List<DeedDescription> deeds=_deedsLoader.postProcess();
    // - achievables geo data injection
    MainGeoDataInjector geoDataInjector=new MainGeoDataInjector(_facade);
    List<Achievable> achievables=new ArrayList<Achievable>();
    achievables.addAll(deeds);
    achievables.addAll(_questsLoader.getQuests());
    geoDataInjector.doIt(achievables);

    // Save
    doSave();
    _logger.finish();
  }

  private void doScan()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=DatQuestDeedsUtils.isQuestOrDeedId(_facade,id);
      if (useIt)
      {
        load(id);
      }
    }
  }

  private void resolveProxies()
  {
    AchievableProxiesResolver resolver=new AchievableProxiesResolver(_questsLoader.getQuests(),_deedsLoader.getDeeds());
    _questsLoader.resolveProxies(resolver);
    _deedsLoader.resolveProxies(resolver);
  }

  private void doSave()
  {
    // Save quests
    _questsLoader.save();
    // Save deeds
    _deedsLoader.save();
    // Save progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    // Save others
    _utils.save();
  }

  private void doIndex()
  {
    PropertiesSet deedsDirectory=_facade.loadProperties(0x79000255);
    Object[] list=(Object[])deedsDirectory.getProperty("Accomplishment_List");
    for(Object entry : list)
    {
      if (entry instanceof Integer)
      {
        // Accomplishment_File
        load(((Integer)entry).intValue());
      }
      else if (entry instanceof Object[])
      {
        // Accomplishment_Set
        Object[] childEntries=(Object[])entry;
        for(Object childEntry : childEntries)
        {
          if (childEntry instanceof Integer)
          {
            // Accomplishment_File
            load(((Integer)childEntry).intValue());
          }
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    DatRewardsLoader rewardsLoader=new DatRewardsLoader(facade);
    new MainDatAchievablesLoader(facade,rewardsLoader).doIt();
    facade.dispose();
  }
}
