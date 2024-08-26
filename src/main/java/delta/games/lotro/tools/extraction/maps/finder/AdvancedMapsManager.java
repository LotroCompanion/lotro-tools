package delta.games.lotro.tools.extraction.maps.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.utils.DataProvider;
import delta.games.lotro.utils.comparators.DelegatingComparator;

/**
 * An 'advanced' maps manager.
 * It can use both the "georeferenced base maps manager" and the "parchment maps manager".
 * @author DAM
 */
public class AdvancedMapsManager
{
  private static final Logger LOGGER=Logger.getLogger(AdvancedMapsManager.class);

  private Map<Integer,DetailedMap> _mapsById;
  private Map<Integer,List<DetailedMap>> _mapsByRegion;
  private GeoreferencedBasemapsManager _mapsMgr;
  private ParchmentMapsManager _parchmentMapsMgr;

  /**
   * Constructor.
   * @param mapsMgr Georeferenced maps manager.
   * @param parchmentMapsMgr Parchment maps manager.
   */
  public AdvancedMapsManager(GeoreferencedBasemapsManager mapsMgr, ParchmentMapsManager parchmentMapsMgr)
  {
    _mapsById=new HashMap<Integer,DetailedMap>();
    _mapsByRegion=new HashMap<Integer,List<DetailedMap>>();
    _mapsMgr=mapsMgr;
    _parchmentMapsMgr=parchmentMapsMgr;
    load();
  }

  private void load()
  {
    for(ParchmentMap parchmentMap : _parchmentMapsMgr.getParchmentMaps())
    {
      int id=parchmentMap.getIdentifier();
      GeoreferencedBasemap basemap=_mapsMgr.getMapById(id);
      if (basemap==null)
      {
        LOGGER.warn("Found a parchment map with no basemap: id="+id);
        continue;
      }
      DetailedMap detailedMap=new DetailedMap(basemap,parchmentMap);
      registerMap(detailedMap);
    }
  }

  private void registerMap(DetailedMap map)
  {
    // Maps
    _mapsById.put(Integer.valueOf(map.getIdentifier()),map);
    // Maps by region
    Integer region=Integer.valueOf(map.getRegion());
    List<DetailedMap> mapsForRegion=_mapsByRegion.get(region);
    if (mapsForRegion==null)
    {
      mapsForRegion=new ArrayList<DetailedMap>();
      _mapsByRegion.put(region,mapsForRegion);
    }
    mapsForRegion.add(map);
  }

  /**
   * Get the best map for the given position.
   * @param region Region.
   * @param lon Longitude.
   * @param lat Latitude.
   * @return A map or <code>null</code>.
   */
  public DetailedMap getBestMapForPoint(int region, float lon, float lat)
  {
    List<DetailedMap> maps=getMapsForPoint(region,lon,lat);
    if (maps.isEmpty())
    {
      return null;
    }
    return maps.get(maps.size()-1);
  }

  /**
   * Get the maps for the given position.
   * @param region Region.
   * @param lon Longitude.
   * @param lat Latitude.
   * @return A possibly empty but never <code>null</code> list of maps.
   */
  public List<DetailedMap> getMapsForPoint(int region, float lon, float lat)
  {
    List<DetailedMap> ret=new ArrayList<DetailedMap>();
    List<DetailedMap> mapsForRegion=_mapsByRegion.get(Integer.valueOf(region));
    if (mapsForRegion!=null)
    {
      for(DetailedMap mapForRegion : mapsForRegion)
      {
        ParchmentMap parchmentMap=mapForRegion.getParchmentMap();
        if (parchmentMap.isQuestGuideDisabled())
        {
          continue;
        }
        if (mapForRegion.contains(lon,lat))
        {
          ret.add(mapForRegion);
        }
      }
    }
    if (ret.size()>1)
    {
      DataProvider<DetailedMap,GeoreferencedBasemap> dataProvider=new DataProvider<DetailedMap,GeoreferencedBasemap>()
      {
        public GeoreferencedBasemap getData(DetailedMap map)
        {
          return map.getBasemap();
        }
      };
      DelegatingComparator<DetailedMap,GeoreferencedBasemap> dc=new DelegatingComparator<>(dataProvider,new MapSizeComparator());
      Collections.sort(ret,dc);
    }
    return ret;
  }
}
