package delta.games.lotro.tools.lore.items.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.lore.items.Armour;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemQuality;
import delta.games.lotro.lore.items.ItemSturdiness;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.Weapon;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
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
      nb++;
      Integer weenieType=(Integer)properties.getProperty("WeenieType");
      Integer itemClass=(Integer)properties.getProperty("Item_Class");
      item=buildItem(weenieType.intValue(),itemClass.intValue());
      // ID
      item.setIdentifier(indexDataId);
      // Name
      String name=getStringProperty(properties,"Name");
      item.setName(name);
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      item.setIcon(iconId+"-"+backgroundIconId);
      // Slot
      EquipmentLocation slot=getSlot(itemClass.intValue());
      item.setEquipmentLocation(slot);
      // Level
      Integer level=(Integer)properties.getProperty("Item_Level");
      item.setItemLevel(level);
      // Min Level
      Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
      item.setMinLevel(minLevel);
      // TODO Max Level
      //Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
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
      item.setSubCategory(itemClass.toString());
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
      // Stats
      if (level!=null)
      {
        BasicStatsSet stats=loadStats(level.intValue(),properties);
        if (stats!=null)
        {
          item.getStats().addStats(stats);
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
  }

  private Item buildItem(int weenieType, int itemClass)
  {
    if (weenieType==0x30081) return new Armour(); // Clothing
    if (weenieType==0x40081) return new Armour(); // Armor
    if (weenieType==0x20081) return new Weapon(); // Weapon
    if (itemClass==0x21) return new Armour(); // Shield
    return new Item();
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
          if (progressId!=null)
          {
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

  private ItemQuality getQuality(int qualityEnum)
  {
    if (qualityEnum==1) return ItemQuality.LEGENDARY;
    if (qualityEnum==2) return ItemQuality.RARE;
    if (qualityEnum==3) return ItemQuality.INCOMPARABLE;
    if (qualityEnum==4) return ItemQuality.UNCOMMON;
    if (qualityEnum==5) return ItemQuality.COMMON;
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
      //int id=itemIds[i];
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
