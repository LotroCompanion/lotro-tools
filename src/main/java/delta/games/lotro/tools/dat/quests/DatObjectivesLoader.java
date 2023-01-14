package delta.games.lotro.tools.dat.quests;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.lore.agents.EntityClassification;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.geo.LandmarkDescription;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.objectives.ConditionTarget;
import delta.games.lotro.lore.quests.objectives.ConditionType;
import delta.games.lotro.lore.quests.objectives.DefaultObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.DetectingCondition;
import delta.games.lotro.lore.quests.objectives.DetectionCondition;
import delta.games.lotro.lore.quests.objectives.EmoteCondition;
import delta.games.lotro.lore.quests.objectives.EnterDetectionCondition;
import delta.games.lotro.lore.quests.objectives.ExternalInventoryItemCondition;
import delta.games.lotro.lore.quests.objectives.FactionLevelCondition;
import delta.games.lotro.lore.quests.objectives.HobbyCondition;
import delta.games.lotro.lore.quests.objectives.InventoryItemCondition;
import delta.games.lotro.lore.quests.objectives.ItemCondition;
import delta.games.lotro.lore.quests.objectives.ItemTalkCondition;
import delta.games.lotro.lore.quests.objectives.ItemUsedCondition;
import delta.games.lotro.lore.quests.objectives.LandmarkDetectionCondition;
import delta.games.lotro.lore.quests.objectives.LevelCondition;
import delta.games.lotro.lore.quests.objectives.MonsterDiedCondition;
import delta.games.lotro.lore.quests.objectives.MonsterDiedCondition.MobSelection;
import delta.games.lotro.lore.quests.objectives.NpcCondition;
import delta.games.lotro.lore.quests.objectives.NpcTalkCondition;
import delta.games.lotro.lore.quests.objectives.NpcUsedCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.lore.quests.objectives.QuestBestowedCondition;
import delta.games.lotro.lore.quests.objectives.QuestCompleteCondition;
import delta.games.lotro.lore.quests.objectives.SkillUsedCondition;
import delta.games.lotro.lore.quests.objectives.TimeExpiredCondition;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.dat.utils.MobLoader;
import delta.games.lotro.tools.dat.utils.NpcLoader;
import delta.games.lotro.tools.dat.utils.PlaceLoader;
import delta.games.lotro.tools.dat.utils.ProxyBuilder;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quest/deed objectives from DAT files.
 * @author DAM
 */
public class DatObjectivesLoader
{
  private static final Logger LOGGER=Logger.getLogger(DatObjectivesLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  private EnumMapper _monsterDivision;
  private EnumMapper _questEvent;
  private EnumMapper _questCategory;
  //private EnumMapper _deedCategory;

  private MobLoader _mobLoader;
  @SuppressWarnings("unused")
  private GeoData _geoData;

  //public static HashSet<String> propNames=new HashSet<String>();

  //private Achievable _currentAchievable;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n support.
   */
  public DatObjectivesLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
    _monsterDivision=_facade.getEnumsManager().getEnumMapper(587202657);
    _questEvent=_facade.getEnumsManager().getEnumMapper(587202639);
    _questCategory=_facade.getEnumsManager().getEnumMapper(587202585);
    //_deedCategory=_facade.getEnumsManager().getEnumMapper(587202587);
    _mobLoader=new MobLoader(facade);
    _geoData=QuestEventTargetLocationLoader.loadGeoData(facade);
  }

  /**
   * Load quest/deed objectives from DAT files data.
   * @param objectivesManager Objectives manager.
   * @param achievable Parent achievable.
   * @param properties Quest/deed properties.
   */
  public void handleObjectives(ObjectivesManager objectivesManager, Achievable achievable, PropertiesSet properties)
  {
    if (objectivesManager==null)
    {
      return;
    }
    //_currentAchievable=achievable;
    Object[] objectivesArray=(Object[])properties.getProperty("Quest_ObjectiveArray");
    if (objectivesArray!=null)
    {
      // Can have several objectives (ordered)
      for(Object objectiveObj : objectivesArray)
      {
        PropertiesSet objectiveProps=(PropertiesSet)objectiveObj;

        Objective objective=new Objective();
        //System.out.println(objectiveProps.dump());
        // Index
        int objectiveIndex=((Integer)objectiveProps.getProperty("Quest_ObjectiveIndex")).intValue();
        objective.setIndex(objectiveIndex);
        // Description
        String description=_i18n.getStringProperty(objectiveProps,"Quest_ObjectiveDescription");
        objective.setDescription(description);
        // Lore override
        String loreOverride=_i18n.getStringProperty(objectiveProps,"Quest_ObjectiveLoreOverride");
        objective.setLoreOverride(loreOverride);
        // Progress override
        String progressOverride=_i18n.getStringProperty(objectiveProps,"Quest_ObjectiveProgressOverride");
        objective.setProgressOverride(progressOverride);
        // Billboard override
        String billboardOverride=_i18n.getStringProperty(objectiveProps,"Quest_ObjectiveBillboardOverride");
        objective.setBillboardOverride(billboardOverride);
        // Conditions (can have several conditions)
        ArrayPropertyValue completionConditionsArray=(ArrayPropertyValue)objectiveProps.getPropertyValueByName("Quest_CompletionConditionArray");
        if (completionConditionsArray!=null)
        {
          for(PropertyValue completionConditionValue : completionConditionsArray.getValues())
          {
            handleObjectiveItem(objective,completionConditionValue);
          }
        }
        // Ignored: Quest_NPCObjRoles (Array) and Quest_ObjectiveVolumeString (String)
        objectivesManager.addObjective(objective);
      }
    }
    objectivesManager.sort();
  }

  private void handleObjectiveItem(Objective objective, PropertyValue item)
  {
    String propertyName=item.getDefinition().getName();
    if ("Quest_CompletionCondition".equals(propertyName))
    {
      ArrayPropertyValue completionConditionArray=(ArrayPropertyValue)item;
      handleCompletionCondition(objective,completionConditionArray);
    }
    else if ("Quest_FailureCondition".equals(propertyName))
    {
      ArrayPropertyValue failureConditionArray=(ArrayPropertyValue)item;
      handleFailureCondition(objective,failureConditionArray);
    }
    else
    {
      LOGGER.warn("Unmanaged item for a completion condition array: "+item);
    }
  }

