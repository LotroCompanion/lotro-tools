package delta.games.lotro.tools.lore.items.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.stats.ItemLevelProgression;
import delta.games.lotro.utils.FixedDecimalsInteger;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.LinearInterpolatingProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get item definitions from DAT files.
 * @author DAM
 */
public class MainDatItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatItemsLoader.class);

  private DataFacade _facade;
  private int _currentId;
  private Item _currentItem;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatItemsLoader(DataFacade facade)
  {
    _facade=facade;
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
      _currentItem=ItemsManager.getInstance().getItem(_currentId);
      _debug=(_currentId==1879000000);
      if (_debug)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
        System.out.println(properties.dump());
      }
      nb++;
      item=buildItem(properties);
      int itemClass=((Integer)properties.getProperty("Item_Class")).intValue();
      // ID
      item.setIdentifier(indexDataId);
      // Name
      String name=getStringProperty(properties,"Name");
      name=fixName(name);
      item.setName(name);
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      item.setIcon(iconId+"-"+backgroundIconId);
      // Slot
      if (item.getEquipmentLocation()==null)
      {
        EquipmentLocation slot=getSlot(itemClass);
        item.setEquipmentLocation(slot);
      }
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
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
      // Armour value
      Integer armourValue=(Integer)properties.getProperty("Item_Armor_Value");
      if ((armourValue!=null) && (armourValue.intValue()>0))
      {
        if (item instanceof Armour)
        {
          ((Armour)item).setArmourValue(armourValue.intValue());
        }
        else
        {
          item.getStats().setStat(STAT.ARMOUR,armourValue.intValue());
        }
      }
      // Sturdiness
      Integer durabilityEnum=(Integer)properties.getProperty("Item_DurabilityEnum");
      if (durabilityEnum!=null)
      {
        item.setSturdiness(getSturdiness(durabilityEnum.intValue()));
      }
      // Description
      String description=getStringProperty(properties,"Description");
      if (description!=null)
      {
        item.setDescription(description.trim());
      }
      // Class requirements
      item.setRequiredClass(getRequiredClass(properties));
      // Stats
      if (level!=null)
      {
        if (_debug)
        {
          ItemLevelProgression progression=buildItemLevelProgression(properties);
          if (progression!=null)
          {
            Integer itemLevel80=progression.getValue(80);
            BasicStatsSet stats80=loadStats(itemLevel80.intValue(),properties);
            System.out.println("Stats at level 80, item level "+itemLevel80+" : "+stats80);
          }
        }
        BasicStatsSet stats=loadStats(level.intValue(),properties);
        if (stats!=null)
        {
          item.getStats().addStats(stats);
        }
      }
      if (item instanceof Weapon)
      {
        loadWeaponSpecifics((Weapon)item,properties);
      }
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
  }

  private String fixName(String name)
  {
    if (name==null)
    {
      return name;
    }
    int index=name.lastIndexOf('[');
    if (index!=-1)
    {
      name=name.substring(0,index);
    }
    return name;
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
    weapon.setDamageType(getDamageType(damageTypeEnum));
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
    EquipmentLocation slot=null;
    WeaponType weaponType=null;
    ArmourType armourType=null;
    long equipmentCategory=getEquipmentCategory(properties);
    if (equipmentCategory==0)
    {
      // Undefined
    }
    else if (equipmentCategory==1) slot=EquipmentLocation.EAR;
    else if (equipmentCategory==1L<<1) slot=EquipmentLocation.POCKET;
    else if (equipmentCategory==1L<<2)
    {
      weaponType=WeaponType.TWO_HANDED_SWORD;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<3)
    {
      weaponType=WeaponType.TWO_HANDED_CLUB;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<4)
    {
      //weaponType=WeaponType.TWO_HANDED_MACE;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<5)
    {
      weaponType=WeaponType.TWO_HANDED_AXE;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<6)
    {
      // Instrument
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<7)
    {
      weaponType=WeaponType.BOW;
      slot=EquipmentLocation.RANGED_ITEM;
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
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<11)
    {
      weaponType=WeaponType.ONE_HANDED_HAMMER;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<12)
    {
      weaponType=WeaponType.SPEAR;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<13)
    {
      weaponType=WeaponType.CROSSBOW;
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<14)
    {
      weaponType=WeaponType.TWO_HANDED_HAMMER;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<15)
    {
      weaponType=WeaponType.HALBERD;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<16)
    {
      armourType=ArmourType.SHIELD;
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<17)
    {
      armourType=ArmourType.LIGHT;
    }
    else if (equipmentCategory==1L<<18) // Ring
    {
      slot=EquipmentLocation.FINGER;
    }
    else if (equipmentCategory==1L<<19)
    {
      weaponType=WeaponType.DAGGER;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<20) // Craft Tool
    {
      slot=EquipmentLocation.TOOL;
    }
    else if (equipmentCategory==1L<<21)
    {
      weaponType=WeaponType.STAFF;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<22) // Necklace
    {
      slot=EquipmentLocation.NECK;
    }
    else if (equipmentCategory==1L<<23)
    {
      weaponType=WeaponType.ONE_HANDED_AXE;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<24) // Class Item
    {
      slot=EquipmentLocation.CLASS_SLOT;
    }
    else if (equipmentCategory==1L<<25)
    {
      weaponType=WeaponType.ONE_HANDED_CLUB;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<26)
    {
      weaponType=WeaponType.ONE_HANDED_MACE;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<27)
    {
      weaponType=WeaponType.ONE_HANDED_SWORD;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<28) // Thrown Weapon
    {
      //
    }
    else if (equipmentCategory==1L<<29) // Armband
    {
      slot=EquipmentLocation.WRIST;
    }
    else if (equipmentCategory==1L<<30) // Cloak
    {
      armourType=ArmourType.LIGHT;
      slot=EquipmentLocation.BACK;
    }
    else if (equipmentCategory==1L<<31) // Cosmetic
    {
      // No slot
    }
    else if (equipmentCategory==1L<<33) // Two-handed implement
    {
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<36) // One-handed implement
    {
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<38)
    {
      weaponType=WeaponType.RUNE_STONE;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<39)
    {
      armourType=ArmourType.WARDEN_SHIELD;
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<40)
    {
      weaponType=WeaponType.JAVELIN;
      slot=EquipmentLocation.MAIN_HAND;
    }
    else if (equipmentCategory==1L<<42) // Oath-bound Armaments
    {
      slot=EquipmentLocation.RANGED_ITEM;
    }
    else if (equipmentCategory==1L<<43) // War-steed Item
    {
      slot=EquipmentLocation.BRIDLE;
    }
    else
    {
      System.out.println("Unmanaged equipment category " + equipmentCategory+" for: "+_currentItem);
    }
    Item ret=null;
    if (weaponType!=null)
    {
      Weapon weapon=new Weapon();
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
      return new Item();
    }
    ret.setEquipmentLocation(slot);
    return ret;
  }

  /**
   * Get the slot of an item, using its item class.
   * @param itemClass Item class.
   * @return A slot or <code>null</code> if not supported.
   */
  private EquipmentLocation getSlot(int itemClass)
  {
    if (itemClass==3) return EquipmentLocation.CHEST;
    if (itemClass==5) return EquipmentLocation.HAND;
    if (itemClass==6) return EquipmentLocation.SHOULDER;
    if (itemClass==7) return EquipmentLocation.HEAD;
    if (itemClass==15) return EquipmentLocation.LEGS;
    if (itemClass==23) return EquipmentLocation.FEET;
    if (itemClass==45) return EquipmentLocation.BACK;
    return null;
  }

  private BasicStatsSet loadStats(int itemLevel, PropertiesSet properties)
  {
    BasicStatsSet ret=null;
    Object[] mods=(Object[])properties.getProperty("Mod_Array");
    if (mods!=null)
    {
      ret=new BasicStatsSet();
      for(int i=0;i<mods.length;i++)
      {
        PropertiesSet statProperties=(PropertiesSet)mods[i];
        Integer statId=(Integer)statProperties.getProperty("Mod_Modified");
        PropertyDefinition def=_facade.getPropertiesRegistry().getPropertyDef(statId.intValue());
        STAT stat=getStatFromName(def.getName());
        if (stat!=null)
        {
          // Always 7 for "add"?
          //Integer modOp=(Integer)statProperties.getProperty("Mod_Op");
          Integer progressId=(Integer)statProperties.getProperty("Mod_Progression");
          if (progressId==null)
          {
            continue;
          }
          Integer minLevel=(Integer)statProperties.getProperty("Mod_ProgressionFloor");
          if ((minLevel!=null) && (itemLevel<minLevel.intValue()))
          {
            continue;
          }
          Integer maxLevel=(Integer)statProperties.getProperty("Mod_ProgressionCeiling");
          if ((maxLevel!=null) && (itemLevel>maxLevel.intValue()))
          {
            continue;
          }

          int progressPropertiesId=progressId.intValue()+0x9000000;
          PropertiesSet progressProperties=_facade.loadProperties(progressPropertiesId);
          if (_debug)
          {
            FileIO.writeFile(new File(progressPropertiesId+".props"),progressProperties.dump().getBytes());
          }
          Progression progression=buildProgression(progressProperties);
          if (progression!=null)
          {
            Float value=progression.getValue(itemLevel);
            if (value!=null)
            {
              float statValue=value.floatValue();
              if (stat==STAT.TACTICAL_CRITICAL_MULTIPLIER)
              {
                statValue=statValue*100;
              }
              if ((stat==STAT.ICMR) || (stat==STAT.ICPR) || (stat==STAT.OCMR) || (stat==STAT.OCPR))
              {
                statValue=statValue*60;
              }
              ret.addStat(stat,new FixedDecimalsInteger(statValue));
            }
          }
        }
      }
    }
    return ret;
  }

  private STAT getStatFromName(String name)
  {
    if ("Health_MaxLevel".equals(name)) return STAT.MORALE;
    if ("Power_MaxLevel".equals(name)) return STAT.POWER;
    if ("Stat_Might".equals(name)) return STAT.MIGHT;
    if ("Stat_Agility".equals(name)) return STAT.AGILITY;
    if ("Stat_Will".equals(name)) return STAT.WILL;
    if ("Stat_Vitality".equals(name)) return STAT.VITALITY;
    if ("Stat_Fate".equals(name)) return STAT.FATE;
    if ("Combat_Class_CriticalPoints_Unified".equals(name)) return STAT.CRITICAL_RATING;
    if ("Combat_FinessePoints_Modifier".equals(name)) return STAT.FINESSE;
    if ("LoE_Light_Modifier".equals(name)) return STAT.LIGHT_OF_EARENDIL;
    if ("Resist_ClassPoints_Resistance_TheOneResistance".equals(name)) return STAT.RESISTANCE;
    if ("Combat_Unified_Critical_Defense".equals(name)) return STAT.CRITICAL_DEFENCE;
    if ("Combat_BlockPoints_Modifier".equals(name)) return STAT.BLOCK;
    if ("Combat_EvadePoints_Modifier".equals(name)) return STAT.EVADE;
    if ("Combat_ParryPoints_Modifier".equals(name)) return STAT.PARRY;
    if ("Vital_PowerPeaceRegenAddMod".equals(name)) return STAT.OCPR;
    if ("Vital_PowerCombatRegenAddMod".equals(name)) return STAT.ICPR;
    if ("Vital_HealthPeaceRegenAddMod".equals(name)) return STAT.OCMR;
    if ("Vital_HealthCombatRegenAddMod".equals(name)) return STAT.ICMR;
    if ("Mood_Hope_Level".equals(name)) return STAT.HOPE;

    if ("Combat_IncomingHealing_Points_Current".equals(name)) return STAT.INCOMING_HEALING;
    if ("Combat_Modifier_OutgoingHealing_Points".equals(name)) return STAT.OUTGOING_HEALING;
    if ("Combat_ArmorDefense_PointsModifier_UnifiedPhysical".equals(name)) return STAT.PHYSICAL_MITIGATION;
    if ("Combat_ArmorDefense_PointsModifier_UnifiedTactical".equals(name)) return STAT.TACTICAL_MITIGATION;
    if ("Combat_PhysicalMastery_Modifier_Unified".equals(name)) return STAT.PHYSICAL_MASTERY;
    if ("Combat_TacticalMastery_Modifier_Unified".equals(name)) return STAT.TACTICAL_MASTERY;
    if ("Stealth_StealthLevelModifier".equals(name)) return STAT.STEALTH_LEVEL;
    if ("Craft_Weaponsmith_CriticalChanceAddModifier".equals(name)) return STAT.WEAPONSMITH_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Metalsmith_CriticalChanceAddModifier".equals(name)) return STAT.METALSMITH_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Woodworker_CriticalChanceAddModifier".equals(name)) return STAT.WOODWORKER_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Cook_CriticalChanceAddModifier".equals(name)) return STAT.COOK_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Scholar_CriticalChanceAddModifier".equals(name)) return STAT.SCHOLAR_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Jeweller_CriticalChanceAddModifier".equals(name)) return STAT.JEWELLER_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Tailor_CriticalChanceAddModifier".equals(name)) return STAT.TAILOR_CRIT_CHANCE_PERCENTAGE;
    if ("Skill_InductionDuration_MiningMod".equals(name)) return STAT.PROSPECTOR_MINING_DURATION;
    if ("Skill_InductionDuration_WoodHarvestMod".equals(name)) return STAT.FORESTER_CHOPPING_DURATION;
    if ("Skill_InductionDuration_FarmHarvestMod".equals(name)) return STAT.FARMER_MINING_DURATION;
    if ("Combat_EffectCriticalMultiplierMod".equals(name)) return STAT.TACTICAL_CRITICAL_MULTIPLIER;

    if ("Skill_HealingMultiplier_Item".equals(name)) return null; // +N% Healing
    if ("Trait_Minstrel_Healing_CriticalMod".equals(name)) return null; // Critical Healing Magnitude

    // Reduces ranged skill induction time
    if ("Skill_InductionDuration_AllSkillsMod".equals(name)) return null;
    // Healing skills power cost
    if ("Skill_VitalCost_LifeSingerMod".equals(name)) return null;

    // Armour value for Herald??? itemId=1879053134,5,6,7
    if ("Trait_Runekeeper_All_Resistance".equals(name)) return null;
    if ("Trait_Loremaster_PetModStat_Slot2".equals(name)) return null;

    // 10% discount at most Ered Luin shops
    if ("Discount_Eredluin".equals(name)) return null;
    // 10% discount at most Bree-land shops
    if ("Discount_Breeland".equals(name)) return null;
    // 10% discount at most Trollshaws shops
    if ("Discount_Trollshaws".equals(name)) return null;
    // 10% discount at most North-down shops
    if ("Discount_Northdowns".equals(name)) return null;

    // Frost mitigation RATING...
    if ("Combat_ArmorDefense_PointsModifier_Frost".equals(name)) return null;
    // Acid mitigation RATING...
    if ("Combat_ArmorDefense_PointsModifier_Acid".equals(name)) return null;
    // Fire mitigation RATING...
    if ("Combat_ArmorDefense_PointsModifier_Fire".equals(name)) return null;
    // Shadow mitigation RATING...
    if ("Combat_ArmorDefense_PointsModifier_Shadow".equals(name)) return null;
    // Critical rating, reloaded?
    if ("Combat_CriticalPoints_Modifier_Unified".equals(name)) return null;

    // Minstrel
    if ("Skill_DamageMultiplier_LightintheDark".equals(name)) return STAT.BALLAD_AND_CODA_DAMAGE_PERCENTAGE;

    //System.out.println("unknown stat name: "+name+", id="+_currentId);
    return null;
  }

  private CharacterClass getRequiredClass(PropertiesSet properties)
  {
    Object[] classReqs=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (classReqs!=null)
    {
      int characterClassId=((Integer)classReqs[0]).intValue();
      return getCharacterClassFromId(characterClassId);
    }
    return null;
  }

  private CharacterClass getCharacterClassFromId(int id) {
    if (id==214) return CharacterClass.BEORNING;
    if (id==40) return CharacterClass.BURGLAR;
    if (id==24) return CharacterClass.CAPTAIN;
    if (id==172) return CharacterClass.CHAMPION;
    if (id==23) return CharacterClass.GUARDIAN;
    if (id==162) return CharacterClass.HUNTER;
    if (id==185) return CharacterClass.LORE_MASTER;
    if (id==31) return CharacterClass.MINSTREL;
    if (id==193) return CharacterClass.RUNE_KEEPER;
    if (id==194) return CharacterClass.WARDEN;
    // Monster Play
    if (id==71) return null; // Reaver
    if (id==128) return null; // Defiler
    if (id==127) return null; // Weaver
    if (id==179) return null; // Blackarrow
    if (id==52) return null; // Warleader
    if (id==126) return null; // Stalker
    System.out.println("Unmanaged ID="+id+" for "+_currentId);
    return null;
  }

  private Progression buildProgression(PropertiesSet properties)
  {
    Progression ret=buildLinearProgression(properties);
    if (ret==null)
    {
      ret=buildArrayProgression(properties);
    }
    if (ret==null)
    {
      System.out.println(properties.dump());
    }
    return ret;
  }

  private ArrayProgression buildArrayProgression(PropertiesSet properties)
  {
    ArrayProgression ret=null;
    Object[] progression=(Object[])properties.getProperty("PropertyProgression_Array");
    if (progression!=null)
    {
      // Always 1?
      Integer minIndexValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int nbItems=progression.length;
      ret=new ArrayProgression(nbItems);
      for(int i=0;i<nbItems;i++)
      {
        Number value=(Number)progression[i];
        ret.set(i,i+minIndexValue.intValue(),value.floatValue());
      }
    }
    return ret;
  }

  private LinearInterpolatingProgression buildLinearProgression(PropertiesSet properties)
  {
    LinearInterpolatingProgression ret=null;
    Object[] progression=(Object[])properties.getProperty("LinearInterpolatingProgression_Array");
    if (progression!=null)
    {
      int nbItems=progression.length;
      ret=new LinearInterpolatingProgression(nbItems);
      for(int i=0;i<nbItems;i++)
      {
        PropertiesSet pointProperties=(PropertiesSet)progression[i];
        Integer key=(Integer)pointProperties.getProperty("LinearInterpolatingProgression_Key");
        Float value=(Float)pointProperties.getProperty("LinearInterpolatingProgression_Value");
        ret.set(i,key.intValue(),value.floatValue());
      }
    }
    return ret;
  }

  private ItemLevelProgression buildItemLevelProgression(PropertiesSet properties)
  {
    ItemLevelProgression ret=null;
    /*
    Integer progressionGroupOverride=(Integer)properties.getProperty("ItemAdvancement_ProgressionGroupOverride");
    if (progressionGroupOverride!=null)
    {
      int progressId=progressionGroupOverride.intValue()+0x9000000;
      PropertiesSet progressProperties=_facade.loadProperties(progressId);
      if (_debug)
      {
        FileIO.writeFile(new File(progressId+".props"),progressProperties.dump().getBytes());
        System.out.println(properties.dump());
      }
    }
    */

    Integer itemLevelProgression=(Integer)properties.getProperty("ItemMunging_ItemLevelOverrideProgression");
    if (itemLevelProgression!=null)
    {
      int progressId=itemLevelProgression.intValue()+0x9000000;
      PropertiesSet progressProperties=_facade.loadProperties(progressId);
      if (_debug)
      {
        FileIO.writeFile(new File(progressId+".props"),progressProperties.dump().getBytes());
        System.out.println(properties.dump());
      }

      Object[] charLevel2ItemLevel=(Object[])progressProperties.getProperty("PropertyProgression_Array");
      if (charLevel2ItemLevel!=null)
      {
        int nbPoints=charLevel2ItemLevel.length;
        ret=new ItemLevelProgression(nbPoints);
        Integer levelOffset=(Integer)progressProperties.getProperty("Progression_MinimumIndexValue");
        int delta=((levelOffset!=null)?levelOffset.intValue():0);
        for(int i=0;i<nbPoints;i++)
        {
          int level=i+delta;
          int itemLevel=((Integer)charLevel2ItemLevel[i]).intValue();
          ret.set(i,level,itemLevel);
        }
      }
    }
    return ret;
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

  private DamageType getDamageType(int damageTypeEnum)
  {
    // 0 Undef
    if (damageTypeEnum==1) return DamageType.COMMON;
    if (damageTypeEnum==2) return DamageType.WESTERNESSE;
    if (damageTypeEnum==4) return DamageType.ANCIENT_DWARF;
    if (damageTypeEnum==8) return DamageType.BELERIAND;
    if (damageTypeEnum==16) return DamageType.FIRE;
    if (damageTypeEnum==32) return DamageType.SHADOW;
    if (damageTypeEnum==64) return DamageType.LIGHT;
    // 128 ImplementInherited
    if (damageTypeEnum==256) return DamageType.FROST;
    if (damageTypeEnum==512) return DamageType.LIGHTNING;
    // 1024  Acid
    // 2048  Morgul-forged
    // 4096  Orc-craft
    // 8192  Fell-wrought
    // 16384 Physical
    // 32768 Tactical
    // 49152 PvP
    // 65407 ALL
    System.out.println("Unmanaged damage type: "+damageTypeEnum);
    return null;
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
    if ((bindOnAcquire!=null) && (bindOnAcquire.intValue()==1)) return ItemBinding.BIND_ON_ACQUIRE;
    Integer bindOnEquip=(Integer)properties.getProperty("Inventory_BindOnEquip");
    if ((bindOnEquip!=null) && (bindOnEquip.intValue()==1)) return ItemBinding.BIND_ON_EQUIP;
    return null;
  }

  private String getStringProperty(PropertiesSet properties, String propertyName)
  {
    String ret=null;
    Object value=properties.getProperty(propertyName);
    if (value!=null)
    {
      if (value instanceof String[])
      {
        ret=((String[])value)[0];
      }
    }
    return ret;
  }

  private void doIt()
  {
    List<Item> items=new ArrayList<Item>();
    ItemsManager itemsManager=ItemsManager.getInstance();
    List<Item> refItems=itemsManager.getAllItems();
    int nbTotal=refItems.size();
    for(int i=0;i<nbTotal;i++)
    {
      int id=refItems.get(i).getIdentifier();
      Item newItem=load(id);
      if (newItem!=null)
      {
        items.add(newItem);
      }
    }
    // Write result file
    File toFile=new File("../lotro-companion/data/lore/items_dat.xml").getAbsoluteFile();
    ItemXMLWriter.writeItemsFile(toFile,items);
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
