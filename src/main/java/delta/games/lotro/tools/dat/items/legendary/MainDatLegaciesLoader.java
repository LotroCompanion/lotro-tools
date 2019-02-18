package delta.games.lotro.tools.dat.items.legendary;

import java.io.File;
import java.util.ArrayList;
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
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.legendary.AbstractLegacy;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.LegacyType;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacy;
import delta.games.lotro.lore.items.legendary.io.xml.LegacyXMLWriter;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegaciesManager;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegacyTier;
import delta.games.lotro.lore.items.legendary.non_imbued.TieredNonImbuedLegacy;
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
    loadNonImbuedLegacies();
    doItWithScan();
    //doItFromIndex();
    //showLegacies();
  }

  void showLegacies()
  {
    EquipmentLocation[] slots= { EquipmentLocation.MAIN_HAND, EquipmentLocation.RANGED_ITEM,
        EquipmentLocation.CLASS_SLOT,EquipmentLocation.BRIDLE
    };
    for(CharacterClass characterClass : CharacterClass.ALL_CLASSES)
    {
      for(EquipmentLocation slot : slots)
      {
        System.out.println("Class: "+characterClass+", slot: "+slot);
        List<DefaultNonImbuedLegacy> legacies=_nonImbuedLegaciesManager.getDefaultLegacies(characterClass,slot);
        for(DefaultNonImbuedLegacy legacy : legacies)
        {
          System.out.println("\t"+legacy);
        }
        List<TieredNonImbuedLegacy> tieredLegacies=_nonImbuedLegaciesManager.getTieredLegacies(characterClass,slot);
        for(TieredNonImbuedLegacy legacy : tieredLegacies)
        {
          System.out.println("\t"+legacy);
        }
      }
    }
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
      StatsProvider dpsProvider=loadDpsLut(dpsLut.intValue());
      ret.setStatsProvider(dpsProvider);
    }

    // Initial max tier
    int initialMaxTier=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_InitialMaxLevel")).intValue();
    ret.setMaxInitialLevel(initialMaxTier);
    // Stats
    Integer imbuedEffect=(Integer)props.getProperty("ItemAdvancement_ImbuedLegacy_Effect");
    if (imbuedEffect!=null)
    {
      StatsProvider provider=DatEffectUtils.loadEffectStats(_facade,imbuedEffect.intValue());
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
        int oldLegacyId=((Integer)oldStatObj).intValue();
        PropertyDefinition propDef=_facade.getPropertiesRegistry().getPropertyDef(oldLegacyId);
        StatsRegistry stats=StatsRegistry.getInstance();
        StatDescription oldStat=stats.getByKey(propDef.getName());
        if (oldStat!=null)
        {
          System.out.println("Old stat: "+oldStat.getName());
          TieredNonImbuedLegacy oldLegacy=_nonImbuedLegaciesManager.getLegacy(oldStat);
          if (oldLegacy!=null)
          {
            ret.setType(oldLegacy.getType());
            ret.setClassAndSlotFilter(oldLegacy.getClassAndSlotFilter());
          }
          else
          {
            System.out.println("Old legacy not found for stat: "+oldStat.getName());
          }
        }
      }
    }
    else
    {
      System.out.println("No mutation data for: "+ret);
    }
    return ret;
  }

  private StatsProvider loadDpsLut(int id)
  {
    StatsProvider ret=new StatsProvider();
    Progression progression=DatStatUtils.getProgression(_facade,id);
    ScalableStatProvider dps=new ScalableStatProvider(WellKnownStat.DPS,progression);
    ret.addStatProvider(dps);
    return ret;
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
    System.out.println(props.dump());

    // Extract imbued class/stat legacies
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
      for(Object obj : array)
      {
        // 300+ items
        int id=((Integer)obj).intValue();
        ImbuedLegacy legacy=loadLegacy(id);
        legaciesMgr.registerLegacy(legacy);
      }
    }

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

    // Tier icons
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_ProgressionTierGroupToIcon_Array");
      for(Object obj : array)
      {
        PropertiesSet legacyProps=(PropertiesSet)obj;
        int tier=((Integer)legacyProps.getProperty("ItemAdvancement_ProgressionTierGroup")).intValue();
        // - small icon
        int smallIconId=((Integer)legacyProps.getProperty("ItemAdvancement_ProgressionIcon_Small")).intValue();
        File smallIconFile=getTierIconFile(true,tier);
        DatIconsUtils.buildImageFile(_facade,smallIconId,smallIconFile);
        // - normal icon
        int iconId=((Integer)legacyProps.getProperty("ItemAdvancement_ProgressionIcon")).intValue();
        File iconFile=getTierIconFile(false,tier);
        DatIconsUtils.buildImageFile(_facade,iconId,iconFile);
      }
    }

    // ItemAdvancement_ClassAgnosticWidgetDID_Array: 5 stat legacies (already included in the previous array)
    // ItemAdvancement_BlankImbuedWidgetDID: blank legacy (useless)
    // ItemAdvancement_ReforgeLevel_AddLegacy_Array: levels with legacy addition (10, 20, 30)
    // ItemAdvancement_ReforgeLevel_Array: levels with reforge (10, 20, 30, 40, 50, 60, 70)
    // ItemAdvancement_RelicCurrency: 1879202375 (shard)
    // ItemAdvancement_RequiredTrait: 1879141627 (Seeker of Deep Places)
    // ItemAdvancement_LegacyReplacement_Extraction_Array: IDs of legacy replacement scrolls (300+ items)
    // ItemAdvancement_LegendarySlotOffer_Array: web store item IDs
    // ItemAdvancement_LegacyTypeName_Array: array of 9 property IDs: ItemAdvancement_Legacy[N]_Type
    // ItemAdvancement_LegacyLevelName_Array: array of 9 property IDs: ItemAdvancement_Legacy[N]_Level
    // ItemAdvancement_StaticEffectTypeName_Array: array of 14 property IDs: ItemAdvancement_StaticEffect[N]_Type

    save(legaciesMgr);
  }

  private File getTierIconFile(boolean small, int tier) {
    File rootDir=new File("../lotro-companion/src/main/java/resources/gui/legendary/tiers");
    String fileName="tier"+tier+(small?"-small":"-large")+".png";
    File iconFile=new File(rootDir,fileName).getAbsoluteFile();
    return iconFile;
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

  private void save(NonImbuedLegaciesManager legaciesMgr)
  {
    List<DefaultNonImbuedLegacy> defaultLegacies=legaciesMgr.getDefaultLegacies();
    List<TieredNonImbuedLegacy> tieredLegacies=legaciesMgr.getTieredLegacies();
    List<AbstractLegacy> all=new ArrayList<AbstractLegacy>();
    all.addAll(defaultLegacies);
    all.addAll(tieredLegacies);

    int nbLegacies=all.size();
    LOGGER.info("Writing "+nbLegacies+" legacies");
    boolean ok=LegacyXMLWriter.write(GeneratedFiles.NON_IMBUED_LEGACIES,all);
    if (ok)
    {
      System.out.println("Wrote non-imbued legacies file: "+GeneratedFiles.NON_IMBUED_LEGACIES);
    }
  }

  private Map<Integer,NonImbuedLegacyTier> _loadedEffects=new HashMap<Integer,NonImbuedLegacyTier>();

  private void loadNonImbuedLegacies()
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
      int combatPropertyType=((Integer)legacyProps.getProperty("Item_RequiredCombatPropertyType")).intValue();
      loadLegaciesTable(effectsTableId,combatPropertyType);
    }
    save(_nonImbuedLegaciesManager);
    //System.out.println(_nonImbuedLegaciesManager.dump());
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
          legacyTier=buildTieredLegacy(effectId.intValue(),major);
          _loadedEffects.put(effectId,legacyTier);
        }
        TieredNonImbuedLegacy legacy=legacyTier.getParentLegacy();
        StatDescription stat=legacy.getStat();
        if (_nonImbuedLegaciesManager.getLegacy(stat)==null)
        {
          _nonImbuedLegaciesManager.addTieredLegacy(legacy);
        }
        _nonImbuedLegaciesManager.registerLegacyUsage(legacy,characterClass,slot);
      }
      index++;
    }
  }

  private NonImbuedLegacyTier buildTieredLegacy(int effectId, Boolean major)
  {
    NonImbuedLegacyTier legacyTier=null;
    Effect effect=DatEffectUtils.loadEffect(_facade,effectId);
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
      // Type
      boolean isStatLegacy=isStatLegacy(stat);
      if (isStatLegacy)
      {
        legacy.setType(LegacyType.STAT);
      }
      else
      {
        legacy.setType(LegacyType.CLASS);
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

  private LegacyType getLegacyTypeFromCombatPropertyType(int code)
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
    if (code==26) return LegacyType.FURY; // Mounted_MomentumMod
    if (code==28) return LegacyType.OUTGOING_HEALING; // Beorning_HealingPS
    return null;
  }

  private CharacterClass getClassFromCombatPropertyType(int code)
  {
    if (code==3) return null; // TacticalDPS
    if (code==6) return CharacterClass.MINSTREL; // Minstrel_TacticalDPS
    if (code==22) return CharacterClass.LORE_MASTER; // Loremaster_TacticalDPS
    if (code==15) return CharacterClass.GUARDIAN; // Guardian_TacticalDPS
    if (code==5) return CharacterClass.RUNE_KEEPER; // Runekeeper_TacticalDPS
    if (code==7) return CharacterClass.MINSTREL; // Minstrel_HealingPS
    if (code==21) return CharacterClass.LORE_MASTER; // Loremaster_HealingPS
    if (code==8) return CharacterClass.CAPTAIN; // Captain_HealingPS
    if (code==13) return CharacterClass.RUNE_KEEPER; // Runekeeper_HealingPS
    if (code==16) return CharacterClass.CHAMPION; // Champion_IncomingHealing
    if (code==23) return CharacterClass.BURGLAR; // Burglar_IncomingHealing
    if (code==25) return CharacterClass.CHAMPION; // Champion_IncomingHealing_65
    if (code==24) return CharacterClass.BURGLAR; // Burglar_IncomingHealing_65
    if (code==26) return null; // Mounted_MomentumMod
    if (code==28) return CharacterClass.BEORNING; // Beorning_HealingPS
    return null;
  }

  private EquipmentLocation getSlotFromCombatPropertyType(int code)
  {
    if (code==3) return null; // TacticalDPS
    if (code==6) return EquipmentLocation.MAIN_HAND; // Minstrel_TacticalDPS
    if (code==22) return EquipmentLocation.MAIN_HAND; // Loremaster_TacticalDPS
    if (code==15) return EquipmentLocation.CLASS_SLOT; // Guardian_TacticalDPS
    if (code==5) return EquipmentLocation.MAIN_HAND; // Runekeeper_TacticalDPS
    if (code==7) return EquipmentLocation.CLASS_SLOT; // Minstrel_HealingPS
    if (code==21) return EquipmentLocation.CLASS_SLOT; // Loremaster_HealingPS
    if (code==8) return EquipmentLocation.CLASS_SLOT; // Captain_HealingPS
    if (code==13) return EquipmentLocation.CLASS_SLOT; // Runekeeper_HealingPS
    if (code==16) return EquipmentLocation.CLASS_SLOT; // Champion_IncomingHealing
    if (code==23) return EquipmentLocation.CLASS_SLOT; // Burglar_IncomingHealing
    if (code==25) return EquipmentLocation.CLASS_SLOT; // Champion_IncomingHealing_65
    if (code==24) return EquipmentLocation.CLASS_SLOT; // Burglar_IncomingHealing_65
    if (code==26) return EquipmentLocation.BRIDLE; // Mounted_MomentumMod
    if (code==28) return EquipmentLocation.CLASS_SLOT; // Beorning_HealingPS
    return null;
  }

  private void loadLegaciesTable(int tableId, int combatPropertyType)
  {
    PropertiesSet props=_facade.loadProperties(tableId+0x9000000);

    LegacyType type=getLegacyTypeFromCombatPropertyType(combatPropertyType);
    CharacterClass characterClass=getClassFromCombatPropertyType(combatPropertyType);
    EquipmentLocation slot=getSlotFromCombatPropertyType(combatPropertyType);

    //int imbuedLegacyId=((Integer)props.getProperty("Item_CPM_IAImbuedCombatPropertyMod")).intValue();
    Object[] qualityArray=(Object[])props.getProperty("Item_QualityCombatPropertyModArray");
    for(Object qualityPropsObj : qualityArray)
    {
      PropertiesSet qualityProps=(PropertiesSet)qualityPropsObj;
      int effectId=((Integer)qualityProps.getProperty("Item_RequiredCombatPropertyModDid")).intValue();
      int quality=((Integer)qualityProps.getProperty("Item_Quality")).intValue();

      if (effectId!=0)
      {
        buildDefaultLegacy(effectId,type,quality,characterClass,slot);
      }
      else
      {
        int prog=((Integer)qualityProps.getProperty("Item_PropertyModProgression")).intValue();
        PropertiesSet progProps=_facade.loadProperties(prog+0x9000000);
        Object[] effectArray=(Object[])progProps.getProperty("DataIDProgression_Array");
        for(Object effectIdObj : effectArray)
        {
          effectId=((Integer)effectIdObj).intValue();
          buildDefaultLegacy(effectId,type,quality,characterClass,slot);
        }
      }
    }
  }

  private DefaultNonImbuedLegacy buildDefaultLegacy(int effectId, LegacyType type, int quality,
      CharacterClass characterClass, EquipmentLocation slot)
  {
    DefaultNonImbuedLegacy legacy=_nonImbuedLegaciesManager.getDefaultLegacy(effectId);
    if (legacy==null)
    {
      Effect effect=DatEffectUtils.loadEffect(_facade,effectId);
      if (effect!=null)
      {
        legacy=new DefaultNonImbuedLegacy();
        legacy.setEffect(effect);
        legacy.setType(type);
        _nonImbuedLegaciesManager.addDefaultLegacy(legacy);
      }
    }
    //System.out.println("Got "+legacy+" for quality "+quality);
    if ((characterClass!=null) || (slot!=null))
    {
      _nonImbuedLegaciesManager.registerLegacyUsage(legacy,characterClass,slot);
    }
    return legacy;
  }

  private boolean isStatLegacy(StatDescription description)
  {
    return ((description==WellKnownStat.MIGHT) || (description==WellKnownStat.AGILITY)
        || (description==WellKnownStat.WILL) || (description==WellKnownStat.VITALITY)
        || (description==WellKnownStat.FATE));
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
