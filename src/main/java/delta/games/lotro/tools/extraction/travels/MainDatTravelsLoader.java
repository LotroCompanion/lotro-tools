package delta.games.lotro.tools.extraction.travels;

import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.money.Money;
import delta.games.lotro.common.requirements.AbstractAchievableRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.travels.TravelDestination;
import delta.games.lotro.lore.travels.TravelMode;
import delta.games.lotro.lore.travels.TravelNode;
import delta.games.lotro.lore.travels.TravelRoute;
import delta.games.lotro.lore.travels.TravelRouteInstance;
import delta.games.lotro.lore.travels.TravelsManager;
import delta.games.lotro.tools.extraction.achievables.QuestRequirementsLoader;
import delta.games.lotro.tools.extraction.requirements.UsageRequirementsLoader;

/**
 * Get travel definitions from DAT files.
 * @author DAM
 */
public class MainDatTravelsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatTravelsLoader.class);

  private DataFacade _facade;
  private TravelsManager _travelsMgr;
  private QuestRequirementsLoader _questRequirementsLoader;
  private UsageRequirementsLoader _usageRequirementsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatTravelsLoader(DataFacade facade)
  {
    _facade=facade;
    _travelsMgr=new TravelsManager();
    _questRequirementsLoader=new QuestRequirementsLoader(facade);
    _usageRequirementsLoader=new UsageRequirementsLoader();
  }

  private TravelNode load(int indexDataId)
  {
    TravelNode node=new TravelNode(indexDataId);
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties==null)
    {
      LOGGER.warn("Could not handle travel node ID={}",Integer.valueOf(indexDataId));
      return null;
    }
    // Home location
    int homeLocationId=((Integer)properties.getProperty("TravelHomeLocation")).intValue();
    TravelDestination homeLocation=getTravelDestination(homeLocationId);
    node.addLocation(homeLocation);
    // Other locations
    Object[] otherLocationsArray=(Object[])properties.getProperty("TravelHomeLocationsArray");
    if (otherLocationsArray!=null)
    {
      for(Object otherLocationObj : otherLocationsArray)
      {
        int otherLocationid=((Integer)otherLocationObj).intValue();
        TravelDestination otherLocation=getTravelDestination(otherLocationid);
        if (otherLocation!=null)
        {
          node.addLocation(otherLocation);
        }
      }
    }

    // Routes
    Object[] routesArray=(Object[])properties.getProperty("TravelDestinationRecordArray");
    for(Object routeObj : routesArray)
    {
      PropertiesSet routeProps = (PropertiesSet)routeObj;
      int routeCost=((Integer)routeProps.getProperty("TravelDestinationCost")).intValue();
      int routeId=((Integer)routeProps.getProperty("TravelDestinationRoute")).intValue();
      TravelRoute route=loadTravelRoute(routeId);
      TravelRouteInstance routeInstance=new TravelRouteInstance(routeCost,route);
      node.addRoute(routeInstance);
    }

    // Register node
    _travelsMgr.addNode(node);
    return node;
  }

  private TravelDestination getTravelDestination(int locationId)
  {
    TravelDestination destination=_travelsMgr.getDestination(locationId);
    if (destination==null)
    {
      PropertiesSet properties=_facade.loadProperties(locationId+DATConstants.DBPROPERTIES_OFFSET);
      // Name
      String name=DatStringUtils.getStringProperty(properties,"TravelDisplayName");
      if (name==null)
      {
        // Sometimes location IDs refer to routes, not destinations... skip them!
        return null;
      }
      // Swift travel?
      Integer swiftTravelInt=(Integer)properties.getProperty("TravelDestinationIsSwiftTravel");
      boolean isSwiftTravel=((swiftTravelInt!=null) && (swiftTravelInt.intValue()==1));
      destination=new TravelDestination(locationId,name,isSwiftTravel);
      _travelsMgr.addDestination(destination);
    }
    return destination;
  }

  private TravelRoute loadTravelRoute(int travelRouteId)
  {
    PropertiesSet properties=_facade.loadProperties(travelRouteId+DATConstants.DBPROPERTIES_OFFSET);
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
Usage_AllowedWhileMounted: 1   ==> always 1
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
    // Name
    String routeName=DatStringUtils.getStringProperty(properties,"Name");
    // Destination
    int destinationId=((Integer)properties.getProperty("TravelRoute_Destination")).intValue();
    TravelDestination destination=getTravelDestination(destinationId);

    // Travel mode
    int travelModeId=((Integer)properties.getProperty("TravelRoute_TravelMode")).intValue();
    TravelMode mode=getTravelMode(travelModeId);
    TravelRoute route=new TravelRoute(travelRouteId,routeName,mode,destination);

    // Route actions
    Object[] routeActionsArray=(Object[])properties.getProperty("TravelRoute_ActionArray");
    for(Object routeActionObj : routeActionsArray)
    {
      int routeActionId=((Integer)routeActionObj).intValue();
      String segmentKey=loadTravelRouteAction(routeActionId);
      if (segmentKey!=null)
      {
        route.addRouteAction(segmentKey);
      }
    }
    // Requirements
    // - quests/deeds
    AbstractAchievableRequirement questRequirement=_questRequirementsLoader.loadQuestRequirements(null,properties);
    if (questRequirement!=null)
    {
      AchievableProxiesResolver.getInstance().resolveQuestRequirement(questRequirement);
      route.setQuestRequirement(questRequirement);
    }
    // - other usage requirements
    UsageRequirement usageRequirement=route.getUsageRequirement();
    _usageRequirementsLoader.loadUsageRequirements(properties,usageRequirement);

    return route;
  }

  private TravelMode getTravelMode(int travelModeId)
  {
    if (travelModeId==1879159548) return TravelMode.BOAT;
    if (travelModeId==1879137174) return TravelMode.GOAT;
    if (travelModeId==1879106525) return TravelMode.HORSE;
    if (travelModeId==1879108779) return TravelMode.SHAGGY;
    if (travelModeId==1879444821) return TravelMode.ELK;
    if (travelModeId==1879417911) return TravelMode.BOAR;
    LOGGER.warn("Unmanaged travel mode: {}",Integer.valueOf(travelModeId));
    return null;
  }

  private String loadTravelRouteAction(int routeActionId)
  {
    PropertiesSet properties=_facade.loadProperties(routeActionId+DATConstants.DBPROPERTIES_OFFSET);
    String ret=(String)properties.getProperty("TravelSegment_HeadName");
    if (ret==null)
    {
      @SuppressWarnings("unused")
      Float hopDelay=(Float)properties.getProperty("TravelHop_Delay");
      String padLocation=(String)properties.getProperty("TravelPad_Location");
      if (padLocation!=null)
      {
        ret=padLocation;
      }
    }
    return ret;
  }

  void doItWithIndex()
  {
    PropertiesSet indexProperties=_facade.loadProperties(1879048794+DATConstants.DBPROPERTIES_OFFSET);
    Object[] idsArray=(Object[])indexProperties.getProperty("TravelWebArray");
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      load(id);
    }
  }

  private void doItWithScan()
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==1508)
        {
          load(did);
        }
      }
    }
    dumpTravels(System.out); // NOSONAR
  }

  private void dumpTravels(PrintStream out)
  {
    List<TravelNode> nodes=_travelsMgr.getNodes();
    for(TravelNode node : nodes)
    {
      out.println("Node: ID="+node.getIdentifier()+", name="+node.getName()); // NOSONAR
      for(TravelDestination location : node.getLocations())
      {
        out.println("\tAssociated location: ID="+location.getIdentifier()+", name="+location.getName());
      }
      List<TravelRouteInstance> routeInstances=node.getRoutes();
      for(TravelRouteInstance routeInstance : routeInstances)
      {
        TravelRoute route=routeInstance.getRoute();
        Money cost=routeInstance.getCost();
        out.println("\tRoute: ID="+route.getIdentifier()+", name="+route.getName());
        out.println("\t\tCost: "+cost);
        out.println("\t\tMode: "+route.getMode());
        out.println("\t\t"+route.getDestination());
        UsageRequirement usageRequirement=route.getUsageRequirement();
        if (!usageRequirement.isEmpty())
        {
          out.println("\t\tRequirements: "+usageRequirement);
        }
        AbstractAchievableRequirement questRequirements=route.getQuestRequirement();
        if (questRequirements!=null)
        {
          out.println("\t\tQuest requirements: "+questRequirements);
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatTravelsLoader(facade).doItWithScan();
    facade.dispose();
  }
}
