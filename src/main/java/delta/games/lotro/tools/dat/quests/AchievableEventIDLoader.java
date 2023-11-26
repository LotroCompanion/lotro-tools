package delta.games.lotro.tools.dat.quests;

import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;

/**
 * Loader for achievable events IDs.
 * @author DAM
 */
public class AchievableEventIDLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public AchievableEventIDLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Handle an achievable.
   * @param achievable Achievable to handle.
   */
  public void doAchievable(Achievable achievable)
  {
    int id=achievable.getIdentifier();
    WStateDataSet dataSet=_facade.loadWState(id);
    Integer mainReference=dataSet.getOrphanReferences().get(0);
    ClassInstance questData=(ClassInstance)dataSet.getValueForReference(mainReference.intValue());
    ObjectivesManager objectivesMgr=achievable.getObjectives();
    @SuppressWarnings("unchecked")
    Map<Integer,ClassInstance> map=(Map<Integer,ClassInstance>)questData.getAttributeValue("m_questEventHash");
    for(Map.Entry<Integer,ClassInstance> entry : map.entrySet())
    {
      Integer eventID=entry.getKey();
      ClassInstance eventInstance=entry.getValue();
      PropertiesSet props=(PropertiesSet)eventInstance.getAttributeValue("m_collection");
      Integer eventOrder=(Integer)eventInstance.getAttributeValue("m_iEventOrder");
      if ((eventOrder==null) || (eventOrder.intValue()<0))
      {
        continue;
      }
      Integer objectiveID=(Integer)props.getProperty("QuestEvent_ObjectiveID");
      Objective objective=objectivesMgr.getObjectives().get(objectiveID.intValue()-1);
      ObjectiveCondition condition=objective.getConditions().get(eventOrder.intValue());
      condition.setEventID(eventID.intValue());
    }
    int eventID=1;
    for(Objective objective : objectivesMgr.getObjectives())
    {
      for(ObjectiveCondition condition : objective.getConditions())
      {
        if (condition.getEventID()==eventID)
        {
          condition.setEventID(0);
        }
        eventID++;
      }
    }
  }
}
