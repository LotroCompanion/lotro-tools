package delta.games.lotro.tools.dat.quests;

import delta.games.lotro.common.requirements.AbstractAchievableRequirement;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.tools.dat.misc.WebStoreItemsLoader;
import delta.games.lotro.tools.dat.misc.WorldEventsLoader;
import delta.games.lotro.tools.dat.utils.StringRenderingUtils;
import delta.games.lotro.tools.dat.utils.WorldEventConditionsLoader;
import delta.games.lotro.utils.Proxy;

/**
 * Utilities for achievable loaders (quests/deeds).
 * @author DAM
 */
public class AchievablesLoadingUtils
{
  private StringRenderer _renderer;
  private QuestRequirementsLoader _requirementsLoader;
  private UsageRequirementsLoader _usageRequirementsLoader;
  private WorldEventConditionsLoader _weConditionsLoader;
  private DatRewardsLoader _rewardsLoader;
  private WorldEventsLoader _worldEventsLoader;
  private WebStoreItemsLoader _webStoreItemsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param rewardsLoader Rewards loader.
   */
  public AchievablesLoadingUtils(DataFacade facade, DatRewardsLoader rewardsLoader)
  {
    _rewardsLoader=rewardsLoader;
    _worldEventsLoader=new WorldEventsLoader(facade);
    _webStoreItemsLoader=new WebStoreItemsLoader(facade);
    _renderer=StringRenderingUtils.buildAllOptionsRenderer();
    _requirementsLoader=new QuestRequirementsLoader(facade);
    _usageRequirementsLoader=new UsageRequirementsLoader();
    _weConditionsLoader=new WorldEventConditionsLoader(_worldEventsLoader);
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
   * Render a string format.
   * @param format Input format.
   * @return a rendered string.
   */
  public String renderName(String format)
  {
    String ret=_renderer.render(format);
    ret=ret.replace("  "," ");
    ret=ret.trim();
    return ret;
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
      _usageRequirementsLoader.loadUsageRequirements(permissions,achievable.getUsageRequirement());
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
    // Save web store items
    _webStoreItemsLoader.save();
  }
}
