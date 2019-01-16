package delta.games.lotro.tools.dat.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsRegistry;
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
    // Filter stats to keep only legacy ones, ordered as before
    filterStats();
    // Save stats
    List<StatDescription> stats=_stats.getAll();
    int nbStats=stats.size();
    File toFile=GeneratedFiles.STATS;
    LOGGER.info("Writing "+nbStats+" stats to: "+toFile);
    StatXMLWriter.write(toFile,stats);
  }

  private void addDatStat(int id, String key, String name, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setKey(key);
    stat.setName(name);
    stat.setPercentage(isPercentage);
    _stats.addStat(stat);
  }

  private void addCustomStat(int id, String legacyKey, boolean isPercentage)
  {
    StatDescription stat=new StatDescription(id);
    stat.setLegacyKey(legacyKey);
    stat.setKey(legacyKey);
    stat.setPercentage(isPercentage);
    _stats.addStat(stat);
  }

  private void addCustomStats()
  {
    int id=-1000;
    addCustomStat(id--,"DEVASTATE_MELEE_PERCENTAGE",true);
    addCustomStat(id--,"DEVASTATE_RANGED_PERCENTAGE",true);
    addCustomStat(id--,"DEVASTATE_TACTICAL_PERCENTAGE",true);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_MELEE_PERCENTAGE",true);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_RANGED_PERCENTAGE",true);
    addCustomStat(id--,"CRIT_DEVASTATE_MAGNITUDE_TACTICAL_PERCENTAGE",true);
    addCustomStat(id--,"FINESSE_PERCENTAGE",true);
    addCustomStat(id--,"RESISTANCE_PERCENTAGE",true);
    addCustomStat(id--,"ARMOUR",false);
  }

  private void filterStats()
  {
    List<StatDescription> newList=new ArrayList<StatDescription>();
    for(OldStatEnum old : OldStatEnum.values())
    {
      String key=old.name();
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
