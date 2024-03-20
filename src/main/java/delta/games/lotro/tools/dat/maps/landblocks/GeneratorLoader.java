package delta.games.lotro.tools.dat.maps.landblocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loads generator data.
 * @author DAM
 */
public class GeneratorLoader
{
  private static final Logger LOGGER=Logger.getLogger(LandblockInfoLoader.class);

  private DataFacade _facade;
  private Set<Integer> _dids;
  private Set<Integer> _idsToUse;
  private Map<String,Set<Integer>> _cache;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public GeneratorLoader(DataFacade facade)
  {
    _facade=facade;
    _idsToUse=new HashSet<Integer>();
    _cache=new HashMap<String,Set<Integer>>();
  }

  /**
   * Load the DIDs referenced by some weenie properties.
   * @param props Weenie properties to use.
   * @return A possibly empty but never <code>null</code> set of DIDs.
   */
  public Set<Integer> handleGenerator(PropertiesSet props)
  {
    Integer profileId=(Integer)props.getProperty("Generator_ProfileDID");
    if (profileId==null)
    {
      return null;
    }
    /*
Generator_PositionSetLimitArray:
  #1:
    Generator_PositionSetID: 165 (resource_scholar)
    Generator_PositionSetLimit: 2
     */
    Set<Integer> filter=null;
    Object[] limitsArray=(Object[])props.getProperty("Generator_PositionSetLimitArray");
    if ((limitsArray!=null) && (limitsArray.length>0))
    {
      for(Object limitEntry : limitsArray)
      {
        PropertiesSet limitProps=(PropertiesSet)limitEntry;
        Integer id=(Integer)limitProps.getProperty("Generator_PositionSetID");
        if (id!=null)
        {
          if (filter==null)
          {
            filter=new TreeSet<Integer>();
          }
          filter.add(id);
        }
      }
    }
    String key=profileId+"#"+((filter!=null)?filter.toString():"");
    Set<Integer> ret=_cache.get(key);
    if (ret==null)
    {
      ret=handleGeneratorProfile(profileId.intValue(),filter);
      _cache.put(key,ret);
    }
    return ret;
  }

  /**
   * Load the DIDs referenced by a generator profile.
   * @param profileId Generator profile identifier.
   * @param idsToUse Optional filter on data to fetch.
   * @return A possibly empty but never <code>null</code> set of DIDs.
   */
  public Set<Integer> handleGeneratorProfile(int profileId, Set<Integer> idsToUse)
  {
    _dids=new HashSet<Integer>();
    _idsToUse=idsToUse;
    PropertiesSet generatorProps=_facade.loadProperties(profileId+DATConstants.DBPROPERTIES_OFFSET);
    handleGeneratorProps(generatorProps);
    Set<Integer> ret=_dids;
    _dids=null;
    return ret;
  }

  private void handleGeneratorProps(PropertiesSet generatorProps)
  {
    Object[] entryObjs=(Object[])generatorProps.getProperty("GeneratorProfile_Profile");
    if (entryObjs==null)
    {
      return;
    }
    for(Object entryObj : entryObjs)
    {
      handleGeneratorEntry(entryObj);
    }
  }

