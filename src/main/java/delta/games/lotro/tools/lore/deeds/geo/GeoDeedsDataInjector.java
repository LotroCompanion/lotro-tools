package delta.games.lotro.tools.lore.deeds.geo;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.collections.filters.Filter;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.geo.DeedGeoData;
import delta.games.lotro.lore.deeds.geo.DeedGeoPoint;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * @author DAM
 */
public class GeoDeedsDataInjector
{
  private HashMap<String,DeedDescription> _deeds;

  public GeoDeedsDataInjector(List<DeedDescription> deeds)
  {
    _deeds=new HashMap<String,DeedDescription>();
    for(DeedDescription deed : deeds)
    {
      _deeds.put(deed.getKey(),deed);
    }
  }

  public void doIt()
  {
    doTreasureCaches();
  }

  // Erebor: dwarf markers
  // Treasure Cache: NMW

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
    System.out.println("Geographic data injection for: "+deedKey);
    DeedDescription deed=_deeds.get(deedKey);
    if (deed==null)
    {
      System.out.println("Deed not found!");
      return;
    }
    List<Marker> markers=findMarkersInMap(mapKey,"Treasure cache");
    int nbPoints=markers.size();
    if (nbPoints>0)
    {
      DeedGeoData data=new DeedGeoData(nbPoints);
      for(Marker marker : markers)
      {
        DeedGeoPoint point=new DeedGeoPoint(mapKey,marker.getId());
        data.addPoint(point);
      }
      System.out.println("Found "+markers.size()+" points");
      deed.setGeoData(data);
    }
    if ((nbPoints==0) || (nbPoints!=expectedPointsCount))
    {
      System.out.println("Bad points count: "+nbPoints+". Expected: "+expectedPointsCount);
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
