package delta.games.lotro.tools.dat.maps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.maps.data.Marker;

/**
 * Index for markers associated to a single integer value (parent zone ID, content layer, ...).
 * @author DAM
 */
public class MarkersStore
{
  private int _key;
  private List<Marker> _markers;

  /**
   * Constructor.
   * @param key Managed key.
   */
  public MarkersStore(int key)
  {
    _key=key;
    _markers=new ArrayList<Marker>();
  }

  /**
   * Get the managed key.
   * @return a key.
   */
  public int getKey()
  {
    return _key;
  }

  /**
   * Get the markers.
   * @return a set of markers.
   */
  public Set<Integer> getMarkers()
  {
    Set<Integer> ret=new HashSet<Integer>();
    for(Marker marker : _markers)
    {
      ret.add(Integer.valueOf(marker.getId()));
    }
    return ret;
  }

  /**
   * Add a marker.
   * @param marker Marker to add.
   */
  public void addMarker(Marker marker)
  {
    _markers.add(marker);
  }
}
