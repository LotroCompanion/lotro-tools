package delta.games.lotro.tools.dat.quests.rewards;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for level-sensitive rewards maps.
 * @author DAM
 */
public class RewardsMapLoader
{
  private static final Logger LOGGER=Logger.getLogger(RewardsMapLoader.class);

  private DataFacade _facade;
  private int _rewardsMapId;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public RewardsMapLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load a rewards map.
   * @param rewardsMapId Rewards map ID.
   * @return A rewards map or <code>null</code> if properties not found.
   */
  public RewardsMap loadMap(int rewardsMapId)
  {
    _rewardsMapId=rewardsMapId;
    RewardsMap rewardsMap=null;
    PropertiesSet props=_facade.loadProperties(rewardsMapId+DATConstants.DBPROPERTIES_OFFSET);
    if (props!=null)
    {
      LOGGER.info("Loading map ID: "+rewardsMapId);
      rewardsMap=new RewardsMap();
      fill(props,rewardsMap);
    }
    return rewardsMap;
  }

  /**
   * Fill a rewards map from properties.
   * @param props Input properties.
   * @param storage Storage for loaded data.
   */
  private void fill(PropertiesSet props, RewardsMap storage)
  {
    fillList(props,"QuestReward_Accomplishment_ExpEntryList","QuestReward_ExpAmount",storage.getAccomplishmentXpMap());
    fillList(props,"QuestReward_Accomplishment_MoneyEntryList","QuestReward_MoneyAmount",storage.getAccomplishmentMoneyMap());
    fillList(props,"QuestReward_Accomplishment_RepEntryList","QuestReward_RepAmount",storage.getAccomplishmentReputationMap());
    fillList(props,"QuestReward_CraftExpEntryList","QuestReward_ExpAmount",storage.getCraftXpMap());
    fillList(props,"QuestReward_ExpEntryList","QuestReward_ExpAmount",storage.getXpMap());
    fillList(props,"QuestReward_GloryEntryList","QuestReward_GloryAmount",storage.getGloryMap());
    fillList(props,"QuestReward_ItemExpEntryList","QuestReward_ExpAmount",storage.getItemXpMap());
    fillList(props,"QuestReward_MithrilCoinEntryList","QuestReward_MithrilCoinAmount",storage.getMithrilCoinsMap());
    fillList(props,"QuestReward_MoneyEntryList","QuestReward_MoneyAmount",storage.getMoneyMap());
    fillList(props,"QuestReward_MountExpEntryList","QuestReward_ExpAmount",storage.getMountXpMap());
    fillList(props,"QuestReward_PaperItemsEntryList","QuestReward_PaperItems",storage.getPaperItemMap());
    fillList(props,"QuestReward_RepEntryList","QuestReward_RepAmount",storage.getReputationMap());
    fillList(props,"QuestReward_TurbinePointEntryList","QuestReward_TurbinePointAmount",storage.getTpMap());
    fillList(props,"QuestReward_SessionPointsEntryList","QuestReward_SessionPointsAmount",storage.getDestinyPointsMap());
    fillList(props,"QuestReward_VirtueExpEntryList","QuestReward_ExpAmount",storage.getVirtueXpMap());
  }

  private <T extends Number> void fillList(PropertiesSet props, String listPropName, String entryPropName, RewardLevelEntryList<T> storage)
  {
    Object[] list=(Object[])props.getProperty(listPropName);
    if (list!=null)
    {
      for(Object entryPropsObj : list)
      {
        PropertiesSet entryProps=(PropertiesSet)entryPropsObj;
        Integer tier=(Integer)entryProps.getProperty("QuestReward_Tier");
        @SuppressWarnings("unchecked")
        T value=(T)entryProps.getProperty(entryPropName);
        storage.addEntry(tier.intValue(),value);
      }
      LOGGER.info("Loaded list: "+listPropName+": "+storage);
    }
    else
    {
      LOGGER.warn("Failed to load list: "+listPropName+" for rewards map ID="+_rewardsMapId);
    }
  }
}
