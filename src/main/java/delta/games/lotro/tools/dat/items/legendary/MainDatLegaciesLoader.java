package delta.games.lotro.tools.dat.items.legendary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Effect;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.stats.ScalableStatProvider;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.LegacyType;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacy;
import delta.games.lotro.lore.items.legendary.io.xml.LegacyXMLWriter;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegaciesManager;
import delta.games.lotro.lore.items.legendary.non_imbued.TieredNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegacyTier;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get legacy descriptions from DAT files.
 * @author DAM
 */
public class MainDatLegaciesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatLegaciesLoader.class);

  private DataFacade _facade;
  private NonImbuedLegaciesManager _nonImbuedLegaciesManager;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegaciesLoader(DataFacade facade)
  {
    _facade=facade;
    _nonImbuedLegaciesManager=new NonImbuedLegaciesManager();
  }

  private void doIt()
  {
    loadLegacies();
    doItWithScan();
  }

  //private Set<String> statNames=new HashSet<String>();

  private ImbuedLegacy loadLegacy(int id)
  {
    //System.out.println("**** ID="+id+" *****");
    PropertiesSet props=_facade.loadProperties(id+0x9000000);
    //System.out.println(props.dump());

    //statNames.addAll(props.getPropertyNames());
    ImbuedLegacy ret=new ImbuedLegacy();
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
    if (categoryCode==0) return LegacyType.FURY;
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
        ImbuedLegacy legacy=loadLegacy(id);
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
        ImbuedLegacy legacy=loadLegacy(id);
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
        ImbuedLegacy legacy=loadLegacy(legacyId);
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
    List<ImbuedLegacy> legacies=legaciesMgr.getAll();
    Collections.sort(legacies,new IdentifiableComparator<ImbuedLegacy>());
    int nbLegacies=legacies.size();
    LOGGER.info("Writing "+nbLegacies+" legacies");
    boolean ok=LegacyXMLWriter.write(GeneratedFiles.LEGACIES,legacies);
    if (ok)
    {
      System.out.println("Wrote legacies file: "+GeneratedFiles.LEGACIES);
    }
  }

  private Map<Integer,NonImbuedLegacyTier> _loadedEffects=new HashMap<Integer,NonImbuedLegacyTier>();

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
    // Load default legacies (CombatPropertyModControl)
    PropertiesSet combatPropertyProps=_facade.loadProperties(1879167147+0x9000000);
    Object[] legacyPropsTable=(Object[])combatPropertyProps.getProperty("Item_CombatPropertyModArray");
    for(Object legacyObj : legacyPropsTable)
    {
      PropertiesSet legacyProps=(PropertiesSet)legacyObj;
      int effectsTableId=((Integer)legacyProps.getProperty("Item_RequiredCombatPropertyModDid")).intValue();
      int typeCode=((Integer)legacyProps.getProperty("Item_RequiredCombatPropertyType")).intValue();
      LegacyType type=getLegacyTypeFromCode(typeCode);
      loadLegaciesTable(effectsTableId,type);
    }

    System.out.println(_nonImbuedLegaciesManager.dump());
  }

  private void handleReforgeTable(int reforgeTableId, EquipmentLocation slot)
  {
    //System.out.println("Slot: "+slot+", reforge table: "+reforgeTableId);
    PropertiesSet reforgeTableProps=_facade.loadProperties(reforgeTableId+0x9000000);
    //System.out.println(reforgeTableProps.dump());
    Object[] reforgeTableItems=(Object[])reforgeTableProps.getProperty("ItemAdvancement_ReforgeTable_Array");
    for(Object reforgeTableItemObj : reforgeTableItems)
    {
      PropertiesSet reforgeTableItemProps=(PropertiesSet)reforgeTableItemObj;
      int classId=((Integer)reforgeTableItemProps.getProperty("ItemAdvancement_Class")).intValue();
      CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classId);
      //System.out.println("Class: "+characterClass);
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
    int index=0;
    for(Object progressionListObj : progressionLists)
    {
      PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
      int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
      //int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();
      //System.out.println("List: "+progressionListId+", weight="+weight);

      Boolean major=null;
      if (slot!=EquipmentLocation.BRIDLE)
      {
        if (index==0) major=Boolean.FALSE;
        if (index==5) major=Boolean.TRUE;
      }
      else
      {
        if (index==0) major=Boolean.FALSE; else major=Boolean.TRUE;
      }
      PropertiesSet progressionListProps=_facade.loadProperties(progressionListId+0x9000000);
      //System.out.println(progressionListProps.dump());
      Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
      //System.out.println("Found "+effectArray.length+" effects");
      for(Object effectEntryObj : effectArray)
      {
        PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
        Integer effectId=(Integer)effectEntry.getProperty("ItemAdvancement_Effect");
        //int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
        //System.out.println("Effect weight:"+effectWeight);
        NonImbuedLegacyTier legacyTier=_loadedEffects.get(effectId);
        if (legacyTier==null)
        {
          legacyTier=buildLegacy(effectId.intValue(),major);
          _loadedEffects.put(effectId,legacyTier);
        }
        TieredNonImbuedLegacy legacy=legacyTier.getParentLegacy();
        StatDescription stat=legacy.getStat();
        if (_nonImbuedLegaciesManager.getLegacy(stat)==null)
        {
          _nonImbuedLegaciesManager.addLegacy(legacy);
        }
        _nonImbuedLegaciesManager.registerLegacyUsage(legacy,characterClass,slot);
      }
      index++;
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
    else
    {
      System.out.println("Effect not found: "+effectId);
    }
    return ret;
  }

  private NonImbuedLegacyTier buildLegacy(int effectId, Boolean major)
  {
    NonImbuedLegacyTier legacyTier=null;
    Effect effect=loadEffect(effectId);
    StatDescription stat=getStat(effect);
    TieredNonImbuedLegacy legacy=_nonImbuedLegaciesManager.getLegacy(stat);
    if (legacy==null)
    {
      legacy=new TieredNonImbuedLegacy(stat);
      // Major / minor
      if (major!=null)
      {
        legacy.setMajor(major.booleanValue());
      }
    }
    StatsProvider statsProvider=effect.getStatsProvider();
    int nbStats=statsProvider.getNumberOfStatProviders();
    if (nbStats>0)
    {
      StatProvider statProvider=statsProvider.getStatProvider(0);
      ScalableStatProvider scalableStatProvider=(ScalableStatProvider)statProvider;
      Progression progression=scalableStatProvider.getProgression();
      int progressionId=progression.getIdentifier();
      PropertiesSet progressionProps=_facade.loadProperties(progressionId+0x9000000);
      int pointTier=((Integer)progressionProps.getProperty("Progression_PointTier")).intValue();
      //Integer progType=(Integer)progressionProps.getProperty("Progression_Type");
      int tier=pointTier-1;
      legacyTier=legacy.addTier(tier,effect);
    }
    return legacyTier;
  }

  private StatDescription getStat(Effect effect)
  {
    StatsProvider statsProvider=effect.getStatsProvider();
    int nbStats=statsProvider.getNumberOfStatProviders();
    if (nbStats>0)
    {
      StatProvider statProvider=statsProvider.getStatProvider(0);
      return statProvider.getStat();
    }
    return null;
  }

  private EquipmentLocation getSlotFromCode(int code)
  {
    if (code==0x10000) return EquipmentLocation.MAIN_HAND;
    else if (code==0x40000) return EquipmentLocation.RANGED_ITEM;
    else if (code==0x100000) return EquipmentLocation.CLASS_SLOT;
    else if (code==0x200000) return EquipmentLocation.BRIDLE;
    return null;
  }

  private LegacyType getLegacyTypeFromCode(int code)
  {
    if (code==3) return LegacyType.TACTICAL_DPS; // TacticalDPS
    if (code==6) return LegacyType.TACTICAL_DPS; // Minstrel_TacticalDPS
    if (code==22) return LegacyType.TACTICAL_DPS; // Loremaster_TacticalDPS
    if (code==15) return LegacyType.TACTICAL_DPS; // Guardian_TacticalDPS
    if (code==5) return LegacyType.TACTICAL_DPS; // Runekeeper_TacticalDPS
    if (code==7) return LegacyType.OUTGOING_HEALING; // Minstrel_HealingPS
    if (code==21) return LegacyType.OUTGOING_HEALING; // Loremaster_HealingPS
    if (code==8) return LegacyType.OUTGOING_HEALING; // Captain_HealingPS
    if (code==13) return LegacyType.OUTGOING_HEALING; // Runekeeper_HealingPS
    if (code==16) return LegacyType.INCOMING_HEALING; // Champion_IncomingHealing
    if (code==23) return LegacyType.INCOMING_HEALING; // Burglar_IncomingHealing
    if (code==25) return LegacyType.INCOMING_HEALING; // Champion_IncomingHealing_65
    if (code==24) return LegacyType.INCOMING_HEALING; // Burglar_IncomingHealing_65
    if (code==26) return LegacyType.DPS; // Mounted_MomentumMod
    if (code==28) return LegacyType.OUTGOING_HEALING; // Beorning_HealingPS
    return null;
  }

  private void loadLegaciesTable(int tableId, LegacyType type)
  {
    PropertiesSet props=_facade.loadProperties(tableId+0x9000000);

    //int imbuedLegacyId=((Integer)props.getProperty("Item_CPM_IAImbuedCombatPropertyMod")).intValue();
    Object[] qualityArray=(Object[])props.getProperty("Item_QualityCombatPropertyModArray");
    for(Object qualityPropsObj : qualityArray)
    {
      PropertiesSet qualityProps=(PropertiesSet)qualityPropsObj;
      int effectId=((Integer)qualityProps.getProperty("Item_RequiredCombatPropertyModDid")).intValue();
      int quality=((Integer)qualityProps.getProperty("Item_Quality")).intValue();

      if (effectId!=0)
      {
        handleEffect(effectId,type,quality);
      }
      else
      {
        int prog=((Integer)qualityProps.getProperty("Item_PropertyModProgression")).intValue();
        PropertiesSet progProps=_facade.loadProperties(prog+0x9000000);
        Object[] effectArray=(Object[])progProps.getProperty("DataIDProgression_Array");
        for(Object effectIdObj : effectArray)
        {
          effectId=((Integer)effectIdObj).intValue();
          handleEffect(effectId,type,quality);
        }
      }
    }
  }

  private void handleEffect(int effectId, LegacyType type, int quality)
  {
    Effect effect=loadEffect(effectId);
    if (effect!=null)
    {
      DefaultNonImbuedLegacy legacy=new DefaultNonImbuedLegacy();
      legacy.setEffect(effect);
      legacy.setType(type);
      System.out.println("Got "+legacy+" for quality "+quality);
    }
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
    new MainDatLegaciesLoader(facade).doIt();
    //DatIconsUtils.buildImageFile(facade,1090519170,new File("1090519170.png"));
    facade.dispose();
  }
}
