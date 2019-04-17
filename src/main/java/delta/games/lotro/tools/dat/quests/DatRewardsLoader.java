package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.rewards.EmoteReward;
import delta.games.lotro.common.rewards.ItemReward;
import delta.games.lotro.common.rewards.RelicReward;
import delta.games.lotro.common.rewards.ReputationReward;
import delta.games.lotro.common.rewards.RewardElement;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.common.rewards.SelectableRewardElement;
import delta.games.lotro.common.rewards.TitleReward;
import delta.games.lotro.common.rewards.TraitReward;
import delta.games.lotro.common.rewards.VirtueReward;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;
import delta.games.lotro.tools.dat.characters.TraitLoader;
import delta.games.lotro.tools.dat.quests.rewards.RewardsMap;
import delta.games.lotro.tools.dat.quests.rewards.RewardsMapLoader;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quest/deed rewards from DAT files.
 * @author DAM
 */
public class DatRewardsLoader
{
  private static final Logger LOGGER=Logger.getLogger(DatRewardsLoader.class);

  private static final int DEBUG_ID=1879000000;

  private static final String VIRTUE_SEED="Trait_Virtue_Rank_";

  private DataFacade _facade;
  private ItemsManager _itemsMgr;
  private EnumMapper _billingGroup;
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
    _billingGroup=_facade.getEnumsManager().getEnumMapper(587202756);
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
   */
  public void fillRewards(PropertiesSet properties, Rewards rewards)
  {
    //System.out.println(properties.getPropertyNames());
    // Challenge level
    Integer challengeLevel=findChallengeLevel(properties);

    // Find out reward level
    RewardsMap rewardLevel=findRewardsMap(challengeLevel, properties);

    // Faction
    getFaction(rewards,properties,rewardLevel);
    // Destiny points: Quest_SessionPointsTier
    /* Disabled - found weird values.
    Integer destinyPointsTier=((Integer)properties.getProperty("Quest_SessionPointsTier"));
    if (destinyPointsTier!=null)
    {
      Integer destinyPoints=rewardLevel.getDestinyPointsMap().getValue(destinyPointsTier.intValue());
      System.out.println("Destiny points tier: "+destinyPointsTier+" => "+destinyPoints);
    }
    */
    // Class points
    Integer classPoints=((Integer)properties.getProperty("Quest_ClassTraitPointReward"));
    if (classPoints!=null)
    {
      rewards.setClassPoints(classPoints.intValue());
      //System.out.println("Class points: "+classPoints);
    }
    // Item XP
    // Loot table (titles, emotes, items...)
    Integer treasureId=((Integer)properties.getProperty("Quest_QuestTreasureDID"));
    if (treasureId!=null)
    {
      getRewards(rewards,treasureId.intValue());
    }
    handleQuestRewards(rewards,properties,rewardLevel);
  }

