package delta.games.lotro.tools.characters.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.io.xml.ClassDescriptionXMLWriter;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.character.stats.base.DerivedStatsContributionsMgr;
import delta.games.lotro.character.stats.base.StartStatsManager;
import delta.games.lotro.character.stats.base.io.xml.DerivedStatsContributionsXMLWriter;
import delta.games.lotro.character.stats.base.io.xml.StartStatsXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;
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
    File classIconFile=new File(className+".png").getAbsoluteFile();
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    classDescription.setIconId(classIconId);
    // Small size (32 pixels)
    int classSmallIconId=((Integer)classInfo.getProperty("AdvTable_ClassSmallIcon")).intValue();
    File smallClassIconFile=new File("small-"+className+".png").getAbsoluteFile();
    DatIconsUtils.buildImageFile(_facade,classSmallIconId,smallClassIconFile);
    classDescription.setSmallIconId(classIconId);

    loadInitialStats(characterClass,properties);
    loadStatDerivations(characterClass,properties);
    loadTraits(classDescription,properties);
    // TODO loadSkills: AdvTable_AvailableSkillEntryList
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
        STAT stat=getStatFromVitalType(type.intValue());
        if (stat!=null)
        {
          stats.setStat(stat,new FixedDecimalsInteger(value.intValue()));
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
        STAT stat=getStatFromStatType(type.intValue());
        if (stat!=null)
        {
          stats.setStat(stat,new FixedDecimalsInteger(value.intValue()));
        }
        else
        {
          LOGGER.warn("Stat not found (2): "+type);
        }
      }
      _startStatsManager.setStats(characterClass,level,stats);
    }
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
              STAT targetStat=getDerivedStat(targetStatId.intValue());
              value=fixStatValue(targetStat,value);
              Integer sourceStatId=(Integer)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Stat");
              STAT sourceStat=getStatFromStatType(sourceStatId.intValue());
              if (sourceStat!=null)
              {
                //System.out.println(sourceStat+"*"+value+" => "+targetStat);
                _derivatedStatsManager.setFactor(sourceStat,targetStat,characterClass,new FixedDecimalsInteger(value));
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
    _derivatedStatsManager.setFactor(STAT.CRITICAL_DEFENCE_PERCENTAGE,STAT.MELEE_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(STAT.CRITICAL_DEFENCE_PERCENTAGE,STAT.RANGED_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(STAT.CRITICAL_DEFENCE_PERCENTAGE,STAT.TACTICAL_CRITICAL_DEFENCE,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(STAT.ARMOUR,STAT.PHYSICAL_MITIGATION,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(STAT.ARMOUR,STAT.TACTICAL_MITIGATION,characterClass,new FixedDecimalsInteger(0.2f));
    _derivatedStatsManager.setFactor(STAT.ARMOUR,STAT.OCFW_MITIGATION,characterClass,new FixedDecimalsInteger(0.2f));
    _derivatedStatsManager.setFactor(STAT.PHYSICAL_MITIGATION,STAT.OCFW_MITIGATION,characterClass,new FixedDecimalsInteger(1));
    _derivatedStatsManager.setFactor(STAT.TACTICAL_MASTERY,STAT.OUTGOING_HEALING,characterClass,new FixedDecimalsInteger(1));
  }

  private float fixStatValue(STAT stat, float value)
  {
    if (stat.isPercentage())
    {
      value=value*100;
    }
    if ((stat==STAT.ICMR) || (stat==STAT.ICPR) || (stat==STAT.OCMR) || (stat==STAT.OCPR))
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

  private STAT getStatFromVitalType(int vitalType)
  {
    if (vitalType==1) return STAT.MORALE;
    if (vitalType==2) return STAT.POWER;
    if (vitalType==3) return STAT.WARSTEED_ENDURANCE;
    if (vitalType==4) return STAT.WARSTEED_POWER;
    return null;
  }

  private STAT getStatFromStatType(int statType)
  {
    if (statType==1) return STAT.VITALITY;
    if (statType==2) return STAT.AGILITY;
    if (statType==3) return STAT.FATE;
    if (statType==4) return STAT.MIGHT;
    if (statType==5) return STAT.WILL;
    if (statType==6) return STAT.WARSTEED_AGILITY;
    if (statType==7) return STAT.WARSTEED_STRENGTH;
    return null;
  }

  private STAT getDerivedStat(int statType)
  {
    // From enum DerivedStatType, (id=587203378)
    if (statType==1) return STAT.MORALE;
    if (statType==2) return STAT.POWER;
    //if (statType==3) return STAT.PHYSICAL_MASTERY; // Melee Offence Rating
    //if (statType==4) return STAT.PHYSICAL_MASTERY; // Ranged Offence Rating
    //if (statType==5) return STAT.TACTICAL_MASTERY; // Tactical Offence Rating
    if (statType==6) return STAT.OUTGOING_HEALING; // Outgoing Healing Rating
    if (statType==7) return STAT.CRITICAL_RATING;
    if (statType==8) return STAT.PHYSICAL_MITIGATION;
    if (statType==9) return STAT.TACTICAL_MITIGATION;
    if (statType==10) return STAT.BLOCK;
    if (statType==11) return STAT.PARRY;
    if (statType==12) return STAT.EVADE;
    if (statType==13) return STAT.RESISTANCE;
    if (statType==14) return STAT.ICMR;
    if (statType==15) return STAT.OCMR;
    if (statType==16) return STAT.ICPR;
    if (statType==17) return STAT.OCPR;
    if (statType==18) return STAT.FINESSE;
    if (statType==19) return null; // Elemental Resist Rating
    if (statType==20) return null; // Resistance_Corruption
    if (statType==21) return null; // Song Resist Rating
    if (statType==22) return null; // Cry Resist Rating
    if (statType==23) return null; // Resistance_Magic
    if (statType==26) return STAT.TACTICAL_MASTERY;
    if (statType==27) return STAT.PHYSICAL_MASTERY;
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
    File startStatsFile=new File("../lotro-companion/data/lore/characters/startStats.xml");
    StartStatsXMLWriter.write(startStatsFile.getAbsoluteFile(),_startStatsManager);
    File statContribsFile=new File("../lotro-companion/data/lore/characters/statContribs.xml");
    DerivedStatsContributionsXMLWriter.write(statContribsFile.getAbsoluteFile(),_derivatedStatsManager);
    // Save classes descriptions
    File classesFile=new File("../lotro-companion/data/lore/characters/classes.xml").getAbsoluteFile();
    ClassDescriptionXMLWriter.write(classesFile,_classes);
  }
}
