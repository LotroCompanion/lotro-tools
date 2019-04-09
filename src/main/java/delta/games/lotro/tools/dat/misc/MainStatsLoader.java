package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.common.stats.io.xml.StatXMLWriter;
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
    int dbPropertiesId=indexDataId+0x09000000;
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
    PropertiesSet indexProperties=_facade.loadProperties(1879048724+0x09000000);
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
    // Filter stats to keep only legacy ones, ordered as before
    filterStats();
    // Save stats
    List<StatDescription> stats=_stats.getAll();
    int nbStats=stats.size();
    File toFile=GeneratedFiles.STATS;
    LOGGER.info("Writing "+nbStats+" stats to: "+toFile);
    StatXMLWriter.write(toFile,stats);
    showStats();
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
        stat.setLegacyName(legacyName);
        // Add index
        stat.setIndex(Integer.valueOf(index));
      }
      index++;
    }
  }

  private void showStats()
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
    addCustomStat(id,legacyKey,legacyName,isPercentage);
  }

  private void addCustomStat(int id, String legacyKey, String legacyName, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setLegacyKey(legacyKey);
    stat.setKey(legacyKey);
    stat.setLegacyName(legacyName);
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
   * Filter loaded stats to keep only previously known stats, ordered as previously.
   */
  void filterStats()
  {
    List<StatDescription> newList=new ArrayList<StatDescription>();
    List<String> keys=getKeysOfStatsToKeep();
    for(String key : keys)
    {
      StatDescription stat=_stats.getByKey(key);
      if (stat!=null)
      {
        newList.add(stat);
      }
      else
      {
        LOGGER.warn("Stat not found: "+key);
      }
    }
    _stats.clear();
    for(StatDescription stat : newList)
    {
      _stats.addStat(stat);
    }
  }

  private List<String> getKeysOfStatsToKeep()
  {
    List<String> keys=new ArrayList<String>();
    for(OldStatEnum old : OldStatEnum.values())
    {
      String key=old.name();
      keys.add(key);
    }
    keys.remove("BLADE_AOE_SKILLS_POWER_COST_PERCENTAGE");
    keys.add("DPS");
    return keys;
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
