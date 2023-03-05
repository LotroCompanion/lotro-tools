package delta.games.lotro.tools.dat.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.ChallengeLevel;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.LockType;
import delta.games.lotro.common.Repeatability;
import delta.games.lotro.common.Size;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.QuestCategory;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.lore.webStore.WebStoreItem;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quests.
 * @author DAM
 */
public class QuestsLoader
{
  private static final Logger LOGGER=Logger.getLogger(QuestsLoader.class);

  private DataFacade _facade;
  private Map<Integer,QuestDescription> _quests;
  private LotroEnum<QuestCategory> _questCategory;
  private I18nUtils _i18n;
  private DatRolesLoader _rolesLoader;
  private DatObjectivesLoader _objectivesLoader;
  private AchievablesLoadingUtils _utils;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param utils Utilities.
   */
  public QuestsLoader(DataFacade facade, AchievablesLoadingUtils utils)
  {
    _facade=facade;
    _utils=utils;
    _quests=new HashMap<Integer,QuestDescription>();
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    _questCategory=registry.get(QuestCategory.class);
    _i18n=new I18nUtils("quests",facade.getGlobalStringsManager());
    _rolesLoader=new DatRolesLoader(facade,_i18n);
    _objectivesLoader=new DatObjectivesLoader(facade,_i18n);
  }

  /**
   * Get loaded quests.
   * @return A list of quests.
   */
  public List<QuestDescription> getQuests()
  {
    return new ArrayList<QuestDescription>(_quests.values());
  }

