package delta.games.lotro.tools.dat.items.legendary;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.tools.dat.utils.ProgressionFactory;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get legendary data from DAT files.
 * @author DAM
 */
public class MainDatLegendariesExplorer
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegendariesExplorer(DataFacade facade)
  {
    _facade=facade;
  }

  private void doIt()
  {
    PropertiesSet props=_facade.loadProperties(1879108262+0x9000000);
    System.out.println(props.dump());
    Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
    int index=0;
    for(Object obj : array)
    {
      if (index<1)
      {
        int id=((Integer)obj).intValue();
        PropertiesSet props2=_facade.loadProperties(id+0x9000000);
        System.out.println(props2.dump());
        index++;
      }
    }
    // ItemAdvancement_LegacyReplacement_Extraction_Array
    //System.out.println(_facade.loadProperties(1879319148+0x9000000).dump());
    // ItemAdvancement_LegendarySlotOffer_Array
    //System.out.println(_facade.loadProperties(1879202229+0x9000000).dump());
    // ItemAdvancement_ClassAgnosticWidgetDID_Array
    //System.out.println(_facade.loadProperties(1879324313+0x9000000).dump());
    // ItemAdvancement_ImbuedLegacy_Effect
    //System.out.println(_facade.loadProperties(1879324318+0x9000000).dump());

    // ItemAdvancement_ImbuedDPSWidget: #1
    System.out.println(_facade.loadProperties(1879325264+0x9000000).dump());
    // ItemAdvancement_AdvanceableWidget_DPSLUT
    System.out.println("DPS"); // Tactical?
    System.out.println(_facade.loadProperties(1879325252+0x9000000).dump());
    // ItemAdvancement_ImbuedDPSWidget: #2
    System.out.println(_facade.loadProperties(1879325265+0x9000000).dump());
    // ItemAdvancement_AdvanceableWidget_DPSLUT
    System.out.println("DPS"); // Physical?
    System.out.println(_facade.loadProperties(1879325253+0x9000000).dump());
  }

  void doIt2()
  {
    // From: 1879311770 Reshaped Champion's Sword of the First Age
    // ItemAdvancement_ProgressionList
    //showProperties(facade,0x7901893B); // 1879148859
    // ItemAdvancement_Effect
    //showProperties(facade,0x7901C573); // 1879164275 => 268447964 - Skill_DamageMultiplier_FeralStrikes
    // Progression
    //showProperties(facade,0x7901C4B5); // 1879164085 ; Progression_Type: 73 ; 0.01-0.167 %?
    // another ItemAdvancement_Effect
    //showProperties(facade,0x7901C578); // 1879164280 => 268447965 - Skill_DamageMultiplier_WildAttack
    // another ItemAdvancement_Effect
    //showProperties(facade,0x7902249F); // 1879188639 => 268447965 - Skill_RecoveryTime_Champion_FerociousStrikes
    PropertiesSet weaponProps=_facade.loadProperties(1879311770+0x9000000);
    //System.out.println(weaponProps.dump());
    Integer progGroupOverride=(Integer)weaponProps.getProperty("ItemAdvancement_ProgressionGroupOverride");
    if (progGroupOverride==null)
    {
      return;
    }
    PropertiesSet props=_facade.loadProperties(progGroupOverride.intValue()+0x9000000);
    //System.out.println(props.dump());
    Object[] progressionLists=(Object[])props.getProperty("ItemAdvancement_ProgressionListArray");
    for(Object progressionListObj : progressionLists)
    {
      PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
      int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
      int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();
      System.out.println("List: "+progressionListId+", weight="+weight);
      PropertiesSet progressionListProps=_facade.loadProperties(progressionListId+0x9000000);
      System.out.println(progressionListProps.dump());
      Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
      for(Object effectEntryObj : effectArray)
      {
        PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
        int effectId=((Integer)effectEntry.getProperty("ItemAdvancement_Effect")).intValue();
        int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
        System.out.println("\tEffect ID: "+effectId+", weight="+effectWeight);
        PropertiesSet effectProps=_facade.loadProperties(effectId+0x9000000);
        System.out.println(effectProps.dump());
        Object[] modArray=(Object[])effectProps.getProperty("Mod_Array");
        if (modArray!=null)
        {
          for(Object singleModObj : modArray)
          {
            PropertiesSet modProps=(PropertiesSet)singleModObj;
            int propertyId=((Integer)modProps.getProperty("Mod_Modified")).intValue();
            int progressionId=((Integer)modProps.getProperty("Mod_Progression")).intValue();
            PropertiesSet progressProperties=_facade.loadProperties(progressionId+0x9000000);
            Progression progression=ProgressionFactory.buildProgression(progressionId,progressProperties);
            PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyId);
            String propertyName=propertyDef.getName();
            System.out.println("\t\t"+propertyName+" = > "+progression);
          }
        }
        /*
Effect_ApplicationProbabilityVariance: 0.0
Effect_Applied_Description: 
Effect_ClassPriority: 1
Effect_ConstantApplicationProbability: 1.0
Effect_Debuff: 0
Effect_Definition_Description: 
Effect_Duration_Permanent: 1
Effect_EquivalenceClass: 0
Effect_Harmful: 0
Effect_Icon: 1090519170 // 41000082
Effect_Name: 
  #1: ModificationEffect
Effect_RemoveOnAwaken: 0
Effect_RemoveOnDefeat: 0
Effect_SentToClient: 1
Effect_UIVisible: 0
Mod_Array: 
  #1: 
    Mod_Modified: 268447964
    Mod_Op: 7
    Mod_Progression: 1879164087
         */
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatLegendariesExplorer(facade).doIt();
    facade.dispose();
  }
}
