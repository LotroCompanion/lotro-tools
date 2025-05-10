package delta.games.lotro.tools.extraction.requirements;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.DifficultyRequirement;
import delta.games.lotro.common.requirements.LevelCapRequirement;
import delta.games.lotro.common.requirements.RaceRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.requirements.WorldEventRequirement;
import delta.games.lotro.common.utils.ComparisonOperator;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.lore.worldEvents.SimpleWorldEventCondition;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventConditionsLoader;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventsLoader;

/**
 * Loader for loot filters.
 * @author DAM
 */
public class LootFilterLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LootFilterLoader.class);

  private List<AbstractClassDescription> _classes;
  private List<RaceDescription> _races;
  private WorldEventConditionsLoader _weConditionsLoader;

  /**
   * Constructor.
   * @param worldEventsLoader World events loader.
   */
  public LootFilterLoader(WorldEventsLoader worldEventsLoader)
  {
    _classes=new ArrayList<AbstractClassDescription>();
    _races=new ArrayList<RaceDescription>();
    _weConditionsLoader=new WorldEventConditionsLoader(worldEventsLoader);
  }

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
      else if ("EntityFilter_WorldEvent".equals(propertyName))
      {
        PropertiesSet worldEventProps=(PropertiesSet)filterEntry.getValue();
        loadWorldEventFilter(worldEventProps,requirements);
      }
      else
      {
        LOGGER.warn("Unmanaged property: {}",propertyName);
      }
    }
    if (!_classes.isEmpty())
    {
      ClassRequirement classRequirement=new ClassRequirement(_classes);
      requirements.setClassRequirement(classRequirement);
      _classes.clear();
    }
    if (!_races.isEmpty())
    {
      RaceRequirement raceRequirement=new RaceRequirement(_races);
      requirements.setRaceRequirement(raceRequirement);
      _races.clear();
    }
  }

  private void handlePropertySet(PropertyValue propertySet, UsageRequirement requirements)
  {
    String propertyName=propertySet.getDefinition().getName();
    if ("Agent_Class".equals(propertyName))
    {
      // Class filter
      int classCode=((Integer)propertySet.getValue()).intValue();
      AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
      if (abstractClass!=null)
      {
        _classes.add(abstractClass);
      }
    }
    else if ("Agent_Species".equals(propertyName))
    {
      // Race filter
      int raceCode=((Integer)propertySet.getValue()).intValue();
      RaceDescription race=RacesManager.getInstance().getByCode(raceCode);
      if (race!=null)
      {
        _races.add(race);
      }
    }
    else if ("WE_Player_Level_Cap".equals(propertyName))
    {
      int playerLevelCap=((Integer)propertySet.getValue()).intValue();
      LevelCapRequirement requirement=new LevelCapRequirement(playerLevelCap);
      requirements.setRequirement(requirement);
    }
    else if ("ze_skirmish_difficulty".equals(propertyName))
    {
      int difficulty=((Integer)propertySet.getValue()).intValue();
      DifficultyRequirement requirement=new DifficultyRequirement(difficulty);
      requirements.setRequirement(requirement);
    }
    else if ("WE_Lootbox_bonus_embers_motes".equals(propertyName))
    {
      Integer value=(Integer)propertySet.getValue();
      SimpleWorldEventCondition condition=_weConditionsLoader.buildWorldEventCondition(1879387811,ComparisonOperator.EQUAL,value,null,null);
      WorldEventRequirement worldEventRequirement=new WorldEventRequirement(condition);
      requirements.setRequirement(worldEventRequirement);
    }
    else
    {
      LOGGER.warn("Unmanaged property name: {}",propertyName);
    }
  }

  void loadWorldEventFilter(PropertiesSet worldEventProps, UsageRequirement requirements)
  {
    PropertiesSet worldEventConditionProps=(PropertiesSet)worldEventProps.getProperty("WorldEvent_Condition");
    if (worldEventConditionProps!=null)
    {
      SimpleWorldEventCondition condition=_weConditionsLoader.handleWorldEventCondition(worldEventConditionProps);
      WorldEventRequirement worldEventRequirement=new WorldEventRequirement(condition);
      requirements.setRequirement(worldEventRequirement);
    }
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
