package delta.games.lotro.tools.dat.items.legendary;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.constraints.ClassAndSlot;
import delta.games.lotro.common.stats.ScalableStatProvider;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.EquipmentLocations;
import delta.games.lotro.lore.items.WeaponType;
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
import delta.games.lotro.tools.dat.misc.ProgressionControlLoader;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.WeaponTypesUtils;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get legacy descriptions from DAT files.
 * @author DAM
 */
public class LegaciesLoader
{
  private static final Logger LOGGER=Logger.getLogger(LegaciesLoader.class);

  private DataFacade _facade;
  private NonImbuedLegaciesManager _nonImbuedLegaciesManager;
  private I18nUtils _i18nNonImbued;
  private DatStatUtils _statUtilsNonImbued;
  private LegaciesManager _imbuedLegaciesManager;
  private I18nUtils _i18nImbued;
  private DatStatUtils _statUtilsImbued;
  private Map<Integer,NonImbuedLegacyTier> _loadedEffects=new HashMap<Integer,NonImbuedLegacyTier>();
  private ProgressionControlLoader _progressionControl;
  private WeaponTypesUtils _weaponUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LegaciesLoader(DataFacade facade)
  {
    _facade=facade;
    _nonImbuedLegaciesManager=new NonImbuedLegaciesManager();
    _i18nNonImbued=new I18nUtils("nonImbuedLegacies",facade.getGlobalStringsManager());
    _statUtilsNonImbued=new DatStatUtils(facade,_i18nNonImbued);
    _imbuedLegaciesManager=new LegaciesManager();
    _i18nImbued=new I18nUtils("legacies",facade.getGlobalStringsManager());
    _statUtilsImbued=new DatStatUtils(facade,_i18nImbued);
    _progressionControl=new ProgressionControlLoader(facade);
    _progressionControl.loadProgressionData();
    _weaponUtils=new WeaponTypesUtils(facade);
  }

  /**
   * Load legacies.
   */
  public void loadLegacies()
  {
    loadNonImbuedLegacies();
    loadImbuedLegacies();
  }

  /**
   * Save legacies.
   */
  public void save()
  {
    // Non-imbued
    save(_nonImbuedLegaciesManager);
    _i18nNonImbued.save();
    // Imbued
    save(_imbuedLegaciesManager);
    _i18nImbued.save();
  }

  private ImbuedLegacy loadImbuedLegacy(int id)
  {
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);

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
    // Effect => Stats & ID
    Integer imbuedEffect=(Integer)props.getProperty("ItemAdvancement_ImbuedLegacy_Effect");
    if (imbuedEffect!=null)
    {
      // Effect ID
      ret.setEffectID(imbuedEffect.intValue());
      // Stats
      StatsProvider provider=DatEffectUtils.loadEffectStats(_statUtilsImbued,imbuedEffect.intValue());
      ret.setStatsProvider(provider);
    }

