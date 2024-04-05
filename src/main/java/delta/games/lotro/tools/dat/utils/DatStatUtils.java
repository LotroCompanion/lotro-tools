package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

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
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Utility methods related to stats from DAT files.
 * @author DAM
 */
public class DatStatUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatStatUtils.class);

  private static final String MOD_DESCRIPTION_OVERRIDE="Mod_DescriptionOverride";

  private DataFacade _facade;
  private I18nUtils _i18nUtils;
  private StatsUsageStatistics _statistics;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DatStatUtils(DataFacade facade)
  {
    this(facade,null);
  }

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18nUtils I18N utilities.
   */
  public DatStatUtils(DataFacade facade, I18nUtils i18nUtils)
  {
    _facade=facade;
    _i18nUtils=i18nUtils;
    _statistics=new StatsUsageStatistics();
  }

  /**
   * Get the data facade.
   * @return the data facade.
   */
  public DataFacade getFacade()
  {
    return _facade;
  }

  /**
   * Constructor.
   */
  public void showStatistics()
  {
    _statistics.showResults();
  }

  /**
   * Load a set of stats from some properties.
   * @param properties Properties to use to get stats.
   * @return A stats provider.
   */
  public StatsProvider buildStatProviders(PropertiesSet properties)
  {
    return buildStatProviders(null,properties);
  }

  /**
   * Load a set of stats from some properties.
   * @param propsPrefix Prefix for properties to use, default is <code>null</code>.
   * @param properties Properties to use to get stats.
   * @return A stats provider.
   */
  public StatsProvider buildStatProviders(String propsPrefix, PropertiesSet properties)
  {
    String arrayPropName="Mod_Array";
    if (propsPrefix!=null)
    {
      arrayPropName=propsPrefix+arrayPropName;
    }

    StatsProvider statsProvider=new StatsProvider();
    Object[] mods=(Object[])properties.getProperty(arrayPropName);
    if (mods!=null)
    {
      Map<StatDescription,RangedStatProvider> rangedStatProviders=null;
      for(int i=0;i<mods.length;i++)
      {
        PropertiesSet statProperties=(PropertiesSet)mods[i];
        StatProvider provider=buildStatProvider(propsPrefix,statProperties,statsProvider);
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

  private StatProvider buildStatProvider(String propsPrefix, PropertiesSet statProperties, StatsProvider statsProvider)
  {
    String descriptionOverride=getDescriptionOverride(statProperties);
    StatProvider provider=buildStatProvider(propsPrefix,statProperties);
    if (provider!=null)
    {
      // Descriptor override
      if (descriptionOverride!=null)
      {
        provider.setDescriptionOverride(descriptionOverride);
      }
      StatDescription stat=provider.getStat();
      _statistics.registerStatUsage(stat);
      provider=handleSpecificCases(provider,statsProvider,descriptionOverride);
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

  private StatProvider handleSpecificCases(StatProvider provider, StatsProvider statsProvider, String descriptionOverride)
  {
    StatDescription stat=provider.getStat();
    if (isSpecialStat(stat))
    {
      // Special case for special stats like "Item_Minstrel_Oathbreaker_Damagetype"
      String label=descriptionOverride;
      if (descriptionOverride==null)
      {
        label=handleSpecialStat((ConstantStatProvider)provider);
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

  private String handleSpecialStat(ConstantStatProvider provider)
  {
    StatDescription stat=provider.getStat();
    String statName=stat.getName();
    float value=provider.getValue();
    EnumMapper mapper=_facade.getEnumsManager().getEnumMapper(587202600);
    String valueName=mapper.getLabel((int)value);
    String result="Set "+statName+" to "+valueName;
    return result;
  }

  private StatProvider buildStatProvider(String propsPrefix, PropertiesSet statProperties)
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
    PropertyDefinition def=_facade.getPropertiesRegistry().getPropertyDef(statId);
    StatDescription stat=getStatDescription(def);
    if (stat==null)
    {
      LOGGER.warn("Unknown stat: "+def.getName());
      return null;
    }
    boolean useStat=useStat(def);
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
      provider=buildStatProvider(stat,progressId.intValue());
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

  private String getDescriptionOverride(PropertiesSet statProperties)
  {
    String ret=null;
    if (_i18nUtils!=null)
    {
      ret=_i18nUtils.getStringProperty(statProperties,MOD_DESCRIPTION_OVERRIDE);
    }
    else
    {
      Object propertyValue=statProperties.getProperty(MOD_DESCRIPTION_OVERRIDE);
      if (propertyValue!=null)
      {
        ret=DatStringUtils.getString(propertyValue);
      }
    }
    if (ret==null)
    {
      if (statProperties.hasProperty(MOD_DESCRIPTION_OVERRIDE))
      {
        ret=StatUtils.NO_DESCRIPTION;
      }
    }
    return ret;
  }

  /**
   * Build a stat provider from the given progression identifier.
   * @param stat Targeted stat.
   * @param progressId Progression ID.
   * @return A stat provider.
   */
  public StatProvider buildStatProvider(StatDescription stat, int progressId)
  {
    if (progressId==0) return null;
    PropertiesSet properties=_facade.loadProperties(progressId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    if (progressionIds!=null)
    {
      return getTieredProgression(stat,properties);
    }
    Progression progression=ProgressionUtils.getProgression(_facade,progressId);
    ScalableStatProvider scalableStat=new ScalableStatProvider(stat,progression);
    return scalableStat;
  }

  /**
   * Get a progression curve.
   * @param stat Involved stat.
   * @param properties Progression properties.
   * @return A progression curve or <code>null</code> if not found.
   */
  private TieredScalableStatProvider getTieredProgression(StatDescription stat, PropertiesSet properties)
  {
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    int nbTiers=progressionIds.length;
    TieredScalableStatProvider ret=new TieredScalableStatProvider(stat,nbTiers);
    int tier=1;
    for(Object progressionIdObj : progressionIds)
    {
      int progressionId=((Integer)progressionIdObj).intValue();
      Progression progression=ProgressionUtils.getProgression(_facade,progressionId);
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
  private static StatDescription getStatDescription(PropertyDefinition propertyDefinition)
  {
    return delta.games.lotro.utils.dat.DatStatUtils.getStatDescription(propertyDefinition.getPropertyId(),propertyDefinition.getName());
  }

  private static boolean useStat(PropertyDefinition def)
  {
    PropertyType type=def.getPropertyType();
    if ((type==PropertyType.BIT_FIELD32) || (type==PropertyType.DATA_FILE) ||
        (type==PropertyType.ARRAY) || (type==PropertyType.BOOLEAN))
    {
      return false;
    }
    return true;
  }

  /**
   * Get a stat description from a vital type code.
   * @param vitalType Vital type code.
   * @return A stat description or <code>null</code> if not found.
   */
  public static StatDescription getStatFromVitalType(int vitalType)
  {
    if (vitalType==1) return WellKnownStat.MORALE;
    if (vitalType==2) return WellKnownStat.POWER;
    if (vitalType==3) return WellKnownStat.WARSTEED_ENDURANCE;
    if (vitalType==4) return WellKnownStat.WARSTEED_POWER;
    return null;
  }
}
