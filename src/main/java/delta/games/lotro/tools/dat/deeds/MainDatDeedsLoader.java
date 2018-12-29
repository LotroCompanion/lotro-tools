package delta.games.lotro.tools.dat.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.PlaceLoader;
import delta.games.lotro.tools.lore.deeds.DeedsWriter;

/**
 * Get deeds definitions from DAT files.
 * @author DAM
 */
public class MainDatDeedsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatDeedsLoader.class);

  private DataFacade _facade;
  private List<DeedDescription> _deeds;
  private EnumMapper _category;
  private EnumMapper _uiTab;
  private EnumMapper _billingGroup;

  private EnumMapper _monsterDivision;
  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDeedsLoader(DataFacade facade)
  {
    _facade=facade;
    _deeds=new ArrayList<DeedDescription>();
    _category=_facade.getEnumsManager().getEnumMapper(587202587);
    _uiTab=_facade.getEnumsManager().getEnumMapper(587202588);
    _billingGroup=_facade.getEnumsManager().getEnumMapper(587202756);
    _monsterDivision=_facade.getEnumsManager().getEnumMapper(587202657);
    _genus=_facade.getEnumsManager().getEnumMapper(587202570);
    _species=_facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=_facade.getEnumsManager().getEnumMapper(587202572);
  }

  private int nb=0;
  private DeedDescription load(int indexDataId)
  {
    DeedDescription deed=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      if (indexDataId==1879048666)
      {
        System.out.println("************* "+indexDataId+" *****************");
        System.out.println(properties.dump());
      }
      deed=new DeedDescription();
      // ID
      deed.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Quest_Name");
      deed.setName(name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Quest_Description");
      deed.setDescription(description);
      // Check
      boolean useIt=useIt(properties);
      if (!useIt)
      {
        //System.out.println("Ignored ID="+indexDataId+", name="+name);
        return null;
      }
      System.out.println("ID: "+indexDataId+", name: "+name);
      // Category
      Integer categoryId=((Integer)properties.getProperty("Accomplishment_Category"));
      if (categoryId!=null)
      {
        String category=_category.getString(categoryId.intValue());
        deed.setCategory(category);
      }
      // UI Tab
      Integer uiTab=((Integer)properties.getProperty("Accomplishment_UITab"));
      String uiTabName=_uiTab.getString(uiTab.intValue());
      System.out.println("UI tab: "+uiTabName);
      // Deed type
      handleDeedType(deed,properties);
      // Min level
      Integer minLevel=((Integer)properties.getProperty("Quest_ChallengeLevel"));
      deed.setMinLevel(minLevel);
      //Accomplishment_MinLevelToStart: 13
      //Quest_ChallengeLevel: 13

      // Rewards
      Integer treasureId=((Integer)properties.getProperty("Quest_QuestTreasureDID"));
      if (treasureId!=null)
      {
        getRewards(treasureId.intValue());
      }
      // Faction
      getFaction(properties);
      // LOTRO points
      Integer tp=getTurbinePoints(properties);
      if (tp!=null)
      {
        System.out.println("TP: "+tp);
      }
      // Children
      getChildDeeds(properties);
      // Web Store (needed xpack/region): WebStoreAccountItem_DataID
      nb++;
      _deeds.add(deed);
    }
    else
    {
      LOGGER.warn("Could not handle deed ID="+indexDataId);
    }
    return deed;
  }

  private boolean useIt(PropertiesSet properties)
  {
    Object isAccomplishment=properties.getProperty("Quest_IsAccomplishment");
    if (isAccomplishment==null) return false;
    if (!(isAccomplishment instanceof Integer)) return false;
    if (((Integer)isAccomplishment).intValue()!=1) return false;
    return true;
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
      else if (typeCode==13)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.MAN);
      }
      else if (typeCode==21)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.ELF);
      }
      else if (typeCode==27)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.DWARF);
      }
      else if (typeCode==29)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.HOBBIT);
      }
      else if (typeCode==37)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.BEORNING);
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
        System.out.println("Unmanaged type: "+typeCode);
      }
    }
    deed.setType(type);
  }

  private void getRewards(int questTreasureId)
  {
    PropertiesSet props=_facade.loadProperties(questTreasureId+0x9000000);

    // Items
    Object[] itemArray=(Object[])props.getProperty("QuestTreasure_FixedItemArray");
    if (itemArray!=null)
    {
      for(Object itemObj : itemArray)
      {
        PropertiesSet itemProps=(PropertiesSet)itemObj;
        int itemId=((Integer)itemProps.getProperty("QuestTreasure_Item")).intValue();
        Integer quantity=(Integer)itemProps.getProperty("QuestTreasure_ItemQuantity");
        System.out.println("Item: "+itemId+", quantity: "+quantity);
      }
    }
    // Virtues
    Object[] virtueArray=(Object[])props.getProperty("QuestTreasure_FixedVirtueArray");
    if (virtueArray!=null)
    {
      for(Object virtueObj : virtueArray)
      {
        PropertiesSet virtueProps=(PropertiesSet)virtueObj;
        int virtueId=((Integer)virtueProps.getProperty("QuestTreasure_Virtue")).intValue();
        Integer increment=(Integer)virtueProps.getProperty("QuestTreasure_Virtue_Increment");
        System.out.println("Virtue: "+virtueId+", quantity: "+increment);
      }
    }
    // Titles
    Object[] titleArray=(Object[])props.getProperty("QuestTreasure_FixedTitleArray");
    if (titleArray!=null)
    {
      for(Object titleObj : titleArray)
      {
        int titleId=((Integer)titleObj).intValue();
        System.out.println("Title: "+titleId);
      }
    }
    // Emote
    Object[] emoteArray=(Object[])props.getProperty("QuestTreasure_FixedEmoteArray");
    if (emoteArray!=null)
    {
      for(Object emoteObj : emoteArray)
      {
        int emoteId=((Integer)emoteObj).intValue();
        System.out.println("Emote: "+emoteId);
      }
    }
    // Trait
    Object[] traitArray=(Object[])props.getProperty("QuestTreasure_FixedTraitArray");
    if (traitArray!=null)
    {
      for(Object traitObj : traitArray)
      {
        int traitId=((Integer)traitObj).intValue();
        System.out.println("Trait: "+traitId);
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

  private void getFaction(PropertiesSet props)
  {
    PropertiesSet factionProps=(PropertiesSet)props.getProperty("Quest_PositiveFaction");
    if (factionProps!=null)
    {
      int factionId=((Integer)factionProps.getProperty("Quest_FactionDID")).intValue();
      // 1879143761: Iron Garrison Guards
      Integer repTier=(Integer)factionProps.getProperty("Quest_RepTier");
      // Tier 3: 500
      System.out.println("Reputation: faction="+factionId+", tier="+repTier);
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
      System.out.println("Unmanaged TP tier: "+tierCode);
    }
    return null;
  }

  private void getChildDeeds(PropertiesSet properties)
  {
    Object[] objectivesArray=(Object[])properties.getProperty("Quest_ObjectiveArray");
    if (objectivesArray!=null)
    {
      for(Object objectiveObj : objectivesArray)
      {
        PropertiesSet objectiveProps=(PropertiesSet)objectiveObj;
        Object[] completionConditionsArray=(Object[])objectiveProps.getProperty("Quest_CompletionConditionArray");
        if (completionConditionsArray!=null)
        {
          for(Object completionConditionObj : completionConditionsArray)
          {
            Object[] completionConditionArray=(Object[])completionConditionObj;
            for(Object completionConditionObj2 : completionConditionArray)
            {
              if (completionConditionObj2 instanceof PropertiesSet)
              {
                PropertiesSet completionConditionProps=(PropertiesSet)completionConditionObj2;
                handleCompletionCondition(completionConditionProps);
              }
            }
          }
        }
      }
    }
  }

  private HashSet<Integer> eventIds=new HashSet<Integer>();

  private void handleEmoteCondition(PropertiesSet properties)
  {
    int emoteId=((Integer)properties.getProperty("QuestEvent_EmoteDID")).intValue();
    int nbTimes=((Integer)properties.getProperty("QuestEvent_Number")).intValue();
    Integer maxTimesPerDay=(Integer)properties.getProperty("QuestEvent_DailyMaximumIncrements");
    String loreInfo=DatUtils.getStringProperty(properties,"Accomplishment_LoreInfo");
    String progressOverride=DatUtils.getStringProperty(properties,"QuestEvent_ProgressOverride");
    String text="Perform emote "+emoteId;
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

  private String concat(String base, String add)
  {
    if (base==null) return add;
    return base+"/"+add;
  }

  private void handleCompletionCondition(PropertiesSet properties)
  {
    Integer questId=(Integer)properties.getProperty("QuestEvent_QuestComplete");
    if (questId!=null)
    {
      // Check if deed or quest
      System.out.println("\tChild deed/quest: "+questId);
    }
    Integer questEventId=(Integer)properties.getProperty("QuestEvent_ID");
    eventIds.add(questEventId);
    if (questEventId.intValue()==24)
    {
      System.out.println("*** emote ***");
      //System.out.println(properties.dump());
      handleEmoteCondition(properties);
    }
    if (questEventId.intValue()==22)
    {
      System.out.println("*** monster die ***");
      System.out.println(properties.dump());
      handleMonsterDieCondition(properties);
    }
    // QuestEvent_Number: occurrences count?
    /*
    Accomplishment_LoreInfo
    QuestEvent_ProgressOverride
    QuestEvent_ID: 31 => InventoryItem
    QuestEvent_DestroyInventoryItems: 1
    QuestEvent_ItemDID: 1879062499
    QuestEvent_Number: 1
    */
    //  QuestEvent_ID: 1, 4, 6, 7, 10, 11, 16, 18, 21, 22, 24, 25, 26, 31, 32, 34, 39, 45, 58, 59 
    /*
  1 => EnterDetection
  4 => MonsterPlayerDied
  6 => SkillApplied
  7 => ItemUsed
  10 => ExternalInventoryItem
  11 => NPCTalk
  16 => ItemTalk
  18 => Level
  21 => LandmarkDetection
  22 => MonsterDied
  24 => Emote
  25 => PlayerDied
  26 => SkillUsed
  31 => InventoryItem
  32 => QuestComplete
  34 => WorldEventCondition
  39 => HobbyItem
  45 => FactionLevel
  58 => ScriptCallback
  59 => QuestBestowed
 */
  }

  /*
Quest_ObjectiveArray: 
  #1: 
    Quest_CompletionConditionArray: 
      #1: 
        #1: 
          QuestEvent_EventOrder: 0
          QuestEvent_ID: 32
          QuestEvent_Number: 1
          QuestEvent_ProgressOverride: 
            #1: Complete Farmhouse of the Entwash Vale
          QuestEvent_QuestComplete: 1879239070
          QuestEvent_ShowBillboardText: 0
        #2: 
          QuestEvent_EventOrder: 1
          QuestEvent_ID: 32
          QuestEvent_Number: 1
          QuestEvent_ProgressOverride: 
            #1: Complete Farmhouse of the Sutcrofts
          QuestEvent_QuestComplete: 1879239085
          QuestEvent_ShowBillboardText: 0
    Quest_ObjectiveIndex: 1
 */
    /*
     * Slayer:
          QuestEvent_MonsterGenus_Array: 
            #1: 
              Quest_MonsterRegion: 1879257217
              Quest_MonsterSpecies: 17
          QuestEvent_Number: 160
     */

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==1398); // TODO: use WStateClass constant
    }
    return false;
  }

  private void doIt()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        DeedDescription deed=load(id);
        if (deed!=null)
        {
          System.out.println("Deed: "+deed);
          deeds.add(deed);
        }
      }
    }
    System.out.println("Nb deeds: "+nb);
    DeedsWriter.writeSortedDeeds(_deeds,new File("deeds_dat.xml").getAbsoluteFile());
    System.out.println(eventIds);
    //System.out.println("Places: "+PlaceLoader._names);
  }

  void doIt2()
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
    System.out.println("Nb deeds: "+nb);
    DeedsWriter.writeSortedDeeds(_deeds,new File("deeds_dat.xml").getAbsoluteFile());
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatDeedsLoader(facade).doIt();
    facade.dispose();
  }
}
