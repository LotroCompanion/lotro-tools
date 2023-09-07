package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.common.utils.text.EndOfLine;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatDescriptionComparator;
import delta.games.lotro.common.stats.StatType;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.common.stats.StatsSorter;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.common.stats.io.xml.StatXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.Locales;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get stats from DAT files.
 * @author DAM
 */
public class MainStatsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainStatsLoader.class);

  /**
   * Get all the locale keys.
   */
  private static final Locale[] LOCALES={Locale.ENGLISH,Locale.FRENCH,Locale.GERMAN};

  private DataFacade _facade;
  private I18nUtils _i18n;
  private PropertiesRegistry _registry;
  private StatLoadingUtils _oldStatsLabels;
  /**
   * Stats registry.
   */
  private StatsRegistry _stats;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainStatsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("stats",facade.getGlobalStringsManager());
    _registry=_facade.getPropertiesRegistry();
    _oldStatsLabels=new StatLoadingUtils();
    _stats=new StatsRegistry();
  }

  private void load(int indexDataId)
  {
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Could not handle property metadata ID="+indexDataId);
      return;
    }
    // Property ID
    int propertyId=((Integer)properties.getProperty("PropertyMetaData_Property")).intValue();
    // Name
    String statName=_i18n.getNameStringProperty(properties,"PropertyMetaData_Name",propertyId,0);
    // Property definition
    PropertyDefinition propertyDefinition=_registry.getPropertyDef(propertyId);
    String propertyKey=propertyDefinition.getName();
    // Percentage?
    Integer percentage=(Integer)properties.getProperty("PropertyMetaData_DisplayAsPercentage");
    boolean isPercentage=((percentage!=null) && (percentage.intValue()==1));

    // Add stat
    StatDescription stat=addDatStat(propertyId,propertyKey,statName,isPercentage);
    // Set stat type
    StatType type=getTypeFromPropertyType(propertyDefinition.getPropertyType());
    stat.setType(type);
  }

  private StatType getTypeFromPropertyType(PropertyType propertyType)
  {
    if (propertyType==PropertyType.BOOLEAN) return StatType.BOOLEAN;
    if (propertyType==PropertyType.INT) return StatType.INTEGER;
    if (propertyType==PropertyType.FLOAT) return StatType.FLOAT;
    if (propertyType==PropertyType.ENUM_MAPPER) return StatType.ENUM;
    if (propertyType==PropertyType.DATA_FILE) return StatType.DID;
    if (propertyType==PropertyType.STRING) return StatType.STRING;
    if (propertyType==PropertyType.BIT_FIELD) return StatType.BITFIELD;
    if (propertyType==PropertyType.BIT_FIELD32) return StatType.BITFIELD;
    if (propertyType==PropertyType.BITFIELD_64) return StatType.BITFIELD;
    if (propertyType==PropertyType.ARRAY) return StatType.ARRAY;
    System.out.println("Unmanaged property type: "+propertyType);
    return StatType.OTHER;
  }

  /**
   * Load stats.
   */
  public void doIt()
  {
    PropertiesSet indexProperties=_facade.loadProperties(1879048724+DATConstants.DBPROPERTIES_OFFSET);
    Object[] idsArray=(Object[])indexProperties.getProperty("PropertyMetaData_PropertyMetaDataList");
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      load(id);
    }
    // Add custom props
    addCustomStats();
    // Add legacy mappings
    StatMappings.setupMappings(_stats);
    // Define 'premium' stats (those with an index)
    definePremiumStats();
    // Add custom labels
    addCustomLabels();
    // Fix percentage stats
    fixPercentageStats();
    // Sort stats
    List<StatDescription> stats=_stats.getAll();
    Collections.sort(stats,new StatDescriptionComparator());
    // Show results
    //showResultStats();
    // Save stats
    int nbStats=stats.size();
    File toFile=GeneratedFiles.STATS;
    LOGGER.info("Writing "+nbStats+" stats to: "+toFile);
    StatXMLWriter.write(toFile,stats);
    // Checks
    checks();
    checkNameDuplicates();
    // Save labels
    _i18n.save();
  }

  private void definePremiumStats()
  {
    List<String> premiumKeys=_oldStatsLabels.getPremiumKeys();
    int nbPremiumKeys=premiumKeys.size();
    for(int i=0;i<nbPremiumKeys;i++)
    {
      StatDescription stat=_stats.getByKey(premiumKeys.get(i));
      if (stat!=null)
      {
        stat.setIndex(Integer.valueOf(i));
      }
    }
  }

  private void addCustomLabels()
  {
    for(Locale locale : LOCALES)
    {
      String localeCode=locale.getLanguage();
      for(String legacyKey : _oldStatsLabels.getLabelKeys(locale))
      {
        StatDescription stat=_stats.getByKey(legacyKey);
        if (stat!=null)
        {
          // Legacy labels
          int id=stat.getIdentifier();
          String legacyName=_oldStatsLabels.getStatLegacyName(legacyKey,locale);
          if ((legacyName!=null) && (legacyName.length()>0))
          {
            String statName=_i18n.getLabelsStorage().getLabel(localeCode,String.valueOf(id));
            if (!legacyName.equals(statName))
            {
              String key="legacy:"+id;
              _i18n.defineLabel(localeCode,key,legacyName);
              if (Locales.EN.equals(localeCode))
              {
                stat.setLegacyName(legacyName);
              }
            }
          }
        }
        // Set percentage
        //stat.setPercentage(_oldStatsLabels.isPercentage(legacyKey));
      }
    }
  }

  private void fixPercentageStats()
  {
    List<String> keys=_oldStatsLabels.getPercentageStats();
    for(String legacyKey : keys)
    {
      StatDescription stat=_stats.getByKey(legacyKey);
      if (stat!=null)
      {
        stat.setPercentage(true);
      }
    }
  }

  private void checks()
  {
    // Well-known stats
    List<StatDescription> wellKnownStats=WellKnownStat.getAllWellKnownStats();
    Set<String> wellKnownStatsKeys=new HashSet<String>();
    for(StatDescription wellKnownStat : wellKnownStats)
    {
      wellKnownStatsKeys.add(wellKnownStat.getLegacyKey());
    }
    LOGGER.info("Well-known stats count: "+wellKnownStatsKeys.size());
    // Not well-known stats
    Set<String> notWellKnownStatsKeys=new HashSet<String>();
    for(NotWellKnownLegacyStats notWellKnownStat : NotWellKnownLegacyStats.values())
    {
      String notWellKnownStatsKey=notWellKnownStat.getKey();
      notWellKnownStatsKeys.add(notWellKnownStatsKey);
    }
    LOGGER.info("Not well-known stats count: "+notWellKnownStatsKeys.size());
    // Check that well-known and not-well-known do not intersect
    {
      Set<String> intersection=new HashSet<String>(wellKnownStatsKeys);
      intersection.retainAll(notWellKnownStatsKeys);
      if (intersection.size()>0)
      {
        LOGGER.warn("Well-known and not well-known do intersect: "+intersection);
      }
    }
  }

  private void checkNameDuplicates()
  {
    Set<String> names=new HashSet<String>();
    for(StatDescription stat : _stats.getAll())
    {
      if (stat.isPremium())
      {
        String name=stat.getName();
        boolean ok=names.add(name);
        if (!ok)
        {
          System.out.println("Found duplicate stat name: "+name);
        }
      }
    }
  }

  void showResultStats()
  {
    List<StatDescription> stats=_stats.getAll();
    StatsSorter.sortStatsForUi(stats);
    StringBuilder sb=new StringBuilder("ID\tIndex\tKey\tLegacy key\tPersistence key\tLegacy name\tInternal name\tName");
    sb.append(EndOfLine.NATIVE_EOL);
    for(StatDescription stat : stats)
    {
      int id=stat.getIdentifier();
      sb.append(id).append('\t');
      Integer index=stat.getIndex();
      sb.append(index).append('\t');
      String key=stat.getKey();
      sb.append(key).append('\t');
      String legacyKey=stat.getLegacyKey();
      sb.append(legacyKey).append('\t');
      String persistenceKey=stat.getPersistenceKey();
      sb.append(persistenceKey).append('\t');
      String legacyName=stat.getLegacyName();
      sb.append(legacyName).append('\t');
      String internalName=stat.getInternalName();
      sb.append(internalName).append('\t');
      String name=stat.getName();
      sb.append(name).append(EndOfLine.NATIVE_EOL);
    }
    String result=sb.toString();
    System.out.println(result);
  }

  private StatDescription addDatStat(int id, String key, String name, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setKey(key);
    stat.setInternalName(name);
    stat.setPercentage(isPercentage);
    _stats.addStat(stat);
    return stat;
  }

  private void addCustomStat(int id, String legacyKey, boolean isPercentage, StatType type)
  {
    for(Locale locale : LOCALES)
    {
      String label=_oldStatsLabels.getStatLegacyName(legacyKey,locale);
      if (label!=null)
      {
        String localeCode=locale.getLanguage();
        String key=String.valueOf(id);
        _i18n.defineLabel(localeCode,key,label);
      }
    }
    String statName=_oldStatsLabels.getStatLegacyName(legacyKey,Locale.ENGLISH);
    //System.out.println("Custom stat: key="+legacyKey+", name="+legacyName);
    StatDescription stat=new StatDescription(id);
    stat.setLegacyKey(legacyKey);
    stat.setKey(legacyKey);
    stat.setInternalName(statName);
    stat.setPercentage(isPercentage);
    stat.setType(type);
    _stats.addStat(stat);
  }

  private void addCustomStats()
  {
    int id=-1000;
    // Add missing well known stats from the old stats enum
    addCustomStat(id--,"DEVASTATE_MELEE_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"DEVASTATE_RANGED_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"DEVASTATE_TACTICAL_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_MELEE_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_RANGED_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_TACTICAL_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"FINESSE_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"RESISTANCE_PERCENTAGE",true,StatType.FLOAT);
    addCustomStat(id--,"ARMOUR",false,StatType.INTEGER);
    // Add other stats
    addCustomStat(id--,"DPS",false,StatType.FLOAT);
    addCustomStat(id--,"Combat_TacticalDPS_Modifier#1",false,StatType.FLOAT);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainStatsLoader(facade).doIt();
    facade.dispose();
  }
}
