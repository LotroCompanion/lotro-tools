package delta.games.lotro.tools.lore.items.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.WeaponType;
import delta.games.lotro.lore.items.comparators.ItemIdComparator;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.items.legendary.LegendaryItem;
import delta.games.lotro.lore.items.legendary.LegendaryWeapon;
import delta.games.lotro.lore.items.stats.ItemLevelProgression;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatStatUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;
import delta.games.lotro.tools.utils.dat.ProgressionFactory;

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
        String iconName;
        if (iconId!=null)
        {
          iconName=iconId+((backgroundIconId!=null)?"-"+backgroundIconId:"");
        }
        else
        {
          iconName=backgroundIconId.toString();
        }
        item.setIcon(iconName);
        File iconFile=new File("icons/"+item.getIcon()+".png").getAbsoluteFile();
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
        if (iconId!=null)
        {
          File mainIconFile=new File("iconsMain/"+iconId+".png").getAbsoluteFile();
          if (!mainIconFile.exists())
          {
            DatIconsUtils.buildImageFile(_facade,iconId.intValue(),mainIconFile);
          }
        }
        if (backgroundIconId!=null)
        {
          File backgroundIconFile=new File("iconsBackground/"+backgroundIconId+".png").getAbsoluteFile();
          if (!backgroundIconFile.exists())
          {
            DatIconsUtils.buildImageFile(_facade,backgroundIconId.intValue(),backgroundIconFile);
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
      if (item.getEquipmentLocation()==null)
      {
        EquipmentLocation slot=getSlot(itemClass);
        item.setEquipmentLocation(slot);
      }
      // Essence slots
      Integer essenceSlots=(Integer)properties.getProperty("Item_Socket_Count");
      if ((essenceSlots!=null) && (essenceSlots.intValue()>0))
      {
        item.setEssenceSlots(essenceSlots.intValue());
      }
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
      //handleMunging(properties);
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
        if (item instanceof Armour)
        {
          ((Armour)item).setArmourValue(armourValue.intValue());
        }
        else
        {
          item.getStats().setStat(STAT.ARMOUR,armourValue.intValue());
        }
        // Armour progression...
        Integer armourProgressId=(Integer)properties.getProperty("Item_Armor_Value_Lookup_Table");
        if (armourProgressId!=null)
        {
          armorStatProvider=DatStatUtils.buildStatProvider(_facade,STAT.ARMOUR,armourProgressId.intValue());
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
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(_facade,properties);
      if (armorStatProvider!=null)
      {
        statsProvider.addStatProvider(armorStatProvider);
      }
      item.setStatsProvider(statsProvider);
      // Item fixes
      itemFixes(item,statsProvider);
      // Stats
      if (level!=null)
      {
        BasicStatsSet stats=statsProvider.getStats(1,level.intValue());
        item.getStats().addStats(stats);
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

  private boolean useItem(String name, Integer itemClassInt)
  {
    if (name==null) return false;
    if (itemClassInt==null) return false;
    if ((name.startsWith("DNT")) || (name.contains("TBD"))) return false;
    if (name.contains("Tester")) return false;
    if (name.contains("Barter Test")) return false;
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
        ConstantStatProvider provider=new ConstantStatProvider(STAT.PARRY_PERCENTAGE,1);
        statsProvider.addStatProvider(provider);
      }
      else if (weaponType==WeaponType.TWO_HANDED_SWORD)
      {
        // +2% parry
        ConstantStatProvider provider=new ConstantStatProvider(STAT.PARRY_PERCENTAGE,2);
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
        ConstantStatProvider provider=new ConstantStatProvider(STAT.RANGED_DEFENCE_PERCENTAGE,-10);
        statsProvider.addStatProvider(provider);
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,STAT.CRITICAL_DEFENCE,1879260945);
        statsProvider.addStatProvider(critDef);
      }
      else if (armourType==ArmourType.SHIELD)
      {
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,STAT.CRITICAL_DEFENCE,1879211641);
        statsProvider.addStatProvider(critDef);
      }
      else if (armourType==ArmourType.WARDEN_SHIELD)
      {
        // Critical defence
        StatProvider critDef=DatStatUtils.buildStatProvider(_facade,STAT.CRITICAL_DEFENCE,1879260947);
        statsProvider.addStatProvider(critDef);
      }
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

  void handleMunging(PropertiesSet properties)
  {
    Integer level=(Integer)properties.getProperty("Item_Level");
    Integer minMungingLevel=(Integer)properties.getProperty("ItemMunging_MinMungeLevel");
    Integer maxMungingLevel=(Integer)properties.getProperty("ItemMunging_MaxMungeLevel");
    Integer progressionId=(Integer)properties.getProperty("ItemMunging_ItemLevelOverrideProgression");
    Integer propertyId=(Integer)properties.getProperty("ItemMunging_ItemLevelOverrideProperty");
    if (((minMungingLevel!=null) && (minMungingLevel.intValue()>0))
        || ((maxMungingLevel!=null) && (maxMungingLevel.intValue()>0))
        || (progressionId!=null) || (propertyId!=null))
    {
      if (progressionId!=null)
      {
        int progressPropertiesId=progressionId.intValue()+0x9000000;
        PropertiesSet progressProperties=_facade.loadProperties(progressPropertiesId);
        if (progressProperties!=null)
        {
          File to=new File("itemLevelOverrideProgression",progressionId.intValue()+".props").getAbsoluteFile();
          if (!to.exists())
          {
            to.getParentFile().mkdirs();
            FileIO.writeFile(to,progressProperties.dump().getBytes());
          }
        }
      }
      String name=_currentItem.getName();
      Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
      Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
      System.out.println(_currentId+"\t"+name+"\t"+level+"\t"+progressionId+"\t"+propertyId+"\t"+minMungingLevel+"\t"+maxMungingLevel+"\t"+minLevel+"\t"+maxLevel);
    }
  }

  ItemLevelProgression buildItemLevelProgression(PropertiesSet properties)
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
      ret=ProgressionFactory.buildItemLevelProgression(progressProperties);
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

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        Item newItem=load(id);
        if (newItem!=null)
        {
          items.add(newItem);
        }
      }
    }
    // Save items
    File toFile=new File("../lotro-companion/data/lore/items_dat.xml").getAbsoluteFile();
    ItemXMLWriter writer=new ItemXMLWriter(true);
    Collections.sort(items,new ItemIdComparator());
    /*boolean ok=*/writer.writeItems(toFile,items,EncodingNames.UTF_8);
    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_ITEMS);
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
