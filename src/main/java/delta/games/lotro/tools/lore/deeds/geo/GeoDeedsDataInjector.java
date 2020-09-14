package delta.games.lotro.tools.lore.deeds.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.collections.filters.Filter;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.deeds.geo.DeedGeoData;
import delta.games.lotro.lore.deeds.geo.DeedGeoPoint;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.MarkersFinder;
import delta.games.lotro.maps.data.markers.MapPointsUtils;

/**
 * Injector for geographic data in deeds.
 * @author DAM
 */
public class GeoDeedsDataInjector
{
  private static final Logger LOGGER=Logger.getLogger(GeoDeedsDataInjector.class);

  private HashMap<String,DeedDescription> _deeds;
  private HashMap<Integer,DeedDescription> _deedsById;
  private MarkersFinder _finder;

  /**
   * Constructor.
   * @param deeds Deeds to update.
   */
  public GeoDeedsDataInjector(List<DeedDescription> deeds)
  {
    // Deeds maps
    _deeds=new HashMap<String,DeedDescription>();
    for(DeedDescription deed : deeds)
    {
      _deeds.put(deed.getKey(),deed);
    }
    _deedsById=new HashMap<Integer,DeedDescription>();
    for(DeedDescription deed : deeds)
    {
      _deedsById.put(Integer.valueOf(deed.getIdentifier()),deed);
    }
    // Maps manager
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    _finder=mapsManager.getMarkersFinder();
  }

  /**
   * Perform data injection.
   */
  public void doIt()
  {
    // Treasure caches
    doTreasureCaches();
    // Pelennor
    doPelennorHaradrimSupplies();
    // The Wastes
    doRangerCaches();
    doAncientWeapons();
    // Mordor
    doUdunForges();
    doMordorRareChests();
    // Northern Mirwood
    doNorthernMirkwoodDwarfMarkers();
    // TODO: path of the company
    // Erebor: Old papers => not possible
    // Dwarf-holds
    doDwarfHolds();
  }

  private void doTreasureCaches()
  {
    // TODO: use new map identifiers
    doTreasureCaches("Treasure_of_Angmar","268437615",12);
    doTreasureCaches("Treasure_of_Evendim","evendim",12);
    doTreasureCaches("Treasure_of_Forochel","forochel",12);
    doTreasureCaches("Treasure_of_the_Misty_Mountains","misty_mountains",12);
    doTreasureCaches("Treasure_of_the_North_Downs","north_downs",12);
    doTreasureCaches("Treasure_of_Southern_Mirkwood","southern_mirkwood",12);
    doTreasureCaches("Gondorian_Treasure_Cache","western_gondor",18);
    doTreasureCaches("Treasure_of_Central_Gondor","central_gondor",20);
    doTreasureCaches("Treasure_of_Eastern_Gondor","eastern_gondor",18);
    doTreasureCaches("Treasure_of_Far_An%C3%B3rien","far_anorien",16);
    doTreasureCaches("Treasure_of_North_Ithilien","post_pelennor_north_ithilien",12);
    doTreasureCaches("Treasure_of_Ud%C3%BBn","mordor_udun",12);
    doTreasureCaches("Treasure_of_Dor_Amarth","mordor_dor_amarth",12);
    doTreasureCaches("Treasure_of_Lhingris","mordor_lhingris",12);
    doTreasureCaches("Treasure_of_Talath_%C3%9Arui","mordor_talath_urui",12);
    doTreasureCaches("Treasure_of_Agarnaith","mordor_agarnaith",12);
    doTreasureCaches("Treasure-seeker_of_the_North","northern_mirkwood",20);
  }

  private void doTreasureCaches(String deedKey,String mapKey,int expectedPointsCount)
  {
    List<Marker> markers=findMarkersInMap(mapKey,"Treasure Cache");
    registerPointsByDeedKey(deedKey,mapKey,markers,expectedPointsCount,expectedPointsCount);
  }

  private void doRangerCaches()
  {
    String mapKey="post_pelennor_the_wastes";
    List<Marker> markers=findMarkersInMap(mapKey,"Ranger cache");
    registerPointsByDeedKey("Forgotten_Caches",mapKey,markers,8,8);
  }