    int iconId=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_Icon")).intValue();
    ret.setIconId(iconId);
    loadIcon(iconId);
    //int smallIconId=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_SmallIcon")).intValue();
    //int levelTable=((Integer)props.getProperty("ItemAdvancement_AdvanceableWidget_LevelTable")).intValue();
    //System.out.println("Level table: "+levelTable);

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
          LOGGER.debug("Old stat: "+oldStat.getName());
          TieredNonImbuedLegacy oldLegacy=_nonImbuedLegaciesManager.getLegacy(oldStat);
          if (oldLegacy!=null)
          {
            ret.setType(oldLegacy.getType());
            ret.setClassAndSlotFilter(oldLegacy.getClassAndSlotFilter());
            oldLegacy.setImbuedLegacyId(id);
          }
          else
          {
            LOGGER.warn("Old legacy not found for stat: "+oldStat.getName());
          }
        }
      }
    }
    else
    {
      LOGGER.debug("No mutation data for: "+ret);
    }
    return ret;
  }

  private StatsProvider loadDpsLut(int id)
  {
    StatsProvider ret=new StatsProvider();
    Progression progression=ProgressionUtils.getProgression(_facade,id);
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

  private void loadImbuedLegacies()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"ItemAdvancementControl");

    // Extract imbued class/stat legacies
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_WidgetDID_Array");
      for(Object obj : array)
      {
        // 300+ items
        int id=((Integer)obj).intValue();
        ImbuedLegacy legacy=_imbuedLegaciesManager.getLegacy(id);
        if (legacy==null)
        {
          legacy=loadImbuedLegacy(id);
          _imbuedLegaciesManager.registerLegacy(legacy);
        }
      }
    }

    // DPS legacies
    {
      Object[] array=(Object[])props.getProperty("ItemAdvancement_ImbuedDPSWidgetMap_Array");
      for(Object obj : array)
      {
        // 2 items
        PropertiesSet legacyProps=(PropertiesSet)obj;
        //System.out.println(legacyProps.dump());
        int legacyId=((Integer)legacyProps.getProperty("ItemAdvancement_ImbuedDPSWidget")).intValue();
        ImbuedLegacy legacy=loadImbuedLegacy(legacyId);
        // Allowed equipment
        long equipmentCategory=((Long)legacyProps.getProperty("Item_EquipmentCategory")).longValue();
        Set<WeaponType> weaponTypes=_weaponUtils.getAllowedEquipment(equipmentCategory);
        if (!weaponTypes.isEmpty())
        {
          legacy.setAllowedWeaponTypes(weaponTypes);
        }
        _imbuedLegaciesManager.registerLegacy(legacy);
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
      LOGGER.info("Wrote legacies file: "+GeneratedFiles.LEGACIES);
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
      LOGGER.info("Wrote non-imbued legacies file: "+GeneratedFiles.NON_IMBUED_LEGACIES);
    }
  }

  private void loadNonImbuedLegacies()
  {
    // Load reforge table: value of ItemAdvancement_ReforgeTable from 1879134775 (NPC: Forge-master)
    PropertiesSet globalReforgeTableProps=_facade.loadProperties(1879138325+DATConstants.DBPROPERTIES_OFFSET);

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
    PropertiesSet combatPropertyProps=_facade.loadProperties(1879167147+DATConstants.DBPROPERTIES_OFFSET);
    Object[] combatPropertyModArray=(Object[])combatPropertyProps.getProperty("Item_CombatPropertyModArray");
    for(Object combatPropertyModObj : combatPropertyModArray)
    {
      PropertiesSet combatPropertyModProps=(PropertiesSet)combatPropertyModObj;
      int effectsTableId=((Integer)combatPropertyModProps.getProperty("Item_RequiredCombatPropertyModDid")).intValue();
      int combatPropertyType=((Integer)combatPropertyModProps.getProperty("Item_RequiredCombatPropertyType")).intValue();
      loadLegaciesTable(effectsTableId,combatPropertyType);
    }
  }

  private void handleReforgeTable(int reforgeTableId, EquipmentLocation slot)
  {
    PropertiesSet reforgeTableProps=_facade.loadProperties(reforgeTableId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] reforgeTableItems=(Object[])reforgeTableProps.getProperty("ItemAdvancement_ReforgeTable_Array");
    for(Object reforgeTableItemObj : reforgeTableItems)
    {
      PropertiesSet reforgeTableItemProps=(PropertiesSet)reforgeTableItemObj;
      int classCode=((Integer)reforgeTableItemProps.getProperty("ItemAdvancement_Class")).intValue();
      ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode);
      Object[] reforgeGroups=(Object[])reforgeTableItemProps.getProperty("ItemAdvancement_ReforgeGroup_Array");
      for(Object reforgeGroupObj : reforgeGroups)
      {
        int reforgeGroupId=((Integer)reforgeGroupObj).intValue();
        handleReforgeGroup(characterClass,slot,reforgeGroupId);
      }
    }
  }

  private void handleReforgeGroup(ClassDescription characterClass, EquipmentLocation slot, int reforgeGroupId)
  {
    //System.out.println("Handle reforge group "+reforgeGroupId+" for class "+characterClass+", slot="+slot);
    PropertiesSet props=_facade.loadProperties(reforgeGroupId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] progressionLists=(Object[])props.getProperty("ItemAdvancement_ProgressionListArray");
    int index=0;
    for(Object progressionListObj : progressionLists)
    {
      PropertiesSet progressionListSpec=(PropertiesSet)progressionListObj;
      int progressionListId=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList")).intValue();
      //int weight=((Integer)progressionListSpec.getProperty("ItemAdvancement_ProgressionList_Weight")).intValue();

      Boolean major=null;
      if (slot!=EquipmentLocations.BRIDLE)
      {
        if (index==0) major=Boolean.FALSE;
        if (index==5) major=Boolean.TRUE;
      }
      else
      {
        if (index==0) major=Boolean.FALSE; else major=Boolean.TRUE;
      }
      PropertiesSet progressionListProps=_facade.loadProperties(progressionListId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] effectArray=(Object[])progressionListProps.getProperty("ItemAdvancement_Effect_Array");
      for(Object effectEntryObj : effectArray)
      {
        PropertiesSet effectEntry=(PropertiesSet)effectEntryObj;
        Integer effectId=(Integer)effectEntry.getProperty("ItemAdvancement_Effect");
        //int effectWeight=((Integer)effectEntry.getProperty("ItemAdvancement_Mod_Weight")).intValue();
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
    StatsProvider statsProvider=DatEffectUtils.loadEffectStats(_statUtilsNonImbued,effectId);
    StatDescription stat=getStat(statsProvider);
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
    int nbStats=statsProvider.getNumberOfStatProviders();
    if (nbStats>0)
    {
      StatProvider statProvider=statsProvider.getStatProvider(0);
      ScalableStatProvider scalableStatProvider=(ScalableStatProvider)statProvider;
      Progression progression=scalableStatProvider.getProgression();
      int progressionId=progression.getIdentifier();
      PropertiesSet progressionProps=_facade.loadProperties(progressionId+DATConstants.DBPROPERTIES_OFFSET);
      // Tier
      int pointTier=((Integer)progressionProps.getProperty("Progression_PointTier")).intValue();
      //Integer progType=(Integer)progressionProps.getProperty("Progression_Type");
      int tier=pointTier-1;
      legacyTier=legacy.addTier(tier,effectId,statsProvider);
      // Progression type
      int typeCode=((Integer)progressionProps.getProperty("Progression_Type")).intValue();
      // Start level
      Integer startLevel=_progressionControl.getStartingLevel(typeCode);
      legacyTier.setStartRank(startLevel);
      //System.out.println("Start level for "+stat+": "+startLevel);
      // Multiplier
      /*Float multiplier=*/_progressionControl.getMultiplier(typeCode);
      //System.out.println("Multiplier for "+stat.getName()+" @tier"+tier+": "+multiplier);
    }
    else
    {
      LOGGER.warn("Legacy with no stat!");
    }
    return legacyTier;
  }

  private StatDescription getStat(StatsProvider statsProvider)
  {
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
    if (code==0x10000) return EquipmentLocations.MAIN_HAND;
    else if (code==0x40000) return EquipmentLocations.RANGED_ITEM;
    else if (code==0x100000) return EquipmentLocations.CLASS_SLOT;
    else if (code==0x200000) return EquipmentLocations.BRIDLE;
    return null;
  }

  private ClassDescription getClassFromCombatPropertyType(int code)
  {
    String classKey=getClassKeyFromCombatPropertyType(code);
    ClassDescription ret=null;
    if (classKey!=null)
    {
      ret=ClassesManager.getInstance().getCharacterClassByKey(classKey);
    }
    return ret;
  }

  private String getClassKeyFromCombatPropertyType(int code)
  {
    if (code==3) return null; // TacticalDPS
    if (code==6) return WellKnownCharacterClassKeys.MINSTREL; // Minstrel_TacticalDPS
    if (code==22) return WellKnownCharacterClassKeys.LORE_MASTER; // Loremaster_TacticalDPS
    if (code==15) return WellKnownCharacterClassKeys.GUARDIAN; // Guardian_TacticalDPS
    if (code==5) return WellKnownCharacterClassKeys.RUNE_KEEPER; // Runekeeper_TacticalDPS
    if (code==7) return WellKnownCharacterClassKeys.MINSTREL; // Minstrel_HealingPS
    if (code==21) return WellKnownCharacterClassKeys.LORE_MASTER; // Loremaster_HealingPS
    if (code==8) return WellKnownCharacterClassKeys.CAPTAIN; // Captain_HealingPS
    if (code==13) return WellKnownCharacterClassKeys.RUNE_KEEPER; // Runekeeper_HealingPS
    if (code==16) return WellKnownCharacterClassKeys.CHAMPION; // Champion_IncomingHealing
    if (code==23) return WellKnownCharacterClassKeys.BURGLAR; // Burglar_IncomingHealing
    if (code==25) return WellKnownCharacterClassKeys.CHAMPION; // Champion_IncomingHealing_65
    if (code==24) return WellKnownCharacterClassKeys.BURGLAR; // Burglar_IncomingHealing_65
    if (code==26) return null; // Mounted_MomentumMod
    if (code==28) return WellKnownCharacterClassKeys.BEORNING; // Beorning_HealingPS
    return null;
  }

  private EquipmentLocation getSlotFromCombatPropertyType(int code)
  {
    if (code==3) return null; // TacticalDPS
    if (code==6) return EquipmentLocations.MAIN_HAND; // Minstrel_TacticalDPS
    if (code==22) return EquipmentLocations.MAIN_HAND; // Loremaster_TacticalDPS
    if (code==15) return EquipmentLocations.CLASS_SLOT; // Guardian_TacticalDPS
    if (code==5) return EquipmentLocations.MAIN_HAND; // Runekeeper_TacticalDPS
    if (code==7) return EquipmentLocations.CLASS_SLOT; // Minstrel_HealingPS
    if (code==21) return EquipmentLocations.CLASS_SLOT; // Loremaster_HealingPS
    if (code==8) return EquipmentLocations.CLASS_SLOT; // Captain_HealingPS
    if (code==13) return EquipmentLocations.CLASS_SLOT; // Runekeeper_HealingPS
    if (code==16) return EquipmentLocations.CLASS_SLOT; // Champion_IncomingHealing
    if (code==23) return EquipmentLocations.CLASS_SLOT; // Burglar_IncomingHealing
    if (code==25) return EquipmentLocations.CLASS_SLOT; // Champion_IncomingHealing_65
    if (code==24) return EquipmentLocations.CLASS_SLOT; // Burglar_IncomingHealing_65
    if (code==26) return EquipmentLocations.BRIDLE; // Mounted_MomentumMod
    if (code==28) return EquipmentLocations.CLASS_SLOT; // Beorning_HealingPS
    return null;
  }

  private void loadLegaciesTable(int tableId, int combatPropertyType)
  {
    PropertiesSet props=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);

    LegacyType type=DatLegendaryUtils.getLegacyTypeFromCombatPropertyType(combatPropertyType);
    ClassDescription characterClass=getClassFromCombatPropertyType(combatPropertyType);
    EquipmentLocation slot=getSlotFromCombatPropertyType(combatPropertyType);

    // Imbued legacy
    int imbuedLegacyId=((Integer)props.getProperty("Item_CPM_IAImbuedCombatPropertyMod")).intValue();
    ImbuedLegacy imbuedLegacy=_imbuedLegaciesManager.getLegacy(imbuedLegacyId);
    if (imbuedLegacy==null)
    {
      imbuedLegacy=loadImbuedLegacy(imbuedLegacyId);
      // Patch stats for specific cases
      patchStats(imbuedLegacy.getStatsProvider(),characterClass,slot);
      _imbuedLegaciesManager.registerLegacy(imbuedLegacy);
    }

    ClassAndSlot spec=new ClassAndSlot(characterClass,slot);
    imbuedLegacy.addAllowedClassAndSlot(spec);

    // Icon
    int iconId=((Integer)props.getProperty("Item_CPM_LargeIcon")).intValue();
    loadIcon(iconId);

    // Default non-imbued legacies
    Object[] qualityArray=(Object[])props.getProperty("Item_QualityCombatPropertyModArray");
    for(Object qualityPropsObj : qualityArray)
    {
      PropertiesSet qualityProps=(PropertiesSet)qualityPropsObj;
      int effectId=((Integer)qualityProps.getProperty("Item_RequiredCombatPropertyModDid")).intValue();
      int quality=((Integer)qualityProps.getProperty("Item_Quality")).intValue();
      if (effectId!=0)
      {
        DefaultNonImbuedLegacy legacy=buildDefaultLegacy(effectId,type,quality,characterClass,slot);
        legacy.setIconId(iconId);
        legacy.setImbuedLegacyId(imbuedLegacyId);
      }
      else
      {
        int prog=((Integer)qualityProps.getProperty("Item_PropertyModProgression")).intValue();
        PropertiesSet progProps=_facade.loadProperties(prog+DATConstants.DBPROPERTIES_OFFSET);
        Object[] effectArray=(Object[])progProps.getProperty("DataIDProgression_Array");
        for(Object effectIdObj : effectArray)
        {
          effectId=((Integer)effectIdObj).intValue();
          DefaultNonImbuedLegacy legacy=buildDefaultLegacy(effectId,type,quality,characterClass,slot);
          legacy.setIconId(iconId);
          legacy.setImbuedLegacyId(imbuedLegacyId);
        }
      }
    }
  }

  private DefaultNonImbuedLegacy buildDefaultLegacy(int effectId, LegacyType type, int quality,
      ClassDescription characterClass, EquipmentLocation slot)
  {
    DefaultNonImbuedLegacy legacy=_nonImbuedLegaciesManager.getDefaultLegacy(effectId);
    if (legacy==null)
    {
      StatsProvider statsProvider=DatEffectUtils.loadEffectStats(_statUtilsNonImbued,effectId);
      legacy=new DefaultNonImbuedLegacy();
      legacy.setEffectID(effectId);
      legacy.setStatsProvider(statsProvider);
      legacy.setType(type);
      // Patch stats for specific cases
      patchStats(statsProvider,characterClass,slot);
      // Register legacy
      _nonImbuedLegaciesManager.addDefaultLegacy(legacy);
    }
    //System.out.println("Got "+legacy+" for quality "+quality);
    if ((characterClass!=null) || (slot!=null))
    {
      _nonImbuedLegaciesManager.registerLegacyUsage(legacy,characterClass,slot);
    }
    return legacy;
  }

  private void patchStats(StatsProvider statsProvider, ClassDescription characterClass, EquipmentLocation slot)
  {
    if (characterClass==null)
    {
      return;
    }
    // Guardian belts do have a special stat
    if ((WellKnownCharacterClassKeys.GUARDIAN.equals(characterClass.getKey())) && (slot==EquipmentLocations.CLASS_SLOT))
    {
      StatProvider provider=statsProvider.getStatProvider(0);
      StatsRegistry stats=StatsRegistry.getInstance();
      StatDescription stat=stats.getByKey("Combat_TacticalDPS_Modifier#1");
      provider.setStat(stat);
    }
  }

  /**
   * Get a non-imbued DPS legacy.
   * @param dpsLutId DPS table identifier.
   * @return a legacy.
   */
  public DefaultNonImbuedLegacy getNonImbuedDpsLegacy(int dpsLutId)
  {
    DefaultNonImbuedLegacy legacy=_nonImbuedLegaciesManager.getDefaultLegacy(dpsLutId);
    if (legacy==null)
    {
      // Build a DPS legacy using the effect identifier as legacy identifier
      legacy=new DefaultNonImbuedLegacy();
      legacy.setType(LegacyType.DPS);
      legacy.setEffectID(dpsLutId); // TODO : this is not a true effect ID!!
      StatsProvider stats=loadDpsLut(dpsLutId);
      legacy.setStatsProvider(stats);
      legacy.setIconId(1091968768);
      // Manually setup link with imbued DPS legacies:
      int imbuedDpsLegacyId=0;
      if (dpsLutId==1879049506) imbuedDpsLegacyId=1879325265;
      else if (dpsLutId==1879049778) imbuedDpsLegacyId=1879325264;
      legacy.setImbuedLegacyId(imbuedDpsLegacyId);
      // Register legacy
      _nonImbuedLegaciesManager.addDefaultLegacy(legacy);
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
   * Load an icon.
   * @param iconId Icon identifier.
   */
  public void loadIcon(int iconId)
  {
    String iconFilename=iconId+".png";
    File to=new File(GeneratedFiles.LEGACIES_ICONS,iconFilename).getAbsoluteFile();
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconId,to);
      if (!ok)
      {
        LOGGER.warn("Could not build legacy icon: "+iconFilename);
      }
    }
  }
}
