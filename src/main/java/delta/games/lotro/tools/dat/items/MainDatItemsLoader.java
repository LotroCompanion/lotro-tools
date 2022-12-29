package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.EquipmentCategory;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.enums.ItemClassUtils;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.common.money.QualityBasedValueLookupTable;
import delta.games.lotro.common.money.io.xml.ValueTablesXMLWriter;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.carryalls.CarryAll;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.lore.items.legendary.LegendaryAttrs;
import delta.games.lotro.lore.items.legendary.LegendaryItem;
import delta.games.lotro.lore.items.legendary.LegendaryWeapon;
import delta.games.lotro.lore.items.legendary2.EnhancementRune;
import delta.games.lotro.lore.items.legendary2.Legendary2;
import delta.games.lotro.lore.items.legendary2.LegendaryAttrs2;
import delta.games.lotro.lore.items.legendary2.LegendaryItem2;
import delta.games.lotro.lore.items.legendary2.LegendaryWeapon2;
import delta.games.lotro.lore.items.legendary2.SocketEntry;
import delta.games.lotro.lore.items.legendary2.SocketsSetup;
import delta.games.lotro.lore.items.legendary2.Tracery;
import delta.games.lotro.lore.items.legendary2.io.xml.EnhancementRunesXMLWriter;
import delta.games.lotro.lore.items.legendary2.io.xml.LegendaryAttrs2XMLWriter;
import delta.games.lotro.lore.items.legendary2.io.xml.TraceriesXMLWriter;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.items.legendary.LegaciesLoader;
import delta.games.lotro.tools.dat.items.legendary.PassivesLoader;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.RequirementsLoadingUtils;
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

  private static final int[] OVERLAY_FOR_TIER=
  {
    1091914756, 1091914773, 1091914770, 1091914772, 1091914776, // 1-5
    1091914767, 1091914762, 1091914765, 1091914774, 1091914766, // 6-10
    1092396132, 1092396316, 1092508824, 1092694659 // 11-14
  };

  private DataFacade _facade;
  private Item _currentItem;
  private PassivesLoader _passivesLoader;
  private ConsumablesLoader _consumablesLoader;
  private LegaciesLoader _legaciesLoader;
  private ItemValueLoader _valueLoader;
  private Map<Integer,Integer> _itemLevelOffsets;
  private LotroEnum<SocketType> _socketTypes;
  private List<Tracery> _traceries;
  private List<EnhancementRune> _enhancementRunes;
  private EnumMapper _uniquenessChannel;
  private ItemSortingDataLoader _sortDataLoader;
  private LotroEnum<ItemClass> _itemClassEnum;
  private LotroEnum<EquipmentCategory> _equipmentCategoryEnum;
  private ItemDetailsLoader _detailsLoader;
  private CosmeticLoader _cosmeticLoader;
  private boolean _live;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatItemsLoader(DataFacade facade)
  {
    _facade=facade;
    _live=Context.isLive();
    _passivesLoader=new PassivesLoader(_facade);
    _consumablesLoader=new ConsumablesLoader(_facade);
    _legaciesLoader=new LegaciesLoader(_facade);
    _valueLoader=new ItemValueLoader(_facade);
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    _socketTypes=enumsRegistry.get(SocketType.class);
    _traceries=new ArrayList<Tracery>();
    _enhancementRunes=new ArrayList<EnhancementRune>();
    _uniquenessChannel=facade.getEnumsManager().getEnumMapper(587203643);
    _sortDataLoader=new ItemSortingDataLoader(facade);
    _itemClassEnum=LotroEnumsRegistry.getInstance().get(ItemClass.class);
    _equipmentCategoryEnum=LotroEnumsRegistry.getInstance().get(EquipmentCategory.class);
    _detailsLoader=new ItemDetailsLoader(_facade);
    _cosmeticLoader=new CosmeticLoader();
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
      if (!useItem(name,itemClassCode)) return null;
      item=buildItem(properties);
      _currentItem=item;
      // ID
      item.setIdentifier(indexDataId);
      // Name
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
      // Loot
      /*
      Integer lootId=(Integer)properties.getProperty("LootGen_CustomSkirmishLootLookupTable");
      if ((lootId!=null) && (lootId.intValue()!=0))
      {
        System.out.println(lootId+" => "+item);
      }
      */
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
            //System.out.println("Updated the min level for: "+_currentItem+" "+level+" => "+minScaledLevel);
            level=minScaledLevel;
            item.setItemLevel(level);
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
      // Classify socketables (essences, traceries, enhancement runes) 
      if (itemClassCode.intValue()==ItemClassUtils.ESSENCE_CODE)
      {
        int code=classifySocketable(item,properties);
        itemClass=_itemClassEnum.getEntry(code);
      }
      item.setItemClass(itemClass);
      // Armour value
      StatProvider armorStatProvider=null;
      Integer armourValue=(Integer)properties.getProperty("Item_Armor_Value");
      if ((armourValue!=null) && (armourValue.intValue()>0))
      {
        // Armour progression...
        Integer armourProgressId=(Integer)properties.getProperty("Item_Armor_Value_Lookup_Table");
        if (armourProgressId!=null)
        {
          armorStatProvider=DatStatUtils.buildStatProvider(_facade,WellKnownStat.ARMOUR,armourProgressId.intValue());
          int statsLevel=item.getItemLevelForStats().intValue();
          Float computedArmourValue=armorStatProvider.getStatValue(1,statsLevel);
          if (Math.abs(armourValue.intValue()-computedArmourValue.floatValue())>1)
          {
            //System.out.println("Delta in armour for "+_currentItem+": got "+computedArmourValue+", expected "+armourValue);
          }
        }
      }
      // Sturdiness
      Integer durabilityEnum=(Integer)properties.getProperty("Item_DurabilityEnum");
      if (durabilityEnum!=null)
      {
        item.setSturdiness(getSturdiness(durabilityEnum.intValue()));
      }
      // Description
      String description=DatUtils.getStringProperty(properties,"Description");
      description=StringUtils.removeMarks(description);
      item.setDescription(description);
      // Requirements
      // - class
      RequirementsLoadingUtils.loadRequiredClasses(properties,item.getUsageRequirements());
      // - race
      RequirementsLoadingUtils.loadRequiredRaces(properties,item.getUsageRequirements());
      // - faction
      RequirementsLoadingUtils.loadRequiredFaction(properties,item.getUsageRequirements());
      // Stats providers
      DatStatUtils._doFilterStats=false;
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(_facade,properties);
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
      // Item fixes
      itemFixes(item,statsProvider);
      // Stats
      if (level!=null)
      {
        int statsLevel=item.getItemLevelForStats().intValue();
        BasicStatsSet stats=statsProvider.getStats(1,statsLevel);
        item.getStats().addStats(stats);
      }
      if (item instanceof Weapon)
      {
        loadWeaponSpecifics((Weapon)item,properties);
      }
      if (item instanceof CarryAll)
      {
        loadCarryAllSpecifics((CarryAll)item,properties);
      }
      // Handle legendaries
      DatStatUtils._doFilterStats=false;
      handleLegendaries(item, properties);
      // Value
      handleItemValue(item,properties);
      // Details
      _detailsLoader.handleItem(item,properties);
      // Cosmetics
      _cosmeticLoader.handleItem(item,properties,type);
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
        SocketType socketType=getSocketType(socketTypeCode);
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
      item.setEssenceSlots(nbSlots);
    }
    //System.out.println("Got new legendary item: "+item+" with "+setup.getSocketsCount()+" slots");
  }

  private CharacterClass getRequiredClass(SocketType socketType)
  {
    int code=socketType.getCode();
    if (code==6) return CharacterClass.BEORNING;
    if (code==7) return CharacterClass.BRAWLER;
    if (code==8) return CharacterClass.BURGLAR;
    if (code==9) return CharacterClass.CAPTAIN;
    if (code==10) return CharacterClass.CHAMPION;
    if (code==11) return CharacterClass.GUARDIAN;
    if (code==12) return CharacterClass.HUNTER;
    if (code==13) return CharacterClass.LORE_MASTER;
    if (code==14) return CharacterClass.MINSTREL;
    if (code==15) return CharacterClass.RUNE_KEEPER;
    if (code==16) return CharacterClass.WARDEN;
    return null;
  }

  private SocketType getSocketType(int code)
  {
    List<SocketType> types=_socketTypes.getFromBitSet(code);
    if (types.size()!=1)
    {
      LOGGER.warn("Unsupported socket type code: "+code);
      return null;
    }
    return types.get(0);
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
      QualityBasedValueLookupTable table=_valueLoader.getTable(itemValueTableId.intValue());
      if (table!=null)
      {
        ItemQuality quality=item.getQuality();
        Integer itemLevel=item.getItemLevel();
        if (itemLevel!=null)
        {
          Integer valueFromTable=table.getValue(quality,itemLevel.intValue());
          if (valueFromTable!=null)
          {
            itemValueFromTable=Integer.valueOf(valueFromTable.intValue());
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

  private boolean useItem(String name, Integer itemClassInt)
  {
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

  private void itemFixes(Item item, StatsProvider statsProvider)
  {
    if (item instanceof Weapon)
    {
      Weapon weapon=(Weapon)item;
      WeaponType weaponType=weapon.getWeaponType();
      if (weaponType==WeaponType.ONE_HANDED_SWORD)
      {
        // +1% parry
        ConstantStatProvider provider=new ConstantStatProvider(WellKnownStat.PARRY_PERCENTAGE,1);
        statsProvider.addStatProvider(provider);
      }
      else if (weaponType==WeaponType.TWO_HANDED_SWORD)
      {
        // +2% parry
        ConstantStatProvider provider=new ConstantStatProvider(WellKnownStat.PARRY_PERCENTAGE,2);
        statsProvider.addStatProvider(provider);
      }
    }
    else if (item instanceof Armour)
    {
      Armour armour=(Armour)item;
      ArmourType armourType=armour.getArmourType();
      if ((armourType==ArmourType.HEAVY_SHIELD) || (armourType==ArmourType.SHIELD) || (armourType==ArmourType.WARDEN_SHIELD))
      {
        // +10% Ranged defence
        ConstantStatProvider provider=new ConstantStatProvider(WellKnownStat.RANGED_DEFENCE_PERCENTAGE,-10);
        provider.setDescriptionOverride("+10% Ranged Defence");
        statsProvider.addStatProvider(provider);
        // Critical defence
        if (_live)
        {
          StatProvider critDef=DatStatUtils.buildStatProvider(_facade,WellKnownStat.CRITICAL_DEFENCE,1879260945);
          statsProvider.addStatProvider(critDef);
        }
      }
    }
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
      return DatStatUtils.buildStatProviders(_facade,effectProps);
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
      // Seem that each legendary item (4764 occurrences) has either ItemAdvancement_CombatDPSLevel or ItemAdvancement_CombatPropertyModLevel
      // depending on whether it has a DPS main legacy of a non-DPS main legacy
      Integer combatDpsLevel=(Integer)properties.getProperty("ItemAdvancement_CombatDPSLevel");
      Integer combatPropertyModLevel=(Integer)properties.getProperty("ItemAdvancement_CombatPropertyModLevel");
      if (combatDpsLevel!=null)
      {
        attrs.setMainLegacyBaseRank(combatDpsLevel.intValue());
        //System.out.println("Found ItemAdvancement_CombatDPSLevel="+combatDpsLevel+" for "+item);
      }
      else if (combatPropertyModLevel!=null)
      {
        attrs.setMainLegacyBaseRank(combatPropertyModLevel.intValue());
        //System.out.println("Found ItemAdvancement_CombatPropertyModLevel="+combatPropertyModLevel+" for "+item);
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

  private void loadWeaponSpecifics(Weapon weapon, PropertiesSet properties)
  {
    int itemLevel=weapon.getItemLevel().intValue();
    float baseDPS=((Float)properties.getProperty("Combat_BaseDPS")).floatValue();
    // Checks
    {
      int levelForChecks=itemLevel;
      Integer level=(Integer)properties.getProperty("ItemAdvancement_CombatPropertyModLevel");
      if (level!=null)
      {
        levelForChecks=itemLevel;
      }
      else
      {
        Integer levelDelta=(Integer)properties.getProperty("Item_MaxLevelUpgrades");
        if (levelDelta!=null)
        {
          levelForChecks-=levelDelta.intValue();
        }
      }
      float computedDps=computeDps(levelForChecks,weapon.getQuality(),properties);
      if (Math.abs(baseDPS-computedDps)>0.01)
      {
        //System.out.println("Bad DPS computation: got "+computedDps+", expected: "+baseDPS);
      }
    }
    weapon.setDPS(baseDPS);
    // Max DPS
    float maxDamage=((Float)properties.getProperty("Combat_Damage")).floatValue();
    weapon.setMaxDamage(Math.round(maxDamage));
    //Combat_DamageVariance: 0.4 => Min damage is 60% of max damage
    // Min DPS
    float variance=((Float)properties.getProperty("Combat_DamageVariance")).floatValue();
    float minDamage=maxDamage*(1-variance);
    weapon.setMinDamage(Math.round(minDamage));
    // Damage type
    int damageTypeEnum=((Integer)properties.getProperty("Combat_DamageType")).intValue();
    weapon.setDamageType(DatEnumsUtils.getDamageType(damageTypeEnum));
  }

  private int getEquipmentCategory(PropertiesSet properties)
  {
    Long value=(Long)properties.getProperty("Item_EquipmentCategory");
    long code=(value!=null)?value.longValue():0;
    if (code!=0)
    {
      long mask=1;
      for(int i=1;i<=64;i++)
      {
        if ((code&mask)!=0)
        {
          return i;
        }
        mask<<=1;
      }
    }
    return 0;
  }

  private float computeDps(int itemLevel, ItemQuality quality, PropertiesSet properties)
  {
    float ret=0;
    // Compute DPS from the DPS LUT table...
    Integer dpsLut=(Integer)properties.getProperty("Combat_DPS_LUT");
    if (dpsLut!=null)
    {
      PropertiesSet dpsLutProperties=_facade.loadProperties(dpsLut.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      Object[] dpsArray=(Object[])dpsLutProperties.getProperty("Combat_BaseDPSArray");
      float baseDPSFromTable=((Float)(dpsArray[itemLevel-1])).floatValue();
      Object[] qualityFactors=(Object[])dpsLutProperties.getProperty("Combat_QualityModArray");
      if (qualityFactors!=null)
      {
        int qualityEnum=getQualityEnum(quality);
        for(int i=0;i<qualityFactors.length;i++)
        {
          PropertiesSet qualityProps=(PropertiesSet)qualityFactors[i];
          if (qualityEnum==((Integer)qualityProps.getProperty("Combat_Quality")).intValue())
          {
            float dpsFactor=((Float)qualityProps.getProperty("Combat_DPSMod")).floatValue();
            baseDPSFromTable*=dpsFactor;
            break;
          }
        }
      }
      ret=baseDPSFromTable;
    }
    return ret;
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
    int equipmentCategoryCode=getEquipmentCategory(properties);
    WeaponType weaponType=DatEnumsUtils.getWeaponTypeFromEquipmentCategory(equipmentCategoryCode);
    ArmourType armourType=DatEnumsUtils.getArmourTypeFromEquipmentCategory(equipmentCategoryCode);
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
        ret=new Item();
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
      Progression progression=null;
      if (progressionId!=null)
      {
        progression=DatStatUtils.getProgression(_facade,progressionId.intValue());
      }
      Munging munging=new Munging(minMungingLevel,maxMungingLevel,progression);
      String mungingSpec=munging.asString();
      _currentItem.setProperty(ItemPropertyNames.MUNGING,mungingSpec);
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

  private int getQualityEnum(ItemQuality quality)
  {
    if (quality==ItemQuality.LEGENDARY) return 1;
    if (quality==ItemQuality.RARE) return 2;
    if (quality==ItemQuality.INCOMPARABLE) return 3;
    if (quality==ItemQuality.UNCOMMON) return 4;
    if (quality==ItemQuality.COMMON) return 5;
    return 0;
  }

  private ItemSturdiness getSturdiness(int durabilityEnum)
  {
    //{0=Undef, 1=Substantial, 2=Brittle, 3=Normal, 4=Tough, 5=Flimsy, 6=, 7=Weak}
    //if (durabilityEnum==0) return ItemSturdiness.UNDEFINED;
    if (durabilityEnum==1) return ItemSturdiness.SUBSTANTIAL;
    if (durabilityEnum==2) return ItemSturdiness.BRITTLE;
    if (durabilityEnum==3) return ItemSturdiness.NORMAL;
    if (durabilityEnum==4) return ItemSturdiness.TOUGH;
    //if (durabilityEnum==5) return ItemSturdiness.FLIMSY;
    //if (durabilityEnum==6) return ???;
    if (durabilityEnum==7) return ItemSturdiness.WEAK;
    return null;
  }

  private ItemBinding getBinding(PropertiesSet properties)
  {
    Integer bindOnAcquire=(Integer)properties.getProperty("Inventory_BindOnAcquire");
    if ((bindOnAcquire!=null) && (bindOnAcquire.intValue()==1))
    {
      Integer bindToAccount=(Integer)properties.getProperty("Inventory_BindToAccount");
      if ((bindToAccount!=null) && (bindToAccount.intValue()==1))
      {
        return ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE;
      }
      return ItemBinding.BIND_ON_ACQUIRE;
    }
    Integer bindOnEquip=(Integer)properties.getProperty("Inventory_BindOnEquip");
    if ((bindOnEquip!=null) && (bindOnEquip.intValue()==1)) return ItemBinding.BIND_ON_EQUIP;
    return null;
  }

  private int classifySocketable(Item item, PropertiesSet properties)
  {
    Integer overlay=(Integer)properties.getProperty("Icon_Layer_OverlayDID");
    if (overlay==null)
    {
      return ItemClassUtils.ESSENCE_CODE;
    }
    String name=item.getName();
    if ((name!=null) && (name.contains("Mordor - Essences")))
    {
      return ItemClassUtils.getBoxOfEssenceCode();
    }
    int nbOverlays=OVERLAY_FOR_TIER.length;
    int tier=0;
    for(int i=0;i<nbOverlays;i++)
    {
      if (overlay.intValue()==OVERLAY_FOR_TIER[i])
      {
        tier=i+1;
        break;
      }
    }
    if (tier==0)
    {
      LOGGER.warn("Unmanaged essence/tracery overlay: "+overlay+" for "+name);
    }
    int code=getSocketableItemClass(item,properties,tier);
    return code;
  }

  private int getSocketableItemClass(Item item, PropertiesSet properties, int tier)
  {
    Long type=(Long)properties.getProperty("Item_Socket_Type");
    if (type==null)
    {
      LOGGER.warn("Expected an Item_Socket_Type property for item: "+item);
      return 0;
    }
    if (type.intValue()==0)
    {
      handleEnhancementRune(item,properties);
      return ItemClassUtils.getEnhancementRuneCode(tier);
    }
    SocketType socketType=getSocketType(type.intValue());
    if (socketType==null)
    {
      LOGGER.warn("Unexpected socket type: "+type+" for item: "+item);
      return 0;
    }
    int socketTypeCode=socketType.getCode();
    if (socketTypeCode==1)
    {
      return ItemClassUtils.getEssenceCode(tier);
    }
    if (socketTypeCode==18)
    {
      return ItemClassUtils.getEssenceOfWarCode(tier);
    }
    if (socketTypeCode==19)
    {
      return ItemClassUtils.getCloakEssenceCode(tier);
    }
    if (socketTypeCode==20)
    {
      return ItemClassUtils.getNecklaceEssenceCode(tier);
    }
    handleTracery(item,socketType,properties);
    if (socketTypeCode==3)
    {
      return ItemClassUtils.getHeraldicTraceryCode(tier);
    }
    else if (socketTypeCode==4)
    {
      return ItemClassUtils.getWordOfPowerCode(tier);
    }
    else if (socketTypeCode==5)
    {
      return ItemClassUtils.getWordOfCraftCode(tier);
    }
    // 6-16
    else if ((socketTypeCode>=6) && (socketTypeCode<=16))
    {
      return ItemClassUtils.getWordOfMasteryCode(tier);
    }
    else //if (socketTypeCode==2)
    {
      LOGGER.warn("Unmanaged socket type "+socketTypeCode+" for item: "+item);
      return 0;
    }
  }

  private void handleTracery(Item item, SocketType socketType, PropertiesSet props)
  {
    int minItemLevel=((Integer)props.getProperty("Item_Socket_GemMinLevel")).intValue();
    int maxItemLevel=((Integer)props.getProperty("Item_Socket_GemMaxLevel")).intValue();
    int levelupIncrement=((Integer)props.getProperty("Item_Socket_LevelupRuneIncrement")).intValue();
    int setId=((Integer)props.getProperty("Item_PropertySet")).intValue();
    String uniquenessChannel=null;
    Integer uniquenessChannelCode=(Integer)props.getProperty("Item_UniquenessChannel");
    if ((uniquenessChannelCode!=null) && (uniquenessChannelCode.intValue()!=0))
    {
      uniquenessChannel=_uniquenessChannel.getLabel(uniquenessChannelCode.intValue());
    }
    Tracery tracery=new Tracery(item,socketType,minItemLevel,maxItemLevel,levelupIncrement,setId,uniquenessChannel);
    _traceries.add(tracery);
    CharacterClass requiredClass=getRequiredClass(socketType);
    item.setRequiredClass(requiredClass);
  }

  private void handleEnhancementRune(Item item, PropertiesSet props)
  {
    int minItemLevel=((Integer)props.getProperty("Item_Socket_GemMinLevel")).intValue();
    int maxItemLevel=((Integer)props.getProperty("Item_Socket_GemMaxLevel")).intValue();
    int levelupIncrement=((Integer)props.getProperty("Item_Socket_LevelupRuneIncrement")).intValue();
    EnhancementRune enhancementRune=new EnhancementRune(item,minItemLevel,maxItemLevel,levelupIncrement);
    _enhancementRunes.add(enhancementRune);
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
    DatStatUtils.STATS_USAGE_STATISTICS.reset();
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
    // Save traceries
    Collections.sort(_traceries,new IdentifiableComparator<Tracery>());
    TraceriesXMLWriter.write(GeneratedFiles.TRACERIES,_traceries);
    // Save enhancement runes
    Collections.sort(_enhancementRunes,new IdentifiableComparator<EnhancementRune>());
    EnhancementRunesXMLWriter.write(GeneratedFiles.ENHANCEMENT_RUNES,_enhancementRunes);
    // Save progressions
    DatStatUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    // Stats usage statistics
    System.out.println("Stats usage statistics (items):");
    DatStatUtils.STATS_USAGE_STATISTICS.showResults();
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
    // Save item cosmetics
    _cosmeticLoader.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatItemsLoader(facade).doIt();
    facade.dispose();
  }
}