  @SuppressWarnings("unused")
  private void handleCompletionCondition(Objective objective, ArrayPropertyValue completionConditionArray)
  {
    /*
268439142 - Quest_CompletionCondition, type=Array
  Property: QuestEvent_CompoundEvent, ID=268439626, type=Array
  Property: Quest_CompletionConditionCount, ID=268461297, type=Int
  Property: Quest_Condition_NeverFinish, ID=268437079, type=boolean
  Property: QuestEvent_Entry, ID=268439867, type=Struct
     */
    for(PropertyValue completionConditionItem : completionConditionArray.getValues())
    {
      String propertyName=completionConditionItem.getDefinition().getName();
      if ("Quest_CompletionConditionCount".equals(propertyName))
      {
        int conditionCount=((Integer)completionConditionItem.getValue()).intValue();
      }
      else if ("QuestEvent_Entry".equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)completionConditionItem.getValue();
        handleQuestEventEntry(objective,questEventEntryProps);
      }
      else if ("QuestEvent_CompoundEvent".equals(propertyName))
      {
        ArrayPropertyValue compoundEventArray=(ArrayPropertyValue)completionConditionItem;
        handleCompoundEvent(objective,compoundEventArray);
      }
      else if ("Quest_Condition_NeverFinish".equals(propertyName))
      {
        int neverFinish=((Integer)completionConditionItem.getValue()).intValue();
      }
      else
      {
        LOGGER.warn("Unmanaged item for a completion condition array: "+completionConditionItem);
      }
    }
  }

  private void handleCompoundEvent(Objective objective, ArrayPropertyValue compoundEventArray)
  {
    /*
268439626 - QuestEvent_CompoundEvent, type=Array
  Property: Accomplishment_LoreInfo, ID=268437995, type=String Info
  Property: QuestEvent_Entry, ID=268439867, type=Struct
  Property: QuestEvent_CompoundProgressOverride, ID=268439136, type=String Info
  Property: QuestEvent_Entry_Array, ID=268457470, type=Array
     */
    for(PropertyValue compoundEventItem : compoundEventArray.getValues())
    {
      String propertyName=compoundEventItem.getDefinition().getName();
      if ("QuestEvent_Entry".equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)compoundEventItem.getValue();
        handleQuestEventEntry(objective,questEventEntryProps);
      }
      else if ("QuestEvent_Entry_Array".equals(propertyName))
      {
        ArrayPropertyValue questEventArray=(ArrayPropertyValue)compoundEventItem;
        handleQuestEventEntryArray(objective,questEventArray);
      }
    }
  }

  private void handleQuestEventEntryArray(Objective objective, ArrayPropertyValue questEventArray)
  {
    /*
268457470 - QuestEvent_Entry_Array, type=Array
  Property: QuestEvent_Entry, ID=268439867, type=Struct
     */
    for(PropertyValue questEventItem : questEventArray.getValues())
    {
      String propertyName=questEventItem.getDefinition().getName();
      if ("QuestEvent_Entry".equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)questEventItem.getValue();
        handleQuestEventEntry(objective,questEventEntryProps);
      }
    }
  }

  private void handleFailureCondition(Objective objective, ArrayPropertyValue failureConditionArray)
  {
    // Unmanaged
  }

  private void handleQuestEventEntry(Objective objective, PropertiesSet properties)
  {
    /*
     * Shared condition attributes:
     * Accomplishment_LoreInfo: verbose text about the condition (deeds only)
     * QuestEvent_EventOrder: usually 0, but can be 1,2,3 if several conditions in an objective.
     * QuestEvent_ID: condition type identifier (see Enum: QuestEventType, (id=587202639))
     * QuestEvent_ProgressOverride: small text for the condition (ex: "Complete quests within the Shire")
     * QuestEvent_BillboardProgressOverride: optional, a String[]. Set if QuestEvent_ShowBillboardText=1?
     * QuestEvent_RoleConstraint: optional, key for additional constraint on condition (for instance, challenge mode for a quest)
     * QuestEvent_ShowBillboardText: usually 0, can be 1 (means that the condition shall be displayed in the UI?)
     * QuestEvent_ShowProgressText: optional, 0 if set, used many times
     */
    // Order
    Integer eventOrder=(Integer)properties.getProperty("QuestEvent_EventOrder");
    //System.out.println("\tEvent #"+eventOrder);
    // ID
    int questEventId=((Integer)properties.getProperty("QuestEvent_ID")).intValue();
    //System.out.println("\t\tEvent ID: "+questEventId+" ("+eventMeaning+")");
    // Billboard
    boolean showBillboardText=true;
    Integer showBillboardTextInt=(Integer)properties.getProperty("QuestEvent_ShowBillboardText");
    if ((showBillboardTextInt!=null) && (showBillboardTextInt.intValue()==0))
    {
      showBillboardText=false;
      //System.out.println("\t\tShow billboard text: "+showBillboardTextInt);
    }
    // Billboard override
    String billboardProgressOverride=_i18n.getStringProperty(properties,"QuestEvent_BillboardProgressOverride");
    if (billboardProgressOverride!=null)
    {
      //System.out.println("\t\tBillboard progress override: "+billboardProgressOverride);
    }
    // Progress
    boolean showProgressText=true;
    Integer showProgressTextInt=(Integer)properties.getProperty("QuestEvent_ShowProgressText");
    if ((showProgressTextInt!=null) && (showProgressTextInt.intValue()==0))
    {
      showProgressText=false;
      //System.out.println("\t\tShow progress text: "+showProgressTextInt);
    }
    // Progress override
    String progressOverride=_i18n.getStringProperty(properties,"QuestEvent_ProgressOverride");
    // Role constraint
    String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
    if (roleConstraint!=null)
    {
      //System.out.println("\t\tRole constraint: "+roleConstraint);
    }
    // Lore info
    String loreInfo=_i18n.getStringProperty(properties,"Accomplishment_LoreInfo");
    // Count
    Integer count=(Integer)properties.getProperty("QuestEvent_Number");

    // Deeds:
    // QuestEvent_ID: {32=3936(done), 22=1142(done), 1=889, 21=869(done), 26=611, 31=487(done), 45=411, 7=349, 25=116,
    // 34=108, 11=82, 39=51, 4=41, 18=29, 24=22, 16=20, 6=15, 10=2, 58=2, 59=1}
    // Quests:
    // QuestEvent_ID:  {11=11545, 7=3845, 22=3164(done), 1=2970, 32=1967(done), 34=1599, 10=1162, 31=831(done), 5=640, 21=425(done),
    // 24=301, 14=293, 19=210, 27=196, 13=187, 56=144, 16=123, 37=97, 29=97, 33=72, 2=58, 59=50, 25=46,
    // 18=43, 26=39, 58=35, 57=35, 4=25, 46=16, 40=15, 39=14, 43=14, 30=12, 45=8, 38=5, 6=1, 9=1}

    /*
  1 => EnterDetection (deeds,quests)
  2 => LeaveDetection (quests)
  4 => MonsterPlayerDied (deeds,quests)
  5 => NPCUsed (quests)
  6 => SkillApplied (deeds,quests)
  7 => ItemUsed (deeds,quests)
  9 => Detecting (quests)
  10 => ExternalInventoryItem (deeds,quests)
  11 => NPCTalk (deeds,quests)
  13 => Channeling (quests)
  14 => TimeExpired (quests)
  16 => ItemTalk (deeds,quests)
  18 => Level (deeds,quests)
  19 => ClearCamp (quests)
  21 => LandmarkDetection (deeds,quests)
  22 => MonsterDied (deeds,quests)
  24 => Emote (deeds,quests)
  25 => PlayerDied (deeds,quests)
  26 => SkillUsed (deeds,quests)
  27 => KungFu (quests)
  29 => Escort (quests)
  30 => SelfDied (quests)
  31 => InventoryItem (deeds,quests)
  32 => QuestComplete (deeds,quests)
  33 => CraftRecipeExecution (quests)
  34 => WorldEventCondition (deeds,quests)
  37 => SessionFinished (quests)
  38 => ResourceSet (quests)
  39 => HobbyItem (deeds,quests)
  40 => ItemAdvancement (quests)
  43 => Dismounted (quests)
  45 => FactionLevel (deeds,quests)
  46 => Teleported (quests)
  56 => EnterPlayerAOI (quests)
  57 => CorpseUsed (quests)
  58 => ScriptCallback (deeds,quests)
  59 => QuestBestowed (deeds,quests)
 */

    ObjectiveCondition condition=null;
    ConditionType type=null;
    if (questEventId==1)
    {
      condition=handleEnterDetection(properties,objective);
    }
    else if (questEventId==5)
    {
      condition=handleNpcUsed(properties,objective);
    }
    else if (questEventId==7)
    {
      condition=handleItemUsed(properties,objective);
    }
    else if (questEventId==9)
    {
      condition=handleDetecting(properties,objective);
    }
    else if (questEventId==10)
    {
      condition=handleExternalInventoryItemUsed(properties,objective);
    }
    else if (questEventId==11)
    {
      condition=handleNpcTalk(properties,objective);
    }
    else if (questEventId==13)
    {
      condition=handleChanneling(properties);
    }
    else if (questEventId==14)
    {
      condition=handleTimeExpired(properties,objective);
    }
    else if (questEventId==16)
    {
      condition=handleItemTalk(properties,objective);
    }
    else if (questEventId==18)
    {
      condition=handleLevelCondition(properties,objective);
    }
    else if (questEventId==19)
    {
      condition=handleClearCamp(properties);
    }
    else if (questEventId==20)
    {
      condition=handleChannelingFailed(properties);
    }
    else if (questEventId==21)
    {
      condition=handleLandmarkDetection(properties,objective);
    }
    else if (questEventId==22)
    {
      condition=handleMonsterDieCondition(properties);
    }
    else if (questEventId==24)
    {
      condition=handleEmoteCondition(properties);
    }
    else if (questEventId==25)
    {
      type=ConditionType.PLAYER_DIED;
      handlePlayerDied(properties);
    }
    else if (questEventId==26)
    {
      condition=handleSkillUsed(properties);
    }
    /*
    else if (questEventId==27)
    {
      //handleKungFu(properties,objective);
    }
    */
    else if (questEventId==30)
    {
      condition=handleSelfDied(properties);
    }
    else if (questEventId==31)
    {
      condition=handleInventoryItem(properties);
    }
    else if (questEventId==32)
    {
      condition=handleQuestComplete(properties);
    }
    else if (questEventId==33)
    {
      condition=handleCraftRecipeExecution(properties);
    }
    else if (questEventId==34)
    {
      type=ConditionType.WORLD_EVENT_CONDITION;
      handleWorldEventCondition(properties);
    }
    else if (questEventId==39)
    {
      condition=handleHobbyItem(properties);
    }
    else if (questEventId==43)
    {
      condition=handleDismounted(properties);
    }
    else if (questEventId==45)
    {
      condition=handleFactionLevel(properties);
    }
    else if (questEventId==46)
    {
      condition=handleTeleported(properties);
    }
    else if (questEventId==59)
    {
      condition=handleQuestBestowed(properties);
    }
    else if (questEventId==2) type=ConditionType.LEAVE_DETECTION;
    else if (questEventId==4) type=ConditionType.MONSTER_PLAYER_DIED;
    else if (questEventId==6) type=ConditionType.SKILL_APPLIED;
    else if (questEventId==27) type=ConditionType.KUNG_FU;
    else if (questEventId==29) type=ConditionType.ESCORT;
    else if (questEventId==37) type=ConditionType.SESSION_FINISHED;
    else if (questEventId==38) type=ConditionType.RESOURCE_SET;
    else if (questEventId==40) type=ConditionType.ITEM_ADVANCEMENT;
    else if (questEventId==56) type=ConditionType.ENTER_PLAYER_AOI;
    else if (questEventId==57) type=ConditionType.CORPSE_USED;
    else if (questEventId==58) type=ConditionType.SCRIPT_CALLBACK;
    else
    {
      String eventMeaning=_questEvent.getString(questEventId);
      LOGGER.warn("Unmanaged quest event: ID="+questEventId+", meaning="+eventMeaning);
    }

    if (condition==null)
    {
      condition=new DefaultObjectiveCondition(type);
    }
    if (eventOrder!=null)
    {
      condition.setIndex(eventOrder.intValue());
    }
    // Lore info
    if (loreInfo!=null)
    {
      condition.setLoreInfo(loreInfo);
    }
    // Progress
    if (progressOverride!=null)
    {
      condition.setProgressOverride(progressOverride);
    }
    condition.setShowProgressText(showProgressText);
    // Billboard
    condition.setShowBillboardText(showBillboardText);
    // Count
    if (count!=null)
    {
      condition.setCount(count.intValue());
    }
    objective.addCondition(condition);
  }

  private DetectingCondition handleDetecting(PropertiesSet properties, Objective objective)
  {
    DetectingCondition ret=new DetectingCondition();
    handleDetectionCondition(ret,properties,objective);
    return ret;
  }

  private EnterDetectionCondition handleEnterDetection(PropertiesSet properties, Objective objective)
  {
    EnterDetectionCondition ret=new EnterDetectionCondition();
    handleDetectionCondition(ret,properties,objective);
    return ret;
  }

  private void handleDetectionCondition(DetectionCondition condition, PropertiesSet properties, Objective objective)
  {
    ConditionTarget target=null;
    Integer detect=(Integer)properties.getProperty("QuestEvent_Detect");
    String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
    //System.out.println("Enter detect: "+detect+", role="+roleConstraint);
    if (detect!=null)
    {
      target=getTarget(detect);
    }
    else if (roleConstraint!=null)
    {
      //System.out.println("\tRole:"+roleConstraint);
    }
    else
    {
      LOGGER.warn("Detect condition: No detect and no role constraint");
    }
    /*
    List<DatPosition> positions=getPositions(detect,roleConstraint,objective.getIndex());
    if (positions!=null)
    {
      System.out.println("\tPositions: "+positions);
    }
    */
    condition.setTarget(target);
  }

  /*
  private static Set<String> _props=new HashSet<String>();

  private void handleKungFu(PropertiesSet properties, Objective objective)
  {
    System.out.println("Kung Fu!");
    _props.addAll(properties.getPropertyNames());
    System.out.println(_props);
    System.out.println(properties.dump());
    // QuestEvent_RunKungFu is 0 or absent...
    // QuestEvent_RoleConstraint: always set
  }
  */

  private ItemUsedCondition handleItemUsed(PropertiesSet properties, Objective objective)
  {
    /*
     * QuestEvent_AllowQuickslot: optional, found 22 times Integer 0.
     * QuestEvent_RequireUniqueItems: optional, found 31 times Integer 1.
     * QuestEvent_Number: optional, number of usages, 1-300.
     * QuestEvent_HasInventoryItem: optional, found 2 times Integer 1.
     * QuestEvent_ItemDID: almost always (306/350). Item ID.
     * or QuestEvent_RoleConstraint: (65/350) for food, dwarves marker and hobbit lamps from Enedwaith...
     * (sometimes both, never none)
     * QuestEvent_DestroyInventoryItems: optional, found 16 times Integer 0 or 1.
     */
    Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    //String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");

    ItemUsedCondition ret=new ItemUsedCondition();
    fillItemCondition(ret,itemId);
    /*List<DatPosition> positions=*/
    //getPositions(itemId,roleConstraint,objective.getIndex());
    return ret;
  }

  @SuppressWarnings("unused")
  private ExternalInventoryItemCondition handleExternalInventoryItemUsed(PropertiesSet properties, Objective objective)
  {
    Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");

    ExternalInventoryItemCondition ret=new ExternalInventoryItemCondition();
    fillItemCondition(ret,itemId);
    //List<DatPosition> positions=getPositions(itemId,roleConstraint,objective.getIndex());
    return ret;
  }

  @SuppressWarnings("unused")
  private ItemTalkCondition handleItemTalk(PropertiesSet properties, Objective objective)
  {
    Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");

    ItemTalkCondition ret=new ItemTalkCondition();
    fillItemCondition(ret,itemId);
    //List<DatPosition> positions=getPositions(itemId,roleConstraint,objective.getIndex());
    return ret;
  }

  private void fillItemCondition(ItemCondition ret, Integer itemId)
  {
    if (itemId!=null)
    {
      Proxy<Item> itemProxy=ProxyBuilder.buildItemProxy(itemId.intValue());
      ret.setProxy(itemProxy);
    }
  }

  private NpcTalkCondition handleNpcTalk(PropertiesSet properties, Objective objective)
  {
    NpcTalkCondition ret=new NpcTalkCondition();
    handleNpcCondition(ret,properties,objective);
    return ret;
  }

  private NpcUsedCondition handleNpcUsed(PropertiesSet properties, Objective objective)
  {
    NpcUsedCondition ret=new NpcUsedCondition();
    handleNpcCondition(ret,properties,objective);
    return ret;
  }

  private void handleNpcCondition(NpcCondition condition, PropertiesSet properties, Objective objective)
  {
    /*
     * QuestEvent_RoleConstraint: used 6 times for limlight spirits.
     * QuestEvent_NPCTalk: almost always (76/82). NPC ID?
     * Quest_Role: gives some text, sound ID... for the NPC... (50/82)
     * QuestEvent_CanTeleportToObjective: optional, used 12 times: Integer 0.
     * QuestEvent_Number: most of the time: 1, sometimes >1.
[QuestEvent_WaitForDrama, QuestEvent_DramaName, Quest_GiveItemArray, QuestEvent_ShowProgressText, QuestEvent_RoleConstraint,
QuestEvent_ShowBillboardText, QuestEvent_ID, QuestEvent_ForceCheckContentLayer, QuestEvent_DramaProgressOverride,
QuestEvent_ProgressOverride, QuestEvent_GiveItemsOnAdvance, QuestEvent_HideRadarIcon, Accomplishment_LoreInfo,
QuestEvent_DisableEntityExamination, QuestEvent_BillboardProgressOverride, QuestEvent_IsRemote, QuestEvent_ItemDID, QuestEvent_RunDramaOnAdvance, QuestEvent_Locations_ForceFullLandblock, QuestEvent_LocationsAreOverrides, QuestEvent_Locations, QuestEvent_IsFellowUseShared, QuestEvent_ApplyEffectArray, Quest_Role]
     */

    Integer npcId=(Integer)properties.getProperty("QuestEvent_NPCTalk");
    if (npcId!=null)
    {
      String npcName=NpcLoader.loadNPC(_facade,npcId.intValue());
      Proxy<NpcDescription> proxy=new Proxy<NpcDescription>();
      proxy.setId(npcId.intValue());
      proxy.setName(npcName);
      condition.setProxy(proxy);
    }
    // TODO
    //String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
    /*List<DatPosition> positions=*/
    //getPositions(npcId,roleConstraint,objective.getIndex());
  }

  private LevelCondition handleLevelCondition(PropertiesSet properties, Objective objective)
  {
    LevelCondition ret=new LevelCondition();
    int level=((Integer)properties.getProperty("QuestEvent_PlayerLevel")).intValue();
    ret.setLevel(level);
    return ret;
  }

  private LandmarkDetectionCondition handleLandmarkDetection(PropertiesSet properties, Objective objective)
  {
    LandmarkDetectionCondition ret=new LandmarkDetectionCondition();
    /*
    QuestEvent_ForceCheckContentLayer: optional, used 2 times: Integer 1
    QuestEvent_QuestComplete_SuppressQuestCountUpdate: optional, used once: Integer 1
    QuestEvent_LocationsAreOverrides: optional, used 20 times, always Integer 1
    Quest_Role: found 7 times, always empty.
    QuestEvent_LandmarkDID: POI identifier. always present (869 times).
    */
    Integer landmarkId=(Integer)properties.getProperty("QuestEvent_LandmarkDID");
    if (landmarkId!=null)
    {
      String landmarkName=PlaceLoader.loadLandmark(_facade,landmarkId.intValue());
      Proxy<LandmarkDescription> landmark=new Proxy<LandmarkDescription>();
      landmark.setId(landmarkId.intValue());
      landmark.setName(landmarkName);
      ret.setLandmarkProxy(landmark);
    }
    /*List<DatPosition> positions=*/
    //getPositions(landmarkId,null,objective.getIndex());
    return ret;
  }

  private MonsterDiedCondition handleMonsterDieCondition(PropertiesSet properties)
  {
    /*
QuestEvent_MonsterGenus_Array: 
  #1: 
    Quest_MonsterRegion: 1879049792
    Quest_MonsterSpecies: 41
QuestEvent_Number: 1
QuestEvent_ShowBillboardText: 0
     */

    MonsterDiedCondition ret=new MonsterDiedCondition();

    Object[] monsterGenusArray=(Object[])properties.getProperty("QuestEvent_MonsterGenus_Array");
    if (monsterGenusArray!=null)
    {
      int nbMonsterGenus=monsterGenusArray.length;
      for(int i=0;i<nbMonsterGenus;i++)
      {
        PropertiesSet monsterGenusProps=(PropertiesSet)monsterGenusArray[i];
        // Where
        Integer regionId=(Integer)monsterGenusProps.getProperty("Quest_MonsterRegion");
        Integer mobDivision=(Integer)monsterGenusProps.getProperty("Quest_MonsterDivision");
        Integer landmarkId=(Integer)monsterGenusProps.getProperty("QuestEvent_LandmarkDID");
        String where=null;
        if (mobDivision!=null)
        {
          String divisionStr=_monsterDivision.getString(mobDivision.intValue());
          where=concat(where,divisionStr);
        }
        if (regionId!=null)
        {
          String regionName=PlaceLoader.loadPlace(_facade,regionId.intValue());
          where=concat(where,regionName);
        }
        if (landmarkId!=null)
        {
          String landmarkName=PlaceLoader.loadLandmark(_facade,landmarkId.intValue());
          where=concat(where,landmarkName);
        }
        // What
        EntityClassification mobReference=_mobLoader.buildMobReference(monsterGenusProps);
        MobSelection selection=new MobSelection();
        selection.setWhere(where);
        selection.setWhat(mobReference);
        ret.getMobSelections().add(selection);
      }
    }
    else
    {
      Integer mobId=(Integer)properties.getProperty("QuestEvent_MonsterDID");
      if (mobId!=null)
      {
        String mobName=_mobLoader.loadMob(mobId.intValue());
        ret.setMobId(mobId);
        ret.setMobName(mobName);
      }
    }
    return ret;
  }

  private EmoteCondition handleEmoteCondition(PropertiesSet properties)
  {
    EmoteCondition ret=new EmoteCondition();
    // Emote ID
    int emoteId=((Integer)properties.getProperty("QuestEvent_EmoteDID")).intValue();
    EmoteDescription emote=EmotesManager.getInstance().getEmote(emoteId);
    if (emote!=null)
    {
      Proxy<EmoteDescription> emoteProxy=new Proxy<EmoteDescription>();
      emoteProxy.setId(emoteId);
      emoteProxy.setName(emote.getCommand());
      emoteProxy.setObject(emote);
      ret.setProxy(emoteProxy);
    }
    else
    {
      LOGGER.warn("Emote not found: "+emoteId);
    }
    // Target
    Integer targetDID=(Integer)properties.getProperty("QuestEvent_EmoteTargetDID");
    ConditionTarget target=null;
    if (targetDID!=null)
    {
      target=getTarget(targetDID);
    }
    ret.setTarget(target);
    // Max daily
    Integer maxTimesPerDay=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    if (maxTimesPerDay!=null)
    {
      ret.setMaxDaily(maxTimesPerDay);
    }
    return ret;
  }

  private void handlePlayerDied(PropertiesSet properties)
  {
    /*
     * QuestEvent_MonsterGenus_Array: always
     *    - Quest_MonsterClass: see Enum: CharacterClassType, (id=587202574). Ex: 162=Hunter
     *    - Quest_MonsterGenus (bitset for Enum: GenusType, (id=587202570)) and Quest_MonsterSpecies (see Enum: Species, (id=587202571)).
     *      Ex: 16384=2^(15-1) 15=Dwarf and 73 (Dwarves).
     * QuestEvent_TerritoryDID: Zone ID (Eregion, Forochel, Angmar)
     * QuestEvent_Number
     */
    /*
    Integer territoryId=(Integer)properties.getProperty("QuestEvent_TerritoryDID");
    if (territoryId!=null)
    {
      String name=PlaceLoader.loadPlace(_facade,territoryId.intValue());
      System.out.println("Territory: "+name);
    }
    */
  }

  //public static String currentName="";

  //public static HashMap<String,List<String>> _flagsToAchievables=new HashMap<String,List<String>>();

  private SkillUsedCondition handleSkillUsed(PropertiesSet properties)
  {
    /*
    // QuestEvent_Number: number of time to use the skill
    // QuestEvent_DailyMaximumIncrements: used often ~400/~600. Max number of skill usages counted by day
    //     QuestEvent_SkillDID: Skill ID (not mandatory)
    // or: QuestEvent_SkillQuestFlags: bitset: skill(s) indicator? Ex: 70368744177664=2^(47-1)
    // QuestEvent_Skill_AttackResultArray: always, array of Integer (see Enum: CombatResultType, (id=587202602))
    //   9: Hit, 10: CriticalHit, 11: SuperCriticalHit
    */

    SkillUsedCondition ret=new SkillUsedCondition();
    Integer skillId=(Integer)properties.getProperty("QuestEvent_SkillDID");
    if (skillId!=null)
    {
      SkillsManager skillsMgr=SkillsManager.getInstance();
      SkillDescription skill=skillsMgr.getSkill(skillId.intValue());
      Proxy<SkillDescription> proxy=null;
      if (skill!=null)
      {
        proxy=new Proxy<SkillDescription>();
        proxy.setId(skill.getIdentifier());
        proxy.setName(skill.getName());
        proxy.setObject(skill);
        ret.setProxy(proxy);
      }
      else
      {
        LOGGER.warn("Skill not found: "+skillId);
      }
    }
    Integer dailyMaxIncrement=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    if (dailyMaxIncrement!=null)
    {
      ret.setMaxPerDay(dailyMaxIncrement);
    }
    /*
    Long flags=(Long)properties.getProperty("QuestEvent_SkillQuestFlags");
    if ((flags!=null) && (flags.longValue()!=0))
    {
      BitSet bits=BitSetUtils.getBitSetFromFlags(flags.longValue());
      //System.out.println("Flags: "+flags+" => "+bits);
      String key=bits.toString();
      List<String> names=_flagsToAchievables.get(key);
      if (names==null)
      {
        names=new ArrayList<String>();
        _flagsToAchievables.put(key,names);
      }
      names.add(currentName);
      Collections.sort(names);
    }
    */
    //Object[] attackResultArray=(Object[])properties.getProperty("QuestEvent_Skill_AttackResultArray");
    //System.out.println(properties.dump());
    return ret;
  }

  private InventoryItemCondition handleInventoryItem(PropertiesSet properties)
  {
    /*
     * QuestEvent_ForceCheckContentLayer: optional, used once: Integer 1
     * QuestEvent_QuestComplete_SuppressQuestCountUpdate: optional, used once: Integer 1
     * QuestEvent_ItemDID: always, item ID
     * QuestEvent_Number: number of items to get.
     * QuestEvent_DestroyInventoryItems: optional, always 1 when present (398/488).
     *     Indicates if the item is destroyed when acquired or not.
     */
    InventoryItemCondition ret=new InventoryItemCondition();

    Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    if (itemId!=null)
    {
      fillItemCondition(ret,itemId);
    }
    else
    {
      LOGGER.warn("No item ID in InventoryItem condition");
    }
    return ret;
  }

  private QuestCompleteCondition handleQuestComplete(PropertiesSet properties)
  {
    /*
    QuestEvent_QuestComplete_SuppressQuestCountUpdate, QuestEvent_Locations_ForceFullLandblock,
    // QuestEvent_QuestComplete: ID of quest/deed to complete
    // or QuestEvent_QuestCompleteCategory: category of quests to complete (see Enum: QuestCategory, (id=587202585)). Ex: 112=Task
    // or QuestEvent_AccomplishmentCompleteCategory: same for deed (see Enum: AccomplishmentCategory, (id=587202587))
    // QuestEvent_Number: number of quests to complete (1, 75...)
    // QuestEvent_DailyMaximumIncrements: used once, with value -1 => ignore!
    // QuestEvent_DisableEntityExamination: optional, 1 if set
    // QuestEvent_GiveItemsOnAdvance: optional, 1 if set, only used 2 times
    // QuestEvent_LocationsAreOverrides: optional, 1 if set, only used 5 times
    // QuestEvent_QuestCompleteScope: optional; 1 or 3 if set, used 8 times (see Enum: UIQuestScope, (id=587202590)?). 1=Repeatable ; 3=Small Fellowship
    */

    QuestCompleteCondition ret=new QuestCompleteCondition();

    Integer questId=(Integer)properties.getProperty("QuestEvent_QuestComplete");
    if (questId!=null)
    {
      // Check if deed or quest
      Boolean isQuest=DatQuestDeedsUtils.isQuestId(_facade,questId.intValue());
      if (isQuest!=null)
      {
        Proxy<Achievable> proxy=new Proxy<Achievable>();
        proxy.setId(questId.intValue());
        ret.setProxy(proxy);
      }
    }
    else
    {
      // Quests in category
      Integer questCategoryCode=(Integer)properties.getProperty("QuestEvent_QuestCompleteCategory");
      if (questCategoryCode!=null)
      {
        String questCategory=_questCategory.getString(questCategoryCode.intValue());
        //System.out.println("\t\tQuest category: "+questCategoryCode+" => "+questCategory);
        ret.setQuestCategory(questCategory);
      }
      // Unused
      /*
      Integer deedCategoryCode=(Integer)properties.getProperty("QuestEvent_AccomplishmentCompleteCategory");
      if (deedCategoryCode!=null)
      {
        String deedCategory=_deedCategory.getString(deedCategoryCode.intValue());
        System.out.println("\t\tDeed category: "+deedCategoryCode+" => "+deedCategory);
        ret.setDeedCategory(deedCategory);
      }
      */
    }
    // Unused
    /*
    Integer dailyMax=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    if (dailyMax!=null)
    {
      System.out.println("dailyMax: "+dailyMax); // -1?
    }
    */
    return ret;
  }

  private void handleWorldEventCondition(PropertiesSet properties)
  {
    /*
     * QuestEvent_WorldEvent_Value: always. Integer 1, rarely 4 or 7.
     * QuestEvent_RoleConstraint: string. 4 uses: 2 for draigoch, 2 for skirmishes (goblin's pot in 21st hall, arrows in defence of bruinen)
     * QuestEvent_WorldEvent: ID (relates to world event, such as festivals)
     * QuestEvent_WorldEvent_Operator: always. Integer 3.
     * QuestEvent_Number: almost always. Integer 1 if set.
     */
    /*
    Integer value=(Integer)properties.getProperty("QuestEvent_WorldEvent_Value");
    int id=((Integer)properties.getProperty("QuestEvent_WorldEvent")).intValue();
    int operator=((Integer)properties.getProperty("QuestEvent_WorldEvent_Operator")).intValue();
    String constraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
    System.out.println("World event: ID="+id+", operator="+operator+", value="+value+", count="+count+", constraint="+constraint);
    PropertiesSet worldEventProps=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    int propId=((Integer)worldEventProps.getProperty("WorldEvent_WorldPropertyName")).intValue();
    PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(propId);
    System.out.println(propDef);
    System.out.println(worldEventProps.dump());
    */
  }

  private HobbyCondition handleHobbyItem(PropertiesSet properties)
  {
    /*
     * QuestEvent_HobbyDID: always. 51 times the same hobby ID (the only one: 1879109150).
     * QuestEvent_ItemDID: always. Item ID (probably all fishes).
     * QuestEvent_Number: set 40 times. Mostly Integer: 1, sometimes 10.
     */
    //int hobbyId=((Integer)properties.getProperty("QuestEvent_HobbyDID")).intValue();
    Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    HobbyCondition ret=new HobbyCondition();
    Proxy<Item> itemProxy=ProxyBuilder.buildItemProxy(itemId.intValue());
    ret.setProxy(itemProxy);
    return ret;
  }

  private FactionLevelCondition handleFactionLevel(PropertiesSet properties)
  {
    /*
     * Condition validated when the given faction tier is reached.
     * QuestEvent_FactionDID: always. Faction ID.
     * QuestEvent_ReputationTier: always. Faction tier: 1->7
     */

    int factionId=((Integer)properties.getProperty("QuestEvent_FactionDID")).intValue();
    int tier=((Integer)properties.getProperty("QuestEvent_ReputationTier")).intValue();
    FactionLevelCondition ret=new FactionLevelCondition();
    Proxy<Faction> factionProxy=new Proxy<Faction>();
    factionProxy.setId(factionId);
    Faction faction=FactionsRegistry.getInstance().getById(factionId);
    String factionName=(faction!=null)?faction.getName():"?";
    factionProxy.setName(factionName);
    ret.setProxy(factionProxy);
    ret.setTier(tier);
    return ret;
  }

  private QuestBestowedCondition handleQuestBestowed(PropertiesSet properties)
  {
    QuestBestowedCondition ret=new QuestBestowedCondition();
    // Quest ID
    int questId=((Integer)properties.getProperty("QuestEvent_BestowedQuestID")).intValue();
    Proxy<Achievable> proxy=new Proxy<Achievable>();
    proxy.setId(questId);
    ret.setProxy(proxy);
    return ret;
  }

  
  private TimeExpiredCondition handleTimeExpired(PropertiesSet properties, Objective objective)
  {
    Integer timeLimit=(Integer)properties.getProperty("QuestEvent_TimeLimit");
    int duration=(timeLimit!=null)?timeLimit.intValue():0;
    TimeExpiredCondition ret=new TimeExpiredCondition();
    ret.setDuration(duration);
    return ret;
  }

  private DefaultObjectiveCondition handleCraftRecipeExecution(PropertiesSet properties)
  {
    /*
    System.out.println("Craft recipe execution");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.CRAFT_RECIPE_EXECUTION);
    // - count
    // - recipe ID
    // - (location)
    /*
Craft recipe execution
QuestEvent_BillboardProgressOverride: 
  #1: Seeds planted
QuestEvent_Locations: 
  #1: 
    QuestEvent_Location_Position: R=1,I=0,bx=93,by=118,x=129.18,y=8.07,z=367.93
QuestEvent_LocationsAreOverrides: 1
QuestEvent_Number: 1
QuestEvent_RecipeDID: 1879088560
QuestEvent_ShowBillboardText: 1
     */
    return ret;
  }

  private DefaultObjectiveCondition handleDismounted(PropertiesSet properties)
  {
    /*
    System.out.println("Dismounted!");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.DISMOUNTED);
    return ret;
  }

  private DefaultObjectiveCondition handleTeleported(PropertiesSet properties)
  {
    /*
    System.out.println("Teleported!");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.TELEPORTED);
    return ret;
  }

  private DefaultObjectiveCondition handleChanneling(PropertiesSet properties)
  {
    /*
Channeling!
QuestEvent_BillboardProgressOverride: 
  #1: You tip the slop into the bucket, and it makes a sickening sound
QuestEvent_ChannelingRoleConstraint: role_slop_to_prisoner_bucket
QuestEvent_ChannelingState: 1879210738
QuestEvent_EventOrder: 0
QuestEvent_FailedBillboardText: 
  #1: You dropped the bucket and spilled the slop! Go get some more
QuestEvent_ID: 13 (Channeling)
QuestEvent_ItemDID: 1879209794
QuestEvent_Number: 1
QuestEvent_ProgressOverride: 
  #1: Get some slop from the slop bucket in the kitchens
QuestEvent_ShowBillboardText: 1
QuestEvent_TappedProgressOverride: 
  #1: Carry the slop to the bucket in the dungeons
     */
    /*
    System.out.println("Channeling!");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.CHANNELING);
    return ret;
  }

  private DefaultObjectiveCondition handleChannelingFailed(PropertiesSet properties)
  {
    /*
Channeling failed!
QuestEvent_BillboardProgressOverride: 
  #1: You have lost the race
QuestEvent_ChannelingState: 1879162589
QuestEvent_ID: 20 (ChannelingFailed)
QuestEvent_Number: 1
QuestEvent_ShowBillboardText: 1
QuestEvent_ShowProgressText: 0
     */
    /*
    System.out.println("Channeling failed!");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.CHANNELING_FAILED);
    return ret;
  }

  private DefaultObjectiveCondition handleClearCamp(PropertiesSet properties)
  {
    /*
    System.out.println("Clear camp!");
    System.out.println(properties.dump());
    */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.CLEAR_CAMP);
    /*
Clear camp!
QuestEvent_BillboardProgressOverride: 
  #1: You have successfully defended Tóki Whitebeard!
QuestEvent_DramaName: drama_stolen_stones_ending
QuestEvent_EventOrder: 0
QuestEvent_FailQuestWhenEventFails: 1
QuestEvent_FailedBillboardText: 
  #1: Tóki Whitebeard has been defeated!
QuestEvent_ID: 19 (ClearCamp)
QuestEvent_KungFuDramaName: drama_stolen_stones_kungfu
QuestEvent_Number: 1
QuestEvent_ProgressOverride: 
  #1: Search Ost Dúrgonn for the stolen stones
QuestEvent_RoleConstraint: role_stolen_stones_dwarf
QuestEvent_ShouldShowEscortVitals: 1
QuestEvent_ShowBillboardText: 1
QuestEvent_TappedProgressOverride: 
  #1: Protect Tóki Whitebeard
QuestEvent_WaitForDrama: 1
Quest_Role: 
  QuestDispenser_RoleSuccessText: 
    #1: 'Are you...here to help? Be careful, friend! A mighty cave-claw has slain...the rest of my....'\n\nThe dwarf shudders with exhaustion.\n\n'This monstrous cave-claw...attacked us...after the trolls stole...the stones we...were bearing....'
  QuestDispenser_StartScriptID: 0
     */
    return ret;
  }

  private DefaultObjectiveCondition handleSelfDied(PropertiesSet properties)
  {
    /*
    System.out.println("Self died!");
    System.out.println(properties.dump());
    */
    /*
Self died!
QuestEvent_ID: 30 (SelfDied)
QuestEvent_Number: 1
QuestEvent_ShowBillboardText: 0
QuestEvent_ShowProgressText: 0
     */
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.SELF_DIED);
    return ret;
  }

  /*
  private List<DatPosition> getPositions(Integer did, String roleConstraint, int index)
  {
    List<DatPosition> ret=null;
    if (did!=null)
    {
      ContentLayerGeoData worldData=_geoData.getWorldGeoData();
      DidGeoData didGeoData=worldData.getGeoData(did.intValue());
      if (didGeoData!=null)
      {
        //System.out.println("\tPositions: "+didGeoData.getPositions());
        ret=didGeoData.getPositions();
      }
    }
    if ((ret==null) && (roleConstraint!=null))
    {
      int achievableId=_currentAchievable.getIdentifier();
      AchievableGeoData achievableGeoData=_geoData.getGeoDataForAchievable(achievableId,index);
      if (achievableGeoData!=null)
      {
        ret=achievableGeoData.getPositions(roleConstraint);
        //System.out.println("\tPositions for key: "+positions+" ("+roleConstraint+")");
      }
    }
    if (ret!=null)
    {
      //System.out.println("\tPositions: "+ret);
    }
    return ret;
  }
  */

  private ConditionTarget getTarget(Integer id)
  {
    ConditionTarget target=null;
    Proxy<NpcDescription> npcProxy=null;
    Proxy<MobDescription> mobProxy=null;
    int wstateClass=LoaderUtils.getWStateClass(_facade,id.intValue());
    // Mostly 1723 (mob) or 1724 (NPC)
    if (wstateClass==WStateClass.NPC)
    {
      int npcId=id.intValue();
      String npcName=NpcLoader.loadNPC(_facade,npcId);
      npcProxy=new Proxy<NpcDescription>();
      npcProxy.setId(npcId);
      npcProxy.setName(npcName);
    }
    else if (wstateClass==WStateClass.MOB)
    {
      int mobId=id.intValue();
      String mobName=_mobLoader.loadMob(mobId);
      mobProxy=new Proxy<MobDescription>();
      mobProxy.setId(mobId);
      mobProxy.setName(mobName);
    }
    else
    {
      LOGGER.warn("Unmanaged target element: "+id+". WStateClass="+wstateClass);
    }
    if ((npcProxy!=null) || (mobProxy!=null))
    {
      target=new ConditionTarget();
      target.setNpcProxy(npcProxy);
      target.setMobProxy(mobProxy);
    }
    return target;
  }

  private String concat(String base, String add)
  {
    if (base==null) return add;
    return base+"/"+add;
  }
}
