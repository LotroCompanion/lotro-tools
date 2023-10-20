package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.EquipmentCategory;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.common.utils.valueTables.QualityBasedValuesTable;
import delta.games.lotro.common.utils.valueTables.io.xml.ValueTablesXMLWriter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemBindings;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.carryalls.CarryAll;
import delta.games.lotro.lore.items.essences.Essence;
import delta.games.lotro.lore.items.essences.EssencesSlotsSetup;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.lore.items.legendary.LegendaryAttrs;
import delta.games.lotro.lore.items.legendary.LegendaryItem;
import delta.games.lotro.lore.items.legendary.LegendaryWeapon;
import delta.games.lotro.lore.items.legendary2.Legendary2;
import delta.games.lotro.lore.items.legendary2.LegendaryAttrs2;
import delta.games.lotro.lore.items.legendary2.LegendaryItem2;
import delta.games.lotro.lore.items.legendary2.LegendaryWeapon2;
import delta.games.lotro.lore.items.legendary2.SocketEntry;
import delta.games.lotro.lore.items.legendary2.SocketsSetup;
import delta.games.lotro.lore.items.legendary2.io.xml.LegendaryAttrs2XMLWriter;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.lore.items.weapons.WeaponSpeedEntry;
import delta.games.lotro.lore.items.weapons.WeaponSpeedTables;
import delta.games.lotro.lore.items.weapons.io.xml.WeaponSpeedTablesXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.effects.EffectLoader;
import delta.games.lotro.tools.dat.effects.ItemEffectsLoader;
import delta.games.lotro.tools.dat.items.legendary.LegaciesLoader;
import delta.games.lotro.tools.dat.items.legendary.PassivesLoader;
import delta.games.lotro.tools.dat.utils.ArmourTypesUtils;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.RequirementsLoadingUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.StringUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get item definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsLoader.class);

  private static final int[] TYPES=
  {
    2097, // Activator (in-world items that activate things when clicked on)
    2814, // PackageItem
    799, // IWeapon
    798, // ITextItem (scrolls, misc. papers)
    797, // IShield
    796, // IItem
    795, // IClothing
    794, // GameplayContainer (chests, fields, resource nodes)
    804, // Milestone (in-world milestones, camp site fires)
    805, // RecipeItem
    802, // Jewelry
    3663, // ? (recipe books)
    803, // Key (keys, and by extension items that allow opening things)
    815, // Waypoint (doors, horse, misc items used to zone)
    1722, // DoorTemplate (doors, misc similar items)
    3924, // ? (epic battles promotion points bestowers)
    4178 // Carry-alls
  };

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;
  private Item _currentItem;
  private PassivesLoader _passivesLoader;
  private ConsumablesLoader _consumablesLoader;
  private LegaciesLoader _legaciesLoader;
  private ItemValueLoader _valueLoader;
  private DPSValueLoader _dpsLoader;
  private Map<Integer,Integer> _itemLevelOffsets;
  private ItemSortingDataLoader _sortDataLoader;
  private ItemDetailsLoader _detailsLoader;
  private CosmeticLoader _cosmeticLoader;
  private WeaponSpeedTables _speedTables;
  private ItemEffectsLoader _itemEffectsLoader;
  private SocketablesManager _socketablesManager;
  private boolean _live;

  // Enums
  private LotroEnum<ItemClass> _itemClassEnum;
  private LotroEnum<EquipmentCategory> _equipmentCategoryEnum;
  private LotroEnum<ItemSturdiness> _itemSturdinessEnum;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader. 
   */
  public MainDatItemsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("items",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
    _live=Context.isLive();
    _passivesLoader=new PassivesLoader(_facade);
    _consumablesLoader=new ConsumablesLoader(_facade);
    _legaciesLoader=new LegaciesLoader(_facade);
    _valueLoader=new ItemValueLoader(_facade);
    _dpsLoader=new DPSValueLoader(_facade);
    _sortDataLoader=new ItemSortingDataLoader(facade);
    _detailsLoader=new ItemDetailsLoader(_facade);
    _cosmeticLoader=new CosmeticLoader();
    if (!_live)
    {
      _speedTables=new SpeedValuesLoader(facade).loadData();
      WeaponSpeedTablesXMLWriter.writeSpeedTablesFile(GeneratedFiles.SPEED_TABLES,_speedTables);
    }
    _itemEffectsLoader=new ItemEffectsLoader(effectsLoader);
    _socketablesManager=new SocketablesManager();
    // Enums
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _itemClassEnum=enumsRegistry.get(ItemClass.class);
    _equipmentCategoryEnum=enumsRegistry.get(EquipmentCategory.class);
    _itemSturdinessEnum=enumsRegistry.get(ItemSturdiness.class);
  }

  private Item load(int indexDataId, int type)
  {
    Item item=null;
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      Integer itemClassCode=(Integer)properties.getProperty("Item_Class");
      String name=DatUtils.getStringProperty(properties,"Name");
      name=StringUtils.removeMarks(name);
      if (!useItem(indexDataId,name,itemClassCode))
      {
        return null;
      }
      item=buildItem(properties);
      _currentItem=item;
      // ID
      item.setIdentifier(indexDataId);
      // Name
      name=_i18n.getNameStringProperty(properties,"Name",indexDataId,I18nUtils.OPTION_REMOVE_MARKS);
      item.setName(name);
      // Sort data
      _sortDataLoader.handleItem(item,properties);
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      Integer shadowIconId=(Integer)properties.getProperty("Icon_Layer_ShadowDID");
      Integer underlayIconId=(Integer)properties.getProperty("Icon_Layer_UnderlayDID");
      String iconName=buildIconName(iconId,backgroundIconId,shadowIconId,underlayIconId);
      item.setIcon(iconName);
      loadIcon(iconId);
      loadIcon(backgroundIconId);
      loadIcon(shadowIconId);
      loadIcon(underlayIconId);
      // Unique
      Integer unique=(Integer)properties.getProperty("Inventory_Unique");
      boolean isUnique=((unique!=null) && (unique.intValue()==1));
      item.setUnique(isUnique);
      // Max stack
      Integer maxStack=(Integer)properties.getProperty("Inventory_MaxQuantity");
      if ((maxStack!=null) && (maxStack.intValue()>1))
      {
        item.setStackMax(maxStack);
      }
      // Slot
      EquipmentLocation slot=getSlot(properties);
      item.setEquipmentLocation(slot);
      // Essence slots & traceries
      handleSockets(item,properties);
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
      // Item level tweak
      Integer itemLevelOffset=getItemLevelOffset(item,properties);
      item.setItemLevelOffset(itemLevelOffset);
      handleMunging(properties);
      if (level!=null)
      {
        Integer minScaledLevel=(Integer)properties.getProperty("ItemMunging_MinMungeLevel");
        if (minScaledLevel!=null)
        {
          if (level.intValue()<minScaledLevel.intValue())
          {
            boolean legendary=((item instanceof Legendary) || (item instanceof Legendary2));
            if (!legendary)
            {
              if (level.intValue()!=1)
              {
                LOGGER.info("Updated the min level for: "+_currentItem+" "+level+" => "+minScaledLevel);
              }
              Munging munging=item.getMunging();
              level=minScaledLevel;
              level=munging.getItemLevel(level.intValue());
              item.setItemLevel(level);
            }
          }
        }
      }
      // Min Level
      Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
      item.setMinLevel(minLevel);
      // Max Level
      Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
      item.setMaxLevel(maxLevel);
      // Binding
      item.setBinding(getBinding(properties));
      // Durability
      Integer durability=(Integer)properties.getProperty("Item_MaxStructurePoints");
      item.setDurability(durability);
      // Quality
      Integer quality=(Integer)properties.getProperty("Item_Quality");
      if (quality!=null)
      {
        item.setQuality(DatEnumsUtils.getQuality(quality.intValue()));
      }
      // Item class
      ItemClass itemClass=_itemClassEnum.getEntry(itemClassCode.intValue());
      item.setItemClass(itemClass);
      // Handle socketables (essences, traceries, enhancement runes) 
      if (itemClassCode.intValue()==ItemClassUtils.ESSENCE_CODE)
      {
        _socketablesManager.handleSocketable(item,properties);
      }
      // Armour value
      StatProvider armorStatProvider=handleArmour(properties,item);
      // Sturdiness
      Integer durabilityEnum=(Integer)properties.getProperty("Item_DurabilityEnum");
      if (durabilityEnum!=null)
      {
        ItemSturdiness itemSturdiness=_itemSturdinessEnum.getEntry(durabilityEnum.intValue());
        item.setSturdiness(itemSturdiness);
      }
      // Description
      String description=_i18n.getStringProperty(properties,"Description",I18nUtils.OPTION_REMOVE_MARKS);
      item.setDescription(description);
      // Requirements
      // - class
      RequirementsLoadingUtils.loadRequiredClasses(properties,item.getUsageRequirements());
      // - race
      RequirementsLoadingUtils.loadRequiredRaces(properties,item.getUsageRequirements());
      // - faction
      RequirementsLoadingUtils.loadRequiredFaction(properties,item.getUsageRequirements());
      // - profession
      RequirementsLoadingUtils.loadRequiredProfession(properties,item.getUsageRequirements());
      // - glory rank
      RequirementsLoadingUtils.loadRequiredGloryRank(properties,item.getUsageRequirements());
      // - effect
      EffectLoader effectsLoader=_itemEffectsLoader.getEffectsLoader();
      RequirementsLoadingUtils.loadRequiredEffect(properties,item.getUsageRequirements(),effectsLoader);
      // Stats providers
      StatsProvider statsProvider=_statUtils.buildStatProviders(properties);
      if (armorStatProvider!=null)
      {
        // Handle special case of the 3 "Shield of the Hammerhand"
        // TODO: find out which armour stat we shall keep: armorStatProvider, statProviderForArmor or both
        StatProvider statProviderForArmor=statsProvider.getStat(WellKnownStat.ARMOUR);
        if (statProviderForArmor!=null)
        {
          statsProvider.removeStat(WellKnownStat.ARMOUR);
        }
        statsProvider.addStatProvider(armorStatProvider);
      }
      item.setStatsProvider(statsProvider);
      // Effects
      handleEffects(properties);
      // Weapon specifics
      if (item instanceof Weapon)
      {
        loadWeaponSpecifics((Weapon)item,properties);
      }
      // Carry-all specifics
      if (item instanceof CarryAll)
      {
        loadCarryAllSpecifics((CarryAll)item,properties);
      }
      // Handle legendaries
      handleLegendaries(item, properties);
      // Value
      handleItemValue(item,properties);
      // Details
      _detailsLoader.handleItem(item,properties);
      // Cosmetics
      _cosmeticLoader.handleItem(item,properties,type);
      // Effects
      _itemEffectsLoader.handleItem(item,properties);
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
  }

  private String buildIconName(Integer iconId, Integer backgroundIconId, Integer shadowIconId, Integer underlayIconId)
  {
    String iconName=null;
    if ((iconId!=null) || (backgroundIconId!=null) || (shadowIconId!=null) || (underlayIconId!=null))
    {
      iconName=((iconId!=null)?iconId:"0")+"-"+((backgroundIconId!=null)?backgroundIconId:"0");
      if (((shadowIconId!=null) && (shadowIconId.intValue()!=0)) ||
          ((underlayIconId!=null) && (underlayIconId.intValue()!=0)))
      {
        iconName=iconName+"-"+((shadowIconId!=null)?shadowIconId:"0");
        if (!Objects.equals(shadowIconId,underlayIconId))
        {
          if ((underlayIconId!=null) && (underlayIconId.intValue()!=0))
          {
            iconName=iconName+"-"+underlayIconId;
          }
        }
      }
    }
    return iconName;
  }

  private void loadIcon(Integer iconId)
  {
    if ((iconId!=null) && (iconId.intValue()!=0))
    {
      File iconFile=new File(GeneratedFiles.ITEM_ICONS_DIR,iconId.intValue()+".png").getAbsoluteFile();
      if (!iconFile.exists())
      {
        DatIconsUtils.buildImageFile(_facade,iconId.intValue(),iconFile);
      }
    }
  }

  private void handleSockets(Item item, PropertiesSet properties)
  {
    Object[] essenceSlots=(Object[])properties.getProperty("Item_Socket_Array");
    if (essenceSlots==null)
    {
      return;
    }
    int nbSlots=essenceSlots.length;
    if (nbSlots==0)
    {
      return;
    }
    if (item instanceof Legendary2)
    {
      int itemId=item.getIdentifier();
      SocketsSetup setup=new SocketsSetup(itemId);
      for(Object essenceSlot : essenceSlots)
      {
        PropertiesSet essenceSlotProps=(PropertiesSet)essenceSlot;
        // Item_Socket_Type: 4 (Heraldric Tracery)
        // Item_Socket_Unlock_ILevel: 52
        int socketTypeCode=((Long)essenceSlotProps.getProperty("Item_Socket_Type")).intValue();
        SocketType socketType=SocketUtils.getSocketType(socketTypeCode);
        int unlockLevel=((Integer)essenceSlotProps.getProperty("Item_Socket_Unlock_ILevel")).intValue();
        SocketEntry entry=new SocketEntry(socketType,unlockLevel);
        setup.addSocket(entry);
      }
      Legendary2 legendary=(Legendary2)item;
      LegendaryAttrs2 attrs=legendary.getLegendaryAttrs();
      attrs.setSockets(setup);
    }
    else
    {
      // Essences
      EssencesSlotsSetup setup=_socketablesManager.loadEssenceSlotsSetup(essenceSlots);
      item.setEssenceSlots(setup);
    }
    //System.out.println("Got new legendary item: "+item+" with "+setup.getSocketsCount()+" slots");
  }

  private boolean isNewLegendaryItem(Object[] essenceSlots)
  {
    if (essenceSlots==null)
    {
      return false;
    }
    for(Object essenceSlot : essenceSlots)
    {
      PropertiesSet essenceSlotProps=(PropertiesSet)essenceSlot;
      /*
  Item_Socket_Type: 4 (Heraldric Tracery)
  Item_Socket_Unlock_ILevel: 52
       */
      int socketType=((Long)essenceSlotProps.getProperty("Item_Socket_Type")).intValue();
      if ((socketType!=1) && (socketType!=131072) && (socketType!=262144) && (socketType!=524288))
      {
        return true;
      }
    }
    return false;
  }

  private void handleItemValue(Item item, PropertiesSet properties)
  {
    Integer itemValue=(Integer)properties.getProperty("Item_Value");
    Integer itemValueFromTable=null;
    Integer itemValueTableId=(Integer)properties.getProperty("Item_ValueLookupTable");
    if ((itemValueTableId!=null) && (itemValueTableId.intValue()!=0))
    {
      QualityBasedValuesTable table=_valueLoader.getTable(itemValueTableId.intValue());
      if (table!=null)
      {
        ItemQuality quality=item.getQuality();
        Integer itemLevel=item.getItemLevel();
        if (itemLevel!=null)
        {
          Float valueFromTable=table.getValue(quality,itemLevel.intValue());
          if (valueFromTable!=null)
          {
            itemValueFromTable=Integer.valueOf(Math.round(valueFromTable.floatValue()));
          }
          else
          {
            LOGGER.warn("Could not build item value from table!");
          }
        }
        else
        {
          LOGGER.warn("Item level not found!");
        }
        item.setValueTable(table);
      }
    }
    else
    {
      itemValueFromTable=Integer.valueOf(0);
    }
    if (!Objects.equals(itemValue,itemValueFromTable))
    {
      //LOGGER.warn("ID: "+item.getIdentifier()+" - Value: "+itemValue);
      //LOGGER.warn("ID: "+item.getIdentifier()+" - Value (from progression): "+itemValueFromTable);
    }
  }

  private boolean useItem(int itemId, String name, Integer itemClassInt)
  {
    if (itemId==1879465779) return true;
    if (name==null) return false;
    if (itemClassInt==null) return false;
    if (name.contains("TBD")) return false;
    if (name.contains("DNT")) return false;
    if (name.contains("GNDN")) return false;
    if (name.contains("Tester")) return false;
    if (name.contains("Barter Test")) return false;
    if (name.contains("Test of Will")) return true;
    if (name.startsWith("Test ")) return false;
    //int itemClass=itemClassInt.intValue();
    //if ((itemClass==230) || (itemClass==231) || (itemClass==232)) return false;
    return true;
  }

  private void handleEffects(PropertiesSet properties)
  {
    // On equip
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_EquipperEffectList");
    if (effects!=null)
    {
      for(Object effectObj : effects)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        StatsProvider effectStats=handleEffect(effectId);
        if (effectStats!=null)
        {
          int nbProviders=effectStats.getNumberOfStatProviders();
          for(int i=0;i<nbProviders;i++)
          {
            _currentItem.getStatsProvider().addStatProvider(effectStats.getStatProvider(i));
          }
        }
      }
    }
    _consumablesLoader.reset();
    // On use
    _consumablesLoader.handleOnUseEffects(_currentItem,properties);
    // Skills
    _consumablesLoader.handleSkillEffects(_currentItem,properties);
  }

  private StatsProvider handleEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    boolean useEffect=useEffect(effectProps);
    if (useEffect)
    {
      return _statUtils.buildStatProviders(effectProps);
    }
    return null;
  }

  private boolean useEffect(PropertiesSet effectProps)
  {
    Object probability=effectProps.getProperty("Effect_ConstantApplicationProbability");
    if ((probability!=null) && (probability.equals(Float.valueOf(1.0f))))
    {
      Integer permanent=(Integer)effectProps.getProperty("Effect_Duration_Permanent");
      if ((permanent!=null) && (permanent.intValue()==1))
      {
        return true;
      }
      Float constantInterval=(Float)effectProps.getProperty("Effect_Duration_ConstantInterval");
      if ((constantInterval!=null) && (constantInterval.intValue()>0))
      {
        return true;
      }
    }
    return false;
  }

  private void handleLegendaries(Item item, PropertiesSet properties)
  {
    int itemId=item.getIdentifier();
    handleLegendaries(itemId, properties,"ItemAdvancement_StaticEffectGroupOverride");
    handleLegendaries(itemId, properties,"ItemAdvancement_StaticEffectGroup2Override");

    if (item instanceof Legendary)
    {
      Legendary legendary=(Legendary)item;
      Integer combatPropertyModDid=(Integer)properties.getProperty("ItemAdvancement_CombatPropertyModDID");
      Integer dpsLut=(Integer)properties.getProperty("Combat_DPS_LUT");
      LegendaryAttrs attrs=legendary.getLegendaryAttrs();
      if (combatPropertyModDid!=null)
      {
        // Non-DPS main legacy
        attrs.setMainLegacyId(combatPropertyModDid.intValue());
      }
      else if (dpsLut!=null)
      {
        // DPS main legacy
        _legaciesLoader.getNonImbuedDpsLegacy(dpsLut.intValue());
        attrs.setMainLegacyId(dpsLut.intValue());
      }
      else
      {
        LOGGER.warn("Legendary item with no main legacy (DPS or not): "+item);
      }
      //int combatPropertyType=((Integer)properties.getProperty("Item_RequiredCombatPropertyType")).intValue();
      // Seems that each legendary item (4764 occurrences) has either ItemAdvancement_CombatDPSLevel or ItemAdvancement_CombatPropertyModLevel
      // depending on whether it has a DPS main legacy of a non-DPS main legacy
      // TODO An automatic check
      Integer combatDpsLevel=(Integer)properties.getProperty("ItemAdvancement_CombatDPSLevel");
      Integer combatPropertyModLevel=(Integer)properties.getProperty("ItemAdvancement_CombatPropertyModLevel");
      if (combatDpsLevel!=null)
      {
        attrs.setCombatDPSLevel(combatDpsLevel.intValue());
      }
      else if (combatPropertyModLevel!=null)
      {
        attrs.setCombatPropertyModLevel(combatPropertyModLevel.intValue());
      }
      else
      {
        LOGGER.warn("Legendary item with no main legacy base rank: "+item);
      }
      //Integer icon=(Integer)properties.getProperty("ItemAdvancement_CombatPropertyModLargeIconDID");
    }
  }

  private void handleLegendaries(int itemId, PropertiesSet properties, String key)
  {
    Integer staticEffectGroup=(Integer)(properties.getProperty(key));
    if (staticEffectGroup!=null)
    {
      _passivesLoader.handleTable(itemId, staticEffectGroup.intValue());
    }
  }

  private StatProvider handleArmour(PropertiesSet properties, Item item)
  {
    StatProvider armorStatProvider=null;
    Integer armourValue=(Integer)properties.getProperty("Item_Armor_Value");
    if ((armourValue!=null) && (armourValue.intValue()>0))
    {
      // Armour progression...
      Integer armourProgressId=(Integer)properties.getProperty("Item_Armor_Value_Lookup_Table");
      if (armourProgressId!=null)
      {
        armorStatProvider=_statUtils.buildStatProvider(WellKnownStat.ARMOUR,armourProgressId.intValue());
        /*
        int statsLevel=item.getItemLevelForStats().intValue();
        Float computedArmourValue=armorStatProvider.getStatValue(1,statsLevel);
        if (Math.abs(armourValue.intValue()-computedArmourValue.intValue())>0)
        {
          System.out.println("Delta in armour for "+_currentItem+": got "+computedArmourValue+", expected "+armourValue);
        }
        */
      }
    }
    return armorStatProvider;
  }

  private void loadWeaponSpecifics(Weapon weapon, PropertiesSet properties)
  {
    // DPS
    float dps=computeDps(weapon,properties);
    weapon.setDPS(dps);
    // Speed
    float speed=1;
    if (!_live)
    {
      speed=handleSpeed(weapon,properties);
    }
    
    // Damage
    // Combat_DamageVariance: 0.4 => Min damage is 60% of max damage
    float variance=((Float)properties.getProperty("Combat_DamageVariance")).floatValue();
    float maxDamage=2*dps/(2-variance)*speed;
    weapon.setMaxDamage(Math.round(maxDamage));
    // Min Damage
    float minDamage=maxDamage*(1-variance);
    weapon.setMinDamage(Math.round(minDamage));

    // Damage type
    int damageTypeEnum=((Integer)properties.getProperty("Combat_DamageType")).intValue();
    DamageType type=DatEnumsUtils.getDamageType(damageTypeEnum);
    weapon.setDamageType(type);
  }

  private float computeDps(Weapon weapon, PropertiesSet properties)
  {
    Integer dpsLut=(Integer)properties.getProperty("Combat_DPS_LUT");
    if (dpsLut==null)
    {
      LOGGER.warn("No DPS LUT for item: "+weapon);
      return 0;
    }

    // Compute item level to use
    int itemLevel=weapon.getItemLevel().intValue();
    Integer dpsLevel=(Integer)properties.getProperty("ItemAdvancement_CombatDPSLevel");
    int itemLevelForDPS=itemLevel;
    if (dpsLevel!=null)
    {
      itemLevelForDPS=dpsLevel.intValue();
    }
    else
    {
      Integer offset=weapon.getItemLevelOffset();
      if (offset!=null)
      {
        itemLevelForDPS+=offset.intValue();
      }
    }
    ItemQuality quality=weapon.getQuality();
    QualityBasedValuesTable table=_dpsLoader.getTable(dpsLut.intValue());
    weapon.setDPSTable(table);
    float dps=table.getValue(quality,itemLevelForDPS).floatValue();
    weapon.setDPS(dps);
    return dps;
  }

  private float handleSpeed(Weapon weapon, PropertiesSet properties)
  {
    Float duration=(Float)properties.getProperty("Item_BaseActionDuration");
    Float mod=(Float)properties.getProperty("Item_BaseAnimDurationMultiplierMod");
    Integer speedCode=(Integer)properties.getProperty("Combat_WeaponSpeed");
    int id=weapon.getIdentifier();
    String name=weapon.getName();
    WeaponType type=weapon.getWeaponType();
    WeaponSpeedEntry entry=_speedTables.getEntry(type,speedCode.intValue());
    float expectedDuration=entry.getBaseActionDuration();
    float expectedAnimDurationMod=entry.getBaseAnimationDurationMultiplierModifier();
    if (Math.abs(expectedDuration-duration.floatValue())>0.01)
    {
      LOGGER.warn("************ BAD DURATION! ID="+id+", name="+name+" ***********");
    }
    if (Math.abs(expectedAnimDurationMod-mod.floatValue())>0.01)
    {
      LOGGER.warn("************ BAD ANIM DURATION MOD! ID="+id+", name="+name+" ***********");
    }
    weapon.setSpeed(entry);
    //System.out.println(id+"\t"+name+"\t"+type+"\t"+speedCode+"\t"+duration+"\t"+mod);
    return duration.floatValue();
  }

  private void loadCarryAllSpecifics(CarryAll carryAll, PropertiesSet properties)
  {
    int stackMax=((Integer)properties.getProperty("BoS_Available_MaxQuantity")).intValue();
    carryAll.setItemStackMax(stackMax);
    int maxItems=((Integer)properties.getProperty("BoS_Available_MaxDifferentTypesOfItems")).intValue();
    carryAll.setMaxItems(maxItems);
  }

  private Item buildItem(PropertiesSet properties)
  {
    Long equipmentCategoryValue=(Long)properties.getProperty("Item_EquipmentCategory");
    int equipmentCategoryCode=DatEnumsUtils.getEquipmentCategoryCode(equipmentCategoryValue);
    WeaponType weaponType=DatEnumsUtils.getWeaponTypeFromEquipmentCategory(equipmentCategoryCode);
    ArmourType armourType=ArmourTypesUtils.getArmourType(equipmentCategoryCode);
    // Legendary stuff?
    Integer isAdvancementItem=(Integer)properties.getProperty("ItemAdvancement_Item");
    boolean isLegendary=((isAdvancementItem!=null) && (isAdvancementItem.intValue()==1));
    Object[] essenceSlots=(Object[])properties.getProperty("Item_Socket_Array");
    boolean isNewLegendary=isNewLegendaryItem(essenceSlots);

    Item ret=null;
    if (weaponType!=null)
    {
      Weapon weapon=(isLegendary?new LegendaryWeapon():(isNewLegendary?new LegendaryWeapon2():new Weapon()));
      weapon.setWeaponType(weaponType);
      ret=weapon;
    }
    else if (armourType!=null)
    {
      Armour armour=new Armour();
      armour.setArmourType(armourType);
      ret=armour;
    }
    else if (isLegendary)
    {
      ret=new LegendaryItem();
    }
    else if (isNewLegendary)
    {
      ret=new LegendaryItem2();
    }
    else
    {
      Integer weenieType=(Integer)properties.getProperty("WeenieType");
      if ((weenieType!=null) && (weenieType.intValue()==15728769))
      {
        ret=new CarryAll();
      }
      else
      {
        SocketType type=_socketablesManager.isEssence(properties);
        if (type!=null)
        {
          Essence essence=new Essence();
          ret=essence;
          essence.setType(type);
        }
        else
        {
          ret=new Item();
        }
      }
    }
    if (equipmentCategoryCode!=0)
    {
      EquipmentCategory equipmentCategory=_equipmentCategoryEnum.getEntry(equipmentCategoryCode);
      ret.setEquipmentCategory(equipmentCategory);
    }
    return ret;
  }

  private EquipmentLocation getSlot(PropertiesSet properties)
  {
    Integer defaultSlotInt=(Integer)properties.getProperty("Inventory_DefaultSlot");
    int defaultSlot=(defaultSlotInt!=null)?defaultSlotInt.intValue():0;
    EquipmentLocation slot=DatEnumsUtils.getSlot(defaultSlot);
    return slot;
  }

  private void handleMunging(PropertiesSet properties)
  {
    //Integer level=(Integer)properties.getProperty("Item_Level");
    Integer minMungingLevel=(Integer)properties.getProperty("ItemMunging_MinMungeLevel");
    Integer maxMungingLevel=(Integer)properties.getProperty("ItemMunging_MaxMungeLevel");
    Integer progressionId=(Integer)properties.getProperty("ItemMunging_ItemLevelOverrideProgression");
    //Integer propertyId=(Integer)properties.getProperty("ItemMunging_ItemLevelOverrideProperty");
    if (((minMungingLevel!=null) && (minMungingLevel.intValue()>0))
        || ((maxMungingLevel!=null) && (maxMungingLevel.intValue()>0))
        || (progressionId!=null))
    {
      // Remove entries with min=-1,max=-1 (and progression ID!=null)
      if ((minMungingLevel!=null) && (minMungingLevel.intValue()<0) &&
          (maxMungingLevel!=null) && (maxMungingLevel.intValue()<0))
      {
        return;
      }
      Progression progression=null;
      if (progressionId!=null)
      {
        progression=ProgressionUtils.getProgression(_facade,progressionId.intValue());
      }
      Munging munging=new Munging(minMungingLevel,maxMungingLevel,progression);
      _currentItem.setMunging(munging);
      //String name=_currentItem.getName();
      //Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
      //Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
      //System.out.println(_currentId+"\t"+name+"\t"+level+"\t"+progressionId+"\t"+propertyId+"\t"+minMungingLevel+"\t"+maxMungingLevel+"\t"+minLevel+"\t"+maxLevel);
    }
  }

  private Integer getItemLevelOffset(Item item, PropertiesSet properties)
  {
    Integer ret=null;
    Integer distributionType=(Integer)properties.getProperty("Item_DistributionType");
    if (distributionType!=null)
    {
      Integer offset=_itemLevelOffsets.get(distributionType);
      if ((offset!=null) && (offset.intValue()!=0))
      {
        ret=offset;
      }
    }
    return ret;
  }

  private ItemBinding getBinding(PropertiesSet properties)
  {
    Integer bindOnAcquire=(Integer)properties.getProperty("Inventory_BindOnAcquire");
    if ((bindOnAcquire!=null) && (bindOnAcquire.intValue()==1))
    {
      Integer bindToAccount=(Integer)properties.getProperty("Inventory_BindToAccount");
      if ((bindToAccount!=null) && (bindToAccount.intValue()==1))
      {
        return ItemBindings.BOUND_TO_ACCOUNT_ON_ACQUIRE;
      }
      return ItemBindings.BIND_ON_ACQUIRE;
    }
    Integer bindOnEquip=(Integer)properties.getProperty("Inventory_BindOnEquip");
    if ((bindOnEquip!=null) && (bindOnEquip.intValue()==1)) return ItemBindings.BIND_ON_EQUIP;
    return null;
  }

  private int getType(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return classDefIndex;
    }
    return 0;
  }

  private boolean useId(int type)
  {
    for(int i=0;i<TYPES.length;i++)
    {
      if (TYPES[i]==type)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Load items, legacies, passives, consumables.
   */
  public void doIt()
  {
    if (_live)
    {
      // Legacies
      _legaciesLoader.loadLegacies();
      // Offsets
      _itemLevelOffsets=ItemLevelOffsetsUtils.buildOffsetsMap(_facade);
    }
    // Items
    List<Item> items=new ArrayList<Item>();
    List<Legendary2> legendaryItems=new ArrayList<Legendary2>();
    HashMap<Integer,Item> mapById=new HashMap<Integer,Item>();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      int type=getType(id);
      boolean useIt=useId(type);
      if (useIt)
      {
        Item newItem=load(id,type);
        if (newItem!=null)
        {
          items.add(newItem);
          mapById.put(Integer.valueOf(id),newItem);
          if (newItem instanceof Legendary2)
          {
            legendaryItems.add((Legendary2)newItem);
          }
        }
      }
    }
    // Field icons
    new FieldIconsLoader(_facade,mapById).doIt();
    // Consistency checks
    ConsistencyChecks checks=new ConsistencyChecks();
    checks.consistencyChecks(items);
    // Statistics
    ItemStatistics statistics=new ItemStatistics();
    statistics.showStatistics(items);
    // Save items
    /*boolean ok=*/ItemXMLWriter.writeItemsFile(GeneratedFiles.ITEMS,items);
    // Save legendary data
    LegendaryAttrs2XMLWriter.write(GeneratedFiles.LEGENDARY_ATTRS,legendaryItems);
    // Save socketables
    _socketablesManager.save();
    // Save progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    // Stats usage statistics
    System.out.println("Stats usage statistics (items):");
    _statUtils.showStatistics();
    // Save passives
    _passivesLoader.savePassives();
    // Save consumables
    _consumablesLoader.saveConsumables();
    // Save legacies
    if (_live)
    {
      _legaciesLoader.save();
    }
    // Save value tables
    ValueTablesXMLWriter.writeValueTablesFile(GeneratedFiles.VALUE_TABLES,_valueLoader.getTables());
    // Save DPS tables
    ValueTablesXMLWriter.writeValueTablesFile(GeneratedFiles.DPS_TABLES,_dpsLoader.getTables());
    // Save item cosmetics
    _cosmeticLoader.save();
    // Save labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    Locale.setDefault(Locale.ENGLISH);
    EffectLoader effectsLoader=new EffectLoader(facade);
    new MainDatItemsLoader(facade,effectsLoader).doIt();
    facade.dispose();
  }
}
