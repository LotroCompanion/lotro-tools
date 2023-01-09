package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.RangedStatProvider;
import delta.games.lotro.common.stats.ScalableStatProvider;
import delta.games.lotro.common.stats.SpecialEffect;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatOperator;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.TieredScalableStatProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Utility methods related to stats from DAT files.
 * @author DAM
 */
public class DatStatUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatStatUtils.class);

  /**
   * Flag to indicate if stats shall be filtered or not.
   */
  public static boolean _doFilterStats=true;

  /**
   * Progressions manager.
   */
  public static final ProgressionsManager PROGRESSIONS_MGR=new ProgressionsManager();

  /**
   * Stats usage statistics.
   */
  public static final StatsUsageStatistics STATS_USAGE_STATISTICS=new StatsUsageStatistics();

  /**
   * Load a set of stats from some properties.
   * @param facade Data facade.
   * @param properties Properties to use to get stats.
   * @return A stats provider.
   */
  public static StatsProvider buildStatProviders(DataFacade facade, PropertiesSet properties)
  {
    return buildStatProviders(null,facade,properties);
  }

  /**
   * Load a set of stats from some properties.
   * @param propsPrefix Prefix for properties to use, default is <code>null</code>.
   * @param facade Data facade.
   * @param properties Properties to use to get stats.
   * @return A stats provider.
   */
  public static StatsProvider buildStatProviders(String propsPrefix, DataFacade facade, PropertiesSet properties)
  {
    String arrayPropName="Mod_Array";
    String progressionPropName="Mod_Progression";
    if (propsPrefix!=null)
    {
      arrayPropName=propsPrefix+arrayPropName;
      progressionPropName=propsPrefix+progressionPropName;
    }

    StatsProvider statsProvider=new StatsProvider();
    Object[] mods=(Object[])properties.getProperty(arrayPropName);
    if (mods!=null)
    {
      Map<StatDescription,RangedStatProvider> rangedStatProviders=null;
      for(int i=0;i<mods.length;i++)
      {
        PropertiesSet statProperties=(PropertiesSet)mods[i];
        StatProvider provider=buildStatProvider(propsPrefix,facade,statProperties,statsProvider);
        if (provider==null)
        {
          continue;
        }
        // Special case for level-ranged providers
        Integer minLevel=(Integer)statProperties.getProperty("Mod_ProgressionFloor");
        Integer maxLevel=(Integer)statProperties.getProperty("Mod_ProgressionCeiling");
        if ((minLevel!=null) || (maxLevel!=null))
        {
          RangedStatProvider rangedProvider=null;
          if (rangedStatProviders==null)
          {
            rangedStatProviders=new HashMap<StatDescription,RangedStatProvider>();
          }
          StatDescription stat=provider.getStat();
          rangedProvider=rangedStatProviders.get(stat);
          if (rangedProvider==null)
          {
            rangedProvider=new RangedStatProvider(stat);
            rangedProvider.setDescriptionOverride(provider.getDescriptionOverride());
            rangedStatProviders.put(stat,rangedProvider);
            statsProvider.addStatProvider(rangedProvider);
          }
          else
          {
            if (!Objects.equals(provider.getDescriptionOverride(),rangedProvider.getDescriptionOverride()))
            {
              LOGGER.warn("Description override mismatch: ["+provider.getDescriptionOverride()+"],["+rangedProvider.getDescriptionOverride()+")");
            }
          }
          rangedProvider.addRange(minLevel,maxLevel,provider);
        }
        else
        {
          statsProvider.addStatProvider(provider);
        }
      }
    }
    return statsProvider;
  }

  private static StatProvider buildStatProvider(String propsPrefix, DataFacade facade, PropertiesSet statProperties, StatsProvider statsProvider)
  {
    String descriptionOverride=getDescriptionOverride(statProperties);
    StatProvider provider=buildStatProvider(propsPrefix,facade,statProperties);
    if (provider!=null)
    {
      // Descriptor override
      if (descriptionOverride!=null)
      {
        provider.setDescriptionOverride(descriptionOverride);
      }
      StatDescription stat=provider.getStat();
      STATS_USAGE_STATISTICS.registerStatUsage(stat);
      provider=handleSpecificCases(provider,facade,statsProvider,descriptionOverride);
      return provider;
    }
    // Effect label only
    if (descriptionOverride!=null)
    {
      if ((descriptionOverride.length()>0) && (!StatUtils.NO_DESCRIPTION.equals(descriptionOverride)))
      {
        SpecialEffect effect=new SpecialEffect(descriptionOverride);
        statsProvider.addSpecialEffect(effect);
      }
    }
    LOGGER.debug("No provider and no override!");
    return null;
  }

  private static StatProvider handleSpecificCases(StatProvider provider, DataFacade facade, StatsProvider statsProvider, String descriptionOverride)
  {
    StatDescription stat=provider.getStat();
    if (isSpecialStat(stat))
    {
      // Special case for special stats like "Item_Minstrel_Oathbreaker_Damagetype"
      String label=descriptionOverride;
      if (descriptionOverride==null)
      {
        label=handleSpecialStat(facade,(ConstantStatProvider)provider);
      }
      if (!StatUtils.NO_DESCRIPTION.equals(label))
      {
        SpecialEffect effect=new SpecialEffect(label);
        statsProvider.addSpecialEffect(effect);
      }
      return null;
    }
    // Constant MULTIPLY on a percentage stat (e.g: CombatStateMod_CC_DurationMultModifier)
    boolean isPercentage=stat.isPercentage();
    StatOperator operator=provider.getOperator();
    boolean isConstant=(provider instanceof ConstantStatProvider);
    if ((isPercentage) && (isConstant) && (operator==StatOperator.MULTIPLY))
    {
      ConstantStatProvider constantProvider=(ConstantStatProvider)provider;
      float value=constantProvider.getValue();
      if (value<1)
      {
        value=Math.round((1-value)*100);
        operator=StatOperator.SUBSTRACT;
      }
      else
      {
        value=Math.round((value-1)*100);
        operator=StatOperator.ADD;
      }
      provider=new ConstantStatProvider(stat,value);
      provider.setOperator(operator);
      provider.setDescriptionOverride(descriptionOverride);
    }
    return provider;
  }

  private static boolean isSpecialStat(StatDescription stat)
  {
    String statKey=stat.getKey();
    if ("Item_Minstrel_Oathbreaker_Damagetype".equals(statKey)) return true;
    if ("Skill_DamageTypeOverride_AllSkillsOverride".equals(statKey)) return true;
    if ("ForwardSource_Combat_TraitCombo".equals(statKey)) return true;
    return false;
  }

  private static String handleSpecialStat(DataFacade facade, ConstantStatProvider provider)
  {
    StatDescription stat=provider.getStat();
    String statName=stat.getName();
    float value=provider.getValue();
    EnumMapper mapper=facade.getEnumsManager().getEnumMapper(587202600);
    String valueName=mapper.getLabel((int)value);
    String result="Set "+statName+" to "+valueName;
    return result;
  }

  private static StatProvider buildStatProvider(String propsPrefix, DataFacade facade, PropertiesSet statProperties)
  {
    StatProvider provider=null;

    String modifiedPropName="Mod_Modified";
    String progressionPropName="Mod_Progression";
    if (propsPrefix!=null)
    {
      modifiedPropName=propsPrefix+modifiedPropName;
      progressionPropName=propsPrefix+progressionPropName;
    }

    int statId=((Integer)statProperties.getProperty(modifiedPropName)).intValue();
    if (statId==0)
    {
      return null;
    }
    PropertyDefinition def=facade.getPropertiesRegistry().getPropertyDef(statId);
    StatDescription stat=getStatDescription(def);
    if (stat==null)
    {
      LOGGER.warn("Unknown stat: "+def.getName());
      return null;
    }
    boolean useStat=useStat(stat,def);
    if (!useStat)
    {
      return null;
    }
    // Operator (often 7 for "add")
    Integer modOp=(Integer)statProperties.getProperty("Mod_Op");
    StatOperator operator=getOperator(modOp);

    Number value=null;
    Integer progressId=(Integer)statProperties.getProperty(progressionPropName);
    if (progressId!=null)
    {
      provider=buildStatProvider(facade,stat,progressId.intValue());
    }
    else
    {
      Object propValue=statProperties.getProperty(def.getName());
      if (propValue instanceof Number)
      {
        value=(Number)propValue;
        float statValue=value.floatValue();
        if (Math.abs(statValue)>0.001)
        {
          if (operator!=StatOperator.MULTIPLY)
          {
            statValue=StatUtils.fixStatValue(stat,statValue);
          }
          provider=new ConstantStatProvider(stat,statValue);
        }
      }
      else
      {
        LOGGER.warn("Property value is not a Number: "+stat);
      }
    }
    if (provider!=null)
    {
      provider.setOperator(operator);
    }
    return provider;
  }

  /**
   * Build a stat operator from an operation code.
   * @param modOpInteger
   * @return A stat operator or <code>null</code>.
   */
  private static StatOperator getOperator(Integer modOpInteger)
  {
    if (modOpInteger==null) return StatOperator.ADD;
    int modOp=modOpInteger.intValue();
    if (modOp==5) return StatOperator.SET;
    if (modOp==6) return StatOperator.SUBSTRACT;
    if (modOp==7) return StatOperator.ADD;
    if (modOp==8) return StatOperator.MULTIPLY;
    LOGGER.warn("Unmanaged operator: "+modOp);
    // 1 => Difference
    // 2 => Divide
    // 3 => Or
    // 4 => Random
    // 10 => ArrayAdd
    // 11 => And
    // 12 => Xor
    return null;
  }

  private static String getDescriptionOverride(PropertiesSet statProperties)
  {
    String ret=null;
    Object propertyValue=statProperties.getProperty("Mod_DescriptionOverride");
    if (propertyValue!=null)
    {
      ret=DatStringUtils.getString(propertyValue);
    }
    else if (statProperties.hasProperty("Mod_DescriptionOverride"))
    {
      ret=StatUtils.NO_DESCRIPTION;
    }
    return ret;
  }

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param progressId Progression ID.
   * @return A progression curve or <code>null</code> if not found.
   */
  public static Progression getProgression(DataFacade facade, int progressId)
  {
    Progression ret=PROGRESSIONS_MGR.getProgression(progressId);
    if (ret==null)
    {
      long progressPropertiesId=progressId+DATConstants.DBPROPERTIES_OFFSET;
      PropertiesSet progressProperties=facade.loadProperties(progressPropertiesId);
      if (progressProperties!=null)
      {
        ret=ProgressionFactory.buildProgression(progressId, progressProperties);
        if (ret!=null)
        {
          PROGRESSIONS_MGR.registerProgression(progressId,ret);
        }
      }
    }
    return ret;
  }

  /**
   * Build a stat provider from the given progression identifier.
   * @param facade Data facade.
   * @param stat Targeted stat.
   * @param progressId Progression ID.
   * @return A stat provider.
   */
  public static StatProvider buildStatProvider(DataFacade facade, StatDescription stat, int progressId)
  {
    if (progressId==0) return null;
    PropertiesSet properties=facade.loadProperties(progressId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    if (progressionIds!=null)
    {
      return getTieredProgression(facade,stat,properties);
    }
    Progression progression=getProgression(facade,progressId);
    ScalableStatProvider scalableStat=new ScalableStatProvider(stat,progression);
    return scalableStat;
  }

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param stat Involved stat.
   * @param properties Progression properties.
   * @return A progression curve or <code>null</code> if not found.
   */
  private static TieredScalableStatProvider getTieredProgression(DataFacade facade, StatDescription stat, PropertiesSet properties)
  {
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    int nbTiers=progressionIds.length;
    TieredScalableStatProvider ret=new TieredScalableStatProvider(stat,nbTiers);
    int tier=1;
    for(Object progressionIdObj : progressionIds)
    {
      int progressionId=((Integer)progressionIdObj).intValue();
      Progression progression=getProgression(facade,progressionId);
      ret.setProgression(tier,progression);
      tier++;
    }
    return ret;
  }

  /**
   * Get a stat description.
   * @param propertyDefinition Property definition.
   * @return A stat description.
   */
  public static StatDescription getStatDescription(PropertyDefinition propertyDefinition)
  {
    return delta.games.lotro.utils.dat.DatStatUtils.getStatDescription(propertyDefinition.getPropertyId(),propertyDefinition.getName());
  }

  private static boolean useStat(StatDescription stat, PropertyDefinition def)
  {
    PropertyType type=def.getPropertyType();
    if ((type==PropertyType.BIT_FIELD32) || (type==PropertyType.DATA_FILE) ||
        (type==PropertyType.ARRAY) || (type==PropertyType.BOOLEAN))
    {
      return false;
    }
    //System.out.println("Type: "+type);
    if (_doFilterStats)
    {
      return stat.isPremium();
    }
    return true;
  }

  /*
  private static boolean old_useStat(String key)
  {
    if ("AI_PetEffect_HeraldBaseWC_Override".equals(key)) return false;
    if ("AI_PetEffect_ArcherBaseWC_Override".equals(key)) return false;
    if ("AI_PetEffect_HeraldHopeWC_Override".equals(key)) return false;
    if ("AI_PetEffect_HeraldVictoryWC_Override".equals(key)) return false;
    if ("Trait_Loremaster_PetModStat_Slot2".equals(key)) return false;
    if ("Trait_Captain_PetModStat_Slot4".equals(key)) return false;
    // Only on a test item
    if ("Skill_RiftSet_Absorb_Fire".equals(key)) return false;
    // Gives affinity for RK stones: fire, frost or lightning
    if ("ForwardSource_Combat_TraitCombo".equals(key)) return false;
    if ("Item_Runekeeper_PreludeofHope_Cleanse".equals(key)) return false;
    if ("Skill_EffectOverride_Burglar_ExploitOpening".equals(key)) return false;
    if ("Combat_MeleeDmgQualifier_WeaponProcEffect".equals(key)) return false;
    if ("Item_Minstrel_Oathbreaker_Damagetype".equals(key)) return false;

    if ("Trait_PvMP_BattleRank".equals(key)) return false;
    if ("Skill_VitalCost_Champion_AOEMod".equals(key)) return false;
    if ("Skill_VitalCost_Champion_StrikeMod".equals(key)) return false;
    if ("Skill_InductionDuration_ResearchingMod".equals(key)) return false;
    if ("TotalThreatModifier_Player".equals(key)) return false;
    if ("Skill_InductionDuration_AllSkillsMod".equals(key)) return false;

    return true;
  }
  */
}