  /**
   * Load a quest.
   * @param questID Quest identifier.
   * @param properties Properties.
   */
  public void loadQuest(int questID, PropertiesSet properties)
  {
    // Check
    boolean useIt=useQuest(questID,properties);
    if (!useIt)
    {
      //System.out.println("Ignored ID="+indexDataId+", name="+name);
      return;
    }
    QuestDescription quest=new QuestDescription();
    // ID
    quest.setIdentifier(questID);
    // Name
    String nameFormat=DatUtils.getStringProperty(properties,"Quest_Name");
    String renderedName=_utils.renderName(nameFormat);
    quest.setName(renderedName);

    //DatObjectivesLoader.currentName=name;
    //System.out.println("Quest name: "+name);
    // Description
    String description=_i18n.getStringProperty(properties,"Quest_Description");
    quest.setDescription(description);
    // Category
    Integer categoryId=((Integer)properties.getProperty("Quest_Category"));
    if (categoryId!=null)
    {
      QuestCategory category=_questCategory.getEntry(categoryId.intValue());
      quest.setCategory(category);
    }
    // Max times
    Integer maxTimes=((Integer)properties.getProperty("Quest_MaxTimesCompletable"));
    Repeatability repeatability=Repeatability.NOT_REPEATABLE;
    if (maxTimes!=null)
    {
      if (maxTimes.intValue()<0)
      {
        maxTimes=Integer.valueOf(-1);
      }
      repeatability=Repeatability.getByCode(maxTimes.byteValue());
    }
    quest.setRepeatability(repeatability);
    // Cooldowns/locks
    LockType lockType=null;
    Integer lockTypeCode=((Integer)properties.getProperty("Quest_LockType"));
    if (lockTypeCode!=null)
    {
      if (lockTypeCode.intValue()==1) lockType=LockType.BIWEEKLY;
      else if (lockTypeCode.intValue()==2) lockType=LockType.DAILY;
      else if (lockTypeCode.intValue()==3) lockType=LockType.WEEKLY;
      else
      {
        LOGGER.warn("Unmanaged lock type: "+lockTypeCode);
      }
    }
    quest.setLockType(lockType);
    // Scope
    //handleScope(quest, properties);
    // Monster play?
    Integer isMonsterPlayCode=((Integer)properties.getProperty("Quest_IsMonsterPlayQuest"));
    boolean isMonsterPlay=((isMonsterPlayCode!=null) && (isMonsterPlayCode.intValue()!=0));
    quest.setMonsterPlay(isMonsterPlay);
    // Quests to complete (only for 'level up' quests)
    /*
    Object questsToComplete=properties.getProperty("Quest_QuestsToComplete");
    if (questsToComplete!=null)
    {
      System.out.println(questsToComplete);
    }
    */
    // Items given when the quest is bestowed
    /*
    Object givenItems=properties.getProperty("Quest_GiveItemArray");
    if (givenItems!=null)
    {
      System.out.println(givenItems);
    }
    */
    // Chain
    // - previous
    _utils.findPrerequisites(quest,properties);
    // - next
    Integer nextQuestId=((Integer)properties.getProperty("Quest_NextQuest"));
    if (nextQuestId!=null)
    {
      Proxy<Achievable> proxy=new Proxy<Achievable>();
      proxy.setId(nextQuestId.intValue());
      quest.setNextQuest(proxy);
      //System.out.println("Next quest: "+nextQuestId);
    }
    // Flags
    handleFlags(quest,properties);
    // Requirements
    _utils.findRequirements(quest,properties);

    // Rewards
    Rewards rewards=quest.getRewards();
    ChallengeLevel challengeLevel=_utils.getRewardsLoader().fillRewards(properties,rewards);
    // Challenge level
    quest.setChallengeLevel(challengeLevel);

    // Quest Loot Table
    // (additional loot tables that are active when the quest is active)
    /*
    Object lootTable=properties.getProperty("Quest_LootTable");
    if (lootTable!=null)
    {
      // [{Quest_LootMonsterGenus_Array=[Ljava.lang.Object;@3d1cfad4, Quest_LootItemDID=1879051728, Quest_LootItemProbability=100}]
      System.out.println("Loot table:" +lootTable);
    }
    */

    // Objectives
    _objectivesLoader.handleObjectives(quest.getObjectives(),quest,properties);

    // Dialogs
    _rolesLoader.loadRoles(quest,properties);

    // Web Store (needed xpack/region): WebStoreAccountItem_DataID
    Integer webStoreItemID=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreItemID!=null)
    {
      WebStoreItem webStoreItem=_utils.getWebStoreItemsLoader().getWebStoreItem(webStoreItemID.intValue());
      quest.setWebStoreItem(webStoreItem);
    }
    // Registration
    _quests.put(Integer.valueOf(quest.getIdentifier()),quest);
  }

  /*
  private void handleScope(QuestDescription quest, PropertiesSet properties)
  {
    Integer scope=((Integer)properties.getProperty("Quest_Scope"));
    if (scope!=null)
    {
      int value=scope.intValue();
      if (value==1)
      {
        //System.out.println("Scope 1: "+quest.getTitle()); // Task (x417) matches category="Task"
        quest.setRepeatable(true);
      }
      else if (value==2)
      {
        //System.out.println("Scope 2: "+quest.getTitle()); // Crafting (x42)
        quest.setSize(Size.FELLOWSHIP);
      }
      else if (value==3)
      {
        //System.out.println("Scope 3: "+quest.getTitle()); // Epic (x ~1k)
        quest.setSize(Size.SMALL_FELLOWSHIP);
      }
      else if (value==5)
      {
        //System.out.println("Scope 5: "+quest.getTitle()); // Legendary Items (x12) (eg Moria or Eregion instance that require shards ; Defence of Glatrev)?
        // Normal...
        quest.setSize(Size.SOLO);
      }
      else if (value==9)
      {
        //System.out.println("Scope 9: "+quest.getTitle()); // Allegiance quests (x44)
        System.out.println("Instance");
      }
      else
      {
        LOGGER.warn("Unmanaged scope: "+value);
      }
    }
  }
  */

  private void handleFlags(QuestDescription quest, PropertiesSet properties)
  {
    // Size
    // - small fellowship
    Integer smallFellowshipRecommended=((Integer)properties.getProperty("Quest_IsSmallFellowshipRecommended"));
    if ((smallFellowshipRecommended!=null) && (smallFellowshipRecommended.intValue()==1))
    {
      quest.setSize(Size.SMALL_FELLOWSHIP);
      //System.out.println("IS Small Fellowship recommended: "+smallFellowshipRecommended);
    }
    // - fellowship
    Integer fellowshipRecommended=((Integer)properties.getProperty("Quest_IsFellowshipRecommended"));
    if ((fellowshipRecommended!=null) && (fellowshipRecommended.intValue()==1))
    {
      quest.setSize(Size.FELLOWSHIP);
      //System.out.println("IS Fellowship recommended: "+fellowshipRecommended);
    }
    // - raid
    Integer isRaidQuest=((Integer)properties.getProperty("Quest_ShowRaidInJournal"));
    if ((isRaidQuest!=null) && (isRaidQuest.intValue()==1))
    {
      quest.setSize(Size.RAID);
      //System.out.println("IS Raid quest: "+isRaidQuest);
    }
    // Instance quest? Default is no.
    Integer isInstanceQuest=((Integer)properties.getProperty("Quest_IsInstanceQuest"));
    if ((isInstanceQuest!=null) && (isInstanceQuest.intValue()==1))
    {
      quest.setInstanced(true);
      //System.out.println("IS Instance quest: "+isInstanceQuest);
    }
    // Shareable? Default is yes.
    Integer isShareable=((Integer)properties.getProperty("Quest_IsShareable"));
    if ((isShareable!=null) && (isShareable.intValue()==0))
    {
      quest.setShareable(false);
      //System.out.println("IS shareable quest: "+isShareable);
    }
    // Session play? Default is no.
    Integer isSessionQuest=((Integer)properties.getProperty("Quest_IsSessionQuest"));
    if ((isSessionQuest!=null) && (isSessionQuest.intValue()==1))
    {
      quest.setSessionPlay(true);
      //System.out.println("IS session quest: "+isSessionQuest);
    }
    // Automatic bestowed quest (landscape, in-instance quest). Default is no.
    Integer isPeBestowedQuest=((Integer)properties.getProperty("Quest_IsPEBestowedQuest"));
    if ((isPeBestowedQuest!=null) && (isPeBestowedQuest.intValue()==1))
    {
      quest.setAutoBestowed(true);
      //System.out.println("IS PE bestowed quest: "+isPeBestowedQuest);
    }
    // Unused:
    // Quest_IsSessionAccomplishment
    // Quest_IsHidden
  }

  private boolean useQuest(int id, PropertiesSet properties)
  {
    Integer categoryId=((Integer)properties.getProperty("Quest_Category"));
    // Ignore 'Test' quests
    if ((categoryId!=null) && (categoryId.intValue()==16))
    {
      return false;
    }
    // Ignore 'Level Up' quests (Bullroarer specifics?)
    if ((categoryId!=null) && (categoryId.intValue()==15))
    {
      return false;
    }
    // Name
    String name=DatUtils.getStringProperty(properties,"Quest_Name");
    if (name.startsWith("DNT"))
    {
      return false;
    }
    if (name.contains("TBD"))
    {
      return false;
    }
    if (name.startsWith("HIDING CONTENT"))
    {
      return false;
    }
    if (id==QuestRequirementsLoader.HIDING_CONTENT_QUEST_ID)
    {
      return false;
    }
    return true;
  }

  /**
   * Load quest arcs.
   */
  public void loadQuestArcs()
  {
    PropertiesSet questArcsDirectory=_facade.loadProperties(0x7900E36F);
    //System.out.println(questArcsDirectory.dump());
    if (questArcsDirectory==null)
    {
      return;
    }
    Object[] list=(Object[])questArcsDirectory.getProperty("QuestArcDirectory_QuestArc_Array");
    for(Object obj : list)
    {
      Integer arcId=(Integer)obj;
      handleArc(arcId.intValue());
    }
  }

  private void handleArc(int arcId)
  {
    PropertiesSet arcProps=_facade.loadProperties(arcId+DATConstants.DBPROPERTIES_OFFSET);
    String arcName=_i18n.getStringProperty(arcProps,"QuestArc_Name");
    Object[] list=(Object[])arcProps.getProperty("QuestArc_Quest_Array");
    for(Object obj : list)
    {
      Integer questId=(Integer)obj;
      QuestDescription quest=_quests.get(questId);
      if (quest!=null)
      {
        quest.setQuestArc(arcName);
      }
      else
      {
        LOGGER.warn("Quest "+questId+" not found for arc: "+arcName);
      }
    }
  }

  /**
   * Resolve proxies.
   * @param resolver Proxies resolver.
   */
  public void resolveProxies(AchievableProxiesResolver resolver)
  {
    for(QuestDescription quest : _quests.values())
    {
      resolver.resolveQuest(quest);
      _utils.cleanup(quest);
    }
  }

  /**
   * Save.
   */
  public void save()
  {
    saveQuests();
    _i18n.save();
  }

  private void saveQuests()
  {
    List<QuestDescription> quests=new ArrayList<QuestDescription>();
    quests.addAll(_quests.values());
    Collections.sort(quests,new IdentifiableComparator<QuestDescription>());
    System.out.println("Nb quests: "+quests.size());
    // Write quests file
    boolean ok=QuestXMLWriter.writeQuestsFile(GeneratedFiles.QUESTS,quests);
    if (ok)
    {
      System.out.println("Wrote quests file: "+GeneratedFiles.QUESTS);
    }
  }
}
