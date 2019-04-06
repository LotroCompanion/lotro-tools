package delta.games.lotro.tools.dat.quests.rewards;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for level-sensitive rewards maps.
 * @author DAM
 */
public class RewardsMapLoader
{
  private DataFacade _facade;

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
   * @param rewardLevelId Reward map ID.
   * @return A rewards map or <code>null</code> if properties not found.
   */
  public RewardsMap loadMap(int rewardLevelId)
  {
    RewardsMap rewardsMap=null;
    PropertiesSet props=_facade.loadProperties(rewardLevelId+0x9000000);
    if (props!=null)
    {
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
    }
  }
}
