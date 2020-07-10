package delta.games.lotro.tools.dat.quests.rewards;

/**
 * Storage for tiered rewards (level sensitive).
 * @author DAM
 */
public class RewardsMap
{
  private RewardLevelEntryList<Integer> _accomplishmentXp;
  private RewardLevelEntryList<Integer> _accomplishmentMoney;
  private RewardLevelEntryList<Integer> _accomplishmentReputation;
  private RewardLevelEntryList<Integer> _craftXp;
  private RewardLevelEntryList<Integer> _xp;
  private RewardLevelEntryList<Integer> _glory;
  private RewardLevelEntryList<Integer> _itemXp;
  private RewardLevelEntryList<Integer> _mithrilCoins;
  private RewardLevelEntryList<Integer> _money;
  private RewardLevelEntryList<Integer> _mountXp;
  private RewardLevelEntryList<Float> _paperItem;
  private RewardLevelEntryList<Integer> _reputation;
  private RewardLevelEntryList<Integer> _tp;
  private RewardLevelEntryList<Integer> _destinyPoints;
  private RewardLevelEntryList<Integer> _virtueXp;

  /**
   * Constructor.
   */
  public RewardsMap()
  {
    _accomplishmentXp=new RewardLevelEntryList<Integer>();
    _accomplishmentMoney=new RewardLevelEntryList<Integer>();
    _accomplishmentReputation=new RewardLevelEntryList<Integer>();
    _craftXp=new RewardLevelEntryList<Integer>();
    _xp=new RewardLevelEntryList<Integer>();
    _glory=new RewardLevelEntryList<Integer>();
    _itemXp=new RewardLevelEntryList<Integer>();
    _mithrilCoins=new RewardLevelEntryList<Integer>();
    _money=new RewardLevelEntryList<Integer>();
    _mountXp=new RewardLevelEntryList<Integer>();
    _paperItem=new RewardLevelEntryList<Float>();
    _reputation=new RewardLevelEntryList<Integer>();
    _tp=new RewardLevelEntryList<Integer>();
    _destinyPoints=new RewardLevelEntryList<Integer>();
    _virtueXp=new RewardLevelEntryList<Integer>();
  }

  /**
   * Get the accomplishment XP map.
   * @return the accomplishment XP map.
   */
  public RewardLevelEntryList<Integer> getAccomplishmentXpMap()
  {
    return _accomplishmentXp;
  }

  /**
   * Get the accomplishment money map.
   * @return the accomplishment money map.
   */
  public RewardLevelEntryList<Integer> getAccomplishmentMoneyMap()
  {
    return _accomplishmentMoney;
  }

  /**
   * Get the accomplishment reputation map.
   * @return the accomplishment reputation map.
   */
  public RewardLevelEntryList<Integer> getAccomplishmentReputationMap()
  {
    return _accomplishmentReputation;
  }

  /**
   * Get the craft XP map.
   * @return the craft XP map.
   */
  public RewardLevelEntryList<Integer> getCraftXpMap()
  {
    return _craftXp;
  }

  /**
   * Get the XP map.
   * @return the XP map.
   */
  public RewardLevelEntryList<Integer> getXpMap()
  {
    return _xp;
  }

  /**
   * Get the glory map.
   * @return the glory map.
   */
  public RewardLevelEntryList<Integer> getGloryMap()
  {
    return _glory;
  }

  /**
   * Get the item XP map.
   * @return the item XP map.
   */
  public RewardLevelEntryList<Integer> getItemXpMap()
  {
    return _itemXp;
  }

  /**
   * Get the mithril coins map.
   * @return the mithril coins map.
   */
  public RewardLevelEntryList<Integer> getMithrilCoinsMap()
  {
    return _mithrilCoins;
  }

  /**
   * Get the money map.
   * @return the money map.
   */
  public RewardLevelEntryList<Integer> getMoneyMap()
  {
    return _money;
  }

  /**
   * Get the mount XP map.
   * @return the mount XP map.
   */
  public RewardLevelEntryList<Integer> getMountXpMap()
  {
    return _mountXp;
  }

  /**
   * Get the paper item map.
   * @return the paper item map.
   */
  public RewardLevelEntryList<Float> getPaperItemMap()
  {
    return _paperItem;
  }

  /**
   * Get the reputation map.
   * @return the reputation map.
   */
  public RewardLevelEntryList<Integer> getReputationMap()
  {
    return _reputation;
  }

  /**
   * Get the Turbine points map.
   * @return the Turbine points map.
   */
  public RewardLevelEntryList<Integer> getTpMap()
  {
    return _tp;
  }

  /**
   * Get the destiny points map.
   * @return the destiny points map.
   */
  public RewardLevelEntryList<Integer> getDestinyPointsMap()
  {
    return _destinyPoints;
  }

  /**
   * Get the virtue XP map.
   * @return the virtue XP map.
   */
  public RewardLevelEntryList<Integer> getVirtueXpMap()
  {
    return _virtueXp;
  }
}
