package delta.games.lotro.tools.dat.quests;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.lore.deeds.geo.MainGeoDataInjector;

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
    //System.out.println(DatObjectivesLoader._flagsToAchievables);

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
    //int[] IDS=new int[]{1879277326,1879139074};
    //for(int id : IDS)
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    //for(int id=DEBUG_ID;id<=DEBUG_ID;id++)
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
    DatStatUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    // Save others
    _utils.save();
  }

  private void doIndex()
  {
    PropertiesSet deedsDirectory=_facade.loadProperties(0x79000255);
    //System.out.println(deedsDirectory.dump());
    Object[] list=(Object[])deedsDirectory.getProperty("Accomplishment_List");
    for(Object obj : list)
    {
      if (obj instanceof Integer)
      {
        load(((Integer)obj).intValue());
      }
      else if (obj instanceof Object[])
      {
        Object[] objs=(Object[])obj;
        for(Object obj2 : objs)
        {
          if (obj2 instanceof Integer)
          {
            load(((Integer)obj2).intValue());
          }
          else
          {
            System.out.println(obj.getClass());
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
    DataFacade facade=new DataFacade();
    DatRewardsLoader rewardsLoader=new DatRewardsLoader(facade);
    new MainDatAchievablesLoader(facade,rewardsLoader).doIt();
    facade.dispose();
  }
}
