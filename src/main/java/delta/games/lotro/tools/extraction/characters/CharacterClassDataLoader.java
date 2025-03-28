package delta.games.lotro.tools.extraction.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
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
import delta.games.lotro.character.utils.TraitAndLevel;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ArmourTypes;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Get class definitions from DAT files.
 * @author DAM
 */
public class CharacterClassDataLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(CharacterClassDataLoader.class);

  private DataFacade _facade;
  private List<ClassDescription> _classes;
  private StartStatsManager _startStatsManager;
  private DerivedStatsContributionsMgr _derivatedStatsManager;
  private ProficienciesLoader _proficiencies;
  private I18nUtils _i18n;
  private EnumMapper _characterClasses;

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
    _i18n=new I18nUtils("classes",facade.getGlobalStringsManager());
    _characterClasses=_facade.getEnumsManager().getEnumMapper(587202574);
  }

  private void handleClass(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
    PropertiesSet classInfo=(PropertiesSet)properties.getProperty("AdvTable_ClassInfo");
    // Code
    int classCode=((Integer)properties.getProperty("AdvTable_Class")).intValue();
    // Key
    String classKey=getClassKeyFromId(classCode);
    ClassDescription classDescription=new ClassDescription(classId,classCode,classKey);
    LOGGER.info("Handling class: {}",classKey);
    // Name
    String className=_i18n.getNameStringProperty(classInfo,"AdvTable_ClassName",classId,0);
    classDescription.setName(className);
    // Tag
    String classFullName=_characterClasses.getLabel(classCode);
    String tag=DatStringUtils.extractTag(classFullName);
    classDescription.setTag(tag);
    // Abbreviation
    String classAbbreviation=DatStringUtils.getStringProperty(classInfo,"AdvTable_AbbreviatedClassName");
    LOGGER.debug("Class abbreviation: {}",classAbbreviation);
    classDescription.setAbbreviation(classAbbreviation);
    // Class description
    String description=_i18n.getStringProperty(classInfo,"AdvTable_ClassDesc");
    LOGGER.debug("Class description: {}",description);
    classDescription.setDescription(description);
    // Icons
    // Normal size (48 pixels)
    int classIconId=((Integer)classInfo.getProperty("AdvTable_ClassIcon")).intValue();
    File classIconFile=getIconFile(classIconId);
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    classDescription.setIconId(classIconId);
    // Small size (32 pixels)
    Integer classSmallIconId=(Integer)classInfo.getProperty("AdvTable_ClassSmallIcon");
    if (classSmallIconId!=null)
    {
      File smallClassIconFile=getIconFile(classSmallIconId.intValue());
      DatIconsUtils.buildImageFile(_facade,classSmallIconId.intValue(),smallClassIconFile);
      classDescription.setSmallIconId(classSmallIconId.intValue());
    }
    // Tactical DPS name
    String tacticalDpsName=DatStringUtils.getStringProperty(properties,"AdvTable_TacticalDPSName");
    classDescription.setTacticalDpsStatName(tacticalDpsName);

    loadInitialStats(classDescription,properties);
    loadStatDerivations(classDescription,properties);
    loadTraits(classDescription,properties);
    loadSkills(classDescription,properties);
    // Proficiencies
    _proficiencies.handleClass(classDescription);
    // Armour type for mitigations
    ArmourType armourType=null;
    Integer calcType=(Integer)properties.getProperty("AdvTable_ArmorDefense_Points_CalcType");
    if (calcType!=null)
    {
      armourType=getArmourTypeForMitigations(calcType.intValue());
    }
    else
    {
      armourType=getDefaultArmourTypeForMitigations(classKey);
    }
    ClassProficiencies proficiencies=classDescription.getProficiencies();
    proficiencies.setArmourTypeForMitigations(armourType);
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

  private ArmourType getArmourTypeForMitigations(int calcType)
  {
    // AdvTable_ArmorDefense_Points_CalcType: 14 (HeavyArmorDefense)
    // AdvTable_ArmorDefense_Points_NonCommon_CalcType: 14 (HeavyArmorDefense)
    if (calcType==12) return ArmourTypes.LIGHT;
    if (calcType==13) return ArmourTypes.MEDIUM;
    if (calcType==14) return ArmourTypes.HEAVY;
    return null;
  }

  private ArmourType getDefaultArmourTypeForMitigations(String classKey)
  {
    if (classKey.equals(WellKnownCharacterClassKeys.BURGLAR)) return ArmourTypes.MEDIUM;
    if (classKey.equals(WellKnownCharacterClassKeys.CAPTAIN)) return ArmourTypes.HEAVY;
    if (classKey.equals(WellKnownCharacterClassKeys.CHAMPION)) return ArmourTypes.HEAVY;
    if (classKey.equals(WellKnownCharacterClassKeys.GUARDIAN)) return ArmourTypes.HEAVY;
    if (classKey.equals(WellKnownCharacterClassKeys.HUNTER)) return ArmourTypes.MEDIUM;
    if (classKey.equals(WellKnownCharacterClassKeys.LORE_MASTER)) return ArmourTypes.LIGHT;
    if (classKey.equals(WellKnownCharacterClassKeys.MINSTREL)) return ArmourTypes.LIGHT;
    return null;
  }

  private File getIconFile(int iconID)
  {
    File iconFile=new File(GeneratedFiles.CLASS_ICONS_DIR,iconID+".png").getAbsoluteFile();
    return iconFile;
  }

  private void loadInitialStats(ClassDescription characterClass, PropertiesSet properties)
  {
    Object[] levelsProperties=(Object[])properties.getProperty("AdvTable_LevelEntryList");
    for(Object levelPropertiesObj : levelsProperties)
    {
      PropertiesSet levelProperties=(PropertiesSet)levelPropertiesObj;
      handleLevel(characterClass, levelProperties);
    }
  }

  private void handleLevel(ClassDescription characterClass, PropertiesSet levelProperties)
  {
    Integer levelInt=(Integer)levelProperties.getProperty("AdvTable_Level");
    LOGGER.debug("Level: {}",levelInt);
    BasicStatsSet stats=new BasicStatsSet();
    // Vitals
    Object[] vitalStats=(Object[])levelProperties.getProperty("AdvTable_BaseVitalEntryList");
    for(Object vitalStatObj : vitalStats)
    {
      PropertiesSet statProperties=(PropertiesSet)vitalStatObj;
      Integer value=(Integer)statProperties.getProperty("AdvTable_BaseVitalValue");
      Integer type=(Integer)statProperties.getProperty("AdvTable_VitalType");
      StatDescription stat=DatStatUtils.getStatFromVitalType(type.intValue());
      if (stat!=null)
      {
        if (useStartStat(stat))
        {
          stats.setStat(stat,value);
        }
      }
      else
      {
        LOGGER.warn("Stat not found (1): {}",type);
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
        LOGGER.warn("Stat not found (2): {}",type);
      }
    }
    // Regen
    String classKey=characterClass.getKey();
    // ICPR
    if (!WellKnownCharacterClassKeys.BEORNING.equals(classKey))
    {
      stats.setStat(WellKnownStat.ICPR,Integer.valueOf(240));
    }
    int level=levelInt.intValue();
    // OCPR
    if (!WellKnownCharacterClassKeys.BEORNING.equals(classKey))
    {
      int ocpr=75;
      if (level>=3) ocpr=90;
      if (level>=8) ocpr=105;
      if (level>=26) ocpr=120;
      stats.setStat(WellKnownStat.OCPR,Integer.valueOf(ocpr));
    }
    // OCMR
    int ocmr=computeOCMR(classKey,level);
    stats.setStat(WellKnownStat.OCMR,Integer.valueOf(ocmr));
    // ICMR
    int icmr=computeICMR(classKey,level);
    stats.setStat(WellKnownStat.ICMR,Integer.valueOf(icmr));
    _startStatsManager.setStats(characterClass,level,stats);
  }

  private int computeOCMR(String classKey, int level)
  {
    int ocmr;
    if ((WellKnownCharacterClassKeys.CHAMPION.equals(classKey)) ||
        (WellKnownCharacterClassKeys.GUARDIAN.equals(classKey)) ||
        (WellKnownCharacterClassKeys.WARDEN.equals(classKey)))
    {
      ocmr=120;
      if (level>=4) ocmr=180;
      if (level>=10) ocmr=240;
      if (level>=31) ocmr=300;
    }
    else if ((WellKnownCharacterClassKeys.BEORNING.equals(classKey)) ||
        (WellKnownCharacterClassKeys.CAPTAIN.equals(classKey)) ||
        (WellKnownCharacterClassKeys.HUNTER.equals(classKey)))
    {
      ocmr=60;
      if (level>=2) ocmr=120;
      if (level>=6) ocmr=180;
      if (level>=16) ocmr=240;
    }
    else
    {
      ocmr=60;
      if (level>=5) ocmr=120;
    }
    return ocmr;
  }

  private int computeICMR(String classKey, int level)
  {
    int icmr;
    if ((WellKnownCharacterClassKeys.CHAMPION.equals(classKey)) ||
        (WellKnownCharacterClassKeys.GUARDIAN.equals(classKey)) ||
        (WellKnownCharacterClassKeys.WARDEN.equals(classKey)))
    {
      icmr=(int)(91.25 + level * 0.75);
    }
    else if ((WellKnownCharacterClassKeys.BEORNING.equals(classKey)) ||
        (WellKnownCharacterClassKeys.CAPTAIN.equals(classKey)) ||
        (WellKnownCharacterClassKeys.HUNTER.equals(classKey)))
    {
      icmr=(int)(80.5 + level * (2.0/3));
    }
    else
    {
      icmr=(int)(71.1 + level * 0.6);
    }
    return icmr;
  }

  private boolean useStartStat(StatDescription stat)
  {
    if (stat.getLegacyKey().indexOf("WARSTEED")!=-1) return false;
    return true;
  }

  private void loadStatDerivations(ClassDescription characterClass, PropertiesSet properties)
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
          handleDerivation(characterClass,derivedStatProperties,formulaProperties);
        }
      }
    }
    addImplicitDerivations(characterClass);
  }

  private void handleDerivation(ClassDescription characterClass, PropertiesSet derivedStatProperties, PropertiesSet formulaProperties)
  {
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
          if (LOGGER.isDebugEnabled())
          {
            LOGGER.debug("{}*{} => {}",sourceStat,Float.valueOf(value),targetStat);
          }
          _derivatedStatsManager.setFactor(sourceStat,targetStat,characterClass,Float.valueOf(value));
          if (targetStat==WellKnownStat.TACTICAL_MASTERY)
          {
            _derivatedStatsManager.setFactor(sourceStat,WellKnownStat.OUTGOING_HEALING,characterClass,Float.valueOf(value));
          }
        }
      }
    }
  }

  private void addImplicitDerivations(ClassDescription characterClass)
  {
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.MELEE_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.RANGED_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.TACTICAL_CRITICAL_DEFENCE,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.PHYSICAL_MITIGATION,characterClass,Integer.valueOf(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.TACTICAL_MITIGATION,characterClass,Float.valueOf(0.2f));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.OCFW_MITIGATION,characterClass,Float.valueOf(0.2f));
    String classKey=characterClass.getKey();
    if (WellKnownCharacterClassKeys.CHAMPION.equals(classKey))
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
      Integer level=(Integer)traitProperties.getProperty("AdvTable_Trait_Level");
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      Integer trainingCost=(Integer)traitProperties.getProperty("AdvTable_Trait_TrainingCost");
      int traitId=((Integer)traitProperties.getProperty("AdvTable_Trait_WC")).intValue();
      String key=level+"#"+traitId;
      if (!knownTraits.contains(key))
      {
        LOGGER.debug("Level: {} (rank={}, training cost={})",level,rank,trainingCost);
        TraitDescription trait=TraitUtils.getTrait(traitId);
        TraitAndLevel classTrait=new TraitAndLevel(level.intValue(),trait);
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
    // 3 => STAT.PHYSICAL_MASTERY Melee Offence Rating
    // 4 => STAT.PHYSICAL_MASTERY Ranged Offence Rating
    // 5 => STAT.TACTICAL_MASTERY Tactical Offence Rating
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
    LOGGER.warn("Unsupported stat type: {}",Integer.valueOf(statType));
    return null;
  }

  /**
   * Get a character class key from a DAT enum code.
   * @param id Input code.
   * @return A character class key or <code>null</code> if not supported.
   */
  public static String getClassKeyFromId(int id)
  {
    if (id==214) return WellKnownCharacterClassKeys.BEORNING;
    if (id==40) return WellKnownCharacterClassKeys.BURGLAR;
    if (id==215) return WellKnownCharacterClassKeys.BRAWLER;
    if (id==24) return WellKnownCharacterClassKeys.CAPTAIN;
    if (id==172) return WellKnownCharacterClassKeys.CHAMPION;
    if (id==23) return WellKnownCharacterClassKeys.GUARDIAN;
    if (id==162) return WellKnownCharacterClassKeys.HUNTER;
    if (id==185) return WellKnownCharacterClassKeys.LORE_MASTER;
    if (id==31) return WellKnownCharacterClassKeys.MINSTREL;
    if (id==193) return WellKnownCharacterClassKeys.RUNE_KEEPER;
    if (id==194) return WellKnownCharacterClassKeys.WARDEN;
    if (id==216) return WellKnownCharacterClassKeys.CORSAIR;
    LOGGER.warn("Unknown class ID: {}",Integer.valueOf(id));
    return "Unknown";
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
    // Save
    save();
  }

  private void save()
  {
    // Save start stats
    StartStatsXMLWriter.write(GeneratedFiles.START_STATS,_startStatsManager);
    // Save classes descriptions
    ClassDescriptionXMLWriter.write(GeneratedFiles.CLASSES,_classes);
    // Save derived stats
    DerivedStatsContributionsXMLWriter.write(GeneratedFiles.STAT_CONTRIBS,_derivatedStatsManager);
    // Labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    new CharacterClassDataLoader(facade).doIt();
  }
}
