package delta.games.lotro.tools.dat.items.legendary;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;

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

  private void loadLegacy(int id)
  {
    System.out.println("**** ID="+id+" *****");
    PropertiesSet props=_facade.loadProperties(id+0x9000000);
    //System.out.println(props.dump());

    Integer imbuedEffect=(Integer)props.getProperty("ItemAdvancement_ImbuedLegacy_Effect");
    if (imbuedEffect!=null)
    {
      DatEffectUtils.loadEffect(_facade,imbuedEffect.intValue());
    }
    PropertiesSet mutationProps=(PropertiesSet)props.getProperty("ItemAdvancement_ImbuedLegacy_ClassicLegacyTransform");
    if (mutationProps!=null)
    {
      Object[] oldLegacies=(Object[])mutationProps.getProperty("ItemAdvancement_LegacyTypeName_Array");
      for(Object oldStatObj : oldLegacies)
      {
        int oldLegacy=((Integer)oldStatObj).intValue();
        PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(oldLegacy);
        System.out.println("Old legacy: "+propDef.getName());
      }
    }
    else
    {
      System.out.println("No mutation data");
    }
  }

  private void doIt()
  {
    PropertiesSet props=_facade.loadProperties(1879108262+0x9000000);
    //System.out.println(props.dump());
    Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
    for(Object obj : array)
    {
      // 327 items
      int id=((Integer)obj).intValue();
      loadLegacy(id);
    }

    // ItemAdvancement_ImbuedLegacy_ClassicLegacyTransform gives pre-imbued->imbued transformation data
    //ItemAdvancement_LegacyTypeName_Array: #1: 268460418 => old stat
    // ItemAdvancement_ImbuedLegacy_Effect:
    /*
    System.out.println("ItemAdvancement_ImbuedLegacy_Effect:");
    System.out.println(_facade.loadProperties(1879321627+0x9000000).dump());
    => Effect
        ID: 268460418, key=Skill_Beorning_IA_BearDamage, name=Bear form Damage, percentage

  => same stat before and after imbuement.
        */

    // ItemAdvancement_LegacyReplacement_Extraction_Array
    //System.out.println(_facade.loadProperties(1879319148+0x9000000).dump());
    // ItemAdvancement_LegendarySlotOffer_Array
    //System.out.println(_facade.loadProperties(1879202229+0x9000000).dump());
    // ItemAdvancement_ClassAgnosticWidgetDID_Array
    //System.out.println(_facade.loadProperties(1879324313+0x9000000).dump());
    // ItemAdvancement_ImbuedLegacy_Effect
    //System.out.println(_facade.loadProperties(1879324318+0x9000000).dump());

    /*
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
    */
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
    System.out.println("ItemAdvancement_ProgressionGroupOverride = "+progGroupOverride);
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
      //System.out.println(progressionListProps.dump());
      Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
      System.out.println("Found "+effectArray.length+" effects");
      for(Object effectEntryObj : effectArray)
      {
        PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
        int effectId=((Integer)effectEntry.getProperty("ItemAdvancement_Effect")).intValue();
        int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
        System.out.println("\tEffect ID: "+effectId+", weight="+effectWeight);
        DatEffectUtils.loadEffect(_facade,effectId);
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
