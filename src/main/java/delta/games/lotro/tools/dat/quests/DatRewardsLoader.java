package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.Emote;
import delta.games.lotro.common.ReputationItem;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Title;
import delta.games.lotro.common.Trait;
import delta.games.lotro.common.Virtue;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
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
    Integer treasureId=((Integer)properties.getProperty("Quest_QuestTreasureDID"));
    if (treasureId!=null)
    {
      getRewards(rewards,treasureId.intValue());
    }
    // Faction
    getFaction(rewards,properties);
    // LOTRO points
    Integer tp=getTurbinePoints(properties);
    if (tp!=null)
    {
      rewards.setLotroPoints(tp.intValue());
    }
  }

  private void getRewards(Rewards rewards, int questTreasureId)
  {
    PropertiesSet props=_facade.loadProperties(questTreasureId+0x9000000);

    if (questTreasureId==DEBUG_ID)
    {
      FileIO.writeFile(new File(questTreasureId+".props"),props.dump().getBytes());
      System.out.println(props.dump());
    }

    // Items
    loadItems(props,"QuestTreasure_FixedItemArray", rewards.getObjects());
    loadItems(props,"QuestTreasure_SelectableItemArray", rewards.getSelectObjects());
    // Class points: not for quests
    // Lotro points: not for quests
    // Destiny points
    // Skills? = traits?
    // Virtues
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
          Virtue virtue=new Virtue(VirtueId.valueOf(virtueName),count);
          rewards.addVirtue(virtue);
        }
        catch(Exception e)
        {
          LOGGER.warn("Unmanaged virtue: "+virtueName);
        }
      }
    }
    // Titles
    Object[] titleArray=(Object[])props.getProperty("QuestTreasure_FixedTitleArray");
    if (titleArray!=null)
    {
      for(Object titleObj : titleArray)
      {
        int titleId=((Integer)titleObj).intValue();
        //System.out.println("Title: "+titleId);
        TitlesManager titlesMgr=TitlesManager.getInstance();
        TitleDescription title=titlesMgr.getTitle(titleId);
        if (title!=null)
        {
          String name=title.getName();
          Title titleReward=new Title(null,name);
          rewards.addTitle(titleReward);
        }
        else
        {
          LOGGER.warn("Title not found: "+titleId);
        }
      }
    }
    // Emote
    Object[] emoteArray=(Object[])props.getProperty("QuestTreasure_FixedEmoteArray");
    if (emoteArray!=null)
    {
      for(Object emoteObj : emoteArray)
      {
        int emoteId=((Integer)emoteObj).intValue();
        //System.out.println("Emote: "+emoteId);
        EmotesManager emotesMgr=EmotesManager.getInstance();
        EmoteDescription emote=emotesMgr.getEmote(emoteId);
        if (emote!=null)
        {
          String command=emote.getCommand();
          Emote emoteReward=new Emote(command);
          rewards.addEmote(emoteReward);
        }
        else
        {
          LOGGER.warn("Emote not found: "+emoteId);
        }
      }
    }
    // Trait
    Object[] traitArray=(Object[])props.getProperty("QuestTreasure_FixedTraitArray");
    if (traitArray!=null)
    {
      for(Object traitObj : traitArray)
      {
        int traitId=((Integer)traitObj).intValue();
        //System.out.println("Trait: "+traitId);
        TraitsManager traitsMgr=TraitsManager.getInstance();
        TraitDescription trait=traitsMgr.getTrait(traitId);
        if (trait==null)
        {
          trait=TraitLoader.loadTrait(_facade,traitId);
        }
        if (trait!=null)
        {
          String traitName=trait.getName();
          Trait traitReward=new Trait(traitName);
          rewards.addTrait(traitReward);
        }
        else
        {
          LOGGER.warn("Trait not found: "+traitId);
        }
      }
    }
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

  private void loadItems(PropertiesSet props, String propertyName, ObjectsSet storage)
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
          storage.addObject(itemProxy,quantity);
        }
        else
        {
          LOGGER.warn("Item not found: "+itemId);
        }
      }
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

  private void getFaction(Rewards rewards, PropertiesSet props)
  {
    // Positive
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_PositiveFaction");
      if (factionProps!=null)
      {
        Integer factionId=(Integer)factionProps.getProperty("Quest_FactionDID");
        if (factionId!=null)
        {
          Integer repTier=(Integer)factionProps.getProperty("Quest_RepTier");
          int reputationValue=(repTier!=null)?getReputation(repTier.intValue()):0;
          System.out.println("Reputation: faction="+factionId+", tier="+repTier+", value="+reputationValue);
          updateFaction(rewards,factionId.intValue(),reputationValue);
        }
      }
    }
    // Negative
    {
      PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_NegativeFaction");
      if (factionProps!=null)
      {
        Integer factionId=(Integer)factionProps.getProperty("Quest_FactionDID");
        if (factionId!=null)
        {
          Integer repTier=(Integer)factionProps.getProperty("Quest_RepTier");
          int reputationValue=(repTier!=null)?getReputation(repTier.intValue()):0;
          System.out.println("Negative reputation: faction="+factionId+", tier="+repTier+", value="+reputationValue);
          updateFaction(rewards,factionId.intValue(),-reputationValue);
        }
      }
    }
  }

  private void updateFaction(Rewards rewards, int factionId, int reputationValue)
  {
    Faction faction=_factions.getById(factionId);
    if (faction!=null)
    {
      ReputationItem repItem=new ReputationItem(faction);
      repItem.setAmount(reputationValue);
      rewards.getReputation().add(repItem);
    }
    else
    {
      LOGGER.warn("Faction not found: "+factionId);
    }
  }

  private Integer getTurbinePoints(PropertiesSet properties)
  {
    Integer tpTier=((Integer)properties.getProperty("Quest_TurbinePointTier"));
    if (tpTier!=null)
    {
      int tierCode=tpTier.intValue();
      if (tierCode==2) return Integer.valueOf(5);
      if (tierCode==3) return Integer.valueOf(10);
      if (tierCode==4) return Integer.valueOf(15);
      if (tierCode==5) return Integer.valueOf(20);
      if (tierCode==6) return Integer.valueOf(50);
      LOGGER.warn("Unmanaged TP tier: "+tierCode);
    }
    return null;
  }

  private int getReputation(int tier)
  {
    if (tier==2) return 300;
    if (tier==3) return 500;
    if (tier==4) return 700;
    if (tier==5) return 900;
    if (tier==6) return 1200;
    LOGGER.warn("Unmanaged reputation tier: "+tier);
    return 0;
  }

  /**
   * Handle quest specific rewards.
   * @param rewards Storage.
   * @param challengeLevel Level to use.
   * @param properties Properties to use.
   */
  public void handleQuestRewards(Rewards rewards, Integer challengeLevel, PropertiesSet properties)
  {
    // Find out reward level
    RewardsMap rewardLevel=null;
    Integer rewardLevelId=((Integer)properties.getProperty("Quest_RewardLevelDID"));
    if (rewardLevelId!=null)
    {
      // Specific one
      rewardLevel=getRewardsMap(rewardLevelId.intValue());
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
      rewardLevel=_defaultRewardMaps.get(Integer.valueOf(level));
    }
    // Gold
    Integer goldTier=((Integer)properties.getProperty("Quest_GoldTier"));
    if (goldTier!=null)
    {
      Integer gold=rewardLevel.getMoneyMap().getValue(goldTier.intValue());
      System.out.println("Gold tier: "+goldTier+" => "+gold);
    }
    // XP
    Integer xpTier=((Integer)properties.getProperty("Quest_ExpTier"));
    if (xpTier!=null)
    {
      Integer xp=rewardLevel.getXpMap().getValue(xpTier.intValue());
      System.out.println("XP tier: "+xpTier+" => "+xp);
    }
    // Item XP
    Integer itemXpTier=((Integer)properties.getProperty("Quest_ItemExpTier"));
    if (itemXpTier!=null)
    {
      Integer itemXp=rewardLevel.getItemXpMap().getValue(itemXpTier.intValue());
      System.out.println("Item XP tier: "+itemXpTier+" => "+itemXp);
    }
    // Mount XP
    Integer mountXpTier=((Integer)properties.getProperty("Quest_MountExpTier"));
    if (mountXpTier!=null)
    {
      Integer mountXp=rewardLevel.getMountXpMap().getValue(mountXpTier.intValue());
      System.out.println("Mount XP tier: "+mountXpTier+" => "+mountXp);
    }
    // Craft XP
    Integer craftXpTier=((Integer)properties.getProperty("Quest_CraftExpTier"));
    if (craftXpTier!=null)
    {
      Integer craftXp=rewardLevel.getCraftXpMap().getValue(craftXpTier.intValue());
      System.out.println("Craft XP tier: "+craftXpTier+" => "+craftXp);
    }
    // Glory
    Integer gloryTier=((Integer)properties.getProperty("Quest_GloryTier"));
    if (gloryTier!=null)
    {
      Integer glory=rewardLevel.getGloryMap().getValue(gloryTier.intValue());
      System.out.println("Glory tier: "+gloryTier+" => "+glory);
    }
    // Mithril coins
    Integer mcTier=((Integer)properties.getProperty("Quest_MithrilCoinsTier"));
    if (mcTier!=null)
    {
      Integer mithrilCoins=rewardLevel.getMithrilCoinsMap().getValue(mcTier.intValue());
      System.out.println("Mithril coin tier: "+mcTier+" => "+mithrilCoins);
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
      Integer rewardLevelId=(Integer)rewardMapObj;
      RewardsMap rewardsMap=_rewardLevelLoader.loadMap(rewardLevelId.intValue());
      _defaultRewardMaps.put(Integer.valueOf(index),rewardsMap);
      index++;
    }
  }
}