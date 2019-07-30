package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
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
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.legendary.Legendary;
import delta.games.lotro.lore.items.legendary.LegendaryAttrs;
import delta.games.lotro.lore.items.legendary.LegendaryItem;
import delta.games.lotro.lore.items.legendary.LegendaryWeapon;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.items.legendary.PassivesLoader;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.lore.items.ConsistencyChecks;
import delta.games.lotro.tools.lore.items.ItemStatistics;
import delta.games.lotro.tools.lore.items.complements.FactoryCommentsInjector;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get item definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsLoader.class);

  private static final int[] TYPES={2097, 2814, 799, 798, 797, 796, 795, /*794,*/ 804, 805, 802, 3663, 803, 815, 1722, 3924 };
  private DataFacade _facade;
  private int _currentId;
  private Item _currentItem;
  private PassivesLoader _passivesLoader;
  private ConsumablesLoader _consumablesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatItemsLoader(DataFacade facade)
  {
    _facade=facade;
    _passivesLoader=new PassivesLoader(_facade);
    _consumablesLoader=new ConsumablesLoader(_facade);
  }

  private boolean _debug=false;
  int nb=0;
  private Item load(int indexDataId)
  {
    Item item=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      _currentId=indexDataId;
      _debug=(_currentId==1879000000);
      if (_debug)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
        System.out.println(properties.dump());
      }
      Integer itemClassInt=(Integer)properties.getProperty("Item_Class");
      String name=DatUtils.getStringProperty(properties,"Name");
      name=DatUtils.fixName(name);
      if (!useItem(name,itemClassInt)) return null;
      nb++;
      item=buildItem(properties);
      _currentItem=item;
      // ID
      item.setIdentifier(indexDataId);
      // Name
      item.setName(name);
      // Item class
      int itemClass=itemClassInt.intValue();
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      if ((iconId!=null) || (backgroundIconId!=null))
      {
        String iconName=((iconId!=null)?iconId:"0")+"-"+((backgroundIconId!=null)?backgroundIconId:"0");
        item.setIcon(iconName);
        File iconFile=new File(GeneratedFiles.ITEM_ICONS_DIR,iconName+".png").getAbsoluteFile();
        if (!iconFile.exists())
        {
          if ((iconId!=null) && (backgroundIconId!=null))
          {
            DatIconsUtils.buildImageFile(_facade,iconId.intValue(),backgroundIconId.intValue(),iconFile);
          }
          else if (iconId==null)
          {
            DatIconsUtils.buildImageFile(_facade,backgroundIconId.intValue(),iconFile);
          }
          else if (backgroundIconId==null)
          {
            DatIconsUtils.buildImageFile(_facade,iconId.intValue(),iconFile);
          }
        }
      }
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
      // Essence slots
      Integer essenceSlots=(Integer)properties.getProperty("Item_Socket_Count");
      if ((essenceSlots!=null) && (essenceSlots.intValue()>0))
      {
        item.setEssenceSlots(essenceSlots.intValue());
      }
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
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
        item.setQuality(getQuality(quality.intValue()));
      }
      // Category
      String category=_facade.getEnumsManager().resolveEnum(0x23000036,itemClass);
      item.setSubCategory(category);
      // Classify essences
      if (itemClass==235)
      {
        classifyEssence(item,properties);
      }
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
          Float computedArmourValue=armorStatProvider.getStatValue(1,level.intValue());
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
      if (description!=null)
      {
        item.setDescription(description.trim());
      }
      // Class requirements
      item.setRequiredClass(getRequiredClass(properties));
      // Stats providers
      DatStatUtils.doFilterStats=true;
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
        BasicStatsSet stats=statsProvider.getStats(1,level.intValue(),true);
        item.getStats().addStats(stats);
      }
      if (item instanceof Weapon)
      {
        loadWeaponSpecifics((Weapon)item,properties);
      }
      // Handle legendaries
      DatStatUtils.doFilterStats=false;
      handleLegendaries(item, properties);
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
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
    if (name.startsWith("Test ")) return false;
    int itemClass=itemClassInt.intValue();
    if ((itemClass==230) || (itemClass==231) || (itemClass==232)) return false;
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
      if (armourType==ArmourType.HEAVY_SHIELD)
      {
        // +10% Ranged defence
        ConstantStatProvider provider=new ConstantStatProvider(WellKnownStat.RANGED_DEFENCE_PERCENTAGE,-10);
        statsProvider.addStatProvider(provider);
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,WellKnownStat.CRITICAL_DEFENCE,1879260945);
        statsProvider.addStatProvider(critDef);
      }
      else if (armourType==ArmourType.SHIELD)
      {
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,WellKnownStat.CRITICAL_DEFENCE,1879211641);
        statsProvider.addStatProvider(critDef);
      }
      else if (armourType==ArmourType.WARDEN_SHIELD)
      {
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,WellKnownStat.CRITICAL_DEFENCE,1879260947);
        statsProvider.addStatProvider(critDef);
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
  }

  private StatsProvider handleEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+0x9000000);
    Object probability=effectProps.getProperty("Effect_ConstantApplicationProbability");
    if ((probability!=null) && (probability.equals(Float.valueOf(1.0f))))
    {
      Integer permanent=(Integer)effectProps.getProperty("Effect_Duration_Permanent");
      if ((permanent!=null) && (permanent.intValue()==1))
      {
        return DatStatUtils.buildStatProviders(_facade,effectProps);
      }
    }
    return null;
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
      if (combatPropertyModDid!=null)
      {
        LegendaryAttrs attrs=legendary.getLegendaryAttrs();
        attrs.setMainLegacyId(combatPropertyModDid);
      }
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
    weapon.setMaxDamage((int)maxDamage);
    //Combat_DamageVariance: 0.4 => Min damage is 60% of max damage
    // Min DPS
    float variance=((Float)properties.getProperty("Combat_DamageVariance")).floatValue();
    float minDamage=maxDamage*(1-variance);
    weapon.setMinDamage((int)minDamage);
    // Damage type
    int damageTypeEnum=((Integer)properties.getProperty("Combat_DamageType")).intValue();
    weapon.setDamageType(DatEnumsUtils.getDamageType(damageTypeEnum));
  }

  private long getEquipmentCategory(PropertiesSet properties)
  {
    Long ret=(Long)properties.getProperty("Item_EquipmentCategory");
    return (ret!=null)?ret.longValue():0;
  }

  private float computeDps(int itemLevel, ItemQuality quality, PropertiesSet properties)
  {
    float ret=0;
    // Compute DPS from the DPS LUT table...
    Integer dpsLut=(Integer)properties.getProperty("Combat_DPS_LUT");
    if (dpsLut!=null)
    {
      PropertiesSet dpsLutProperties=_facade.loadProperties(dpsLut.intValue()+0x9000000);
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

  private Item buildItem(PropertiesSet properties)
  {
    //EquipmentLocation slot=null;
    WeaponType weaponType=null;
    ArmourType armourType=null;
    long equipmentCategory=getEquipmentCategory(properties);
    if (equipmentCategory==0)
    {
      // Undefined
    }
    else if (equipmentCategory==1)
    {
      //slot=EquipmentLocation.EAR;
    }
    else if (equipmentCategory==1L<<1)
    {
      //slot=EquipmentLocation.POCKET;
    }
    else if (equipmentCategory==1L<<2)
    {
      weaponType=WeaponType.TWO_HANDED_SWORD;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<3)
    {
      weaponType=WeaponType.TWO_HANDED_CLUB;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<4)
    {
      //weaponType=WeaponType.TWO_HANDED_MACE;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<5)
    {
      weaponType=WeaponType.TWO_HANDED_AXE;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<6)
    {
      // Instrument
    }
    else if (equipmentCategory==1L<<7)
    {
      weaponType=WeaponType.BOW;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<8)
    {
      armourType=ArmourType.MEDIUM;
    }
    else if (equipmentCategory==1L<<9)
    {
      armourType=ArmourType.HEAVY;
    }
    else if (equipmentCategory==1L<<10)
    {
      armourType=ArmourType.HEAVY_SHIELD;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<11)
    {
      weaponType=WeaponType.ONE_HANDED_HAMMER;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<12)
    {
      weaponType=WeaponType.SPEAR;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<13)
    {
      weaponType=WeaponType.CROSSBOW;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<14)
    {
      weaponType=WeaponType.TWO_HANDED_HAMMER;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<15)
    {
      weaponType=WeaponType.HALBERD;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<16)
    {
      armourType=ArmourType.SHIELD;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<17)
    {
      armourType=ArmourType.LIGHT;
    }
    else if (equipmentCategory==1L<<18) // Ring
    {
      //slot=EquipmentLocation.FINGER;
    }
    else if (equipmentCategory==1L<<19)
    {
      weaponType=WeaponType.DAGGER;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<20) // Craft Tool
    {
      //slot=EquipmentLocation.TOOL;
    }
    else if (equipmentCategory==1L<<21)
    {
      weaponType=WeaponType.STAFF;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<22) // Necklace
    {
      //slot=EquipmentLocation.NECK;
    }
    else if (equipmentCategory==1L<<23)
    {
      weaponType=WeaponType.ONE_HANDED_AXE;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<24) // Class Item
    {
      //slot=EquipmentLocation.CLASS_SLOT;
    }
    else if (equipmentCategory==1L<<25)
    {
      weaponType=WeaponType.ONE_HANDED_CLUB;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<26)
    {
      weaponType=WeaponType.ONE_HANDED_MACE;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<27)
    {
      weaponType=WeaponType.ONE_HANDED_SWORD;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<28) // Thrown Weapon
    {
      //
    }
    else if (equipmentCategory==1L<<29) // Armband
    {
      //slot=EquipmentLocation.WRIST;
    }
    else if (equipmentCategory==1L<<30) // Cloak
    {
      armourType=ArmourType.LIGHT;
      //slot=EquipmentLocation.BACK;
    }
    else if (equipmentCategory==1L<<31) // Cosmetic
    {
      // No slot
    }
    else if (equipmentCategory==1L<<33) // Two-handed implement
    {
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<36) // One-handed implement
    {
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<38)
    {
      weaponType=WeaponType.RUNE_STONE;
      //slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<39)
    {
      armourType=ArmourType.WARDEN_SHIELD;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<40)
    {
      weaponType=WeaponType.JAVELIN;
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<42)
    {
      // Oath-bound Armaments
      //slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<43)
    {
      // War-steed Item
      //slot=EquipmentLocation.BRIDLE;
    }
    else
    {
      System.out.println("Unmanaged equipment category " + equipmentCategory+" for: "+_currentId);
    }
    // Legendary stuff?
    Integer isAdvancementItem=(Integer)properties.getProperty("ItemAdvancement_Item");
    boolean isLegendary=((isAdvancementItem!=null) && (isAdvancementItem.intValue()==1));

    Item ret=null;
    if (weaponType!=null)
    {
      Weapon weapon=(isLegendary?new LegendaryWeapon():new Weapon());
      weapon.setWeaponType(weaponType);
      ret=weapon;
    }
    else if (armourType!=null)
    {
      Armour armour=new Armour();
      armour.setArmourType(armourType);
      ret=armour;
    }
    else
    {
      ret=(isLegendary?new LegendaryItem():new Item());
    }
    //ret.setEquipmentLocation(slot);
    return ret;
  }

  private EquipmentLocation getSlot(PropertiesSet properties)
  {
    Integer defaultSlotInt=(Integer)properties.getProperty("Inventory_DefaultSlot");
    int defaultSlot=(defaultSlotInt!=null)?defaultSlotInt.intValue():0;

    if ((defaultSlot&1L<<1)!=0) return EquipmentLocation.HEAD;
    if ((defaultSlot&1L<<2)!=0) return EquipmentLocation.CHEST;
    if ((defaultSlot&1L<<3)!=0) return EquipmentLocation.LEGS;
    if ((defaultSlot&1L<<4)!=0) return EquipmentLocation.HAND;
    if ((defaultSlot&1L<<5)!=0) return EquipmentLocation.FEET;
    if ((defaultSlot&1L<<6)!=0) return EquipmentLocation.SHOULDER;
    if ((defaultSlot&1L<<7)!=0) return EquipmentLocation.BACK;
    if (((defaultSlot&1L<<8)!=0) || ((defaultSlot&1L<<9)!=0)) return EquipmentLocation.WRIST;
    if ((defaultSlot&1L<<10)!=0) return EquipmentLocation.NECK;
    if (((defaultSlot&1L<<11)!=0) || ((defaultSlot&1L<<12)!=0)) return EquipmentLocation.FINGER;
    if (((defaultSlot&1L<<13)!=0) || ((defaultSlot&1L<<14)!=0)) return EquipmentLocation.EAR;
    if ((defaultSlot&1L<<15)!=0) return EquipmentLocation.POCKET;
    if ((defaultSlot&1L<<16)!=0) return EquipmentLocation.MAIN_HAND;
    if ((defaultSlot&1L<<17)!=0) return EquipmentLocation.OFF_HAND;
    if ((defaultSlot&1L<<18)!=0) return EquipmentLocation.RANGED_ITEM;
    if ((defaultSlot&1L<<19)!=0) return EquipmentLocation.TOOL;
    if ((defaultSlot&1L<<20)!=0) return EquipmentLocation.CLASS_SLOT;
    if ((defaultSlot&1L<<21)!=0) return EquipmentLocation.BRIDLE;
    return null;
  }

  private CharacterClass getRequiredClass(PropertiesSet properties)
  {
    Object[] classReqs=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (classReqs!=null)
    {
      int characterClassId=((Integer)classReqs[0]).intValue();
      CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(characterClassId);
      return characterClass;
    }
    return null;
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

  private ItemQuality getQuality(int qualityEnum)
  {
    if (qualityEnum==1) return ItemQuality.LEGENDARY;
    if (qualityEnum==2) return ItemQuality.RARE;
    if (qualityEnum==3) return ItemQuality.INCOMPARABLE;
    if (qualityEnum==4) return ItemQuality.UNCOMMON;
    if (qualityEnum==5) return ItemQuality.COMMON;
    return null;
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

  private void classifyEssence(Item essence, PropertiesSet properties)
  {
    Integer overlay=(Integer)properties.getProperty("Icon_Layer_OverlayDID");
    String category=null;
    if (overlay==null) category="Essence";
    else
    {
      String name=essence.getName();
      if ((name!=null) && (name.contains("Mordor - Essences")))
      {
        category="Box of Essences";
      }
      else if (overlay.intValue()==1091914756) category="Essence:Tier1";
      else if (overlay.intValue()==1091914773) category="Essence:Tier2";
      else if (overlay.intValue()==1091914770) category="Essence:Tier3";
      else if (overlay.intValue()==1091914772) category="Essence:Tier4";
      else if (overlay.intValue()==1091914776) category="Essence:Tier5";
      else if (overlay.intValue()==1091914767) category="Essence:Tier6";
      else if (overlay.intValue()==1091914762) category="Essence:Tier7";
      else if (overlay.intValue()==1091914765) category="Essence:Tier8";
      else if (overlay.intValue()==1091914774) category="Essence:Tier9";
      else if (overlay.intValue()==1091914766) category="Essence:Tier10";
      else if (overlay.intValue()==1092396132) category="Essence:Tier11";
      else if (overlay.intValue()==1092396316) category="Essence:Tier12";
      else
      {
        LOGGER.warn("Unmanaged essence overlay: "+overlay+" for "+name);
      }
    }
    if (category!=null)
    {
      essence.setSubCategory(category);
    }
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      for(int i=0;i<TYPES.length;i++)
      {
        if (TYPES[i]==classDefIndex)
        {
          return true;
        }
      }
    }
    return false;
  }

  private void doIt()
  {
    List<Item> items=new ArrayList<Item>();

    HashMap<Integer,Item> mapById=new HashMap<Integer,Item>();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        Item newItem=load(id);
        if (newItem!=null)
        {
          items.add(newItem);
          mapById.put(Integer.valueOf(id),newItem);
        }
      }
    }
    // Custom data injection
    FactoryCommentsInjector injector=new FactoryCommentsInjector(mapById);
    injector.doIt();
    // Consistency checks
    ConsistencyChecks checks=new ConsistencyChecks();
    checks.consistencyChecks(items);
    // Statistics
    ItemStatistics statistics=new ItemStatistics();
    statistics.showStatistics(items);
    // Save items
    /*boolean ok=*/ItemXMLWriter.writeItemsFile(GeneratedFiles.ITEMS,items);
    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    // Stats usage statistics
    DatStatUtils._statsUsageStatistics.showResults();
    // Save passives
    _passivesLoader.savePassives();
    // Save consumables
    _consumablesLoader.saveConsumables();
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
