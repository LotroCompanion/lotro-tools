package delta.games.lotro.tools.dat.quests;

import java.util.HashMap;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.PlaceLoader;

/**
 * Loader for quest/deed objectives from DAT files.
 * @author DAM
 */
public class DatObjectivesLoader
{
  private DataFacade _facade;

  private EnumMapper _monsterDivision;
  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  HashMap<Integer,IntegerHolder> eventIds=new HashMap<Integer,IntegerHolder>();

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DatObjectivesLoader(DataFacade facade)
  {
    _facade=facade;
    _monsterDivision=_facade.getEnumsManager().getEnumMapper(587202657);
    _genus=_facade.getEnumsManager().getEnumMapper(587202570);
    _species=_facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=_facade.getEnumsManager().getEnumMapper(587202572);
  }

  /**
   * Load quest/deed objectives from DAT files data.
   * @param properties Quest/deed properties.
   */
  public void handleObjectives(PropertiesSet properties)
  {
    Object[] objectivesArray=(Object[])properties.getProperty("Quest_ObjectiveArray");
    if (objectivesArray!=null)
    {
      // Can have several objectives (ordered)
      for(Object objectiveObj : objectivesArray)
      {
        PropertiesSet objectiveProps=(PropertiesSet)objectiveObj;
        //System.out.println(objectiveProps.dump());
        // Index
        Integer objectiveIndex=(Integer)objectiveProps.getProperty("Quest_ObjectiveIndex");
        System.out.println("Objective #"+objectiveIndex);
        // Description
        String description=DatUtils.getFullStringProperty(objectiveProps,"Quest_ObjectiveDescription","{***}");
        System.out.println("\tDescription: "+description);
        // Conditions (can have several conditions)
        Object[] completionConditionsArray=(Object[])objectiveProps.getProperty("Quest_CompletionConditionArray");
        if (completionConditionsArray!=null)
        {
          for(Object item : completionConditionsArray)
          {
            handleObjectiveItem(item);
          }
        }
      }
    }
  }

  private void handleObjectiveItem(Object item)
  {
    if (item instanceof Object[])
    {
      for(Object childItem : (Object[])item)
      {
        handleObjectiveItem(childItem);
      }
    }
    else if (item instanceof PropertiesSet)
    {
      handleCompletionCondition((PropertiesSet)item);
    }
  }

  private void handleCompletionCondition(PropertiesSet properties)
  {
    Integer questEventId=(Integer)properties.getProperty("QuestEvent_ID");
    IntegerHolder holder=eventIds.get(questEventId);
    if (holder==null)
    {
      holder=new IntegerHolder();
      eventIds.put(questEventId,holder);
    }
    holder.increment();

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
    System.out.println("\tEvent #"+eventOrder);
    // ID
    Integer eventId=(Integer)properties.getProperty("QuestEvent_ID");
    System.out.println("\t\tEvent ID: "+eventId);
    // Billboard
    Integer showBillboardText=(Integer)properties.getProperty("QuestEvent_ShowBillboardText");
    if ((showBillboardText!=null) && (showBillboardText.intValue()!=0))
    {
      System.out.println("\t\tShow billboard text: "+showBillboardText);
    }
    String billboardProgressOverride=DatUtils.getFullStringProperty(properties,"QuestEvent_BillboardProgressOverride",Markers.CHARACTER);
    if (billboardProgressOverride!=null)
    {
      System.out.println("\t\tBillboard progress override: "+billboardProgressOverride);
    }
    // Progress override
    String progressOverride=DatUtils.getFullStringProperty(properties,"QuestEvent_ProgressOverride",Markers.CHARACTER);
    if (progressOverride!=null)
    {
      System.out.println("\t\tProgress override: "+progressOverride);
    }
    // Role constraint
    String roleConstraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
    if (roleConstraint!=null)
    {
      System.out.println("\t\tRole constraint: "+roleConstraint);
    }
    // Lore info
    String loreInfo=DatUtils.getStringProperty(properties,"Accomplishment_LoreInfo");
    if (loreInfo!=null)
    {
      System.out.println("\t\tLore info: "+loreInfo);
    }

    // Deeds:
    // QuestEvent_ID: {32=3936, 22=1142, 1=889, 21=869, 26=611, 31=487, 45=411, 7=349, 25=116,
    // 34=108, 11=82, 39=51, 4=41, 18=29, 24=22, 16=20, 6=15, 10=2, 58=2, 59=1}
    // Quests:
    // QuestEvent_ID:  {11=11545, 7=3845, 22=3164, 1=2970, 32=1967, 34=1599, 10=1162, 31=831, 5=640, 21=425,
    // 24=301, 14=293, 19=210, 27=196, 13=187, 56=17320=144, 16=123, 37=97, 29=97, 33=72, 2=58, 59=50, 25=46,
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

    if (questEventId.intValue()==1)
    {
      handleEnterDetection(properties);
    }
    else if (questEventId.intValue()==7)
    {
      handleItemUsed(properties);
    }
    else if (questEventId.intValue()==11)
    {
      handleNpcTalk(properties);
    }
    else if (questEventId.intValue()==21)
    {
      handleLandmarkDetection(properties);
    }
    else if (questEventId.intValue()==22)
    {
      handleMonsterDieCondition(properties);
    }
    else if (questEventId.intValue()==24)
    {
      handleEmoteCondition(properties);
    }
    else if (questEventId.intValue()==25)
    {
      handlePlayerDied(properties);
    }
    else if (questEventId.intValue()==26)
    {
      handleSkillUsed(properties);
    }
    else if (questEventId.intValue()==31)
    {
      handleInventoryItem(properties);
    }
    else if (questEventId.intValue()==32)
    {
      handleQuestComplete(properties);
    }
    else if (questEventId.intValue()==34)
    {
      handleWorldEventCondition(properties);
    }
    else if (questEventId.intValue()==39)
    {
      handleHobbyItem(properties);
    }
    else if (questEventId.intValue()==45)
    {
      handleFactionLevel(properties);
    }
  }

