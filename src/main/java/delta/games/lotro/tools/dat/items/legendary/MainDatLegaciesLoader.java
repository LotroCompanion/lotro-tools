package delta.games.lotro.tools.dat.items.legendary;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Effect;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.Legacy;
import delta.games.lotro.lore.items.legendary.LegacyType;
import delta.games.lotro.lore.items.legendary.io.xml.LegacyXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

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

  void doItWithScan()
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

  private Set<Integer> _loadedEffects=new HashSet<Integer>();

  private void loadLegacies()
  {
    // Load reforge table: value of ItemAdvancement_ReforgeTable from 1879134775 (NPC: Forge-master)
    PropertiesSet globalReforgeTableProps=_facade.loadProperties(1879138325+0x9000000);
    //System.out.println(globalReforgeTableProps.dump());

    Object[] reforgeTables=(Object[])globalReforgeTableProps.getProperty("ItemAdvancement_ReforgeSlotInfo_Array");
    for(Object reforgeTableObj : reforgeTables)
    {
      PropertiesSet reforgeTableProps=(PropertiesSet)reforgeTableObj;
      int reforgeTableId=((Integer)reforgeTableProps.getProperty("ItemAdvancement_ReforgeTable")).intValue();
      int slotMask=((Integer)reforgeTableProps.getProperty("ItemAdvancement_Slot")).intValue();
      EquipmentLocation slot=getSlotFromCode(slotMask);
      handleReforgeTable(reforgeTableId,slot);
    }
  }

  private void handleReforgeTable(int reforgeTableId, EquipmentLocation slot)
  {
    System.out.println("Slot: "+slot);
    PropertiesSet reforgeTableProps=_facade.loadProperties(reforgeTableId+0x9000000);
    //System.out.println(reforgeTableProps.dump());
    Object[] reforgeTableItems=(Object[])reforgeTableProps.getProperty("ItemAdvancement_ReforgeTable_Array");
    for(Object reforgeTableItemObj : reforgeTableItems)
    {
      PropertiesSet reforgeTableItemProps=(PropertiesSet)reforgeTableItemObj;
      int classId=((Integer)reforgeTableItemProps.getProperty("ItemAdvancement_Class")).intValue();
      CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classId);
      System.out.println("Class: "+characterClass);
      Object[] reforgeGroups=(Object[])reforgeTableItemProps.getProperty("ItemAdvancement_ReforgeGroup_Array");
      for(Object reforgeGroupObj : reforgeGroups)
      {
        int reforgeGroupId=((Integer)reforgeGroupObj).intValue();
        handleReforgeGroup(characterClass,slot,reforgeGroupId);
      }
    }
  }

  private void handleReforgeGroup(CharacterClass characterClass, EquipmentLocation slot, int reforgeGroupId)
  {
    //System.out.println("Handle reforge group "+reforgeGroupId+" for class "+characterClass+", slot="+slot);
    PropertiesSet props=_facade.loadProperties(reforgeGroupId+0x9000000);
    Object[] progressionLists=(Object[])props.getProperty("ItemAdvancement_ProgressionListArray");
    for(Object progressionListObj : progressionLists)
    {
      PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
      int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
      //int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();
      //System.out.println("List: "+progressionListId+", weight="+weight);

      PropertiesSet progressionListProps=_facade.loadProperties(progressionListId+0x9000000);
      //System.out.println(progressionListProps.dump());
      Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
      //System.out.println("Found "+effectArray.length+" effects");
      for(Object effectEntryObj : effectArray)
      {
        PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
        Integer effectId=(Integer)effectEntry.getProperty("ItemAdvancement_Effect");
        //int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
        if (!_loadedEffects.contains(effectId))
        {
          Effect effect=loadEffect(effectId.intValue());
          _loadedEffects.add(effectId);
          System.out.println(effect);
        }
        else
        {
          System.out.println("Already known: "+effectId);
        }
        //System.out.println("\tEffect ID: "+effectId+", weight="+effectWeight+", label="+effect.getLabel());
      }
    }
  }

  private Effect loadEffect(int effectId)
  {
    Effect ret=null;
    PropertiesSet effectProps=_facade.loadProperties(effectId+0x9000000);
    if (effectProps!=null)
    {
      ret=new Effect();
      ret.setId(effectId);
      StatsProvider provider=DatStatUtils.buildStatProviders(_facade,effectProps);
      ret.setStatsProvider(provider);
    }
    return ret;
  }

  private EquipmentLocation getSlotFromCode(int code)
  {
    if (code==0x10000) return EquipmentLocation.MAIN_HAND;
    else if (code==0x40000) return EquipmentLocation.RANGED_ITEM;
    else if (code==0x100000) return EquipmentLocation.CLASS_SLOT;
    else if (code==0x200000) return EquipmentLocation.BRIDLE;
    return null;
  }

  void inspectLegendaryItem()
  {
    // From: 1879311770 Reshaped Champion's Sword of the First Age
    //PropertiesSet weaponProps=_facade.loadProperties(1879311770+0x9000000);
    // From: 1879311779 Reshaped Captain's Greatsword of the First Age
    PropertiesSet weaponProps=_facade.loadProperties(1879311779+0x9000000);
    //System.out.println(weaponProps.dump());
    {
      Integer progGroupOverride=(Integer)weaponProps.getProperty("ItemAdvancement_ProgressionGroupOverride");
      if (progGroupOverride!=null)
      {
        inspectProgressionListArray(progGroupOverride.intValue());
      }
    }
    // Passives
    {
      System.out.println("Passives:");
      Integer staticEffectGroupOverride=(Integer)weaponProps.getProperty("ItemAdvancement_StaticEffectGroupOverride");
      if (staticEffectGroupOverride!=null)
      {
        System.out.println("ItemAdvancement_StaticEffectGroupOverride = "+staticEffectGroupOverride);
        PropertiesSet props=_facade.loadProperties(staticEffectGroupOverride.intValue()+0x9000000);
        //System.out.println(props.dump());
        Object[] progressionLists=(Object[])props.getProperty("ItemAdvancement_ProgressionListArray");
        for(Object progressionListObj : progressionLists)
        {
          PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
          int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
          int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();
          System.out.println("List: "+progressionListId+", weight="+weight);
          inspectEffectArray(progressionListId);
        }
      }
    }
    System.out.println("Other effect array: ");
    // Minor non-imbued legacies of captain weapons:
    inspectEffectArray(1879173132);
  }

  private void inspectProgressionListArray(int progGroupOverride)
  {
    System.out.println("ItemAdvancement_ProgressionGroupOverride = "+progGroupOverride);
    PropertiesSet props=_facade.loadProperties(progGroupOverride+0x9000000);
    //System.out.println(props.dump());
    Object[] progressionLists=(Object[])props.getProperty("ItemAdvancement_ProgressionListArray");
    for(Object progressionListObj : progressionLists)
    {
      PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
      int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
      int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();
      System.out.println("List: "+progressionListId+", weight="+weight);
      inspectEffectArray(progressionListId);
    }
  }

  private void inspectEffectArray(int id)
  {
    PropertiesSet progressionListProps=_facade.loadProperties(id+0x9000000);
    //System.out.println(progressionListProps.dump());
    Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
    System.out.println("Found "+effectArray.length+" effects");
    for(Object effectEntryObj : effectArray)
    {
      PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
      int effectId=((Integer)effectEntry.getProperty("ItemAdvancement_Effect")).intValue();
      DatEffectUtils.loadEffect(_facade,effectId);
      int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
      StatsProvider effect=DatEffectUtils.loadEffect(_facade,effectId);
      System.out.println("\tEffect ID: "+effectId+", weight="+effectWeight+", label="+effect.getLabel());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatLegaciesLoader(facade).loadLegacies();
    //DatIconsUtils.buildImageFile(facade,1090519170,new File("1090519170.png"));
    facade.dispose();
  }
}
