package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;
import delta.games.lotro.maps.data.io.xml.MapXMLWriter;
import delta.games.lotro.maps.data.markers.index.DIDMarkersIndex;
import delta.games.lotro.maps.data.markers.index.MarkersIndexesManager;

/**
 * Manager for all maps data.
 * @author DAM
 */
public class MapsDataManager
{
  private MapsManager _mapsManager;
  private MarkersIndexesManager _index;
  private Map<Integer,MarkersManager> _markersByRegion;
  private Map<Integer,MarkersManager> _markersByLayerId;

  /**
   * Constructor.
   */
  public MapsDataManager()
  {
    _mapsManager=new MapsManager(new File("../lotro-maps-db"));
    _index=new MarkersIndexesManager();
    _markersByRegion=new HashMap<Integer,MarkersManager>();
    _markersByLayerId=new HashMap<Integer,MarkersManager>();
  }

  /**
   * Get the maps manager.
   * @return the maps manager.
   */
  public MapsManager getMapsManager()
  {
    return _mapsManager;
  }

  /**
   * Register a marker.
   * @param did Associated DID (dungeon, area, NPC...).
   * @param marker Marker to add.
   */
  public void registerDidMarker(int did, Marker marker)
  {
    DIDMarkersIndex index=_index.getDidIndex(did,true);
    index.addMarker(marker);
  }

  /**
   * Register a world marker.
   * @param region Region to use.
   * @param marker Marker to register.
   */
  public void registerWorldMarker(int region, Marker marker)
  {
    MarkersManager markersMgr=getMarkersForRegion(region);
    markersMgr.addMarker(marker);
  }

  /**
   * Register a content layer marker.
   * @param layerId Identifier of the layer to use.
   * @param marker Marker to register.
   */
  public void registerContentLayerMarker(int layerId, Marker marker)
  {
    MarkersManager markersMgr=getMarkersForContentLayer(layerId);
    markersMgr.addMarker(marker);
  }

  private MarkersManager getMarkersForRegion(int region)
  {
    Integer key=Integer.valueOf(region);
    MarkersManager ret=_markersByRegion.get(key);
    if (ret==null)
    {
      ret=new MarkersManager();
      _markersByRegion.put(key,ret);
    }
    return ret;
  }

  private MarkersManager getMarkersForContentLayer(int contentLayer)
  {
    Integer key=Integer.valueOf(contentLayer);
    MarkersManager ret=_markersByLayerId.get(key);
    if (ret==null)
    {
      ret=new MarkersManager();
      _markersByLayerId.put(key,ret);
    }
    return ret;
  }

  /**
   * Write markers file.
   */
  public void writeMarkers()
  {
    File rootDir=_mapsManager.getRootDir();
    File markersDir=new File(rootDir,"markers");
    for(Integer region : _markersByRegion.keySet())
    {
      File to=new File(markersDir,"region"+region+".xml");
      MarkersManager markersMgr=getMarkersForRegion(region.intValue());
      MapXMLWriter.writeMarkersFile(to,markersMgr,EncodingNames.UTF_8);
    }
    for(Integer layerId : _markersByLayerId.keySet())
    {
      File to=new File(markersDir,"layer"+layerId+".xml");
      MarkersManager markersMgr=getMarkersForContentLayer(layerId.intValue());
      MapXMLWriter.writeMarkersFile(to,markersMgr,EncodingNames.UTF_8);
    }
  }
}