  private void handleEnterDetection(PropertiesSet properties)
  {
    /*
     * QuestEvent_Detect: ID of NPC?, 20 times.
     * QuestEvent_HideRadarIcon: optional, 5 times, always 1.
     * QuestEvent_DramaProgressOverride: String[], 40 times, mainly for session play quests (chicken or horse)
     * QuestEvent_RoleConstraint seems to give the string ID of the thing to detect.
     * QuestEvent_ProgressOverride gives a useable text.
     */
  }

  private void handleItemUsed(PropertiesSet properties)
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
    //Integer itemId=(Integer)properties.getProperty("QuestEvent_ItemDID");
    //String constraint=(String)properties.getProperty("QuestEvent_RoleConstraint");
  }

  private void handleNpcTalk(PropertiesSet properties)
  {
    /*
     * QuestEvent_RoleConstraint: used 6 times for limlight spirits.
     * QuestEvent_NPCTalk: almost always (76/82). NPC ID?
     * Quest_Role: gives some text, sound ID... for the NPC... (50/82)
     * QuestEvent_CanTeleportToObjective: optional, used 12 times: Integer 0.
     * QuestEvent_Number: always 1.
     */
  }

  private void handleLandmarkDetection(PropertiesSet properties)
  {
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
      System.out.println("Landmark detection: ID="+landmarkId+", name="+landmarkName);
    }
  }

  private void handleMonsterDieCondition(PropertiesSet properties)
  {
    /*
QuestEvent_MonsterGenus_Array: 
  #1: 
    Quest_MonsterRegion: 1879049792
    Quest_MonsterSpecies: 41
QuestEvent_Number: 1
QuestEvent_ShowBillboardText: 0
     */

    /*
QuestEvent_MonsterGenus_Array: 
  #1: 
    Quest_MonsterRegion: 1879049792
    Quest_MonsterSpecies: 57
     */
    String typesRegions="";
    Object[] monsterGenusArray=(Object[])properties.getProperty("QuestEvent_MonsterGenus_Array");
    if (monsterGenusArray!=null)
    {
      int nbMonsterGenus=monsterGenusArray.length;
      for(int i=0;i<nbMonsterGenus;i++)
      {
        PropertiesSet mobRegionProps=(PropertiesSet)monsterGenusArray[i];
        // Where
        
        Integer regionId=(Integer)mobRegionProps.getProperty("Quest_MonsterRegion");
        Integer mobDivision=(Integer)mobRegionProps.getProperty("Quest_MonsterDivision");
        Integer landmarkId=(Integer)mobRegionProps.getProperty("QuestEvent_LandmarkDID");
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
        Integer speciesId=(Integer)mobRegionProps.getProperty("Quest_MonsterSpecies");
        Integer subSpeciesId=(Integer)mobRegionProps.getProperty("Quest_MonsterSubspecies");
        Integer genusId=(Integer)mobRegionProps.getProperty("Quest_MonsterGenus");
        Integer mobId=(Integer)mobRegionProps.getProperty("QuestEvent_MonsterDID");
        String mobType=null;
        if (subSpeciesId!=null)
        {
          String subSpeciesStr=_subSpecies.getString(subSpeciesId.intValue());
          mobType=concat(mobType,"Subspecies:"+subSpeciesStr);
        }
        if (speciesId!=null)
        {
          String speciesStr=_species.getString(speciesId.intValue());
          mobType=concat(mobType,"Species:"+speciesStr);
        }
        if (genusId!=null)
        {
          String genusStr=_genus.getString(genusId.intValue());
          mobType=concat(mobType,"Genus:"+genusStr);
        }
        if (mobId!=null)
        {
          mobType=concat(mobType,"Mob:"+mobId);
        }
        typesRegions=mobType+((where!=null)?" in "+where:"");
      }
    }
    else
    {
      Integer mobId=(Integer)properties.getProperty("QuestEvent_MonsterDID");
      if (mobId!=null)
      {
        String mobType="Mob:"+mobId;
        typesRegions=mobType;
      }
    }
    Integer nbTimes=(Integer)properties.getProperty("QuestEvent_Number");
    String loreInfo=DatUtils.getStringProperty(properties,"Accomplishment_LoreInfo");
    String progressOverride=DatUtils.getStringProperty(properties,"QuestEvent_ProgressOverride");
    String text="*** Kill "+((nbTimes!=null)?nbTimes:"")+" monster(s):"+typesRegions;
    System.out.println(text);
    if (loreInfo!=null)
    {
      System.out.println(loreInfo);
    }
    if ((progressOverride!=null) && (!progressOverride.equals(loreInfo)))
    {
      System.out.println(progressOverride);
    }
  }

  private void handleEmoteCondition(PropertiesSet properties)
  {
    int emoteId=((Integer)properties.getProperty("QuestEvent_EmoteDID")).intValue();
    Integer nbTimesInt=(Integer)properties.getProperty("QuestEvent_Number");
    Integer maxTimesPerDay=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    String loreInfo=DatUtils.getStringProperty(properties,"Accomplishment_LoreInfo");
    String progressOverride=DatUtils.getStringProperty(properties,"QuestEvent_ProgressOverride");
    String text="Perform emote "+emoteId;
    int nbTimes=(nbTimesInt!=null)?nbTimesInt.intValue():1;
    if (nbTimes>1) text=text+" "+nbTimes+" times";
    if (maxTimesPerDay!=null)
    {
      text=text+" (max "+maxTimesPerDay+" times/day)";
    }
    System.out.println(text);
    if (loreInfo!=null)
    {
      System.out.println(loreInfo);
    }
    if ((progressOverride!=null) && (!progressOverride.equals(loreInfo)))
    {
      System.out.println(progressOverride);
    }
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

  private void handleSkillUsed(PropertiesSet properties)
  {
    /*
    // QuestEvent_Number: number of time to use the skill
    // QuestEvent_DailyMaximumIncrements: used often ~400/~600. Max number of skill usages counted by day
    //     QuestEvent_SkillDID: Skill ID (not mandatory)
    // or: QuestEvent_SkillQuestFlags: bitset: skill(s) indicator? Ex: 70368744177664=2^(47-1)
    // QuestEvent_Skill_AttackResultArray: always, array of Integer (see Enum: CombatResultType, (id=587202602))
    //   9: Hit, 10: CriticalHit, 11: SuperCriticalHit
    */
  }

  private void handleInventoryItem(PropertiesSet properties)
  {
    /*
     * QuestEvent_ForceCheckContentLayer: optional, used once: Integer 1
     * QuestEvent_QuestComplete_SuppressQuestCountUpdate: optional, used once: Integer 1
     * QuestEvent_ItemDID: always, item ID
     * QuestEvent_Number: number of items to get.
     * QuestEvent_DestroyInventoryItems: optional, always 1 when present (398/488).
     *     Indicates if the item is destroyed when acquired or not.
     */
  }

  private void handleQuestComplete(PropertiesSet properties)
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

    Integer questId=(Integer)properties.getProperty("QuestEvent_QuestComplete");
    if (questId!=null)
    {
      // Check if deed or quest
      System.out.println("\tChild deed/quest: "+questId);
    }
    Integer deedCategory=(Integer)properties.getProperty("QuestEvent_AccomplishmentCompleteCategory");
    if (deedCategory!=null)
    {
      System.out.println("Deed category: "+deedCategory);
    }
    Integer dailyMax=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    if (dailyMax!=null)
    {
      System.out.println("dailyMax: "+dailyMax); // -1?
    }
    // QuestEvent_RoleConstraint
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
  }

  private void handleHobbyItem(PropertiesSet properties)
  {
    //propNames.addAll(properties.getPropertyNames());
    //System.out.println("Condition properties: "+properties.dump());
    /*
     * QuestEvent_HobbyDID: always. 51 times the same hobby ID (the only one: 1879109150).
     * QuestEvent_ItemDID: always. Item ID (probably all fishes).
     * QuestEvent_Number: set 40 times. Mostly Integer: 1, sometimes 10.
     */
  }

  private void handleFactionLevel(PropertiesSet properties)
  {
    /*
     * Condition validated when the given faction tier is reached.
     * QuestEvent_FactionDID: always. Faction ID.
     * QuestEvent_ReputationTier: always. Faction tier: 1->7
     */
  }

  private String concat(String base, String add)
  {
    if (base==null) return add;
    return base+"/"+add;
  }
}