  private void handleGeneratorEntry(Object entryObj)
  {
    if (entryObj instanceof PropertiesSet)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      handleGeneratorEntryProps(entryProps);
    }
    else if (entryObj instanceof Object[])
    {
      Object[] subEntriesArray=(Object[])entryObj;
      for(Object subEntryObj : subEntriesArray)
      {
        handleGeneratorEntryProps((PropertiesSet)subEntryObj);
      }
    }
    else if (entryObj instanceof Integer)
    {
      // Ignore
    }
    else
    {
      LOGGER.warn("Unmanaged entry: "+entryObj);
    }
  }

  private void handleGeneratorEntryProps(PropertiesSet entryProps)
  {
    // Use this to filter if needed
    Integer id=(Integer)entryProps.getProperty("GeneratorProfile_PositionSetID");
    if ((_idsToUse!=null) && (id!=null) && (!_idsToUse.contains(id)))
    {
      return;
    }
    handleProfileEntry(entryProps);
    handleProfileDefinitionEntry(entryProps);
    handleOneOfEntry(entryProps);
    handleSimpleEntry(entryProps);
  }

  private void handleProfileEntry(PropertiesSet entryProps)
  {
    PropertiesSet profileEntryProps=(PropertiesSet)entryProps.getProperty("GeneratorProfile_Entry");
    if (profileEntryProps!=null)
    {
      Integer profileId=(Integer)profileEntryProps.getProperty("GeneratorProfile_ProfileDefinition");
      if (profileId!=null)
      {
        Integer probability=(Integer)entryProps.getProperty("GeneratorProfile_Probability");
        if ((probability==null) || ((probability!=null) && (probability.intValue()>0)))
        {
          PropertiesSet generatorProps=_facade.loadProperties(profileId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
          handleGeneratorProps(generatorProps);
        }
        else
        {
          LOGGER.warn("Probability is: "+probability);
        }
      }
    }
    /*
    GeneratorProfile_Entry:
      GeneratorProfile_Probability: 800
      GeneratorProfile_ProfileDefinition: 1879162624
      GeneratorProfile_Properties:
        #1: 1
    GeneratorProfile_PositionSetID: 12 (Boss)
    */
  }

  private void handleProfileDefinitionEntry(PropertiesSet entryProps)
  {
    /*
  #1:
    GeneratorProfile_ProfileDefinition: 1879162707
    GeneratorProfile_Weight: 20
     */
    Integer profileId=(Integer)entryProps.getProperty("GeneratorProfile_ProfileDefinition");
    if (profileId!=null)
    {
      Integer weight=(Integer)entryProps.getProperty("GeneratorProfile_Weight");
      if ((weight!=null) && (weight.intValue()>0))
      {
        PropertiesSet generatorProps=_facade.loadProperties(profileId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
        handleGeneratorProps(generatorProps);
      }
      else
      {
        LOGGER.warn("Weight is: "+weight);
      }
    }
  }

  private void handleOneOfEntry(PropertiesSet entryProps)
  {
    /*
  #1:
    GeneratorProfile_OneOf:
      #1:
        GeneratorProfile_RepeatMaxTimes: 1
        GeneratorProfile_RepeatMinTimes: 1
      #2:
        GeneratorProfile_WSLEntity: 1879162674
        GeneratorProfile_Weight: 70
      #3:
        GeneratorProfile_WSLEntity: 1879162665
        GeneratorProfile_Weight: 25
      #4:
        GeneratorProfile_WSLEntity: 1879162618
        GeneratorProfile_Weight: 10
    GeneratorProfile_PositionSetID: 152 (Base)
    GeneratorProfile_Weight: 1
     */
    Object[] oneOfArray=(Object[])entryProps.getProperty("GeneratorProfile_OneOf");
    if (oneOfArray!=null)
    {
      Integer weight=(Integer)entryProps.getProperty("GeneratorProfile_Weight");
      if ((weight==null) || ((weight!=null) && (weight.intValue()>0)))
      {
        for(Object oneOfEntryObj : oneOfArray)
        {
          PropertiesSet oneOfProps=(PropertiesSet)oneOfEntryObj;
          handleGeneratorEntryProps(oneOfProps);
        }
      }
      else
      {
        LOGGER.warn("OneOf Weight is: "+weight);
      }
    }
  }

  private boolean handleSimpleEntry(PropertiesSet entryProps)
  {
    Integer entityId=(Integer)entryProps.getProperty("GeneratorProfile_WSLEntity");
    if (entityId!=null)
    {
      Integer weight=(Integer)entryProps.getProperty("GeneratorProfile_Weight");
      if ((weight==null) || ((weight!=null) && (weight.intValue()>0)))
      {
        _dids.add(entityId);
        return true;
      }
      LOGGER.warn("SimpleEntry Weight is: "+weight);
    }
    return false;
  }
}
