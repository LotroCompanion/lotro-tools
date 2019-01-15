package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.RangedStatProvider;
import delta.games.lotro.common.stats.ScalableStatProvider;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatOperator;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.common.stats.TieredScalableStatProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.utils.maths.Progression;

/**
 * Utility methods related to stats from DAT files.
 * @author DAM
 */
public class DatStatUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatStatUtils.class);

  /**
   * Progressions manager.
   */
  public static ProgressionsManager _progressions=new ProgressionsManager();

  /**
   * Stats usage statistics.
   */
  public static StatsUsageStatistics _statsUsageStatistics=new StatsUsageStatistics();

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
    String modifiedPropName="Mod_Modified";
    String progressionPropName="Mod_Progression";
    if (propsPrefix!=null)
    {
      arrayPropName=propsPrefix+arrayPropName;
      modifiedPropName=propsPrefix+modifiedPropName;
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
        Integer statId=(Integer)statProperties.getProperty(modifiedPropName);
        PropertyDefinition def=facade.getPropertiesRegistry().getPropertyDef(statId.intValue());
        StatDescription stat=DatStatUtils.getStatDescription(def);
        if (stat!=null)
        {
          _statsUsageStatistics.registerStatUsage(stat);
          StatProvider provider=null;
          Number value=null;
          // Often 7 for "add"
          Integer modOp=(Integer)statProperties.getProperty("Mod_Op");
          StatOperator operator=getOperator(modOp);
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
              float statValue=StatUtils.fixStatValue(stat,value.floatValue());
              if (Math.abs(statValue)>0.001)
              {
                provider=new ConstantStatProvider(stat,statValue);
              }
            }
            else
            {
              LOGGER.warn("No progression ID and no direct value... Stat is "+stat.getName());
            }
          }
          if (provider!=null)
          {
            Integer minLevel=(Integer)statProperties.getProperty("Mod_ProgressionFloor");
            Integer maxLevel=(Integer)statProperties.getProperty("Mod_ProgressionCeiling");
            if ((minLevel!=null) || (maxLevel!=null))
            {
              RangedStatProvider rangedProvider=null;
              if (rangedStatProviders==null) rangedStatProviders=new HashMap<StatDescription,RangedStatProvider>();
              rangedProvider=rangedStatProviders.get(stat);
              if (rangedProvider==null)
              {
                rangedProvider=new RangedStatProvider(stat);
                rangedStatProviders.put(stat,rangedProvider);
                statsProvider.addStatProvider(rangedProvider);
              }
              rangedProvider.addRange(minLevel,maxLevel,provider);
            }
            else
            {
              statsProvider.addStatProvider(provider);
            }
            provider.setOperator(operator);
          }
        }
      }
    }
    return statsProvider;
  }

  private static StatOperator getOperator(Integer modOpInteger)
  {
    if (modOpInteger==null) return StatOperator.ADD;
    int modOp=modOpInteger.intValue();
    if (modOp==5) return StatOperator.SET;
    if (modOp==6) return StatOperator.SUBSTRACT;
    if (modOp==7) return StatOperator.ADD;
    if (modOp==8) return StatOperator.MULTIPLY;
    LOGGER.warn("Unmanaged operator: "+modOp);
    return null;
  }

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param progressId Progression ID.
   * @return A progression curve or <code>null</code> if not found.
   */
  public static Progression getProgression(DataFacade facade, int progressId)
  {
    Progression ret=_progressions.getProgression(progressId);
    if (ret==null)
    {
      int progressPropertiesId=progressId+0x9000000;
      PropertiesSet progressProperties=facade.loadProperties(progressPropertiesId);
      if (progressProperties!=null)
      {
        ret=ProgressionFactory.buildProgression(progressId, progressProperties);
        if (ret!=null)
        {
          _progressions.registerProgression(progressId,ret);
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
    PropertiesSet properties=facade.loadProperties(progressId+0x9000000);
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
    StatsRegistry registry=StatsRegistry.getInstance();
    int id=propertyDefinition.getPropertyId();
    if (id==0)
    {
      return null;
    }
    StatDescription ret=registry.getById(id);
    if (ret==null)
    {
      String key=propertyDefinition.getName();
      LOGGER.warn("Added missing stat: ID="+id+", key="+key);
      StatDescription stat=new StatDescription(id);
      stat.setKey(key);
      registry.addStat(stat);
      ret=stat;
    }
    return ret;
  }
}
