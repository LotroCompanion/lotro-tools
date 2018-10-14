package delta.games.lotro.tools.lore.maps.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * Attempt to identify/merge identical markers among maps.
 * @author DAM
 */
public class MarkersMerge
{
  private int _nextId;
  private HashMap<String,Marker> _foundMarkers;

  /**
   * Constructor.
   */
  public MarkersMerge()
  {
    _foundMarkers=new HashMap<String,Marker>();
  }

  private MapsManager load(File rootDir)
  {
    MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();
    return mapsManager;
  }

  private int getFirstFreeId(MapsManager mapsManager)
  {
    int id=-1;
    List<MapBundle> mapBundles=mapsManager.getMaps();
    for(MapBundle mapBundle : mapBundles)
    {
      MarkersManager markersMgr=mapBundle.getData();
      List<Marker> markers=markersMgr.getAllMarkers();
      for(Marker marker : markers)
      {
        int newId=marker.getId();
        if (newId>id)
        {
          id=newId;
        }
      }
    }
    return id+1;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    MapsManager refMaps=load(new File("../lotro-maps-db"));
    MapsManager newMaps=load(new File("data/maps/output"));

    _nextId=getFirstFreeId(refMaps);

    List<MapBundle> newMapBundles=newMaps.getMaps();
    for(MapBundle newMapBundle : newMapBundles)
    {
      String key=newMapBundle.getKey();
      MapBundle refMapBundle=refMaps.getMapByKey(key);
      handleMap(refMapBundle,newMapBundle);
    }
    System.out.println("# different points:" + _nextId);
    newMaps.saveMaps();
  }

  private void handleMap(MapBundle refMapBundle, MapBundle newMapBundle)
  {
    List<Marker> availableMarkers=loadAvailableMarkers(refMapBundle);
    List<Marker> newMarkers=newMapBundle.getData().getAllMarkers();
    List<Marker> markersToRegister=new ArrayList<Marker>();
    for(Marker newMarker : newMarkers)
    {
      String footPrint=buildMarkerFootPrint(newMarker);
      Marker refMarker=findMarker(footPrint,availableMarkers);
      if (refMarker!=null)
      {
        newMarker.setId(refMarker.getId());
      }
      else
      {
        int id=_nextId;
        _nextId++;
        newMarker.setId(id);
        markersToRegister.add(newMarker);
      }
    }
    for(Marker markerToRegister : markersToRegister)
    {
      String footPrint=buildMarkerFootPrint(markerToRegister);
      _foundMarkers.put(footPrint,markerToRegister);
    }
  }

  private List<Marker> loadAvailableMarkers(MapBundle refMapBundle)
  {
    List<Marker> ret=new ArrayList<Marker>();
    if (refMapBundle!=null)
    {
      List<Marker> markers=refMapBundle.getData().getAllMarkers();
      ret.addAll(markers);
    }
    return ret;
  }

  private Marker findMarker(String footPrint, List<Marker> availableMarkers)
  {
    Marker ret=_foundMarkers.get(footPrint);
    if (ret!=null)
    {
      return ret;
    }
    for(Iterator<Marker> it=availableMarkers.iterator();it.hasNext();)
    {
      Marker availableMarker=it.next();
      String availableMarkerFootprint=buildMarkerFootPrint(availableMarker);
      if (availableMarkerFootprint.equals(footPrint))
      {
        it.remove();
        ret=availableMarker;
        break;
      }
    }
    return ret;
  }

  private String buildMarkerFootPrint(Marker marker)
  {
    int code=marker.getCategoryCode();
    GeoPoint point=marker.getPosition();
    float latitude=point.getLatitude();
    float longitude=point.getLongitude();
    String label=marker.getLabel();
    String footPrint=label+"#"+code+"#"+latitude+"/"+longitude;
    return footPrint;
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MarkersMerge().doIt();
  }
}
