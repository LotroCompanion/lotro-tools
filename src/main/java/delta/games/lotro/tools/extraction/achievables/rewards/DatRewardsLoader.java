package delta.games.lotro.tools.extraction.achievables.rewards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.VirtuesManager;
import delta.games.lotro.common.ChallengeLevel;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.rewards.BillingTokenReward;
import delta.games.lotro.common.rewards.CraftingXpReward;
import delta.games.lotro.common.rewards.EmoteReward;
import delta.games.lotro.common.rewards.ItemReward;
import delta.games.lotro.common.rewards.QuestCompleteReward;
import delta.games.lotro.common.rewards.RelicReward;
import delta.games.lotro.common.rewards.ReputationReward;
import delta.games.lotro.common.rewards.RewardElement;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.common.rewards.SelectableRewardElement;
import delta.games.lotro.common.rewards.TitleReward;
import delta.games.lotro.common.rewards.TraitReward;
import delta.games.lotro.common.rewards.VirtueReward;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.lore.parameters.Game;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;
import delta.games.lotro.tools.extraction.characters.TraitUtils;
import delta.games.lotro.tools.extraction.utils.DatEnumsUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quest/deed rewards from DAT files.
 * @author DAM
 */
public class DatRewardsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DatRewardsLoader.class);

  private static final int TIER_ARTISAN=4;

  private DataFacade _facade;
  private ItemsManager _itemsMgr;
  private FactionsRegistry _factions;
  private Map<Integer,RewardsMap> _rewardLevels;
  private Map<Integer,RewardsMap> _defaultRewardMaps;
  private RewardsMapLoader _rewardLevelLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DatRewardsLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsMgr=ItemsManager.getInstance();
    _factions=FactionsRegistry.getInstance();
    _rewardLevels=new HashMap<Integer,RewardsMap>();
    _defaultRewardMaps=new HashMap<Integer,RewardsMap>();
    _rewardLevelLoader=new RewardsMapLoader(facade);
    loadMainMaps();
  }

  /**
   * Fill rewards from DAT files data.
   * @param properties Quest/deed properties.
   * @param rewards Storage for loaded data.
   * @return challenge level.
   */
  public ChallengeLevel fillRewards(PropertiesSet properties, Rewards rewards)
  {
    // Challenge level
    ChallengeLevel challengeLevel=findChallengeLevel(properties);

    // Find out reward level
    RewardsMap rewardsMap=findRewardsMap(challengeLevel, properties);

    // Faction
    getFaction(rewards,properties,rewardsMap);
    // Destiny points: Quest_SessionPointsTier. See enum 587202823.
    // Disabled - found weird values.
    // Class points
    Integer classPoints=((Integer)properties.getProperty("Quest_ClassTraitPointReward"));
    if (classPoints!=null)
    {
      rewards.setClassPoints(classPoints.intValue());
    }
    // Item XP
    // Loot table (titles, emotes, items...)
    Integer treasureId=((Integer)properties.getProperty("Quest_QuestTreasureDID"));
    if (treasureId!=null)
    {
      loadRewards(rewards,treasureId.intValue(),rewardsMap);
    }
    loadScalableRewards(rewards,properties,rewardsMap);
    loadFixedRewards(rewards,properties);
    // Quests to complete
    handleQuestCompletes(properties,rewards.getRewardElements());
    return challengeLevel;
  }

  /**
   * Load rewards from a treasure identifier.
   * @param rewards Storage.
   * @param questTreasureId Treasure identifier.
   * @param rewardsMap Rewards map.
   */
  public void loadRewards(Rewards rewards, int questTreasureId, RewardsMap rewardsMap)
  {
    PropertiesSet props=_facade.loadProperties(questTreasureId+DATConstants.DBPROPERTIES_OFFSET);

    // Traits
    handleTraits(rewards,props);
    // Titles
    handleTitles(rewards,props);
    // Virtues
    handleVirtues(rewards,props);
    // Virtue XP
    Integer virtueXpTier=((Integer)props.getProperty("QuestTreasure_Virtue_XP_Tier"));
    if ((virtueXpTier!=null) && (virtueXpTier.intValue()>0))
    {
      if (rewardsMap==null)
      {
        LOGGER.warn("No rewards map. Using a default rewards map.");
        rewardsMap=_defaultRewardMaps.get(Integer.valueOf(1));
      }
      Integer virtueXp=rewardsMap.getVirtueXpMap().getValue(virtueXpTier.intValue());
      if (virtueXp!=null)
      {
        rewards.setVirtueXp(virtueXp.intValue());
      }
    }
    // Emotes
    handleEmotes(rewards,props);
    // Items
    handleItems(props,"QuestTreasure_FixedItemArray", rewards.getRewardElements());
    SelectableRewardElement selectableReward=new SelectableRewardElement();
    handleItems(props,"QuestTreasure_SelectableItemArray", selectableReward.getElements());
    if (selectableReward.getNbElements()>0)
    {
      rewards.addRewardElement(selectableReward);
    }
    // Relics
    handleRelics(rewards,props);
    // Billing token
    handleBillingTokens(props,rewards.getRewardElements());
    // Destiny points?
  }

  // Virtues
  private void handleVirtues(Rewards rewards, PropertiesSet props)
  {
    Object[] virtueArray=(Object[])props.getProperty("QuestTreasure_FixedVirtueArray");
    if (virtueArray!=null)
    {
      for(Object virtueObj : virtueArray)
      {
        PropertiesSet virtueProps=(PropertiesSet)virtueObj;
        int virtueStatId=((Integer)virtueProps.getProperty("QuestTreasure_Virtue")).intValue();
        Integer increment=(Integer)virtueProps.getProperty("QuestTreasure_Virtue_Increment");
        VirtueDescription virtue=getVirtue(virtueStatId);
        int count=(increment!=null)?increment.intValue():1;
        if (virtue!=null)
        {
          VirtueReward virtueReward=new VirtueReward(virtue,count);
          rewards.addRewardElement(virtueReward);
        }
      }
    }
  }

  private void handleTitles(Rewards rewards, PropertiesSet props)
  {
    handleTitlesArray(props,"QuestTreasure_FixedTitleArray",rewards.getRewardElements());
    Object hasSelectableTitles=props.getProperty("QuestTreasure_SelectableTitleArray");
    if (hasSelectableTitles!=null)
    {
      SelectableRewardElement selectableTitles=new SelectableRewardElement();
      handleTitlesArray(props,"QuestTreasure_SelectableTitleArray",selectableTitles.getElements());
      rewards.addRewardElement(selectableTitles);
    }
  }

  private void handleTitlesArray(PropertiesSet props, String propertyName, List<RewardElement> rewards)
  {
    Object[] titleArray=(Object[])props.getProperty(propertyName);
    if (titleArray!=null)
    {
      for(Object titleObj : titleArray)
      {
        int titleId=((Integer)titleObj).intValue();
        handleTitle(titleId,rewards);
      }
    }
  }

  private void handleTitle(int titleId, List<RewardElement> rewards)
  {
    TitlesManager titlesMgr=TitlesManager.getInstance();
    TitleDescription title=titlesMgr.getTitle(titleId);
    if (title!=null)
    {
      TitleReward titleReward=new TitleReward(title);
      rewards.add(titleReward);
    }
    else
    {
      LOGGER.warn("Title not found: {}",Integer.valueOf(titleId));
    }
  }

  private void handleEmotes(Rewards rewards, PropertiesSet props)
  {
    handleEmotesArray(props,"QuestTreasure_FixedEmoteArray",rewards.getRewardElements());
    Object hasSelectableEmotes=props.getProperty("QuestTreasure_SelectableEmoteArray");
    if (hasSelectableEmotes!=null)
    {
      SelectableRewardElement selectableEmotes=new SelectableRewardElement();
      handleEmotesArray(props,"QuestTreasure_SelectableEmoteArray",selectableEmotes.getElements());
      rewards.addRewardElement(selectableEmotes);
    }
  }

  private void handleEmotesArray(PropertiesSet props, String propertyName, List<RewardElement> rewards)
  {
    Object[] emoteArray=(Object[])props.getProperty(propertyName);
    if (emoteArray!=null)
    {
      for(Object emoteObj : emoteArray)
      {
        int emoteId=((Integer)emoteObj).intValue();
        handleEmote(emoteId,rewards);
      }
    }
  }

  private void handleEmote(int emoteId, List<RewardElement> rewards)
  {
    EmotesManager emotesMgr=EmotesManager.getInstance();
    EmoteDescription emote=emotesMgr.getEmote(emoteId);
    if (emote!=null)
    {
      EmoteReward emoteReward=new EmoteReward(emote);
      rewards.add(emoteReward);
    }
    else
    {
      LOGGER.warn("Emote not found: {}",Integer.valueOf(emoteId));
    }
  }

  private void handleTraits(Rewards rewards, PropertiesSet props)
  {
    Object[] traitArray=(Object[])props.getProperty("QuestTreasure_FixedTraitArray");
    if (traitArray!=null)
    {
      for(Object traitObj : traitArray)
      {
        int traitId=((Integer)traitObj).intValue();
        handleTrait(traitId,rewards.getRewardElements());
      }
    }
  }

  private void handleTrait(int traitId, List<RewardElement> rewards)
  {
    TraitDescription trait=TraitUtils.getTrait(traitId);
    if (trait!=null)
    {
      TraitReward traitReward=new TraitReward(trait);
      rewards.add(traitReward);
    }
    else
    {
      LOGGER.warn("Trait not found: {}",Integer.valueOf(traitId));
    }
  }

  private void handleItems(PropertiesSet props, String propertyName, List<RewardElement> rewards)
  {
    Object[] itemArray=(Object[])props.getProperty(propertyName);
    if (itemArray!=null)
    {
      for(Object itemObj : itemArray)
      {
        PropertiesSet itemProps=(PropertiesSet)itemObj;
        int itemId=((Integer)itemProps.getProperty("QuestTreasure_Item")).intValue();
        Integer quantityValue=(Integer)itemProps.getProperty("QuestTreasure_ItemQuantity");
        Item item=_itemsMgr.getItem(itemId);
        if (item!=null)
        {
          int quantity=(quantityValue!=null?quantityValue.intValue():1);
          ItemReward itemReward=new ItemReward(item,quantity);
          rewards.add(itemReward);
        }
        else
        {
          LOGGER.warn("Item not found: {}",Integer.valueOf(itemId));
        }
      }
    }
  }

  private void handleRelics(Rewards rewards, PropertiesSet props)
  {
    handleRelicsArray(props,"QuestTreasure_FixedRunicArray",rewards.getRewardElements());
    Object hasSelectableEmotes=props.getProperty("QuestTreasure_SelectableRunicArray");
    if (hasSelectableEmotes!=null)
    {
      SelectableRewardElement selectableEmotes=new SelectableRewardElement();
      handleRelicsArray(props,"QuestTreasure_SelectableRunicArray",selectableEmotes.getElements());
      rewards.addRewardElement(selectableEmotes);
    }
  }

  private void handleRelicsArray(PropertiesSet props, String propertyName, List<RewardElement> rewards)
  {
    Object[] relicsArray=(Object[])props.getProperty(propertyName);
    if (relicsArray!=null)
    {
      for(Object relicObj : relicsArray)
      {
        PropertiesSet relicProps=(PropertiesSet)relicObj;
        int relicId=((Integer)relicProps.getProperty("QuestTreasure_Item")).intValue();
        Integer quantityValue=(Integer)relicProps.getProperty("QuestTreasure_ItemQuantity");
        handleRelic(relicId,quantityValue,rewards);
      }
    }
  }

  private void handleRelic(int relicId, Integer quantityValue, List<RewardElement> rewards)
  {
    RelicsManager relicsMgr=RelicsManager.getInstance();
    Relic relic=relicsMgr.getById(relicId);
    if (relic!=null)
    {
      int quantity=(quantityValue!=null?quantityValue.intValue():1);
      RelicReward relicReward=new RelicReward(relic,quantity);
      rewards.add(relicReward);
    }
    else
    {
      LOGGER.warn("Relic not found: {}",Integer.valueOf(relicId));
    }
  }

  private VirtueDescription getVirtue(int statId)
  {
    VirtueDescription ret=null;
    StatDescription stat=StatsRegistry.getInstance().getById(statId);
    String statKey=stat.getKey();
    List<VirtueDescription> virtues=VirtuesManager.getInstance().getAll();
    for(VirtueDescription virtue : virtues)
    {
      if (virtue.getRankStatKey().equals(statKey))
      {
        ret=virtue;
        break;
      }
    }
    if (ret==null)
    {
      LOGGER.warn("Unmanaged virtue: {}",statKey);
    }
    return ret;
  }

  private void getFaction(Rewards rewards, PropertiesSet props, RewardsMap rewardsMap)
  {
    // Positive
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_PositiveFaction");
      handleFactionProps(rewards,rewardsMap,props,factionProps,1);
    }
    // Negative
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_NegativeFaction");
      handleFactionProps(rewards,rewardsMap,props,factionProps,-1);
    }
  }

  private void handleFactionProps(Rewards rewards, RewardsMap rewardsMap, PropertiesSet props, PropertiesSet factionProps, int factor)
  {
    if (factionProps!=null)
    {
      Integer factionId=(Integer)factionProps.getProperty("Quest_FactionDID");
      if (factionId!=null)
      {
        int reputationValue=0;
        // Rep tier?
        Integer repTier=(Integer)factionProps.getProperty("Quest_RepTier");
        if (repTier!=null)
        {
          Integer repValue=rewardsMap.getReputationMap().getValue(repTier.intValue());
          reputationValue=(repValue!=null)?repValue.intValue():0;
        }
        // Rep amount?
        Integer posRep=(Integer)props.getProperty("Quest_PosRepToGive");
        if (posRep!=null)
        {
          reputationValue=posRep.intValue();
        }
        Integer negRep=(Integer)props.getProperty("Quest_NegRepToGive");
        if (negRep!=null)
        {
          reputationValue=Math.abs(negRep.intValue());
        }
        handleFactionReward(rewards,factionId,reputationValue*factor);
      }
    }
  }

  private void handleFactionReward(Rewards rewards, Integer factionId, int reputationValue)
  {
    if (reputationValue!=0)
    {
      Faction faction=_factions.getById(factionId.intValue());
      if (faction!=null)
      {
        ReputationReward repItem=new ReputationReward(faction);
        repItem.setAmount(reputationValue);
        rewards.addRewardElement(repItem);
      }
      else
      {
        LOGGER.warn("Faction not found: {}",factionId);
      }
    }
  }

  private void handleBillingTokens(PropertiesSet props, List<RewardElement> rewards)
  {
    Object[] billingTokenArray=(Object[])props.getProperty("QuestTreasure_FixedBillingTokenArray");
    if (billingTokenArray!=null)
    {
      for(Object billingTokenObj : billingTokenArray)
      {
        int billingGroupId=((Integer)billingTokenObj).intValue();
        handleBillingGroup(billingGroupId,rewards);
      }
    }
  }

  private void handleBillingGroup(int billingGroupId, List<RewardElement> rewards)
  {
    LotroEnum<BillingGroup> billingGroupsEnum=LotroEnumsRegistry.getInstance().get(BillingGroup.class);
    BillingGroup billingGroup=billingGroupsEnum.getEntry(billingGroupId);
    if (billingGroup!=null)
    {
      BillingTokenReward billingTokenReward=new BillingTokenReward(billingGroup);
      rewards.add(billingTokenReward);
    }
    else
    {
      LOGGER.warn("Billing token not found: {}",Integer.valueOf(billingGroupId));
    }
  }

  private void handleQuestCompletes(PropertiesSet props, List<RewardElement> rewards)
  {
    Object[] questsToCompleteArray=(Object[])props.getProperty("Quest_QuestsToComplete");
    if (questsToCompleteArray!=null)
    {
      for(Object questsToCompleteObj : questsToCompleteArray)
      {
        int achievableId=((Integer)questsToCompleteObj).intValue();
        handleQuestComplete(achievableId,rewards);
      }
    }
  }

  private void handleQuestComplete(int achievableId, List<RewardElement> rewards)
  {
    Proxy<Achievable> proxy=new Proxy<Achievable>();
    proxy.setId(achievableId);
    QuestCompleteReward reward=new QuestCompleteReward(proxy);
    rewards.add(reward);
  }

  private RewardsMap findRewardsMap(ChallengeLevel challengeLevel, PropertiesSet properties)
  {
    RewardsMap rewardsMap=null;
    Integer rewardLevelId=((Integer)properties.getProperty("Quest_RewardLevelDID"));
    if (rewardLevelId!=null)
    {
      // Specific one
      rewardsMap=getRewardsMap(rewardLevelId.intValue());
    }
    else
    {
      // Generic one, based on the challenge level
      int level=challengeLevel.getEffectiveLevel();
      if (level==0)
      {
        level=Game.getParameters().getMaxCharacterLevel();
      }
      rewardsMap=_defaultRewardMaps.get(Integer.valueOf(level));
    }
    return rewardsMap;
  }

  private void loadScalableRewards(Rewards rewards, PropertiesSet properties, RewardsMap rewardsMap)
  {
    // Gold
    Integer goldTier=((Integer)properties.getProperty("Quest_GoldTier"));
    if (goldTier!=null)
    {
      Integer gold=rewardsMap.getMoneyMap().getValue(goldTier.intValue());
      if (gold!=null)
      {
        rewards.getMoney().setRawValue(gold.intValue());
      }
    }
    // XP
    Integer xpTier=((Integer)properties.getProperty("Quest_ExpTier"));
    if (xpTier!=null)
    {
      Integer xp=rewardsMap.getXpMap().getValue(xpTier.intValue());
      if (xp!=null)
      {
        rewards.setXp(xp.intValue());
      }
    }
    // Item XP
    Integer itemXpTier=((Integer)properties.getProperty("Quest_ItemExpTier"));
    if (itemXpTier!=null)
    {
      Integer itemXp=rewardsMap.getItemXpMap().getValue(itemXpTier.intValue());
      if (itemXp!=null)
      {
        rewards.setItemXp(itemXp.intValue());
      }
    }
    // Mount XP
    Integer mountXpTier=((Integer)properties.getProperty("Quest_MountExpTier"));
    if (mountXpTier!=null)
    {
      Integer mountXp=rewardsMap.getMountXpMap().getValue(mountXpTier.intValue());
      if (mountXp!=null)
      {
        rewards.setMountXp(mountXp.intValue());
      }
    }
    // Craft XP
    Integer craftXpTier=((Integer)properties.getProperty("Quest_CraftExpTier"));
    if (craftXpTier!=null)
    {
      Integer craftXp=rewardsMap.getCraftXpMap().getValue(craftXpTier.intValue());
      Integer professionId=(Integer)properties.getProperty("Quest_CraftProfessionDID");
      if ((craftXp!=null) && (professionId!=null))
      {
        Profession profession=DatEnumsUtils.getProfessionFromId(professionId.intValue());
        int craftingTier=TIER_ARTISAN;
        CraftingXpReward craftingXpReward=new CraftingXpReward(profession,craftingTier,craftXp.intValue());
        rewards.addRewardElement(craftingXpReward);
      }
      else
      {
        LOGGER.warn("Could not find XP value for a crafting XP reward!");
      }
    }
    // Glory (Renown or Infamy if MONSTER_PLAY)
    Integer gloryTier=((Integer)properties.getProperty("Quest_GloryTier"));
    if (gloryTier!=null)
    {
      Integer glory=rewardsMap.getGloryMap().getValue(gloryTier.intValue());
      if (glory!=null)
      {
        rewards.setGlory(glory.intValue());
      }
    }
    // Mithril coins (Quest_MithrilCoinsTier). Used but no effect in-game!
    // Turbine/Lotro points
    Integer tpTier=((Integer)properties.getProperty("Quest_TurbinePointTier"));
    if (tpTier!=null)
    {
      Integer lotroPoints=rewardsMap.getTpMap().getValue(tpTier.intValue());
      if (lotroPoints!=null)
      {
        rewards.setLotroPoints(lotroPoints.intValue());
      }
    }
  }

  private void loadFixedRewards(Rewards rewards, PropertiesSet properties)
  {
    // Gold
    Integer gold=((Integer)properties.getProperty("Quest_GoldToGive"));
    if (gold!=null)
    {
      rewards.getMoney().setRawValue(gold.intValue());
    }
    // XP
    Integer xp=((Integer)properties.getProperty("Quest_ExpToGive"));
    if (xp!=null)
    {
      rewards.setXp(xp.intValue());
    }
    // Glory (Renown or Infamy if MONSTER_PLAY)
    Integer glory=((Integer)properties.getProperty("Quest_GloryToGive"));
    if (glory!=null)
    {
      rewards.setGlory(glory.intValue());
    }
  }

  private ChallengeLevel findChallengeLevel(PropertiesSet properties)
  {
    ChallengeLevel ret=null;
    Integer challengeLevel=(Integer)properties.getProperty("Quest_ChallengeLevel");
    Integer challengeLevelOverrideProperty=(Integer)properties.getProperty("Quest_ChallengeLevelOverrideProperty");
    Integer ignoreDefaultChallengeLevel=(Integer)properties.getProperty("Quest_IgnoreDefaultChallengeLevel");
    boolean ignoreChallengeLevel=((ignoreDefaultChallengeLevel!=null) && (ignoreDefaultChallengeLevel.intValue()!=0));
    if ((ignoreChallengeLevel) || (challengeLevel==null) || (challengeLevelOverrideProperty!=null))
    {
      if (challengeLevelOverrideProperty!=null)
      {
        if (challengeLevelOverrideProperty.intValue()==268439569)
        {
          ret=ChallengeLevel.CHARACTER_LEVEL;
        }
        else if (challengeLevelOverrideProperty.intValue()==268446666)
        {
          ret=ChallengeLevel.SKIRMISH_LEVEL;
        }
        else
        {
          LOGGER.warn("Unmanaged challenge level property: {}",challengeLevelOverrideProperty);
        }
      }
    }
    /*
    Quest_ChallengeLevel: 100
    Quest_ChallengeLevelOverrideProperty: 268439569
    Quest_IgnoreDefaultChallengeLevel: 1
    */
    if (ret==null)
    {
      if (challengeLevel!=null)
      {
        ret=ChallengeLevel.getByCode(challengeLevel.intValue());
      }
      else
      {
        ret=ChallengeLevel.ONE;
      }
    }
    return ret;
  }

  private RewardsMap getRewardsMap(int rewardLevelId)
  {
    Integer key=Integer.valueOf(rewardLevelId);
    RewardsMap rewardLevel=_rewardLevels.get(key);
    if (rewardLevel==null)
    {
      rewardLevel=_rewardLevelLoader.loadMap(rewardLevelId);
      _rewardLevels.put(key,rewardLevel);
    }
    return rewardLevel;
  }

  private void loadMainMaps()
  {
    // QuestControl
    PropertiesSet questControlProps=_facade.loadProperties(1879048802+DATConstants.DBPROPERTIES_OFFSET);
    Object[] rewardMaps=(Object[])questControlProps.getProperty("QuestControl_RewardLevelDIDArray");
    if (rewardMaps!=null)
    {
      int index=1;
      for(Object rewardMapObj : rewardMaps)
      {
        LOGGER.debug("Loading default rewards map for level: {}",Integer.valueOf(index));
        Integer rewardLevelId=(Integer)rewardMapObj;
        RewardsMap rewardsMap=_rewardLevelLoader.loadMap(rewardLevelId.intValue());
        _defaultRewardMaps.put(Integer.valueOf(index),rewardsMap);
        index++;
      }
    }
  }
}