  private void doAncientWeapons()
  {
    String mapKey="post_pelennor_the_wastes";
    List<Marker> markers=findMarkersInMap(mapKey,"Ancient weapon");
    registerPointsByDeedKey("Relics_of_the_Last_Alliance",mapKey,markers,8,8);
  }

  private void doMordorRareChests()
  {
    doMordorRareChestsForMap("mordor_udun","Rare_Gorgoroth_Chests_of_Ud%C3%BBn");
    doMordorRareChestsForMap("mordor_dor_amarth","Rare_Gorgoroth_Chests_of_Dor_Amarth");
    doMordorRareChestsForMap("mordor_lhingris","Rare_Gorgoroth_Chests_of_Lhingris");
    doMordorRareChestsForMap("mordor_talath_urui","Rare_Gorgoroth_Chests_of_Talath_%C3%9Arui");
    doMordorRareChestsForMap("mordor_agarnaith","Rare_Gorgoroth_Chests_of_Agarnaith");
  }

  private void doMordorRareChestsForMap(String mapKey, String deedKey)
  {
    List<Marker> markers=findMarkersInMap(mapKey,"Rare Mordor chest");
    registerPointsByDeedKey(deedKey,mapKey,markers,5,5);
  }

  private void doNorthernMirkwoodDwarfMarkers()
  {
    List<Marker> markers=findMarkersInMap("northern_mirkwood","Dwarf marker");
    registerPointsByDeedKey("Surveyor_of_the_Dwarvish_Markers","northern_mirkwood",markers,16,16);
  }

  private void doPelennorHaradrimSupplies()
  {
    List<Marker> markers=findMarkersInMap("post_pelennor_march_of_the_king","Haradrim Supplies");
    registerPointsByDeedKey("Haradrim_Remnants","post_pelennor_march_of_the_king",markers,6,6);
  }

  private void doUdunForges()
  {
    String mapKey="mordor_udun";
    List<Marker> markersForgework=findMarkersInMap(mapKey,"Great forge-work");
    registerPointsByDeedKey("Forgeworks_of_Ud%C3%BBn",mapKey,markersForgework,17,16);
    List<Marker> markersForgefires=findMarkersInMap(mapKey,"Forging furnage");
    registerPointsByDeedKey("Forge-fires_of_Ud%C3%BBn",mapKey,markersForgefires,17,15);
  }

  private void doDwarfHolds()
  {
    // Iron Hills
    doIronHillsMiningCaches();
    doIronHillsIronVeins();
    // Ered Mithrin
    doEredMithrinTreasureCaches();
    // Both...
    doSurveyMarkersOfTheDwarfHolds();
  }

  private void doIronHillsMiningCaches()
  {
    List<Marker> markers=findMarkersInMap("iron_hills","Mining cache");
    registerPointsByDeedId(1879378533,"iron_hills",markers,10);
  }

  private void doIronHillsIronVeins()
  {
    List<Marker> markers=findMarkersInMap("iron_hills","Rich iron vein");
    registerPointsByDeedId(1879378529,"iron_hills",markers,10);
  }

  private void doEredMithrinTreasureCaches()
  {
    List<Marker> markers=findMarkersInMap("ered_mithrin","Treasure cache");
    registerPointsByDeedId(1879378543,"ered_mithrin",markers,12);
  }

  private void doSurveyMarkersOfTheDwarfHolds()
  {
    List<Marker> markers1=findMarkersInMap("ered_mithrin","Dwarf-marker");
    List<Marker> markers2=findMarkersInMap("iron_hills","Dwarf-marker");
    String[] maps={"ered_mithrin","iron_hills"};
    List<List<Marker>> markers=new ArrayList<List<Marker>>();
    markers.add(markers1);
    markers.add(markers2);
    int[] expectedMarkersCount={6,6};
    DeedDescription deed=_deedsById.get(Integer.valueOf(1879378540));
    registerPointsOnMultipleMaps(deed,maps,markers,expectedMarkersCount,12);
  }

