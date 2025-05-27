package delta.games.lotro.tools.extraction.achievables;

import delta.games.lotro.common.requirements.AbstractAchievableRequirement;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.tools.extraction.achievables.rewards.DatRewardsLoader;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventConditionsLoader;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventsLoader;
import delta.games.lotro.tools.extraction.misc.WebStoreItemsLoader;
import delta.games.lotro.tools.extraction.requirements.UsageRequirementsLoader;
import delta.games.lotro.utils.Proxy;

/**
 * Utilities for achievable loaders (quests/deeds).
 * @author DAM
 */
public class AchievablesLoadingUtils
{
  private QuestRequirementsLoader _requirementsLoader;
  private WorldEventConditionsLoader _weConditionsLoader;
  private DatRewardsLoader _rewardsLoader;
  private WorldEventsLoader _worldEventsLoader;
  private WebStoreItemsLoader _webStoreItemsLoader;
  private AchievableEventIDLoader _eventIDsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param worldEventsLoader World events loader.
   * @param rewardsLoader Rewards loader.
   */
  public AchievablesLoadingUtils(DataFacade facade, WorldEventsLoader worldEventsLoader, DatRewardsLoader rewardsLoader)
  {
    _rewardsLoader=rewardsLoader;
    _worldEventsLoader=worldEventsLoader;
    _webStoreItemsLoader=new WebStoreItemsLoader(facade);
    _requirementsLoader=new QuestRequirementsLoader(facade);
    _weConditionsLoader=new WorldEventConditionsLoader(_worldEventsLoader);
    _eventIDsLoader=new AchievableEventIDLoader(facade);
  }

  /**
   * Get the rewards loader.
   * @return the rewards loader.
   */
  public DatRewardsLoader getRewardsLoader()
  {
    return _rewardsLoader;
  }

  /**
   * Get the web store items loader.
   * @return the web store items loader.
   */
  public WebStoreItemsLoader getWebStoreItemsLoader()
  {
    return _webStoreItemsLoader;
  }

  /**
   * Get the event IDs loader.
   * @return the event IDs loader.
   */
  public AchievableEventIDLoader getEventIDsLoader()
  {
    return _eventIDsLoader;
  }

  /**
   * Find quests requirements.
   * @param achievable Targeted achievable.
   * @param properties Properties to load from.
   */
  public void findPrerequisites(Achievable achievable, PropertiesSet properties)
  {
    PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissions!=null)
    {
      AbstractAchievableRequirement questRequirements=_requirementsLoader.loadQuestRequirements(achievable,permissions);
      if (questRequirements!=null)
      {
        achievable.setQuestRequirements(questRequirements);
      }
    }
    /*
    DefaultPermissionBlobStruct:
      Usage_QuestRequirements:
        #1:
          Usage_Operator: 3
          Usage_QuestID: 1879048439
          Usage_QuestStatus: 805306368
    */
  }

  /**
   * Find requirements.
   * @param achievable Targeted achievable.
   * @param properties Properties to load from.
   */
  public void findRequirements(Achievable achievable, PropertiesSet properties)
  {
    PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissions!=null)
    {
      // - generic usage requirements
      UsageRequirementsLoader.loadUsageRequirements(permissions,achievable.getUsageRequirement());
      // - world events
      AbstractWorldEventCondition worldEventsRequirements=_weConditionsLoader.loadWorldEventsUsageConditions(permissions);
      achievable.setWorldEventsRequirement(worldEventsRequirements);
    }
  }

  /**
   * Clean-up an achievable.
   * @param achievable Targeted achievable.
   */
  public void cleanup(Achievable achievable)
  {
    // Cleanup requirements
    AbstractAchievableRequirement requirement=achievable.getQuestRequirements();
    requirement=_requirementsLoader.deepRequirementCleanup(requirement);
    achievable.setQuestRequirements(requirement);
    // Next quest
    if (achievable instanceof QuestDescription)
    {
      QuestDescription quest=(QuestDescription)achievable;
      Proxy<Achievable> next=quest.getNextQuest();
      if (next!=null)
      {
        Achievable nextQuest=next.getObject();
        if (nextQuest==null)
        {
          quest.setNextQuest(null);
        }
      }
    }
  }

  /**
   * Save.
   */
  public void save()
  {
    // Save world events
    _worldEventsLoader.save();
  }
}
