package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.collections.CompoundComparator;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.ChallengeLevel;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.LockType;
import delta.games.lotro.common.Race;
import delta.games.lotro.common.Repeatability;
import delta.games.lotro.common.Size;
import delta.games.lotro.common.requirements.AbstractAchievableRequirement;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.deeds.comparators.DeedDescriptionComparator;
import delta.games.lotro.lore.deeds.comparators.DeedNameComparator;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.lore.webStore.WebStoreItem;
import delta.games.lotro.lore.webStore.io.xml.WebStoreItemsXMLWriter;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.io.xml.WorldEventsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.misc.WebStoreItemsLoader;
import delta.games.lotro.tools.dat.misc.WorldEventsLoader;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.StringRenderingUtils;
import delta.games.lotro.tools.dat.utils.WorldEventConditionsLoader;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.tools.lore.deeds.geo.MainGeoDataInjector;
import delta.games.lotro.tools.lore.deeds.keys.DeedKeysInjector;
import delta.games.lotro.utils.Proxy;

/**
 * Get quests/deeds definitions from DAT files.
 * @author DAM
 */
public class MainDatAchievablesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatAchievablesLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private Map<Integer,QuestDescription> _quests;
  private Map<Integer,DeedDescription> _deeds;
  private EnumMapper _questCategory;
  private EnumMapper _deedUiTabName;
  private DatRewardsLoader _rewardsLoader;
  private DatObjectivesLoader _objectivesLoader;
  private DatRolesLoader _rolesLoader;
  private QuestRequirementsLoader _requirementsLoader;
  private StringRenderer _renderer;
  private AchievablesLogger _logger;
  private UsageRequirementsLoader _usageRequirementsLoader;
  private WorldEventsLoader _worldEventsLoader;
  private WorldEventConditionsLoader _weConditionsLoader;
  private WebStoreItemsLoader _webStoreItemsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param rewardsLoader Rewards loader.
   */
  public MainDatAchievablesLoader(DataFacade facade, DatRewardsLoader rewardsLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("quests",facade.getGlobalStringsManager());
    _quests=new HashMap<Integer,QuestDescription>();
    _deeds=new HashMap<Integer,DeedDescription>();
    _questCategory=_facade.getEnumsManager().getEnumMapper(587202585);
    _deedUiTabName=_facade.getEnumsManager().getEnumMapper(587202588);
    _rewardsLoader=rewardsLoader;
    _objectivesLoader=new DatObjectivesLoader(facade,_i18n);
    _rolesLoader=new DatRolesLoader(facade,_i18n);
    _requirementsLoader=new QuestRequirementsLoader(facade);
    _renderer=StringRenderingUtils.buildAllOptionsRenderer();
    _logger=new AchievablesLogger(true,true,"achievables.txt");
    _worldEventsLoader=new WorldEventsLoader(facade);
    _weConditionsLoader=new WorldEventConditionsLoader(_worldEventsLoader);
    _usageRequirementsLoader=new UsageRequirementsLoader();
    _webStoreItemsLoader=new WebStoreItemsLoader(facade);
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
        loadQuest(indexDataId,properties);
      }
      else
      {
        loadDeed(indexDataId,properties);
      }
      _logger.handleAchievable(indexDataId,isQuest,properties);
    }
    else
    {
      LOGGER.warn("Could not handle achievable ID="+indexDataId);
    }
  }

  private String renderName(String titleFormat)
  {
    String ret=_renderer.render(titleFormat);
    ret=ret.replace("  "," ");
    ret=ret.trim();
    return ret;
  }

  private void loadQuest(int indexDataId, PropertiesSet properties)
  {
    // Check
    boolean useIt=useQuest(indexDataId,properties);
    if (!useIt)
    {
      //System.out.println("Ignored ID="+indexDataId+", name="+name);
      return;
    }
    QuestDescription quest=new QuestDescription();
    // ID
    quest.setIdentifier(indexDataId);
    // Name
    String nameFormat=DatUtils.getStringProperty(properties,"Quest_Name");
    String renderedName=renderName(nameFormat);
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
      String category=_questCategory.getString(categoryId.intValue());
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
    findPrerequisites(quest,properties);
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
    findRequirements(quest,properties);

    // Rewards
    Rewards rewards=quest.getRewards();
    ChallengeLevel challengeLevel=_rewardsLoader.fillRewards(properties,rewards);
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
      WebStoreItem webStoreItem=_webStoreItemsLoader.getWebStoreItem(webStoreItemID.intValue());
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

  private void loadDeed(int indexDataId, PropertiesSet properties)
  {
    DeedDescription deed=new DeedDescription();
    // ID
    deed.setIdentifier(indexDataId);
    // Name
    String nameFormat=DatUtils.getStringProperty(properties,"Quest_Name");
    String renderedName=renderName(nameFormat);
    deed.setName(renderedName);
    //System.out.println("Deed name: "+name);
    //DatObjectivesLoader.currentName=name;
    // Description
    String description=_i18n.getStringProperty(properties,"Quest_Description");
    deed.setDescription(description);
    // UI Tab
    Integer uiTab=((Integer)properties.getProperty("Accomplishment_UITab"));
    String uiTabName=_deedUiTabName.getString(uiTab.intValue());
    deed.setCategory(uiTabName);
    // Deed type
    handleDeedType(deed,properties);
    // Monster play?
    Integer isMonsterPlayCode=((Integer)properties.getProperty("Quest_IsMonsterPlayQuest"));
    boolean isMonsterPlay=((isMonsterPlayCode!=null) && (isMonsterPlayCode.intValue()!=0));
    deed.setMonsterPlay(isMonsterPlay);

    // Pre-requisites
    findPrerequisites(deed,properties);

    // Requirements
    findRequirements(deed,properties);
    // Min level
    Integer minLevel=deed.getMinimumLevel();
    if (minLevel==null)
    {
      minLevel=((Integer)properties.getProperty("Accomplishment_MinLevelToStart"));
      deed.setMinimumLevel(minLevel);
    }

    // Rewards
    Rewards rewards=deed.getRewards();
    ChallengeLevel challengeLevel=_rewardsLoader.fillRewards(properties,rewards);
    // Challenge level
    deed.setChallengeLevel(challengeLevel);

    // Objectives
    _objectivesLoader.handleObjectives(deed.getObjectives(),deed,properties);

    // Web Store (needed xpack/region): WebStoreAccountItem_DataID
    Integer webStoreItemID=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreItemID!=null)
    {
      WebStoreItem webStoreItem=_webStoreItemsLoader.getWebStoreItem(webStoreItemID.intValue());
      deed.setWebStoreItem(webStoreItem);
    }
    // Registration
    _deeds.put(Integer.valueOf(deed.getIdentifier()),deed);
  }

  private void handleDeedType(DeedDescription deed, PropertiesSet properties)
  {
    DeedType type=null;
    Integer categoryId=((Integer)properties.getProperty("Accomplishment_Category"));
    if (categoryId!=null)
    {
      int typeCode=categoryId.intValue();
      if (typeCode==22)
      {
        type=DeedType.CLASS;
      }
      else if (typeCode==2)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.CAPTAIN);
      }
      else if (typeCode==3)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.GUARDIAN);
      }
      else if (typeCode==5)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.MINSTREL);
      }
      else if (typeCode==6)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.BURGLAR);
      }
      else if (typeCode==26)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.HUNTER);
      }
      else if (typeCode==28)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.CHAMPION);
      }
      else if (typeCode==30)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.LORE_MASTER);
      }
      else if (typeCode==35)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.WARDEN);
      }
      else if (typeCode==36)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.RUNE_KEEPER);
      }
      else if (typeCode==38)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.BEORNING);
      }
      else if (typeCode==40)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.BRAWLER);
      }
      else if (typeCode==34)
      {
        type=DeedType.EVENT;
      }
      else if (typeCode==1)
      {
        type=DeedType.EXPLORER;
      }
      else if (typeCode==33)
      {
        type=DeedType.LORE;
      }
      else if (typeCode==25)
      {
        type=DeedType.RACE;
      }
      else if (typeCode==11)
      {
        type=DeedType.REPUTATION;
      }
      else if (typeCode==20)
      {
        type=DeedType.SLAYER;
      }
      else
      {
        // 0 => Undef
        // 13 => Man
        // 21 => Elf
        // 25 => Race
        // 27 => Dwarf
        // 29 => Hobbit
        // 37 => Beorning
        // 39 => Allegiance
        LOGGER.warn("Unmanaged type: "+typeCode);
      }
    }
    deed.setType(type);
  }

  private void findRequirements(Achievable achievable, PropertiesSet properties)
  {
    PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissions!=null)
    {
      _usageRequirementsLoader.loadUsageRequirements(permissions,achievable.getUsageRequirement());
      // - world events
      AbstractWorldEventCondition worldEventsRequirements=_weConditionsLoader.loadWorldEventsUsageConditions(permissions);
      achievable.setWorldEventsRequirement(worldEventsRequirements);
    }
  }

  private void findPrerequisites(Achievable achievable, PropertiesSet properties)
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
   * Load quests and deeds.
   */
  public void doIt()
  {
    // Scan quests and deeds
    doScan();
    // Use deeds index
    doIndex();
    // Add quest arcs
    loadQuestArcs();
    // Add race/class requirements for deed
    loadRaceRequirementsForDeeds();
    loadClassRequirementsForDeeds();
    // Resolve proxies
    resolveProxies();
    //System.out.println(DatObjectivesLoader._flagsToAchievables);

    // Post processings:
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    deeds.addAll(_deeds.values());
    sortDeeds(deeds);
    // - deed keys injection
    DeedKeysInjector injector=new DeedKeysInjector();
    injector.doIt(deeds);
    // - achievables geo data injection
    MainGeoDataInjector geoDataInjector=new MainGeoDataInjector(_facade);
    List<Achievable> achievables=new ArrayList<Achievable>();
    achievables.addAll(deeds);
    achievables.addAll(_quests.values());
    geoDataInjector.doIt(achievables);

    // Save
    doSave();
    _i18n.save();
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
    AchievableProxiesResolver resolver=new AchievableProxiesResolver(_quests.values(),_deeds.values());
    for(QuestDescription quest : _quests.values())
    {
      resolver.resolveQuest(quest);
      cleanup(quest);
    }
    for(DeedDescription deed : _deeds.values())
    {
      resolver.resolveDeed(deed);
      cleanup(deed);
    }
  }

  private void cleanup(Achievable achievable)
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

  private void doSave()
  {
    // Save quests
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
    // Save deeds
    {
      List<DeedDescription> deeds=new ArrayList<DeedDescription>();
      deeds.addAll(_deeds.values());
      int nbDeeds=_deeds.size();
      System.out.println("Nb deeds: "+nbDeeds);
      // Write deeds file
      sortDeeds(deeds);
      DeedXMLWriter writer=new DeedXMLWriter();
      boolean ok=writer.writeDeeds(GeneratedFiles.DEEDS,deeds,EncodingNames.UTF_8);
      if (ok)
      {
        System.out.println("Wrote deeds file: "+GeneratedFiles.DEEDS);
      }
    }
    // Save progressions
    DatStatUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    // Save world events
    {
      WorldEventsXMLWriter worldEventsWriter=new WorldEventsXMLWriter();
      File worldEventsFile=GeneratedFiles.WORLD_EVENTS;
      boolean ok=worldEventsWriter.write(worldEventsFile,_worldEventsLoader.getWorldEvents(),EncodingNames.UTF_8);
      if (ok)
      {
        System.out.println("Wrote world events file: "+GeneratedFiles.WORLD_EVENTS);
      }
    }
    // Save web store items
    {
      WebStoreItemsXMLWriter webStoreItemsWriter=new WebStoreItemsXMLWriter();
      File webStoreItemsFile=GeneratedFiles.WEB_STORE_ITEMS;
      boolean ok=webStoreItemsWriter.write(webStoreItemsFile,_webStoreItemsLoader.getWebStoreItems(),EncodingNames.UTF_8);
      if (ok)
      {
        System.out.println("Wrote web store items file: "+GeneratedFiles.WEB_STORE_ITEMS);
      }
    }
  }

  private void sortDeeds(List<DeedDescription> deeds)
  {
    List<Comparator<DeedDescription>> comparators=new ArrayList<Comparator<DeedDescription>>();
    comparators.add(new IdentifiableComparator<DeedDescription>());
    comparators.add(new DeedNameComparator());
    comparators.add(new DeedDescriptionComparator());
    CompoundComparator<DeedDescription> comparator=new CompoundComparator<DeedDescription>(comparators);
    Collections.sort(deeds,comparator);
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

  private void loadQuestArcs()
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

  private void loadRaceRequirementsForDeeds()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020F);
    Object[] raceIdsArray=(Object[])properties.getProperty("RaceTable_RaceTableList");
    for(Object raceIdObj : raceIdsArray)
    {
      int raceId=((Integer)raceIdObj).intValue();
      PropertiesSet raceProps=_facade.loadProperties(raceId+DATConstants.DBPROPERTIES_OFFSET);
      int raceCode=((Integer)raceProps.getProperty("RaceTable_Race")).intValue();
      Race race=DatEnumsUtils.getRaceFromRaceId(raceCode);
      int accomplishmentDirectoryId=((Integer)raceProps.getProperty("RaceTable_AccomplishmentDirectory")).intValue();
      PropertiesSet accomplishmentDirProps=_facade.loadProperties(accomplishmentDirectoryId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] accomplishmentList=(Object[])accomplishmentDirProps.getProperty("Accomplishment_List");
      if (accomplishmentList!=null)
      {
        for(Object accomplishmentListObj : accomplishmentList)
        {
          for(Object accomplishmentListObj2 : (Object[])accomplishmentListObj)
          {
            Integer deedId=(Integer)accomplishmentListObj2;
            DeedDescription deed=_deeds.get(deedId);
            if (deed!=null)
            {
              deed.setRequiredRace(race);
            }
          }
        }
      }
    }
  }

  private void loadClassRequirementsForDeeds()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020E);
    Object[] classIdsArray=(Object[])properties.getProperty("AdvTable_LevelTableList");
    for(Object classIdObj : classIdsArray)
    {
      int classId=((Integer)classIdObj).intValue();
      PropertiesSet classProps=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
      int classCode=((Integer)classProps.getProperty("AdvTable_Class")).intValue();
      CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classCode);
      int accomplishmentDirectoryId=((Integer)classProps.getProperty("AdvTable_AccomplishmentDirectory")).intValue();
      PropertiesSet accomplishmentDirProps=_facade.loadProperties(accomplishmentDirectoryId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] accomplishmentList=(Object[])accomplishmentDirProps.getProperty("Accomplishment_List");
      if (accomplishmentList!=null)
      {
        for(Object listItem : accomplishmentList)
        {
          if (listItem instanceof Integer)
          {
            Integer deedId=(Integer)listItem;
            handleClassRequirementForDeed(deedId,characterClass);
          }
          else if (listItem instanceof Object[])
          {
            for(Object listItem2 : (Object[])listItem)
            {
              Integer deedId=(Integer)listItem2;
              handleClassRequirementForDeed(deedId,characterClass);
            }
          }
        }
      }
    }
  }

  private void handleClassRequirementForDeed(Integer deedId, CharacterClass characterClass)
  {
    DeedDescription deed=_deeds.get(deedId);
    if (deed!=null)
    {
      deed.setRequiredClass(characterClass);
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
