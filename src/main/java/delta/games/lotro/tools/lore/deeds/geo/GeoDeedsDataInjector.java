package delta.games.lotro.tools.lore.deeds.geo;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.collections.filters.Filter;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.geo.DeedGeoData;
import delta.games.lotro.lore.deeds.geo.DeedGeoPoint;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * Injector for geographic data in deeds.
 * @author DAM
 */
public class GeoDeedsDataInjector
{
  private static final Logger LOGGER=Logger.getLogger(GeoDeedsDataInjector.class);

  private HashMap<String,DeedDescription> _deeds;

  /**
   * Constructor.
   * @param deeds Deeds to update.
   */
  public GeoDeedsDataInjector(List<DeedDescription> deeds)
  {
    _deeds=new HashMap<String,DeedDescription>();
    for(DeedDescription deed : deeds)
    {
      _deeds.put(deed.getKey(),deed);
    }
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
  }

  private void doTreasureCaches()
  {
    doTreasureCaches("Treasure_of_Angmar","angmar",12);
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
    List<Marker> markers=findMarkersInMap(mapKey,"Treasure cache");
    registerPoints(deedKey,mapKey,markers,expectedPointsCount);
  }

  private void doRangerCaches()
  {
    String mapKey="post_pelennor_the_wastes";
    List<Marker> markers=findMarkersInMap(mapKey,"Ranger cache");
    registerPoints("Forgotten_Caches",mapKey,markers,8);
  }

  private void doAncientWeapons()
  {
    String mapKey="post_pelennor_the_wastes";
    List<Marker> markers=findMarkersInMap(mapKey,"Ancient weapon");
    registerPoints("Relics_of_the_Last_Alliance",mapKey,markers,8);
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
    registerPoints(deedKey,mapKey,markers,5);
  }

  private void doNorthernMirkwoodDwarfMarkers()
  {
    List<Marker> markers=findMarkersInMap("northern_mirkwood","Dwarf marker");
    registerPoints("Surveyor_of_the_Dwarvish_Markers","northern_mirkwood",markers,16);
  }

  private void doPelennorHaradrimSupplies()
  {
    List<Marker> markers=findMarkersInMap("post_pelennor_march_of_the_king","Haradrim Supplies");
    registerPoints("Haradrim_Remnants","post_pelennor_march_of_the_king",markers,6);
  }

  private void doUdunForges()
  {
    String mapKey="mordor_udun";
    List<Marker> markersForgework=findMarkersInMap(mapKey,"Great forge-work");
    registerPoints("Forgeworks_of_Ud%C3%BBn",mapKey,markersForgework,17,16);
    List<Marker> markersForgefires=findMarkersInMap(mapKey,"Forging furnage");
    registerPoints("Forge-fires_of_Ud%C3%BBn",mapKey,markersForgefires,17,15);
  }

  private void registerPoints(String deedKey, String mapKey, List<Marker> markers, int expectedPointsCount)
  {
    registerPoints(deedKey,mapKey,markers,expectedPointsCount,expectedPointsCount);
  }

  private void registerPoints(String deedKey, String mapKey, List<Marker> markers, int expectedPointsCount, int requiredPointsCount)
  {
    LOGGER.info("Geographic data injection for: "+deedKey);
    DeedDescription deed=_deeds.get(deedKey);
    if (deed==null)
    {
      LOGGER.warn("Deed not found!");
      return;
    }
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

  private List<Marker> findMarkersInMap(String mapKey, final String name)
  {
   File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();

    MapBundle map=mapsManager.getMapByKey(mapKey);
    MarkersManager markers=map.getData();
    Filter<Marker> filter=new Filter<Marker>() {

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
    List<Marker> ret=markers.getMarkers(filter);
    return ret;
  }
}
