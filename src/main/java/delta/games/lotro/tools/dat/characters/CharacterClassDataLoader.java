package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.InitialGearDefinition;
import delta.games.lotro.character.classes.InitialGearElement;
import delta.games.lotro.character.classes.io.xml.ClassDescriptionXMLWriter;
import delta.games.lotro.character.classes.proficiencies.ClassProficiencies;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.base.DerivedStatsContributionsMgr;
import delta.games.lotro.character.stats.base.StartStatsManager;
import delta.games.lotro.character.stats.base.io.xml.DerivedStatsContributionsXMLWriter;
import delta.games.lotro.character.stats.base.io.xml.StartStatsXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get class definitions from DAT files.
 * @author DAM
 */
public class CharacterClassDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(CharacterClassDataLoader.class);

  private DataFacade _facade;
  private List<ClassDescription> _classes;
  private StartStatsManager _startStatsManager;
  private DerivedStatsContributionsMgr _derivatedStatsManager;
  private ProficienciesLoader _proficiencies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public CharacterClassDataLoader(DataFacade facade)
  {
    _facade=facade;
    _classes=new ArrayList<ClassDescription>();
    _startStatsManager=new StartStatsManager();
    _derivatedStatsManager=new DerivedStatsContributionsMgr();
    _proficiencies=new ProficienciesLoader(facade);
  }

  private void handleClass(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(properties.dump());
    PropertiesSet classInfo=(PropertiesSet)properties.getProperty("AdvTable_ClassInfo");
    int classCode=((Integer)properties.getProperty("AdvTable_Class")).intValue();
    CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classCode);
    ClassDescription classDescription=new ClassDescription(characterClass);
    LOGGER.info("Handling class: "+characterClass);
    String classAbbreviation=DatUtils.getStringProperty(classInfo,"AdvTable_AbbreviatedClassName");
    LOGGER.debug("Class abbreviation: "+classAbbreviation);
    classDescription.setAbbreviation(classAbbreviation);
    // Class description
    String description=DatUtils.getStringProperty(classInfo,"AdvTable_ClassDesc");
    LOGGER.debug("Class description: "+description);
    classDescription.setDescription(description);
    // Icons
    // Normal size (48 pixels)
    int classIconId=((Integer)classInfo.getProperty("AdvTable_ClassIcon")).intValue();
    File classIconFile=getIconFile(characterClass,"compact");
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    classDescription.setIconId(classIconId);
    // Small size (32 pixels)
    Integer classSmallIconId=(Integer)classInfo.getProperty("AdvTable_ClassSmallIcon");
    if (classSmallIconId!=null)
    {
      File smallClassIconFile=getIconFile(characterClass,"small");
      DatIconsUtils.buildImageFile(_facade,classSmallIconId.intValue(),smallClassIconFile);
    classDescription.setSmallIconId(classSmallIconId.intValue());
    }
    // Tactical DPS name
    String tacticalDpsName=DatUtils.getStringProperty(properties,"AdvTable_TacticalDPSName");
    classDescription.setTacticalDpsStatName(tacticalDpsName);

    loadInitialStats(characterClass,properties);
    loadStatDerivations(characterClass,properties);
    loadTraits(classDescription,properties);
    loadSkills(classDescription,properties);
    // Initial gear:
    InitialGearDefinition initialGear=classDescription.getInitialGear();
    // AdvTable_StartingInventory_List: initial gear at level 1
    Object[] inventory=(Object[])properties.getProperty("AdvTable_StartingInventory_List");
    for(Object inventoryElement : inventory)
    {
      PropertiesSet inventoryElementProps=(PropertiesSet)inventoryElement;
      int startsEquipped=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_StartsEquipped")).intValue();
      int quantity=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_Quantity")).intValue();
      if ((startsEquipped>0) && (quantity==1))
      {
        int itemId=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_Item")).intValue();
        InitialGearElement element=new InitialGearElement();
        element.setItemId(itemId);
        int raceId=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_RequiredRace")).intValue();
        if (raceId!=0)
        {
          Race race=DatEnumsUtils.getRaceFromRaceId(raceId);
          element.setRequiredRace(race);
        }
        initialGear.addGearElement(element);
      }
    }
    // Proficiencies
    _proficiencies.handleClass(classDescription);
    // Armour type for mitigations
    ArmourType armourType=getArmourTypeForMitigations(characterClass);
    ClassProficiencies proficiencies=classDescription.getProficiencies();
    proficiencies.setArmourTypeForMitigations(armourType);
    // Default buffs
    /*
    if (characterClass==CharacterClass.CAPTAIN)
    {
      BuffSpecification idome=new BuffSpecification("IN_DEFENCE_OF_MIDDLE_EARTH",null);
      classDescription.addDefaultBuff(idome);
      BuffSpecification motivated=new BuffSpecification("MOTIVATED",null);
      classDescription.addDefaultBuff(motivated);
    }
    */
    /*
AdvTable_AdvancedCharacterStart_AdvancedTierCASI_List: 
  #1: 
    AdvTable_AdvancedCharacterStart_CharacterStartInfo: 1879228369
    AdvTable_AdvancedCharacterStart_Tier: 8
     */
    // Class deeds?
    // AdvTable_AccomplishmentDirectory: 1879064046
    _classes.add(classDescription);
  }

  private ArmourType getArmourTypeForMitigations(CharacterClass cClass)
  {
    /*
     * See class properties:
AdvTable_ArmorDefense_Points_CalcType: 14 (HeavyArmorDefense)
AdvTable_ArmorDefense_Points_NonCommon_CalcType: 14 (HeavyArmorDefense)
     */
    if ((cClass==CharacterClass.HUNTER) || (cClass==CharacterClass.BURGLAR)
        || (cClass==CharacterClass.WARDEN))
    {
      return ArmourType.MEDIUM;
    }
    if ((cClass==CharacterClass.CHAMPION) || (cClass==CharacterClass.GUARDIAN)
        || (cClass==CharacterClass.CAPTAIN) || (cClass==CharacterClass.BEORNING)
        || (cClass==CharacterClass.BRAWLER) )
    {
      return ArmourType.HEAVY;
    }
    return ArmourType.LIGHT;
  }

  private File getIconFile(CharacterClass characterClass, String size)
  {
    String classIconPath=characterClass.getIconPath();
    File rootDir=new File("../lotro-companion/src/main/resources/resources/gui/classes");
    File iconFile=new File(rootDir,size+"/"+classIconPath+".png").getAbsoluteFile();
    return iconFile;
  }

  private void loadInitialStats(CharacterClass characterClass, PropertiesSet properties)
  {
    Object[] levelsProperties=(Object[])properties.getProperty("AdvTable_LevelEntryList");
    for(Object levelPropertiesObj : levelsProperties)
    {
      PropertiesSet levelProperties=(PropertiesSet)levelPropertiesObj;
      int level=((Integer)levelProperties.getProperty("AdvTable_Level")).intValue();
      LOGGER.debug("Level: "+level);
      BasicStatsSet stats=new BasicStatsSet();
      // Vitals
      Object[] vitalStats=(Object[])levelProperties.getProperty("AdvTable_BaseVitalEntryList");
      for(Object vitalStatObj : vitalStats)
      {
        PropertiesSet statProperties=(PropertiesSet)vitalStatObj;
        Integer value=(Integer)statProperties.getProperty("AdvTable_BaseVitalValue");
        Integer type=(Integer)statProperties.getProperty("AdvTable_VitalType");
        StatDescription stat=getStatFromVitalType(type.intValue());
        if (stat!=null)
        {
          if (useStartStat(stat))
          {
            stats.setStat(stat,value);
          }
        }
        else
        {
          LOGGER.warn("Stat not found (1): "+type);
        }
      }
      // Other stats
      Object[] otherStats=(Object[])levelProperties.getProperty("AdvTable_MaxStatEntryList");
      for(Object otherStatObj : otherStats)
      {
        PropertiesSet statProperties=(PropertiesSet)otherStatObj;
        Integer value=(Integer)statProperties.getProperty("AdvTable_StatValue");
        Integer type=(Integer)statProperties.getProperty("AdvTable_StatType");
        StatDescription stat=getStatFromStatType(type.intValue());
        if (stat!=null)
        {
          if (useStartStat(stat))
          {
            stats.setStat(stat,value);
          }
        }
        else
        {
          LOGGER.warn("Stat not found (2): "+type);
        }
      }
      // Regen
      // ICPR
      if (characterClass!=CharacterClass.BEORNING)
      {
        stats.setStat(WellKnownStat.ICPR,Integer.valueOf(240));
      }
      // OCPR
      if (characterClass!=CharacterClass.BEORNING)
      {
        int ocpr=75;
        if (level>=3) ocpr=90;
        if (level>=8) ocpr=105;
        if (level>=26) ocpr=120;
        stats.setStat(WellKnownStat.OCPR,Integer.valueOf(ocpr));
      }
      // OCMR
      if ((characterClass==CharacterClass.CHAMPION) || (characterClass==CharacterClass.GUARDIAN) || (characterClass==CharacterClass.WARDEN))
      {
        int ocmr=120;
        if (level>=4) ocmr=180;
        if (level>=10) ocmr=240;
        if (level>=31) ocmr=300;
        stats.setStat(WellKnownStat.OCMR,Integer.valueOf(ocmr));
      }
      else if ((characterClass==CharacterClass.BEORNING) || (characterClass==CharacterClass.CAPTAIN) || (characterClass==CharacterClass.HUNTER))
      {
        int ocmr=60;
        if (level>=2) ocmr=120;
        if (level>=6) ocmr=180;
        if (level>=16) ocmr=240;
        stats.setStat(WellKnownStat.OCMR,Integer.valueOf(ocmr));
      }
      else
      {
        int ocmr=60;
        if (level>=5) ocmr=120;
        stats.setStat(WellKnownStat.OCMR,Integer.valueOf(ocmr));
      }
      // ICMR
      int icmr;
      if ((characterClass==CharacterClass.CHAMPION) || (characterClass==CharacterClass.GUARDIAN) || (characterClass==CharacterClass.WARDEN))
      {
        icmr=(int)(91.25 + level * 0.75);
      }
      else if ((characterClass==CharacterClass.BEORNING) || (characterClass==CharacterClass.CAPTAIN) || (characterClass==CharacterClass.HUNTER))
      {
        icmr=(int)(80.5 + level * (2.0/3));
      }
      else
      {
        icmr=(int)(71.1 + level * 0.6);
      }
      stats.setStat(WellKnownStat.ICMR,Integer.valueOf(icmr));
      _startStatsManager.setStats(characterClass,level,stats);
    }
  }

  private boolean useStartStat(StatDescription stat)
  {
    if (stat.getLegacyKey().indexOf("WARSTEED")!=-1) return false;
    return true;
  }

  private void loadStatDerivations(CharacterClass characterClass, PropertiesSet properties)
  {
    Object[] derivedStatsProperties=(Object[])properties.getProperty("AdvTable_DerivedStat_Configuration");
    if (derivedStatsProperties==null)
    {
      return;
    }
    for(Object derivedStatPropertiesObj : derivedStatsProperties)
    {
      PropertiesSet derivedStatProperties=(PropertiesSet)derivedStatPropertiesObj;
      Object[] formulasProperties=(Object[])derivedStatProperties.getProperty("AdvTable_DerivedStat_Formula_StatList");
      if (formulasProperties.length>0)
      {
        for(Object formulaPropertiesObj : formulasProperties)
        {
          PropertiesSet formulaProperties=(PropertiesSet)formulaPropertiesObj;
          float value=((Float)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Value")).floatValue();
          if (Math.abs(value)>0.001)
          {
            Integer targetStatId=(Integer)derivedStatProperties.getProperty("AdvTable_DerivedStat");
            if (targetStatId.intValue()<=27) // Ignore war-steed related stats
            {
              StatDescription targetStat=getDerivedStat(targetStatId.intValue());
              value=StatUtils.fixStatValue(targetStat,value);
              Integer sourceStatId=(Integer)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Stat");
              StatDescription sourceStat=getStatFromStatType(sourceStatId.intValue());
              if (sourceStat!=null)
              {
                //System.out.println(sourceStat+"*"+value+" => "+targetStat);
                _derivatedStatsManager.setFactor(sourceStat,targetStat,characterClass,Float.valueOf(value));
                if (targetStat==WellKnownStat.TACTICAL_MASTERY)
                {
                  _derivatedStatsManager.setFactor(sourceStat,WellKnownStat.OUTGOING_HEALING,characterClass,Float.valueOf(value));
                }
              }
            }
          }
        }
      }
    }
    addImplicitDerivations(characterClass);
  }

  private void addImplicitDerivations(CharacterClass characterClass)
  {
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.MELEE_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.RANGED_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.TACTICAL_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.PHYSICAL_MITIGATION,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.TACTICAL_MITIGATION,characterClass,Float.valueOf(0.2f));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.OCFW_MITIGATION,characterClass,Float.valueOf(0.2f));
    if (characterClass==CharacterClass.CHAMPION)
    {
      _derivatedStatsManager.setFactor(WellKnownStat.MIGHT,WellKnownStat.OCFW_MITIGATION,characterClass,Integer.valueOf(1));
    }
    _derivatedStatsManager.setFactor(WellKnownStat.PHYSICAL_MITIGATION,WellKnownStat.OCFW_MITIGATION,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.TACTICAL_MASTERY,WellKnownStat.OUTGOING_HEALING,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.PHYSICAL_MITIGATION_PERCENTAGE,WellKnownStat.OCFW_MITIGATION_PERCENTAGE,characterClass,Integer.valueOf(1));
  }

  private void loadTraits(ClassDescription description, PropertiesSet properties)
  {
    Set<String> knownTraits=new HashSet<String>();
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_ClassCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      int level=((Integer)traitProperties.getProperty("AdvTable_Trait_Level")).intValue();
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      Integer trainingCost=(Integer)traitProperties.getProperty("AdvTable_Trait_TrainingCost");
      int traitId=((Integer)traitProperties.getProperty("AdvTable_Trait_WC")).intValue();
      String key=level+"#"+traitId;
      if (!knownTraits.contains(key))
      {
        LOGGER.debug("Level: "+level+" (rank="+rank+", training cost="+trainingCost+")");
        TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
        ClassTrait classTrait=new ClassTrait(level,trait);
        description.addTrait(classTrait);
        knownTraits.add(key);
      }
    }
  }

  private void loadSkills(ClassDescription description, PropertiesSet properties)
  {
/*
AdvTable_AvailableSkillEntryList: 
  #1: 
    AdvTable_AdvancedCharacterShortcutIndex: 1
    AdvTable_Level: 1
    AdvTable_PrerequisiteSkill: 0
    AdvTable_Rank: 0
    AdvTable_Skill: 1879064061
    AdvTable_TrainingCost: 0
 */
    SkillsManager skillsMgr=SkillsManager.getInstance();
    Object[] skillsProperties=(Object[])properties.getProperty("AdvTable_AvailableSkillEntryList");
    for(Object skillPropertiesObj : skillsProperties)
    {
      PropertiesSet skillProperties=(PropertiesSet)skillPropertiesObj;
      int level=((Integer)skillProperties.getProperty("AdvTable_Level")).intValue();
      int skillId=((Integer)skillProperties.getProperty("AdvTable_Skill")).intValue();
      SkillDescription skill=skillsMgr.getSkill(skillId);
      if (skill!=null)
      {
        ClassSkill classSkill=new ClassSkill(level,skill);
        description.addSkill(classSkill);
      }
    }
  }

  private StatDescription getStatFromVitalType(int vitalType)
  {
    if (vitalType==1) return WellKnownStat.MORALE;
    if (vitalType==2) return WellKnownStat.POWER;
    if (vitalType==3) return WellKnownStat.WARSTEED_ENDURANCE;
    if (vitalType==4) return WellKnownStat.WARSTEED_POWER;
    return null;
  }

  private StatDescription getStatFromStatType(int statType)
  {
    if (statType==1) return WellKnownStat.VITALITY;
    if (statType==2) return WellKnownStat.AGILITY;
    if (statType==3) return WellKnownStat.FATE;
    if (statType==4) return WellKnownStat.MIGHT;
    if (statType==5) return WellKnownStat.WILL;
    if (statType==6) return WellKnownStat.WARSTEED_AGILITY;
    if (statType==7) return WellKnownStat.WARSTEED_STRENGTH;
    return null;
  }

  private StatDescription getDerivedStat(int statType)
  {
    // From enum DerivedStatType, (id=587203378)
    if (statType==1) return WellKnownStat.MORALE;
    if (statType==2) return WellKnownStat.POWER;
    //if (statType==3) return STAT.PHYSICAL_MASTERY; // Melee Offence Rating
    //if (statType==4) return STAT.PHYSICAL_MASTERY; // Ranged Offence Rating
    //if (statType==5) return STAT.TACTICAL_MASTERY; // Tactical Offence Rating
    if (statType==6) return WellKnownStat.OUTGOING_HEALING; // Outgoing Healing Rating
    if (statType==7) return WellKnownStat.CRITICAL_RATING;
    if (statType==8) return WellKnownStat.PHYSICAL_MITIGATION;
    if (statType==9) return WellKnownStat.TACTICAL_MITIGATION;
    if (statType==10) return WellKnownStat.BLOCK;
    if (statType==11) return WellKnownStat.PARRY;
    if (statType==12) return WellKnownStat.EVADE;
    if (statType==13) return WellKnownStat.RESISTANCE;
    if (statType==14) return WellKnownStat.ICMR;
    if (statType==15) return WellKnownStat.OCMR;
    if (statType==16) return WellKnownStat.ICPR;
    if (statType==17) return WellKnownStat.OCPR;
    if (statType==18) return WellKnownStat.FINESSE;
    if (statType==19) return null; // Elemental Resist Rating
    if (statType==20) return null; // Resistance_Corruption
    if (statType==21) return null; // Song Resist Rating
    if (statType==22) return null; // Cry Resist Rating
    if (statType==23) return null; // Resistance_Magic
    if (statType==26) return WellKnownStat.TACTICAL_MASTERY;
    if (statType==27) return WellKnownStat.PHYSICAL_MASTERY;
    System.out.println("Unsupported stat type: "+statType);
    return null;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020E);
    Object[] classIds=(Object[])properties.getProperty("AdvTable_LevelTableList");
    for(Object classId : classIds)
    {
      handleClass(((Integer)classId).intValue());
    }
    // Load trait trees
    new TraitTreesDataLoader(_facade).doIt(_classes);

    // Save start stats
    StartStatsXMLWriter.write(GeneratedFiles.START_STATS,_startStatsManager);
    DerivedStatsContributionsXMLWriter.write(GeneratedFiles.STAT_CONTRIBS,_derivatedStatsManager);
    // Save classes descriptions
    ClassDescriptionXMLWriter.write(GeneratedFiles.CLASSES,_classes);
  }
}
