package delta.games.lotro.tools.dat.items.legendary;

import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.lore.items.legendary.Legacy;
import delta.games.lotro.lore.items.legendary.LegacyType;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get legacy descriptions from DAT files.
 * @author DAM
 */
public class MainDatLegaciesLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegaciesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private Set<String> statNames=new HashSet<String>();

  private Legacy loadLegacy(int id)
  {
    System.out.println("**** ID="+id+" *****");
    PropertiesSet props=_facade.loadProperties(id+0x9000000);
    //System.out.println(props.dump());

    statNames.addAll(props.getPropertyNames());
    Legacy ret=new Legacy();
    // ID
    ret.setId(id);
    // Max tier
    int maxTier=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_AbsoluteMaxLevel")).intValue();
    ret.setMaxTier(maxTier);
    // Category
    Integer categoryCode=(Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_Category");
    if (categoryCode!=null)
    {
      LegacyType type=getLegacyType(categoryCode.intValue());
      ret.setType(type);
    }
    // DPS LUT
    Integer dpsLut=(Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_DPSLUT");
    if (dpsLut!=null)
    {
      loadDpsLut(dpsLut.intValue());
    }

    // Initial max tier
    int initialMaxTier=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_InitialMaxLevel")).intValue();
    ret.setMaxInitialLevel(initialMaxTier);
    // Stats
    Integer imbuedEffect=(Integer)props.getProperty("ItemAdvancement_ImbuedLegacy_Effect");
    if (imbuedEffect!=null)
    {
      StatsProvider provider=DatEffectUtils.loadEffect(_facade,imbuedEffect.intValue());
      ret.setStatsProvider(provider);
    }

    System.out.println(ret);

    PropertiesSet mutationProps=(PropertiesSet)props.getProperty("ItemAdvancement_ImbuedLegacy_ClassicLegacyTransform");
    if (mutationProps!=null)
    {
      Object[] oldLegacies=(Object[])mutationProps.getProperty("ItemAdvancement_LegacyTypeName_Array");
      for(Object oldStatObj : oldLegacies)
      {
        int oldLegacy=((Integer)oldStatObj).intValue();
        PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(oldLegacy);
        StatsRegistry stats=StatsRegistry.getInstance();
        StatDescription oldStat=stats.getByKey(propDef.getName());
        System.out.println("Old stat: "+oldStat.getName());
      }
    }
    else
    {
      System.out.println("No mutation data");
    }
    return ret;
  }

  private void loadDpsLut(int id)
  {
    Progression prog=DatStatUtils.getProgression(_facade,id);
    System.out.println("Nb points: "+((ArrayProgression)prog).getNumberOfPoints());
    System.out.println("ID: "+((ArrayProgression)prog).getIdentifier());
  }

  private LegacyType getLegacyType(int categoryCode)
  {
    if (categoryCode==1) return LegacyType.STAT;
    if (categoryCode==2) return LegacyType.CLASS;
    if (categoryCode==3) return LegacyType.DPS;
    if (categoryCode==4) return LegacyType.OUTGOING_HEALING;
    if (categoryCode==5) return LegacyType.INCOMING_HEALING;
    if (categoryCode==6) return LegacyType.TACTICAL_DPS;
    return null;
  }

  private void doIt()
  {
    PropertiesSet props=_facade.loadProperties(1879108262+0x9000000);
    System.out.println(props.dump());
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
      for(Object obj : array)
      {
        // 327 items
        int id=((Integer)obj).intValue();
        loadLegacy(id);
      }
    }
    // Stat legacies (already included in the previous array)
    /*
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_ClassAgnosticWidgetDID_Array");
      for(Object obj : array)
      {
        // 5 items
        int id=((Integer)obj).intValue();
        loadLegacy(id);
      }
    }
    */
    // Blank legacy (useless)
    //int blank=((Integer)props.getProperty("ItemAdvancement_BlankImbuedWidgetDID")).intValue();
    //loadLegacy(blank);

    // DPS legacies
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_ImbuedDPSWidgetMap_Array");
      for(Object obj : array)
      {
        // 2 items
        PropertiesSet legacyProps=(PropertiesSet)obj;
        int legacyId=((Integer)legacyProps.getProperty("ItemAdvancement_ImbuedDPSWidget")).intValue();
        //int equipmentCategory=((Integer)legacyProps.getProperty("Item_EquipmentCategory")).intValue();
        loadLegacy(legacyId);
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
    System.out.println(statNames);
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
    new MainDatLegaciesLoader(facade).doIt();
    facade.dispose();
  }
}
