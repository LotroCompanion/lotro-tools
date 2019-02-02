package delta.games.lotro.tools.dat.items.legendary;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.Legacy;
import delta.games.lotro.lore.items.legendary.LegacyType;
import delta.games.lotro.lore.items.legendary.io.xml.LegacyXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;

/**
 * Get legacy descriptions from DAT files.
 * @author DAM
 */
public class MainDatLegaciesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatLegaciesLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegaciesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  //private Set<String> statNames=new HashSet<String>();

  private Legacy loadLegacy(int id)
  {
    //System.out.println("**** ID="+id+" *****");
    PropertiesSet props=_facade.loadProperties(id+0x9000000);
    //System.out.println(props.dump());

    //statNames.addAll(props.getPropertyNames());
    Legacy ret=new Legacy();
    // ID
    ret.setIdentifier(id);
    // Max tier
    int maxTier=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_AbsoluteMaxLevel")).intValue();
    ret.setMaxLevel(maxTier);
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

    //int iconId=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_Icon")).intValue();
    //int smallIconId=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_SmallIcon")).intValue();
    //int levelTable=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_LevelTable")).intValue();
    //System.out.println("Level table: "+levelTable);

    //System.out.println(ret);

    /*
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
    */
    return ret;
  }

  private void loadDpsLut(int id)
  {
    /*
    Progression prog=DatStatUtils.getProgression(_facade,id);
    System.out.println("Nb points: "+((ArrayProgression)prog).getNumberOfPoints());
    System.out.println("ID: "+((ArrayProgression)prog).getIdentifier());
    */
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

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      // 4015 (found in 300+ array), 4024 (DPS legacies)
      // 4025 ()
      // Add constant: WStateClass.LEGACY
      return ((classDefIndex==4015) || (classDefIndex==4024) || (classDefIndex==4025));
    }
    return false;
  }

  private void doItWithScan()
  {
    LegaciesManager legaciesMgr=new LegaciesManager();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        Legacy legacy=loadLegacy(id);
        legaciesMgr.registerLegacy(legacy);
      }
    }
    save(legaciesMgr);
  }

  void doItFromIndex()
  {
    LegaciesManager legaciesMgr=new LegaciesManager();
    PropertiesSet props=_facade.loadProperties(1879108262+0x9000000);
    //System.out.println(props.dump());
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
      for(Object obj : array)
      {
        // 327 items
        int id=((Integer)obj).intValue();
        Legacy legacy=loadLegacy(id);
        legaciesMgr.registerLegacy(legacy);
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
        Legacy legacy=loadLegacy(legacyId);
        legaciesMgr.registerLegacy(legacy);
      }
    }

    // ItemAdvancement_LegacyReplacement_Extraction_Array
    //System.out.println(_facade.loadProperties(1879319148+0x9000000).dump());
    // ItemAdvancement_LegendarySlotOffer_Array
    //System.out.println(_facade.loadProperties(1879202229+0x9000000).dump());
    //System.out.println(statNames);
    save(legaciesMgr);
  }

  private void save(LegaciesManager legaciesMgr)
  {
    List<Legacy> legacies=legaciesMgr.getAll();
    Collections.sort(legacies,new IdentifiableComparator<Legacy>());
    int nbLegacies=legacies.size();
    LOGGER.info("Writing "+nbLegacies+" legacies");
    boolean ok=LegacyXMLWriter.write(GeneratedFiles.LEGACIES,legacies);
    if (ok)
    {
      System.out.println("Wrote legacies file: "+GeneratedFiles.LEGACIES);
    }
  }

  void inspectLegendaryItem()
  {
    // From: 1879311770 Reshaped Champion's Sword of the First Age
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
    // Here we find 6 lists that all contain the same 8 major legacies for a champion weapon
    // 6 is the number of tiers?
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
        // Feral/Savage Strikes Damage
        // Progression gives values for tier 1-9 at: 47:0.01, 48:0.027, 49:0.045, 50:0.062, 51:0.08, 52:0.097, 53:0.115, 54:0.132
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
    new MainDatLegaciesLoader(facade).doItWithScan();
    //DatIconsUtils.buildImageFile(facade,1090519170,new File("1090519170.png"));
    facade.dispose();
  }
}
