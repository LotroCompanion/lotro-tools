package delta.games.lotro.tools.dat.travels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get travel definitions from DAT files.
 * @author DAM
 */
public class MainDatTravelsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatTravelsLoader.class);

  private DataFacade _facade;
  private Map<Integer,String> _nodes;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatTravelsLoader(DataFacade facade)
  {
    _facade=facade;
    _nodes=new HashMap<Integer,String>();
  }

  private void load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
      //propNames.addAll(properties.getPropertyNames());
    }
    else
    {
      LOGGER.warn("Could not handle travel ID="+indexDataId);
    }
    // Home location
    int homeLocationId=((Integer)properties.getProperty("TravelHomeLocation")).intValue();
    String nodeName=loadTravelNode(homeLocationId);
    System.out.println("Node: "+nodeName);

    // Destinations
    Object[] routesArray=(Object[])properties.getProperty("TravelDestinationRecordArray");
    for(Object routeObj : routesArray)
    {
      PropertiesSet routeProps = (PropertiesSet)routeObj;
      @SuppressWarnings("unused")
      int routeCost=((Integer)routeProps.getProperty("TravelDestinationCost")).intValue();
      int destinationId=((Integer)routeProps.getProperty("TravelDestinationRoute")).intValue();
      loadTravelRoute(destinationId);
    }
  }

  private String loadTravelNode(int locationId)
  {
    String nodeName=_nodes.get(Integer.valueOf(locationId));
    if (nodeName==null)
    {
      PropertiesSet properties=_facade.loadProperties(locationId+0x09000000);
      //System.out.println("************* "+locationId+" *****************");
      //System.out.println(properties.dump());
      //propNames.addAll(properties.getPropertyNames());
      // Name
      nodeName=DatUtils.getStringProperty(properties,"TravelDisplayName");
      // Swift travel?
      Integer swiftTravelInt=(Integer)properties.getProperty("TravelDestinationIsSwiftTravel");
      @SuppressWarnings("unused")
      boolean isSwiftTravel=((swiftTravelInt!=null) && (swiftTravelInt.intValue()==1));
      //System.out.println("Loaded Node ID: "+locationId+", name: "+nodeName + " (swift travel="+isSwiftTravel+")");
      _nodes.put(Integer.valueOf(locationId),nodeName);
    }
    return nodeName;
  }

  private void loadTravelRoute(int travelRouteId)
  {
    PropertiesSet properties=_facade.loadProperties(travelRouteId+0x09000000);
    //System.out.println("************* "+locationId+" *****************");
    //System.out.println(properties.dump());
    /*
************* 1879103917 *****************
Name: 
  #1: Gath Forthnír - Swift Travel
TravelRoute_ActionArray: 
  #1: 1879072604
  #2: 1879072630
  #3: 1879103929
TravelRoute_Destination: 1879103928
TravelRoute_TravelMode: 1879106525
UI_Examination_Tooltip_0Click_Element: 268441090   ==> ignored
UI_Examination_Tooltip_0Click_Layout: 570425344   ==> ignored
UI_Examination_Tooltip_1Click_Element: 268441090   ==> ignored
UI_Examination_Tooltip_1Click_Layout: 570425344   ==> ignored
Usage_AllowedWhileMounted: 1   ==> always 1
Usage_Permission: 1879049255
Usage_QuestRequirements: 
  #1: 
    Usage_Operator: 3
    Usage_QuestID: 1879053183
    Usage_QuestStatus: 805306368
Usage_RequiredFaction: 
  Usage_RequiredFaction_DataID: 1879091341
  Usage_RequiredFaction_Tier: 4
Usage_RequiresSubscriberOrUnsub: 1
     */
    //propNames.addAll(properties.getPropertyNames());
    // Name
    String routeName=DatUtils.getStringProperty(properties,"Name");
    // Destination
    int destinationId=((Integer)properties.getProperty("TravelRoute_Destination")).intValue();
    String destinationName=loadTravelNode(destinationId);
    // Min level
    Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");

    // Travel mode
    //int travelModeId=((Integer)properties.getProperty("TravelRoute_TravelMode")).intValue();
    // Travel modes: [1879159548, 1879137174, 1879106525, 1879108779]
    // 1879159548=boat
    // 1879137174, 1879106525, 1879108779 => effect 1879048717
    //loadTravelMode(travelModeId);
    String label = "\tRoute ID: "+travelRouteId+", name: "+routeName + " to " + destinationName;
    if (minLevel!=null)
    {
      label=label+" (min level="+minLevel+")";
    }
    System.out.println(label);
    List<String> segments=new ArrayList<String>();
    // Route actions
    Object[] routeActionsArray=(Object[])properties.getProperty("TravelRoute_ActionArray");
    for(Object routeActionObj : routeActionsArray)
    {
      int routeActionId=((Integer)routeActionObj).intValue();
      String segmentKey=loadTravelRouteAction(routeActionId);
      if (segmentKey!=null)
      {
        segments.add(segmentKey);
      }
    }
    System.out.println("\t"+segments);
  }

  private String loadTravelRouteAction(int routeActionId)
  {
    PropertiesSet properties=_facade.loadProperties(routeActionId+0x09000000);
    //System.out.println("************* "+routeActionId+" *****************");
    //System.out.println(properties.dump());
    String ret=(String)properties.getProperty("TravelSegment_HeadName");
    if (ret==null)
    {
      //Float hopDelay=(Float)properties.getProperty("TravelHop_Delay");
      String padLocation=(String)properties.getProperty("TravelPad_Location");
      //System.out.println("NULL: "+hopDelay+" - "+padLocation);
      if (padLocation!=null)
      {
        ret=padLocation;
      }
    }
    return ret;
  }

  private void doIt()
  {
    PropertiesSet indexProperties=_facade.loadProperties(1879048794+0x9000000);
    Object[] idsArray=(Object[])indexProperties.getProperty("TravelWebArray");
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      load(id);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatTravelsLoader(facade).doIt();
    facade.dispose();
  }
}
