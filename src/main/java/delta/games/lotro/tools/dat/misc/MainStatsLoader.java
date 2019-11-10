package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.common.utils.text.EndOfLine;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatDescriptionComparator;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.common.stats.StatsSorter;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.common.stats.io.xml.StatXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get stats from DAT files.
 * @author DAM
 */
public class MainStatsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainStatsLoader.class);

  /**
   * Stats registry.
   */
  public static StatsRegistry _stats=new StatsRegistry();

  private DataFacade _facade;
  private PropertiesRegistry _registry;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainStatsLoader(DataFacade facade)
  {
    _facade=facade;
    _registry=_facade.getPropertiesRegistry();
  }

  private void load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Could not handle property metadata ID="+indexDataId);
      return;
    }
    // Name
    String propertyName=DatUtils.getStringProperty(properties,"PropertyMetaData_Name");
    // Property ID
    int propertyId=((Integer)properties.getProperty("PropertyMetaData_Property")).intValue();
    // Property definition
    PropertyDefinition propertyDefinition=_registry.getPropertyDef(propertyId);
    String propertyKey=propertyDefinition.getName();
    // Percentage?
    Integer percentage=(Integer)properties.getProperty("PropertyMetaData_DisplayAsPercentage");
    boolean isPercentage=((percentage!=null) && (percentage.intValue()==1));

    // Add stat
    addDatStat(propertyId,propertyKey,propertyName,isPercentage);
  }

  private void doIt()
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
    // Add legacy data (name, index)
    addLegacyData();
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
  }

  private void addLegacyData()
  {
    int index=0;
    for(OldStatEnum oldStat : OldStatEnum.values())
    {
      String legacyKey=oldStat.getKey();
      StatDescription stat=_stats.getByKey(legacyKey);
      if (stat!=null)
      {
        // Add legacy name
        String legacyName=oldStat.getName();
        String statName=stat.getName();
        if (statName.equals(legacyName))
        {
          legacyName=null;
        }
        stat.setLegacyName(legacyName);
        // Set percentage
        stat.setPercentage(oldStat.isPercentage());
        // Add index
        stat.setIndex(Integer.valueOf(index));
      }
      index++;
    }
  }

  private void checks()
  {
    // Old STATs
    Set<String> oldStatsKeys=new HashSet<String>();
    for(OldStatEnum oldEnum : OldStatEnum.values())
    {
      oldStatsKeys.add(oldEnum.getKey());
    }
    LOGGER.info("Old stats count: "+oldStatsKeys.size());
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
    // Check that old stats are covered by well-known and not-well known
    {
      Set<String> allKnown=new HashSet<String>(wellKnownStatsKeys);
      allKnown.addAll(notWellKnownStatsKeys);
      Set<String> oldOrphans=new HashSet<String>(oldStatsKeys);
      oldOrphans.removeAll(allKnown);
      if (oldOrphans.size()>0)
      {
        LOGGER.warn("Old stats have orphans: "+oldOrphans);
      }
    }
    // Show well-known stats not found in old stats
    {
      Set<String> rest=new HashSet<String>(wellKnownStatsKeys);
      rest.removeAll(oldStatsKeys);
      LOGGER.info("New well-known stats: "+rest);
    }
  }

  private void checkNameDuplicates()
  {
    Set<String> names=new HashSet<String>();
    for(StatDescription stat : _stats.getAll())
    {
      Integer index=stat.getIndex();
      if (index!=null)
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

  private void addDatStat(int id, String key, String name, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setKey(key);
    stat.setInternalName(name);
    stat.setPercentage(isPercentage);
    _stats.addStat(stat);
  }

  private void addCustomStatFromOldStat(int id, OldStatEnum oldStat)
  {
    String legacyKey=oldStat.name();
    boolean isPercentage=oldStat.isPercentage();
    String legacyName=oldStat.getName();
    //System.out.println("Custom stat: key="+legacyKey+", name="+legacyName);
    addCustomStat(id,legacyKey,legacyName,isPercentage);
  }

  private void addCustomStat(int id, String legacyKey, String legacyName, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setLegacyKey(legacyKey);
    stat.setKey(legacyKey);
    stat.setInternalName(legacyName);
    stat.setPercentage(isPercentage);
    _stats.addStat(stat);
  }

  private void addCustomStats()
  {
    int id=-1000;
    // Add missing well known stats from the old stats enum
    addCustomStatFromOldStat(id--,OldStatEnum.DEVASTATE_MELEE_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.DEVASTATE_RANGED_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.DEVASTATE_TACTICAL_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.CRIT_DEVASTATE_MAGNITUDE_MELEE_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.CRIT_DEVASTATE_MAGNITUDE_RANGED_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.CRIT_DEVASTATE_MAGNITUDE_TACTICAL_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.FINESSE_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.RESISTANCE_PERCENTAGE);
    addCustomStatFromOldStat(id--,OldStatEnum.ARMOUR);
    // Add other stats
    addCustomStat(id--,"DPS","DPS",false);
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
