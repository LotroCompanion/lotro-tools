package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.io.xml.ClassDescriptionXMLWriter;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.base.DerivedStatsContributionsMgr;
import delta.games.lotro.character.stats.base.StartStatsManager;
import delta.games.lotro.character.stats.base.io.xml.DerivedStatsContributionsXMLWriter;
import delta.games.lotro.character.stats.base.io.xml.StartStatsXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.FixedDecimalsInteger;

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
  private TraitsManager _traitsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param traitsManager Traits manager.
   */
  public CharacterClassDataLoader(DataFacade facade, TraitsManager traitsManager)
  {
    _facade=facade;
    _classes=new ArrayList<ClassDescription>();
    _startStatsManager=new StartStatsManager();
    _derivatedStatsManager=new DerivedStatsContributionsMgr();
    _traitsManager=traitsManager;
  }

  private void handleClass(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+0x9000000);
    //System.out.println(properties.dump());
    PropertiesSet classInfo=(PropertiesSet)properties.getProperty("AdvTable_ClassInfo");
    // Class name
    String className=DatUtils.getStringProperty(classInfo,"AdvTable_ClassName");
    CharacterClass characterClass=CharacterClass.getByName(className);
    ClassDescription classDescription=new ClassDescription(characterClass);
    LOGGER.info("Handling class: "+characterClass);
    String classAbbreviation=DatUtils.getStringProperty(classInfo,"AdvTable_AbbreviatedClassName");
    LOGGER.info("Class abbreviation: "+classAbbreviation);
    //classDescription.setAbbreviation(classAbbreviation);
    // Class description
    String description=DatUtils.getStringProperty(classInfo,"AdvTable_ClassDesc");
    LOGGER.info("Class description: "+description);
    //classDescription.setDescription(description);
    // Icons
    // Normal size (48 pixels)
    int classIconId=((Integer)classInfo.getProperty("AdvTable_ClassIcon")).intValue();
    File classIconFile=getIconFile(characterClass,"compact");
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    classDescription.setIconId(classIconId);
    // Small size (32 pixels)
    int classSmallIconId=((Integer)classInfo.getProperty("AdvTable_ClassSmallIcon")).intValue();
    File smallClassIconFile=getIconFile(characterClass,"small");
    DatIconsUtils.buildImageFile(_facade,classSmallIconId,smallClassIconFile);
    classDescription.setSmallIconId(classIconId);

    loadInitialStats(characterClass,properties);
    loadStatDerivations(characterClass,properties);
    loadTraits(classDescription,properties);
    loadSkills(classDescription,properties);
    // TODO Initial gear:
    // AdvTable_StartingInventory_List: initial gear at level 1, ...
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

  private File getIconFile(CharacterClass characterClass, String size) {
    String classIconPath=characterClass.getIconPath();
    File rootDir=new File("../lotro-companion/src/main/java/resources/gui/classes");
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
      LOGGER.info("Level: "+level);
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
            stats.setStat(stat,new FixedDecimalsInteger(value.intValue()));
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
            stats.setStat(stat,new FixedDecimalsInteger(value.intValue()));
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
        stats.setStat(WellKnownStat.ICPR,new FixedDecimalsInteger(240));
      }
      // OCPR
      if (characterClass!=CharacterClass.BEORNING)
      {
        int ocpr=75;
        if (level>=3) ocpr=90;
        if (level>=8) ocpr=105;
        if (level>=26) ocpr=120;
        stats.setStat(WellKnownStat.OCPR,new FixedDecimalsInteger(ocpr));
      }
      // OCMR
      if ((characterClass==CharacterClass.CHAMPION) || (characterClass==CharacterClass.GUARDIAN) || (characterClass==CharacterClass.WARDEN))
      {
        int ocmr=120;
        if (level>=4) ocmr=180;
        if (level>=10) ocmr=240;
        if (level>=31) ocmr=300;
        stats.setStat(WellKnownStat.OCMR,new FixedDecimalsInteger(ocmr));
      }
      else if ((characterClass==CharacterClass.BEORNING) || (characterClass==CharacterClass.CAPTAIN) || (characterClass==CharacterClass.HUNTER))
      {
        int ocmr=60;
        if (level>=2) ocmr=120;
        if (level>=6) ocmr=180;
        if (level>=16) ocmr=240;
        stats.setStat(WellKnownStat.OCMR,new FixedDecimalsInteger(ocmr));
      }
      else
      {
        int ocmr=60;
        if (level>=5) ocmr=120;
        stats.setStat(WellKnownStat.OCMR,new FixedDecimalsInteger(ocmr));
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
      stats.setStat(WellKnownStat.ICMR,new FixedDecimalsInteger(icmr));
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
              value=fixStatValue(targetStat,value);
              Integer sourceStatId=(Integer)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Stat");
              StatDescription sourceStat=getStatFromStatType(sourceStatId.intValue());
              if (sourceStat!=null)
              {
                //System.out.println(sourceStat+"*"+value+" => "+targetStat);
                _derivatedStatsManager.setFactor(sourceStat,targetStat,characterClass,new FixedDecimalsInteger(value));
                if (targetStat==WellKnownStat.TACTICAL_MASTERY)
                {
                  _derivatedStatsManager.setFactor(sourceStat,WellKnownStat.OUTGOING_HEALING,characterClass,new FixedDecimalsInteger(value));
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
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.MELEE_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.RANGED_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(WellKnownStat.CRITICAL_DEFENCE_PERCENTAGE,WellKnownStat.TACTICAL_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.PHYSICAL_MITIGATION,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.TACTICAL_MITIGATION,characterClass,new FixedDecimalsInteger(0.2f));
    _derivatedStatsManager.setFactor(WellKnownStat.ARMOUR,WellKnownStat.OCFW_MITIGATION,characterClass,new FixedDecimalsInteger(0.2f));
    _derivatedStatsManager.setFactor(WellKnownStat.PHYSICAL_MITIGATION,WellKnownStat.OCFW_MITIGATION,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(WellKnownStat.TACTICAL_MASTERY,WellKnownStat.OUTGOING_HEALING,characterClass,new FixedDecimalsInteger(1));
  }

  private float fixStatValue(StatDescription stat, float value)
  {
    if (stat.isPercentage())
    {
      value=value*100;
    }
    if ((stat==WellKnownStat.ICMR) || (stat==WellKnownStat.ICPR) || (stat==WellKnownStat.OCMR) || (stat==WellKnownStat.OCPR))
    {
      value=value*60;
    }
    return value;
  }

  private void loadTraits(ClassDescription description, PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_ClassCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      int level=((Integer)traitProperties.getProperty("AdvTable_Trait_Level")).intValue();
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      Integer trainingCost=(Integer)traitProperties.getProperty("AdvTable_Trait_TrainingCost");
      int traitId=((Integer)traitProperties.getProperty("AdvTable_Trait_WC")).intValue();
      LOGGER.info("Level: "+level+" (rank="+rank+", training cost="+trainingCost+")");
      TraitDescription trait=TraitLoader.loadTrait(_facade,traitId);
      _traitsManager.registerTrait(trait);
      ClassTrait classTrait=new ClassTrait(level,trait);
      description.addTrait(classTrait);
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
    Object[] skillsProperties=(Object[])properties.getProperty("AdvTable_AvailableSkillEntryList");
    for(Object skillPropertiesObj : skillsProperties)
    {
      PropertiesSet skillProperties=(PropertiesSet)skillPropertiesObj;
      int level=((Integer)skillProperties.getProperty("AdvTable_Level")).intValue();
      int skillId=((Integer)skillProperties.getProperty("AdvTable_Skill")).intValue();
      SkillDescription skill=SkillLoader.getSkill(_facade,skillId);
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
    new TraitTreesDataLoader(_facade,_traitsManager).doIt(_classes);

    // Save start stats
    StartStatsXMLWriter.write(GeneratedFiles.START_STATS,_startStatsManager);
    DerivedStatsContributionsXMLWriter.write(GeneratedFiles.STAT_CONTRIBS,_derivatedStatsManager);
    // Save classes descriptions
    ClassDescriptionXMLWriter.write(GeneratedFiles.CLASSES,_classes);
  }
}