  private void registerPointsByDeedId(int deedId, String mapKey, List<Marker> markers, int expectedPointsCount)
  {
    LOGGER.info("Geographic data injection for: "+deedId);
    DeedDescription deed=_deedsById.get(Integer.valueOf(deedId));
    if (deed==null)
    {
      LOGGER.warn("Deed not found!");
      return;
    }
    int mapId=NumericTools.parseInt(mapKey,0);
    registerPoints(deed,mapId,markers,expectedPointsCount,expectedPointsCount);
  }

  private void registerPointsByDeedKey(String deedKey, String mapKey, List<Marker> markers, int expectedPointsCount, int requiredPointsCount)
  {
    LOGGER.info("Geographic data injection for: "+deedKey);
    DeedDescription deed=_deeds.get(deedKey);
    if (deed==null)
    {
      LOGGER.warn("Deed not found!");
      return;
    }
    int mapId=NumericTools.parseInt(mapKey,0);
    registerPoints(deed,mapId,markers,expectedPointsCount,requiredPointsCount);
  }

  private void registerPoints(DeedDescription deed, int mapKey, List<Marker> markers, int expectedPointsCount, int requiredPointsCount)
  {
    int nbPoints=markers.size();
    if (nbPoints>0)
    {
      DeedGeoData data=new DeedGeoData(requiredPointsCount);
      for(Marker marker : markers)
      {
        DeedGeoPoint point=new DeedGeoPoint(mapKey,marker.getId());
        data.addPoint(point);
      }
      LOGGER.info("Found "+markers.size()+" points");
      deed.setGeoData(data);
    }
    if ((nbPoints==0) || (nbPoints!=expectedPointsCount))
    {
      LOGGER.warn("Bad points count: "+nbPoints+". Expected: "+expectedPointsCount);
    }
  }

  private void registerPointsOnMultipleMaps(DeedDescription deed, String[] mapKey, List<List<Marker>> markers, int[] expectedPointsCount, int requiredPointsCount)
  {
    int nbMaps=mapKey.length;
    DeedGeoData data=new DeedGeoData(requiredPointsCount);
    int totalPoints=0;
    for(int i=0;i<nbMaps;i++)
    {
      int nbPoints=0;
      for(Marker marker : markers.get(i))
      {
        int mapId=NumericTools.parseInt(mapKey[i],0);
        DeedGeoPoint point=new DeedGeoPoint(mapId,marker.getId());
        data.addPoint(point);
        nbPoints++;
      }
      LOGGER.info("Found "+nbPoints+" points in map "+mapKey[i]);
      if ((nbPoints==0) || (nbPoints!=expectedPointsCount[i]))
      {
        LOGGER.warn("Bad points count: "+nbPoints+". Expected: "+expectedPointsCount[i]);
      }
      totalPoints+=nbPoints;
    }
    deed.setGeoData(data);
    LOGGER.info("Found "+totalPoints+" points");
  }

  private List<Marker> findMarkersInMap(String mapKey, final String name)
  {
    List<Marker> ret=new ArrayList<Marker>();
    Integer mapId=NumericTools.parseInteger(mapKey);
    if (mapId==null)
    {
      return ret;
    }
    ParchmentMapsManager parchmentMapsMgr=ParchmentMapsManager.getInstance();
    ParchmentMap parchmentMap=parchmentMapsMgr.getMapById(mapId.intValue());
    if (parchmentMap==null)
    {
      return ret;
    }
    List<Marker> mapMarkers=new ArrayList<Marker>();
    List<Area> areas=parchmentMap.getAreas();
    for(Area area : areas)
    {
      int areaId=area.getIdentifier();
      List<Marker> areaMarkers=_finder.findMarkers(areaId,0);
      mapMarkers.addAll(areaMarkers);
    }
    Filter<Marker> filter=new Filter<Marker>()
    {
      public boolean accept(Marker item)
      {
        String label=item.getLabel();
        if (label.contains(name))
        {
          return true;
        }
        return false;
      }
    };
    ret=MapPointsUtils.getFilteredMarkers(filter,mapMarkers);
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    List<DeedDescription> deeds=deedsMgr.getAll();
    GeoDeedsDataInjector geoInjector=new GeoDeedsDataInjector(deeds);
    geoInjector.doIt();
  }
}
