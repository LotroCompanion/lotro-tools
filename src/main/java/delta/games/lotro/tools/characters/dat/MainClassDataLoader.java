package delta.games.lotro.tools.characters.dat;

import java.io.File;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;

/**
 * Get class definitions from DAT files.
 * @author DAM
 */
public class MainClassDataLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MainClassDataLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainClassDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void handleClass(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+0x9000000);
    //System.out.println(properties.dump());
    PropertiesSet classInfo=(PropertiesSet)properties.getProperty("AdvTable_ClassInfo");
    // Class name
    String className=DatUtils.getStringProperty(classInfo,"AdvTable_ClassName");
    String classAbbreviation=DatUtils.getStringProperty(classInfo,"AdvTable_AbbreviatedClassName");
    System.out.println("Class name: "+className + " ("+classAbbreviation+")");
    // Class description
    String classDescription=DatUtils.getStringProperty(classInfo,"AdvTable_ClassDesc");
    System.out.println("Class description: "+classDescription);
    // Icons
    // Normal size (48 pixels)
    int classIconId=((Integer)classInfo.getProperty("AdvTable_ClassIcon")).intValue();
    File classIconFile=new File(className+".png").getAbsoluteFile();
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    // Small size (32 pixels)
    int classSmallIconId=((Integer)classInfo.getProperty("AdvTable_ClassSmallIcon")).intValue();
    File smallClassIconFile=new File("small-"+className+".png").getAbsoluteFile();
    DatIconsUtils.buildImageFile(_facade,classSmallIconId,smallClassIconFile);

    loadInitialStats(properties);
    loadStatDerivations(properties);
    loadTraits(properties);
    // TODO loadSkills: AdvTable_AvailableSkillEntryList
    // TODO Initial gear: AdvTable_StartingInventory_List: initial gear at level 1, ...
  }

  private void loadInitialStats(PropertiesSet properties)
  {
    Object[] levelsProperties=(Object[])properties.getProperty("AdvTable_LevelEntryList");
    for(Object levelPropertiesObj : levelsProperties)
    {
      PropertiesSet levelProperties=(PropertiesSet)levelPropertiesObj;
      int level=((Integer)levelProperties.getProperty("AdvTable_Level")).intValue();
      if (level>5)
      {
        continue;
      }
      System.out.println("Level: "+level);
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
          System.out.println("\t"+stat+": "+value);
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
          System.out.println("\t"+stat+": "+value);
        }
      }
    }
  }

  private void loadStatDerivations(PropertiesSet properties)
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
          Float value=(Float)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Value");
          if (Math.abs(value.floatValue())>0.001)
          {
            Integer targetStatId=(Integer)derivedStatProperties.getProperty("AdvTable_DerivedStat");
            if (targetStatId.intValue()<=27) // Ignore war-steed related stats
            {
              STAT targetStat=getDerivedStat(targetStatId.intValue());
              Integer sourceStatId=(Integer)formulaProperties.getProperty("AdvTable_DerivedStat_Formula_Stat");
              STAT sourceStat=getStatFromStatType(sourceStatId.intValue());
              if (sourceStat!=null)
              {
                System.out.println(sourceStat+"*"+value+" => "+targetStat);
              }
            }
          }
        }
      }
    }
  }

  private void loadTraits(PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_ClassCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      Integer level=(Integer)traitProperties.getProperty("AdvTable_Trait_Level");
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      Integer trainingCost=(Integer)traitProperties.getProperty("AdvTable_Trait_TrainingCost");
      Integer traitId=(Integer)traitProperties.getProperty("AdvTable_Trait_WC");
      System.out.println("Level: "+level+" (rank="+rank+", training cost="+trainingCost+")");
      loadTrait(traitId.intValue());
    }
  }

  private void loadTrait(int traitId)
  {
    PropertiesSet traitProperties=_facade.loadProperties(0x9000000+traitId);
    String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
    Integer iconId=(Integer)traitProperties.getProperty("Trait_Icon");
    Integer minLevel=(Integer)traitProperties.getProperty("Trait_Minimum_Level");
    String traitIconFile=traitName.replace(":","-")+".png";
    File to=new File("traits/"+traitIconFile).getAbsoluteFile();
    if (!to.exists())
    {
      DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
    }
    System.out.println("Trait name: "+traitName+" (min level="+minLevel+")");
  }

  private STAT getStatFromVitalType(int vitalType)
  {
    if (vitalType==1) return STAT.MORALE;
    if (vitalType==2) return STAT.POWER;
    if (vitalType==3) return null; // War-steed Endurance
    if (vitalType==4) return null; // War-steed Power
    return null;
  }

  private STAT getStatFromStatType(int statType)
  {
    if (statType==1) return STAT.VITALITY;
    if (statType==2) return STAT.AGILITY;
    if (statType==3) return STAT.FATE;
    if (statType==4) return STAT.MIGHT;
    if (statType==5) return STAT.WILL;
    if (statType==6) return null; // War-steed Agility
    if (statType==7) return null; // War-steed Strength
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

  private void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020E);
    Object[] classIds=(Object[])properties.getProperty("AdvTable_LevelTableList");
    for(Object classId : classIds)
    {
      handleClass(((Integer)classId).intValue());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainClassDataLoader(facade).doIt();
    facade.dispose();
  }
}
