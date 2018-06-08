package delta.games.lotro.tools.lore.maps.tools;

import java.util.ArrayList;
import java.util.HashMap;
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
  private int _currentId;
  private HashMap<String,Marker> _foundMarkers;

  /**
   * Constructor.
   */
  public MarkersMerge()
  {
    _currentId=0;
    _foundMarkers=new HashMap<String,Marker>();
  }

  /**
   * Do it.
   * @param mapsManager Maps manager to use.
   */
  public void doIt(MapsManager mapsManager)
  {
    List<MapBundle> mapBundles=mapsManager.getMaps();
    for(MapBundle mapBundle : mapBundles)
    {
      handleMap(mapBundle);
    }
    System.out.println("# different points:" + _currentId);
  }

  private void handleMap(MapBundle mapBundle)
  {
    MarkersManager markersMgr=mapBundle.getData();
    List<Marker> markers=markersMgr.getAllMarkers();
    List<Marker> markersToAdd=new ArrayList<Marker>();
    for(Marker marker : markers)
    {
      String footPrint=buildMarkerFootPrint(marker);
      Marker match=_foundMarkers.get(footPrint);
      if (match!=null)
      {
        //System.out.println("Found match: 1="+marker+", 2="+match);
        marker.setId(match.getId());
      }
      else
      {
        int id=_currentId;
        _currentId++;
        marker.setId(id);
        markersToAdd.add(marker);
      }
    }
    for(Marker markerToAdd : markersToAdd)
    {
      String footPrint=buildMarkerFootPrint(markerToAdd);
      _foundMarkers.put(footPrint,markerToAdd);
    }
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
}