  private void getRewards(Rewards rewards, int questTreasureId)
  {
    PropertiesSet props=_facade.loadProperties(questTreasureId+0x9000000);

    if (questTreasureId==DEBUG_ID)
    {
      FileIO.writeFile(new File(questTreasureId+".props"),props.dump().getBytes());
      System.out.println(props.dump());
    }

    // Traits
    handleTraits(rewards,props);
    // Titles
    handleTitles(rewards,props);
    // Virtues
    handleVirtues(rewards,props);
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

    // Class points: not for quests
    // Lotro points: not for quests
    // Destiny points
    // Skills? = traits?

    // Billing token
    Object[] billingTokenArray=(Object[])props.getProperty("QuestTreasure_FixedBillingTokenArray");
    if (billingTokenArray!=null)
    {
      for(Object billingTokenObj : billingTokenArray)
      {
        int billingTokenId=((Integer)billingTokenObj).intValue();
        String key=_billingGroup.getString(billingTokenId);
        System.out.println("Billing token: "+billingTokenId+": "+key);
      }
    }
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
        int virtueId=((Integer)virtueProps.getProperty("QuestTreasure_Virtue")).intValue();
        Integer increment=(Integer)virtueProps.getProperty("QuestTreasure_Virtue_Increment");
        //System.out.println("Virtue: "+virtueId+", quantity: "+increment);
        String virtueName=getVirtue(virtueId);
        int count=(increment!=null)?increment.intValue():1;
        try
        {
          VirtueReward virtue=new VirtueReward(VirtueId.valueOf(virtueName),count);
          rewards.addRewardElement(virtue);
        }
        catch(Exception e)
        {
          LOGGER.warn("Unmanaged virtue: "+virtueName);
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
    //System.out.println("Title: "+titleId);
    TitlesManager titlesMgr=TitlesManager.getInstance();
    TitleDescription title=titlesMgr.getTitle(titleId);
    if (title!=null)
    {
      Proxy<TitleDescription> proxy=new Proxy<TitleDescription>();
      proxy.setId(title.getIdentifier());
      proxy.setName(title.getName());
      proxy.setObject(title);
      TitleReward titleReward=new TitleReward(proxy);
      rewards.add(titleReward);
    }
    else
    {
      LOGGER.warn("Title not found: "+titleId);
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
    //System.out.println("Emote: "+emoteId);
    EmotesManager emotesMgr=EmotesManager.getInstance();
    EmoteDescription emote=emotesMgr.getEmote(emoteId);
    if (emote!=null)
    {
      Proxy<EmoteDescription> proxy=new Proxy<EmoteDescription>();
      proxy.setId(emote.getIdentifier());
      proxy.setName(emote.getCommand());
      proxy.setObject(emote);
      EmoteReward emoteReward=new EmoteReward(proxy);
      rewards.add(emoteReward);
    }
    else
    {
      LOGGER.warn("Emote not found: "+emoteId);
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
    //System.out.println("Trait: "+traitId);
    TraitsManager traitsMgr=TraitsManager.getInstance();
    TraitDescription trait=traitsMgr.getTrait(traitId);
    if (trait==null)
    {
      trait=TraitLoader.loadTrait(_facade,traitId);
    }
    if (trait!=null)
    {
      Proxy<TraitDescription> proxy=new Proxy<TraitDescription>();
      proxy.setId(trait.getIdentifier());
      proxy.setName(trait.getName());
      proxy.setObject(trait);
      TraitReward traitReward=new TraitReward(proxy);
      rewards.add(traitReward);
    }
    else
    {
      LOGGER.warn("Trait not found: "+traitId);
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
        //System.out.println("Item: "+itemId+", quantity: "+quantityValue);
        Item item=_itemsMgr.getItem(itemId);
        if (item!=null)
        {
          String name=(item!=null)?item.getName():"???";
          Proxy<Item> itemProxy=new Proxy<Item>();
          itemProxy.setObject(item);
          itemProxy.setName(name);
          itemProxy.setId(itemId);
          int quantity=(quantityValue!=null?quantityValue.intValue():1);
          ItemReward itemReward=new ItemReward(itemProxy,quantity);
          rewards.add(itemReward);
        }
        else
        {
          LOGGER.warn("Item not found: "+itemId);
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
    //System.out.println("Relic: "+relicId);
    RelicsManager relicsMgr=RelicsManager.getInstance();
    Relic item=relicsMgr.getById(relicId);
    if (item!=null)
    {
      String name=(item!=null)?item.getName():"???";
      Proxy<Relic> itemProxy=new Proxy<Relic>();
      itemProxy.setObject(item);
      itemProxy.setName(name);
      itemProxy.setId(relicId);
      int quantity=(quantityValue!=null?quantityValue.intValue():1);
      RelicReward itemReward=new RelicReward(itemProxy,quantity);
      rewards.add(itemReward);
    }
    else
    {
      LOGGER.warn("Relic not found: "+relicId);
    }
  }

  private String getVirtue(int virtueId)
  {
    PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(virtueId);
    String propName=propDef.getName();
    if (propName.startsWith(VIRTUE_SEED))
    {
      String virtueName=propName.substring(VIRTUE_SEED.length()).toUpperCase();
      if ("COMPASSIONATE".equals(virtueName)) virtueName="COMPASSION";
      if ("TOLERANT".equals(virtueName)) virtueName="TOLERANCE";
      if ("JUST".equals(virtueName)) virtueName="JUSTICE";
      if ("VALOR".equals(virtueName)) virtueName="VALOUR";
      if ("MERCIFUL".equals(virtueName)) virtueName="MERCY";
      return virtueName;
    }
    return propName;
  }

  private void getFaction(Rewards rewards, PropertiesSet props, RewardsMap rewardsMap)
  {
    // Positive
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_PositiveFaction");
      handleFactionProps(rewards,rewardsMap,factionProps,1);
    }
    // Negative
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_NegativeFaction");
      handleFactionProps(rewards,rewardsMap,factionProps,-1);
    }
  }

  private void handleFactionProps(Rewards rewards, RewardsMap rewardsMap, PropertiesSet factionProps, int factor)
  {
    if (factionProps!=null)
    {
      Integer factionId=(Integer)factionProps.getProperty("Quest_FactionDID");
      if (factionId!=null)
      {
        int reputationValue=0;
        Integer repTier=(Integer)factionProps.getProperty("Quest_RepTier");
        if (repTier!=null)
        {
          Integer repValue=rewardsMap.getReputationMap().getValue(repTier.intValue());
          reputationValue=(repValue!=null)?repValue.intValue():0;
        }
        //System.out.println("Reputation: faction="+factionId+", tier="+repTier+", value="+reputationValue);
        if (reputationValue!=0)
        {
          Faction faction=_factions.getById(factionId.intValue());
          if (faction!=null)
          {
            ReputationReward repItem=new ReputationReward(faction);
            repItem.setAmount(reputationValue*factor);
            rewards.addRewardElement(repItem);
          }
          else
          {
            LOGGER.warn("Faction not found: "+factionId);
          }
        }
      }
    }
  }

  private RewardsMap findRewardsMap(Integer challengeLevel, PropertiesSet properties)
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
      int level;
      if (challengeLevel!=null)
      {
        level=challengeLevel.intValue();
      }
      else
      {
        LOGGER.warn("No challenge level and no reward map!");
        level=1;
      }
      rewardsMap=_defaultRewardMaps.get(Integer.valueOf(level));
    }
    return rewardsMap;
  }

  private void handleQuestRewards(Rewards rewards, PropertiesSet properties, RewardsMap rewardsMap)
  {
    // Gold
    Integer goldTier=((Integer)properties.getProperty("Quest_GoldTier"));
    if (goldTier!=null)
    {
      Integer gold=rewardsMap.getMoneyMap().getValue(goldTier.intValue());
      System.out.println("Gold tier: "+goldTier+" => "+gold);
    }
    // XP
    Integer xpTier=((Integer)properties.getProperty("Quest_ExpTier"));
    if (xpTier!=null)
    {
      Integer xp=rewardsMap.getXpMap().getValue(xpTier.intValue());
      System.out.println("XP tier: "+xpTier+" => "+xp);
    }
    // Item XP
    Integer itemXpTier=((Integer)properties.getProperty("Quest_ItemExpTier"));
    if (itemXpTier!=null)
    {
      Integer itemXp=rewardsMap.getItemXpMap().getValue(itemXpTier.intValue());
      System.out.println("Item XP tier: "+itemXpTier+" => "+itemXp);
    }
    // Mount XP
    Integer mountXpTier=((Integer)properties.getProperty("Quest_MountExpTier"));
    if (mountXpTier!=null)
    {
      Integer mountXp=rewardsMap.getMountXpMap().getValue(mountXpTier.intValue());
      System.out.println("Mount XP tier: "+mountXpTier+" => "+mountXp);
    }
    // Craft XP
    Integer craftXpTier=((Integer)properties.getProperty("Quest_CraftExpTier"));
    if (craftXpTier!=null)
    {
      Integer craftXp=rewardsMap.getCraftXpMap().getValue(craftXpTier.intValue());
      System.out.println("Craft XP tier: "+craftXpTier+" => "+craftXp); // Quest_CraftExpTier:6, Usage_RequiredCraftProfession:1879061252,Quest_CraftProfessionDID:1879061252
    }
    // Glory (Renown or Infamy if MONSTER_PLAY)
    Integer gloryTier=((Integer)properties.getProperty("Quest_GloryTier"));
    if (gloryTier!=null)
    {
      Integer glory=rewardsMap.getGloryMap().getValue(gloryTier.intValue());
      System.out.println("Glory tier: "+gloryTier+" => "+glory);
    }
    // Mithril coins
    // Do not use for deeds... seems to be the same value as LP
    Integer mcTier=((Integer)properties.getProperty("Quest_MithrilCoinsTier"));
    if (mcTier!=null)
    {
      Integer mithrilCoins=rewardsMap.getMithrilCoinsMap().getValue(mcTier.intValue());
      System.out.println("Mithril coin tier: "+mcTier+" => "+mithrilCoins);
    }
    // Turbine/Lotro points
    Integer tpTier=((Integer)properties.getProperty("Quest_TurbinePointTier"));
    if (tpTier!=null)
    {
      Integer turbinePoints=rewardsMap.getTpMap().getValue(tpTier.intValue());
      System.out.println("Turbine points tier: "+tpTier+" => "+turbinePoints);
      if (turbinePoints!=null)
      {
        rewards.setLotroPoints(turbinePoints.intValue());
      }
    }

    // Only for 'Level Up' quests:
    /*
    Integer goldToGive=((Integer)properties.getProperty("Quest_GoldToGive"));
    if (goldToGive!=null) System.out.println("Gold to give: "+goldToGive);
    Integer xpToGive=((Integer)properties.getProperty("Quest_ExpToGive"));
    if (xpToGive!=null) System.out.println("XP to give: "+xpToGive);
    Integer repToGive=((Integer)properties.getProperty("Quest_PosRepToGive"));
    if (repToGive!=null) System.out.println("Rep to give: "+repToGive);
    Integer gloryToGive=((Integer)properties.getProperty("Quest_GloryToGive"));
    if (gloryToGive!=null) System.out.println("Glory to give: "+gloryToGive);
    */
  }

  private Integer findChallengeLevel(PropertiesSet properties)
  {
    Integer challengeLevel=(Integer)properties.getProperty("Quest_ChallengeLevel");
    Integer challengeLevelOverrideProperty=(Integer)properties.getProperty("Quest_ChallengeLevelOverrideProperty");
    Integer ignoreDefaultChallengeLevel=(Integer)properties.getProperty("Quest_IgnoreDefaultChallengeLevel");
    boolean ignoreChallengeLevel=((ignoreDefaultChallengeLevel!=null) && (ignoreDefaultChallengeLevel.intValue()!=0));
    if ((ignoreChallengeLevel) || (challengeLevel==null))
    {
      if (challengeLevelOverrideProperty!=null)
      {
        if (challengeLevelOverrideProperty.intValue()==268439569)
        {
          System.out.println("Challenge level is character level");
          challengeLevel=Integer.valueOf(120); // TODO tmp
        }
        else if (challengeLevelOverrideProperty.intValue()==268446666)
        {
          System.out.println("Challenge level is skirmish level");
          challengeLevel=Integer.valueOf(120); // TODO tmp
        }
        else
        {
          LOGGER.warn("Unmanaged challenge level property: "+challengeLevelOverrideProperty);
        }
      }
      else
      {
        //LOGGER.warn("No challenge level property!");
      }
    }
    else
    {
      //System.out.println("Challenge level is: "+challengeLevel);
    }
    /*
    Quest_ChallengeLevel: 100
    Quest_ChallengeLevelOverrideProperty: 268439569
    Quest_IgnoreDefaultChallengeLevel: 1
    */
    return challengeLevel;
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
    PropertiesSet questControlProps=_facade.loadProperties(1879048802+0x9000000);
    Object[] rewardMaps=(Object[])questControlProps.getProperty("QuestControl_RewardLevelDIDArray");
    int index=1;
    for(Object rewardMapObj : rewardMaps)
    {
      LOGGER.info("Loading default rewards map for level: "+index);
      Integer rewardLevelId=(Integer)rewardMapObj;
      RewardsMap rewardsMap=_rewardLevelLoader.loadMap(rewardLevelId.intValue());
      _defaultRewardMaps.put(Integer.valueOf(index),rewardsMap);
      index++;
    }
  }
}
