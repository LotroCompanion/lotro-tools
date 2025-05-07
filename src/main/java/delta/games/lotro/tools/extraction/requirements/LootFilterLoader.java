package delta.games.lotro.tools.extraction.requirements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.tools.extraction.loot.LootLoader;

/**
 * Loader for loot filters.
 * @author DAM
 */
public class LootFilterLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LootLoader.class);

  /**
   * Load filter.
   * @param filterArray Filter data.
   * @param requirements Storage for results.
   */
  public void loadFilterData(ArrayPropertyValue filterArray, UsageRequirement requirements)
  {
    for(PropertyValue filterEntry : filterArray.getValues())
    {
      String propertyName=filterEntry.getDefinition().getName();
      if ("EntityFilter_PropertyRange".equals(propertyName))
      {
        // Level filter
        PropertiesSet levelProps=(PropertiesSet)filterEntry.getValue();
        loadLevelFilter(levelProps,requirements);
      }
      else if ("EntityFilter_PropertySet".equals(propertyName))
      {
        ArrayPropertyValue propertyArray=(ArrayPropertyValue)filterEntry;
        for(PropertyValue propertySet : propertyArray.getValues())
        {
          handlePropertySet(propertySet,requirements);
        }
      }
      /*
      else if ("EntityFilter_WorldEvent".equals(propertyName))
      {
        PropertiesSet worldEventProps=(PropertiesSet)filterEntry.getValue();
        loadWorldEventFilter(worldEventProps);
      }
      */
      else
      {
        LOGGER.warn("Unmanaged property: "+propertyName);
      }
    }
  }

  private void handlePropertySet(PropertyValue propertySet,UsageRequirement requirements)
  {
    String propertyName=propertySet.getDefinition().getName();
    if ("Agent_Class".equals(propertyName))
    {
      // Class filter
      int classCode=((Integer)propertySet.getValue()).intValue();
      AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
      if (abstractClass!=null)
      {
        requirements.addAllowedClass(abstractClass);
      }
    }
    else if ("Agent_Species".equals(propertyName))
    {
      // Race filter
      int raceCode=((Integer)propertySet.getValue()).intValue();
      RaceDescription race=RacesManager.getInstance().getByCode(raceCode);
      if (race!=null)
      {
        requirements.addAllowedRace(race);
      }
    }
    /*
    else if ("WE_Player_Level_Cap".equals(propertyName))
    {
      int playerLevelCap=((Integer)propertySet.getValue()).intValue();
      System.out.println("Player level cap: "+playerLevelCap);
    }
    else if ("ze_skirmish_difficulty".equals(propertyName))
    {
      int difficulty=((Integer)propertySet.getValue()).intValue();
      System.out.println("Difficulty: "+difficulty);
    }
    */
    else
    {
      LOGGER.warn("Unmanaged property name: "+propertyName);
    }
  }

  private void loadWorldEventFilter(PropertiesSet worldEventProps)
  {
    System.out.println("World event filter: "+worldEventProps.dump());
  }

  private void loadLevelFilter(PropertiesSet levelProps, UsageRequirement requirements)
  {
    /*
    EntityFilter_PropertyRange_Max:
      #1: 39
    EntityFilter_PropertyRange_Min:
      #1: 30
     */
    Object[] minArray=(Object[])levelProps.getProperty("EntityFilter_PropertyRange_Min");
    Object[] maxArray=(Object[])levelProps.getProperty("EntityFilter_PropertyRange_Max");
    if ((minArray!=null) && (maxArray!=null))
    {
      int nbRanges=Math.min(minArray.length,maxArray.length);
      for(int i=0;i<nbRanges;i++)
      {
        Integer min=(Integer)minArray[i];
        Integer max=(Integer)maxArray[i];
        requirements.setLevelRange(min,max);
      }
    }
  }
}
