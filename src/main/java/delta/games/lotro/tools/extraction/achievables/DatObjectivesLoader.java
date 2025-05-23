package delta.games.lotro.tools.extraction.achievables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.Interactable;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MobDivision;
import delta.games.lotro.common.enums.QuestCategory;
import delta.games.lotro.common.enums.QuestScope;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.lore.agents.AgentDescription;
import delta.games.lotro.lore.agents.EntityClassification;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobsManager;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.geo.landmarks.LandmarkDescription;
import delta.games.lotro.lore.geo.landmarks.LandmarksManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.LandDivision;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.objectives.CompoundQuestEvent;
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
import delta.games.lotro.lore.quests.objectives.MobLocation;
import delta.games.lotro.lore.quests.objectives.MobSelection;
import delta.games.lotro.lore.quests.objectives.MonsterDiedCondition;
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
import delta.games.lotro.lore.utils.InteractableUtils;
import delta.games.lotro.tools.extraction.utils.MobUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quest/deed objectives from DAT files.
 * @author DAM
 */
public class DatObjectivesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DatObjectivesLoader.class);

  private static final String QUEST_EVENT_ITEM_DID="QuestEvent_ItemDID";
  private static final String QUEST_EVENT_ENTRY="QuestEvent_Entry";
  private static final String QUEST_EVENT_ROLE_CONSTRAINT="QuestEvent_RoleConstraint";

  private DataFacade _facade;
  private I18nUtils _i18n;

  private LotroEnum<MobDivision> _mobDivision;
  private EnumMapper _questEvent;
  private LotroEnum<QuestCategory> _questCategory;
  private LotroEnum<QuestScope> _questScope;

  @SuppressWarnings("unused")
  private GeoData _geoData;
  private Achievable _currentAchievable;

  private Map<Integer,Function<PropertiesSet,ObjectiveCondition>> _builders;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n support.
   */
  public DatObjectivesLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
    _mobDivision=LotroEnumsRegistry.getInstance().get(MobDivision.class);
    _questEvent=_facade.getEnumsManager().getEnumMapper(587202639);
    _questCategory=LotroEnumsRegistry.getInstance().get(QuestCategory.class);
    _questScope=LotroEnumsRegistry.getInstance().get(QuestScope.class);
    _geoData=QuestEventTargetLocationLoader.loadGeoData(facade);
    _builders=initConditionBuilders();
  }

  /**
   * Load quest/deed objectives from DAT files data.
   * @param objectivesManager Objectives manager.
   * @param achievable Parent achievable.
   * @param properties Quest/deed properties.
   */
  public void handleObjectives(ObjectivesManager objectivesManager, Achievable achievable, PropertiesSet properties)
  {
    _currentAchievable=achievable;
    handleMainFailureConditions(achievable,properties);
    Object[] objectivesArray=(Object[])properties.getProperty("Quest_ObjectiveArray");
    if (objectivesArray!=null)
    {
      // Can have several objectives (ordered)
      for(Object objectiveObj : objectivesArray)
      {
        PropertiesSet objectiveProps=(PropertiesSet)objectiveObj;

        Objective objective=new Objective();
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
        // Completion and failure conditions (only 1 max of each)
        ArrayPropertyValue completionConditionsArray=(ArrayPropertyValue)objectiveProps.getPropertyValueByName("Quest_CompletionConditionArray");
        if (completionConditionsArray!=null)
        {
          for(PropertyValue completionConditionValue : completionConditionsArray.getValues())
          {
            handleObjectiveCondition(objective,completionConditionValue);
          }
        }
        // Check completions count
        Integer completionsCount=objective.getCompletionConditionsCount();
        if (completionsCount!=null)
        {
          int nbConditions=objective.getConditions().size();
          if (nbConditions==completionsCount.intValue())
          {
            objective.setCompletionConditionsCount(null);
          }
        }
        // Ignored: Quest_NPCObjRoles (Array) and Quest_ObjectiveVolumeString (String)
        objectivesManager.addObjective(objective);
      }
    }
    objectivesManager.sort();
  }

  private void handleObjectiveCondition(Objective objective, PropertyValue item)
  {
    String propertyName=item.getDefinition().getName();
    if ("Quest_CompletionCondition".equals(propertyName))
    {
      ArrayPropertyValue completionConditionArray=(ArrayPropertyValue)item;
      handleCompletionConditions(objective,completionConditionArray);
    }
    else if ("Quest_FailureCondition".equals(propertyName))
    {
      ArrayPropertyValue failureConditionArray=(ArrayPropertyValue)item;
      handleFailureCondition(objective,failureConditionArray);
    }
    else
    {
      LOGGER.warn("Unmanaged element for a completion condition array: {}",item);
    }
  }

  private void handleCompletionConditions(Objective objective, ArrayPropertyValue completionConditionArray)
  {
    /*
268439142 - Quest_CompletionCondition, type=Array
  Property: QuestEvent_CompoundEvent, ID=268439626, type=Array
  Property: Quest_CompletionConditionCount, ID=268461297, type=Int
  Property: Quest_Condition_NeverFinish, ID=268437079, type=boolean
  Property: QuestEvent_Entry, ID=268439867, type=Struct
     */
    for(PropertyValue completionCondition : completionConditionArray.getValues())
    {
      String propertyName=completionCondition.getDefinition().getName();
      if ("Quest_CompletionConditionCount".equals(propertyName))
      {
        int conditionCount=((Integer)completionCondition.getValue()).intValue();
        objective.setCompletionConditionsCount(Integer.valueOf(conditionCount));
      }
      else if (QUEST_EVENT_ENTRY.equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)completionCondition.getValue();
        ObjectiveCondition condition=handleQuestEventEntry(questEventEntryProps);
        objective.addCondition(condition);
      }
      else if ("QuestEvent_CompoundEvent".equals(propertyName))
      {
        ArrayPropertyValue compoundEventArray=(ArrayPropertyValue)completionCondition;
        CompoundQuestEvent compoundEvent=handleCompoundEvent(compoundEventArray);
        objective.addCondition(compoundEvent);
      }
      else if ("Quest_Condition_NeverFinish".equals(propertyName))
      {
        @SuppressWarnings("unused")
        int neverFinish=((Integer)completionCondition.getValue()).intValue();
      }
      else
      {
        LOGGER.warn("Unmanaged element for a completion condition array: {}",completionCondition);
      }
    }
  }

  private CompoundQuestEvent handleCompoundEvent(ArrayPropertyValue compoundEventArray)
  {
    /*
268439626 - QuestEvent_CompoundEvent, type=Array
  Property: Accomplishment_LoreInfo, ID=268437995, type=String Info
  Property: QuestEvent_Entry, ID=268439867, type=Struct
  Property: QuestEvent_CompoundProgressOverride, ID=268439136, type=String Info
  Property: QuestEvent_Entry_Array, ID=268457470, type=Array
     */
    CompoundQuestEvent ret=new CompoundQuestEvent();
    for(PropertyValue compoundEventItem : compoundEventArray.getValues())
    {
      String propertyName=compoundEventItem.getDefinition().getName();
      if (QUEST_EVENT_ENTRY.equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)compoundEventItem.getValue();
        ObjectiveCondition event=handleQuestEventEntry(questEventEntryProps);
        ret.addQuestEvent(event);
      }
      else if ("QuestEvent_Entry_Array".equals(propertyName))
      {
        ArrayPropertyValue questEventArray=(ArrayPropertyValue)compoundEventItem;
        List<ObjectiveCondition> events=handleQuestEventEntryArray(questEventArray);
        for(ObjectiveCondition event : events)
        {
          ret.addQuestEvent(event);
        }
      }
      else if ("QuestEvent_CompoundProgressOverride".equals(propertyName))
      {
        String progressOverride=_i18n.getStringProperty(compoundEventItem,0);
        ret.setCompoundProgressOverride(progressOverride);
      }
    }
    return ret;
  }

  private List<ObjectiveCondition> handleQuestEventEntryArray(ArrayPropertyValue questEventArray)
  {
    List<ObjectiveCondition> ret=new ArrayList<ObjectiveCondition>();
    /*
268457470 - QuestEvent_Entry_Array, type=Array
  Property: QuestEvent_Entry, ID=268439867, type=Struct
     */
    for(PropertyValue questEventItem : questEventArray.getValues())
    {
      String propertyName=questEventItem.getDefinition().getName();
      if (QUEST_EVENT_ENTRY.equals(propertyName))
      {
        PropertiesSet questEventEntryProps=(PropertiesSet)questEventItem.getValue();
        ObjectiveCondition condition=handleQuestEventEntry(questEventEntryProps);
        ret.add(condition);
      }
    }
    return ret;
  }

  private void handleFailureCondition(Objective objective, ArrayPropertyValue failureConditionArray)
  {
    for(PropertyValue entry : failureConditionArray.getValues())
    {
      PropertiesSet questEventEntryProps=(PropertiesSet)entry.getValue();
      ObjectiveCondition condition=handleQuestEventEntry(questEventEntryProps);
      condition.setEventID(0);
      objective.addFailureCondition(condition);
    }
  }

  private ObjectiveCondition handleQuestEventEntry(PropertiesSet properties)
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
    // ID
    int questEventId=((Integer)properties.getProperty("QuestEvent_ID")).intValue();
    // Billboard
    boolean showBillboardText=true;
    Integer showBillboardTextInt=(Integer)properties.getProperty("QuestEvent_ShowBillboardText");
    if ((showBillboardTextInt!=null) && (showBillboardTextInt.intValue()==0))
    {
      showBillboardText=false;
    }
    // Billboard override
    @SuppressWarnings("unused")
    String billboardProgressOverride=_i18n.getStringProperty(properties,"QuestEvent_BillboardProgressOverride");
    // Progress
    boolean showProgressText=true;
    Integer showProgressTextInt=(Integer)properties.getProperty("QuestEvent_ShowProgressText");
    if ((showProgressTextInt!=null) && (showProgressTextInt.intValue()==0))
    {
      showProgressText=false;
    }
    // Progress override
    String progressOverride=_i18n.getStringProperty(properties,"QuestEvent_ProgressOverride");
    // Role constraint
    @SuppressWarnings("unused")
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);
    // Lore info
    String loreInfo=_i18n.getStringProperty(properties,"Accomplishment_LoreInfo");
    // Count
    Integer count=(Integer)properties.getProperty("QuestEvent_Number");

    ObjectiveCondition condition=buildCondition(questEventId,properties);
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
    // Order
    Integer eventOrder=(Integer)properties.getProperty("QuestEvent_EventOrder");
    if (eventOrder!=null)
    {
      condition.setIndex(eventOrder.intValue());
    }
    return condition;
  }

  private Map<Integer,Function<PropertiesSet,ObjectiveCondition>> initConditionBuilders()
  {
    Map<Integer,Function<PropertiesSet,ObjectiveCondition>> map=new HashMap<>();
    map.put(Integer.valueOf(1),this::handleEnterDetection);
    map.put(Integer.valueOf(5),this::handleNpcUsed);
    map.put(Integer.valueOf(7),this::handleItemUsed);
    map.put(Integer.valueOf(9),this::handleDetecting);
    map.put(Integer.valueOf(10),this::handleExternalInventoryItemUsed);
    map.put(Integer.valueOf(11),this::handleNpcTalk);
    map.put(Integer.valueOf(13),this::handleChanneling);
    map.put(Integer.valueOf(14),this::handleTimeExpired);
    map.put(Integer.valueOf(16),this::handleItemTalk);
    map.put(Integer.valueOf(18),this::handleLevelCondition);
    map.put(Integer.valueOf(19),this::handleClearCamp);
    map.put(Integer.valueOf(20),this::handleChannelingFailed);
    map.put(Integer.valueOf(21),this::handleLandmarkDetection);
    map.put(Integer.valueOf(22),this::handleMonsterDieCondition);
    map.put(Integer.valueOf(24),this::handleEmoteCondition);
    return map;
  }

  private ObjectiveCondition buildCondition(int questEventId, PropertiesSet properties)
  {
    Function<PropertiesSet,ObjectiveCondition> builder=_builders.get(Integer.valueOf(questEventId));
    if (builder!=null)
    {
      return builder.apply(properties);
    }
    ObjectiveCondition condition=null;
    ConditionType type=null;
    if (questEventId==24)
    {
      condition=handleEmoteCondition(properties);
    }
    else if (questEventId==25)
    {
      type=ConditionType.PLAYER_DIED;
      handlePlayerDied();
    }
    else if (questEventId==26)
    {
      condition=handleSkillUsed(properties);
    }
    else if (questEventId==27)
    {
      type=ConditionType.KUNG_FU;
      handleKungFu();
    }
    else if (questEventId==30)
    {
      condition=handleSelfDied();
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
      condition=handleCraftRecipeExecution();
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
      condition=handleDismounted();
    }
    else if (questEventId==45)
    {
      condition=handleFactionLevel(properties);
    }
    else if (questEventId==46)
    {
      condition=handleTeleported();
    }
    else if (questEventId==59)
    {
      condition=handleQuestBestowed(properties);
    }
    else if (questEventId==2) type=ConditionType.LEAVE_DETECTION;
    else if (questEventId==4) type=ConditionType.MONSTER_PLAYER_DIED;
    else if (questEventId==6) type=ConditionType.SKILL_APPLIED;
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
      LOGGER.warn("Unmanaged quest event: ID={}, meaning={}",Integer.valueOf(questEventId),eventMeaning);
    }

    if (condition==null)
    {
      condition=new DefaultObjectiveCondition(type);
    }
    return condition;
  }

  private DetectingCondition handleDetecting(PropertiesSet properties)
  {
    DetectingCondition ret=new DetectingCondition();
    handleDetectionCondition(ret,properties);
    return ret;
  }

  private EnterDetectionCondition handleEnterDetection(PropertiesSet properties)
  {
    EnterDetectionCondition ret=new EnterDetectionCondition();
    handleDetectionCondition(ret,properties);
    return ret;
  }

  private void handleDetectionCondition(DetectionCondition condition, PropertiesSet properties)
  {
    ConditionTarget target=null;
    Integer detect=(Integer)properties.getProperty("QuestEvent_Detect");
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);
    if (detect!=null)
    {
      target=getTarget(detect);
    }
    else if (roleConstraint!=null)
    {
      // Nothing!
    }
    else
    {
      LOGGER.warn("Detect condition: No detect and no role constraint");
    }
    condition.setTarget(target);
  }

  private ObjectiveCondition handleKungFu()
  {
    // QuestEvent_RunKungFu is 0 or absent...
    // QuestEvent_RoleConstraint: always set
    return new DefaultObjectiveCondition(ConditionType.KUNG_FU);
  }

  private ItemUsedCondition handleItemUsed(PropertiesSet properties)
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
    Integer itemId=(Integer)properties.getProperty(QUEST_EVENT_ITEM_DID);
    @SuppressWarnings("unused")
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);

    ItemUsedCondition ret=new ItemUsedCondition();
    fillItemCondition(ret,itemId);
    return ret;
  }

  private ExternalInventoryItemCondition handleExternalInventoryItemUsed(PropertiesSet properties)
  {
    Integer itemId=(Integer)properties.getProperty(QUEST_EVENT_ITEM_DID);
    @SuppressWarnings("unused")
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);

    ExternalInventoryItemCondition ret=new ExternalInventoryItemCondition();
    fillItemCondition(ret,itemId);
    return ret;
  }

  private ItemTalkCondition handleItemTalk(PropertiesSet properties)
  {
    Integer itemId=(Integer)properties.getProperty(QUEST_EVENT_ITEM_DID);
    @SuppressWarnings("unused")
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);

    ItemTalkCondition ret=new ItemTalkCondition();
    fillItemCondition(ret,itemId);
    return ret;
  }

  private void fillItemCondition(ItemCondition ret, Integer itemId)
  {
    if (itemId!=null)
    {
      Item item=ItemsManager.getInstance().getItem(itemId.intValue());
      ret.setItem(item);
    }
  }

  private NpcTalkCondition handleNpcTalk(PropertiesSet properties)
  {
    NpcTalkCondition ret=new NpcTalkCondition();
    handleNpcCondition(ret,properties);
    return ret;
  }

  private NpcUsedCondition handleNpcUsed(PropertiesSet properties)
  {
    NpcUsedCondition ret=new NpcUsedCondition();
    handleNpcCondition(ret,properties);
    return ret;
  }

  private void handleNpcCondition(NpcCondition condition, PropertiesSet properties)
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
      Interactable npc=InteractableUtils.findInteractable(npcId.intValue());
      condition.setNpc(npc);
    }
    @SuppressWarnings("unused")
    String roleConstraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);
  }

  private LevelCondition handleLevelCondition(PropertiesSet properties)
  {
    LevelCondition ret=new LevelCondition();
    int level=((Integer)properties.getProperty("QuestEvent_PlayerLevel")).intValue();
    ret.setLevel(level);
    return ret;
  }

  private LandmarkDetectionCondition handleLandmarkDetection(PropertiesSet properties)
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
      LandmarkDescription landmark=LandmarksManager.getInstance().getLandmarkById(landmarkId.intValue());
      ret.setLandmark(landmark);
    }
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
        MobDivision mobDivision=null;
        Integer mobDivisionCode=(Integer)monsterGenusProps.getProperty("Quest_MonsterDivision");
        if (mobDivisionCode!=null)
        {
          mobDivision=_mobDivision.getEntry(mobDivisionCode.intValue());
        }
        LandDivision landDivision=null;
        Integer regionId=(Integer)monsterGenusProps.getProperty("Quest_MonsterRegion");
        if (regionId!=null)
        {
          landDivision=GeoAreasManager.getInstance().getLandById(regionId.intValue());
        }
        LandmarkDescription landmark=null;
        Integer landmarkId=(Integer)monsterGenusProps.getProperty("QuestEvent_LandmarkDID");
        if (landmarkId!=null)
        {
          landmark=LandmarksManager.getInstance().getLandmarkById(landmarkId.intValue());
        }
        // What
        EntityClassification mobReference=MobUtils.buildMobReference(monsterGenusProps);
        MobSelection selection=new MobSelection();
        MobLocation location=new MobLocation(mobDivision,landDivision,landmark);
        selection.setWhere(location);
        selection.setWhat(mobReference);
        ret.getMobSelections().add(selection);
      }
    }
    else
    {
      Integer mobId=(Integer)properties.getProperty("QuestEvent_MonsterDID");
      if (mobId!=null)
      {
        MobDescription mob=MobsManager.getInstance().getMobById(mobId.intValue());
        ret.setMob(mob);
      }
    }
    return ret;
  }

  private EmoteCondition handleEmoteCondition(PropertiesSet properties)
  {
    EmoteCondition ret=new EmoteCondition();
    // Emote ID
    Integer emoteId=(Integer)properties.getProperty("QuestEvent_EmoteDID");
    EmoteDescription emote=EmotesManager.getInstance().getEmote(emoteId.intValue());
    if (emote!=null)
    {
      ret.setEmote(emote);
    }
    else
    {
      LOGGER.warn("Emote not found: {}",emoteId);
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

  private void handlePlayerDied()
  {
    /*
     * QuestEvent_MonsterGenus_Array: always
     *    - Quest_MonsterClass: see Enum: CharacterClassType, (id=587202574). Ex: 162=Hunter
     *    - Quest_MonsterGenus (bitset for Enum: GenusType, (id=587202570)) and Quest_MonsterSpecies (see Enum: Species, (id=587202571)).
     *      Ex: 16384=2^(15-1) 15=Dwarf and 73 (Dwarves).
     * QuestEvent_TerritoryDID: Zone ID (Eregion, Forochel, Angmar)
     * QuestEvent_Number
     */
  }

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
    // See also: QuestEvent_SkillQuestFlags
    Integer skillId=(Integer)properties.getProperty("QuestEvent_SkillDID");
    if (skillId!=null)
    {
      SkillsManager skillsMgr=SkillsManager.getInstance();
      SkillDescription skill=skillsMgr.getSkill(skillId.intValue());
      if (skill!=null)
      {
        ret.setSkill(skill);
      }
      else
      {
        LOGGER.warn("Skill not found: {}",skillId);
      }
    }
    Integer dailyMaxIncrement=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    if (dailyMaxIncrement!=null)
    {
      ret.setMaxPerDay(dailyMaxIncrement);
    }
    // Use QuestEvent_Skill_AttackResultArray for attack result types
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

    Integer itemId=(Integer)properties.getProperty(QUEST_EVENT_ITEM_DID);
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

    Integer scopeCode=(Integer)properties.getProperty("QuestEvent_QuestCompleteScope");
    if (scopeCode!=null)
    {
      QuestScope scope=_questScope.getEntry(scopeCode.intValue());
      ret.setQuestScope(scope);
    }

    Integer questId=(Integer)properties.getProperty("QuestEvent_QuestComplete");
    if (questId!=null)
    {
      // Check if deed or quest
      PropertiesSet questProps=_facade.loadProperties(questId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      if (questProps!=null)
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
        QuestCategory questCategory=_questCategory.getEntry(questCategoryCode.intValue());
        ret.setQuestCategory(questCategory);
      }
      else
      {
        LOGGER.warn("No ID and no category for QuestCompleteCondition in: {}",_currentAchievable);
        // May be use: QuestEvent_AccomplishmentCompleteCategory?
      }
    }
    return ret;
  }

  private void handleWorldEventCondition(PropertiesSet properties)
  {
    /*
     * QuestEvent_WorldEvent_Value: always. Integer 1, rarely 4 or 7.
     * QuestEvent_RoleConstraint: string. 4 uses: 2 for draigoch, 2 for skirmishes (goblin's pot in 21st hall, arrows in defence of bruinen)
     * QuestEvent_WorldEvent: ID (relates to world event, such as festivals)
     * QuestEvent_WorldEvent_Operator: always. Integer 3 => "EqualTo".
     * QuestEvent_Number: almost always. Integer 1 if set.
     */
    Integer value=(Integer)properties.getProperty("QuestEvent_WorldEvent_Value");
    Integer id=(Integer)properties.getProperty("QuestEvent_WorldEvent");
    Integer operator=(Integer)properties.getProperty("QuestEvent_WorldEvent_Operator");
    String constraint=(String)properties.getProperty(QUEST_EVENT_ROLE_CONSTRAINT);
    PropertiesSet worldEventProps=_facade.loadProperties(id.intValue()+DATConstants.DBPROPERTIES_OFFSET);
    int propId=((Integer)worldEventProps.getProperty("WorldEvent_WorldPropertyName")).intValue();
    PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(propId);
    String propName=(propDef!=null)?propDef.getName():"?";
    LOGGER.debug("World event: ID={}, name={}, operator={}, value={}, constraint={}",id,propName,operator,value,constraint);
  }

  private HobbyCondition handleHobbyItem(PropertiesSet properties)
  {
    /*
     * QuestEvent_HobbyDID: always. 51 times the same hobby ID (the only one: 1879109150).
     * QuestEvent_ItemDID: always. Item ID (probably all fishes).
     * QuestEvent_Number: set 40 times. Mostly Integer: 1, sometimes 10.
     */
    @SuppressWarnings("unused")
    int hobbyId=((Integer)properties.getProperty("QuestEvent_HobbyDID")).intValue();
    Integer itemId=(Integer)properties.getProperty(QUEST_EVENT_ITEM_DID);
    HobbyCondition ret=new HobbyCondition();
    Item item=ItemsManager.getInstance().getItem(itemId.intValue());
    ret.setItem(item);
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
    Faction faction=FactionsRegistry.getInstance().getById(factionId);
    ret.setFaction(faction);
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

  private TimeExpiredCondition handleTimeExpired(PropertiesSet properties)
  {
    Integer timeLimit=(Integer)properties.getProperty("QuestEvent_TimeLimit");
    int duration=(timeLimit!=null)?timeLimit.intValue():0;
    TimeExpiredCondition ret=new TimeExpiredCondition();
    ret.setDuration(duration);
    return ret;
  }

  private DefaultObjectiveCondition handleCraftRecipeExecution()
  {
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

  private DefaultObjectiveCondition handleDismounted()
  {
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.DISMOUNTED);
    return ret;
  }

  private DefaultObjectiveCondition handleTeleported()
  {
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.TELEPORTED);
    return ret;
  }

  private DefaultObjectiveCondition handleChanneling(PropertiesSet properties)
  {
    /*
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
    DefaultObjectiveCondition ret=new DefaultObjectiveCondition(ConditionType.CHANNELING_FAILED);
    return ret;
  }

  private DefaultObjectiveCondition handleClearCamp(PropertiesSet properties)
  {
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

  private DefaultObjectiveCondition handleSelfDied()
  {
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

  private ConditionTarget getTarget(Integer id)
  {
    ConditionTarget target=null;
    AgentDescription agent=null;
    int wstateClass=LoaderUtils.getWStateClass(_facade,id.intValue());
    // Mostly 1723 (mob) or 1724 (NPC)
    if (wstateClass==WStateClass.NPC)
    {
      int npcId=id.intValue();
      agent=NPCsManager.getInstance().getNPCById(npcId);
    }
    else if (wstateClass==WStateClass.MOB)
    {
      int mobId=id.intValue();
      agent=MobsManager.getInstance().getMobById(mobId);
    }
    else
    {
      LOGGER.warn("Unmanaged target element: {}. WStateClass={}",id,Integer.valueOf(wstateClass));
    }
    if (agent!=null)
    {
      target=new ConditionTarget();
      target.setAgent(agent);
    }
    return target;
  }

  private void handleMainFailureConditions(Achievable achievable, PropertiesSet props)
  {
    Object[] conditionsArray=(Object[])props.getProperty("Quest_FailureConditionArray");
    if (conditionsArray==null)
    {
      return;
    }
    ObjectivesManager objectivesMgr=achievable.getObjectives();
    for(Object conditionObj : conditionsArray)
    {
      Object[] conditionEntryArray=(Object[])conditionObj;
      for(Object conditionEntry : conditionEntryArray)
      {
        PropertiesSet failureConditionProps=(PropertiesSet)conditionEntry;
        ObjectiveCondition failureCondition=handleQuestEventEntry(failureConditionProps);
        failureCondition.setEventID(0);
        objectivesMgr.addFailureCondition(failureCondition);
      }
    }
  }
}
